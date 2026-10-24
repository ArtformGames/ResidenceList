package com.artformgames.plugin.residencelist.storage.yaml;

import cc.carm.lib.easyplugin.EasyPlugin;
import com.artformgames.plugin.residencelist.ResidenceListAPI;
import com.artformgames.plugin.residencelist.api.residence.ResidenceRate;
import com.artformgames.plugin.residencelist.api.sort.SortFunctions;
import com.artformgames.plugin.residencelist.storage.AbstractStorage;
import com.artformgames.plugin.residencelist.storage.data.CachedUserData;
import com.artformgames.plugin.residencelist.storage.data.ResidenceSnapshot;
import com.artformgames.plugin.residencelist.storage.data.UserSnapshot;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Filesystem implementation of the storage IO hooks. All hooks run on AbstractStorage's IO worker.
 */
public class YAMLStorage extends AbstractStorage {

    public static final String USER_DATA_FOLDER = "users";
    public static final String RESIDENCE_DATA_FOLDER = "residences";

    private final Path userDataFolder;
    private final Path residenceDataFolder;

    public YAMLStorage(@NotNull EasyPlugin plugin) {
        super(plugin);
        Path dataFolder = plugin.getDataFolder().toPath();
        this.userDataFolder = dataFolder.resolve(USER_DATA_FOLDER);
        this.residenceDataFolder = dataFolder.resolve(RESIDENCE_DATA_FOLDER);
    }

    @Override
    protected void openStorage() throws IOException {
        Files.createDirectories(userDataFolder);
        Files.createDirectories(residenceDataFolder);
    }

    @Override
    protected @NotNull Map<String, ResidenceSnapshot> readResidences() throws Exception {
        Map<String, ResidenceSnapshot> result = new LinkedHashMap<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(residenceDataFolder, "*.yaml")) {
            for (Path file : stream) {
                if (!Files.isRegularFile(file)) continue;
                String fileName = file.getFileName().toString();
                String name = fileName.substring(0, fileName.length() - ".yaml".length());
                if (name.isBlank()) throw new IOException("Invalid residence data file: " + file);
                result.put(name, readResidence(file));
            }
        }
        return result;
    }

    @Override
    protected @Nullable CachedUserData readUser(@NotNull UUID key) throws Exception {
        Path file = userFile(key);
        if (!Files.exists(file)) return null;
        return new CachedUserData(readUserFile(file, key));
    }

    static UserSnapshot readUserFile(Path file, UUID key) throws Exception {
        YamlConfiguration conf = readYaml(file);
        requireStringList(conf, "pinned", file);
        requireType(conf, "sort", conf.isInt("sort"), file);
        requireType(conf, "reversed", conf.isBoolean("reversed"), file);
        return new UserSnapshot(key, new ArrayList<>(conf.getStringList("pinned")),
                SortFunctions.parse(conf.getInt("sort", 0)), conf.getBoolean("reversed", false));
    }

    @Override
    protected void writeUsers(@NotNull List<UserSnapshot> users) throws Exception {
        for (UserSnapshot user : users) {
            writeUserFile(userFile(user.key()), user);
        }
    }

    static void writeUserFile(Path file, UserSnapshot user) throws Exception {
        if (user.pinned().isEmpty() && user.sortFunction() == SortFunctions.NAME && !user.reversed()) {
            Files.deleteIfExists(file);
            return;
        }
        YamlConfiguration conf = Files.exists(file) ? readYaml(file) : new YamlConfiguration();
        conf.set("pinned", user.pinned());
        conf.set("sort", user.sortFunction() == SortFunctions.NAME ? null : user.sortFunction().ordinal());
        conf.set("reversed", user.reversed() ? true : null);
        writeYaml(file, conf);
    }

    @Override
    protected void writeResidences(@NotNull Map<String, ResidenceSnapshot> residences) throws Exception {
        for (Map.Entry<String, ResidenceSnapshot> entry : residences.entrySet()) {
            writeResidenceFile(residenceFile(entry.getKey()), entry.getValue());
        }
    }

    static void writeResidenceFile(Path file, ResidenceSnapshot data) throws Exception {
        YamlConfiguration conf = Files.exists(file) ? readYaml(file) : new YamlConfiguration();
        if (data.icon() == null) {
            conf.set("icon", null);
        } else {
            String icon = XMaterial.matchXMaterial(data.icon()).name();
            conf.set("icon", data.customModelData() > 0 ? icon + ":" + data.customModelData() : icon);
        }
        conf.set("nickname", data.aliasName());
        conf.set("description", data.description());
        conf.set("rates", null);
        data.rates().forEach((key, rate) -> conf.set("rates." + key, serializeRate(rate)));
        writeYaml(file, conf);
    }

    @Override
    protected void deleteResidence(@NotNull String name) throws IOException {
        Files.deleteIfExists(residenceFile(name));
    }

    private Path userFile(UUID key) {
        return userDataFolder.resolve(key + ".yaml");
    }

    private Path residenceFile(String name) {
        if (name.isBlank() || name.contains("/") || name.contains("\\") || name.equals(".") || name.equals("..")) {
            throw new IllegalArgumentException("Invalid residence name for YAML storage: " + name);
        }
        return residenceDataFolder.resolve(name + ".yaml");
    }

    private static YamlConfiguration readYaml(Path file) throws Exception {
        YamlConfiguration conf = new YamlConfiguration();
        conf.loadFromString(Files.readString(file, StandardCharsets.UTF_8));
        return conf;
    }

    static ResidenceSnapshot readResidence(Path file) throws Exception {
        YamlConfiguration conf = readYaml(file);
        requireType(conf, "icon", conf.isString("icon"), file);
        requireType(conf, "nickname", conf.isString("nickname"), file);
        requireStringList(conf, "description", file);
        requireType(conf, "rates", conf.isConfigurationSection("rates"), file);
        Material icon = null;
        int customModelData = -1;
        String iconText = conf.getString("icon");
        if (iconText != null && !iconText.isBlank()) {
            String[] parts = iconText.split(":", 2);
            icon = XMaterial.matchXMaterial(parts[0]).map(XMaterial::parseMaterial)
                    .orElseThrow(() -> new IOException("Unknown residence icon in " + file + ": " + parts[0]));
            if (parts.length > 1) customModelData = Integer.parseInt(parts[1]);
        }
        return new ResidenceSnapshot(icon, customModelData, conf.getString("nickname"),
                new ArrayList<>(conf.getStringList("description")), readRates(conf.getConfigurationSection("rates"), file));
    }

    private static Map<UUID, ResidenceRate> readRates(@Nullable ConfigurationSection root, Path file) throws IOException {
        Map<UUID, ResidenceRate> rates = new LinkedHashMap<>();
        if (root == null) return rates;
        for (String key : root.getKeys(false)) {
            ConfigurationSection section = root.getConfigurationSection(key);
            if (section == null) throw new IllegalArgumentException("Invalid residence rate: " + key);
            requireType(section, "time", section.isString("time"), file);
            requireType(section, "content", section.isString("content"), file);
            requireType(section, "recommend", section.isBoolean("recommend"), file);
            UUID author = UUID.fromString(key);
            String timeText = section.getString("time");
            LocalDateTime time = timeText == null ? LocalDateTime.now() : ResidenceListAPI.parse(timeText);
            rates.put(author, new ResidenceRate(author, section.getString("content", ""),
                    section.getBoolean("recommend"), time));
        }
        return rates;
    }

    private static void requireStringList(ConfigurationSection conf, String key, Path file) throws IOException {
        if (!conf.contains(key)) return;
        if (!conf.isList(key) || conf.getList(key).stream().anyMatch(value -> !(value instanceof String))) {
            throw new IOException("Expected a list of strings at " + key + " in " + file);
        }
    }

    private static void requireType(ConfigurationSection conf, String key, boolean valid, Path file) throws IOException {
        if (conf.contains(key) && !valid) throw new IOException("Invalid value at " + key + " in " + file);
    }

    private static Map<String, Object> serializeRate(ResidenceRate rate) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("time", ResidenceListAPI.format(rate.time()));
        result.put("recommend", rate.recommend());
        result.put("content", rate.content());
        return result;
    }

    private static void writeYaml(Path file, YamlConfiguration conf) throws IOException {
        Path temporary = Files.createTempFile(file.getParent(), file.getFileName().toString(), ".tmp");
        boolean moved = false;
        try {
            Files.writeString(temporary, conf.saveToString(), StandardCharsets.UTF_8);
            try {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
            moved = true;
        } finally {
            if (!moved) Files.deleteIfExists(temporary);
        }
    }
}
