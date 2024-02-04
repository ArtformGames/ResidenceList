package com.artformgames.plugin.residencelist.storage.yaml;

import cc.carm.lib.easyplugin.EasyPlugin;
import cc.carm.lib.easyplugin.user.UserDataManager;
import com.artformgames.plugin.residencelist.Main;
import com.artformgames.plugin.residencelist.api.residence.ResidenceData;
import com.artformgames.plugin.residencelist.api.sort.SortFunctions;
import com.artformgames.plugin.residencelist.storage.DataStorage;
import com.artformgames.plugin.residencelist.storage.yaml.data.YAMLResidenceData;
import com.artformgames.plugin.residencelist.storage.yaml.data.YAMLUserData;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;

public class YAMLStorage extends UserDataManager<UUID, YAMLUserData>
        implements DataStorage<YAMLUserData, YAMLResidenceData> {

    public static final String USER_DATA_FOLDER = "users";
    public static final String RESIDENCE_DATA_FOLDER = "residences";

    protected final @NotNull File userDataFolder;
    protected final @NotNull File residenceDataFolder;
    protected Map<String, YAMLResidenceData> residences = new HashMap<>();

    public YAMLStorage(@NotNull EasyPlugin plugin) {
        super(plugin);
        this.residenceDataFolder = initializeFolder(plugin.getDataFolder(), RESIDENCE_DATA_FOLDER);
        this.userDataFolder = initializeFolder(plugin.getDataFolder(), USER_DATA_FOLDER);
    }

    @Override
    public void initialize() {

        int loaded = loadAllResidences();
        plugin.log("Successfully loaded " + loaded + " residence data.");

        if (!Bukkit.getOnlinePlayers().isEmpty()) {
            plugin.log("Load online users' data...");
            loadOnline(Player::getUniqueId);
        }

    }

    @Override
    public void shutdown() {
        saveAllUsers();
        saveAllResidences();
        super.shutdown();
    }

    protected File initializeFolder(File parent, String folderName) {
        File folder = new File(parent, folderName);
        if (!folder.exists()) {
            folder.mkdirs();
        } else if (!folder.isDirectory()) {
            folder.delete();
            folder.mkdirs();
        }
        return folder;
    }

    @Override
    public @NotNull YAMLUserData emptyUser(@NotNull UUID key) {
        return new YAMLUserData(key, new ArrayList<>(), SortFunctions.NAME, false);
    }

    @Override
    protected @Nullable YAMLUserData loadData(@NotNull UUID key) {
        File userFile = new File(this.userDataFolder, key + ".yaml");
        if (!userFile.exists()) return null; // No data files.

        YamlConfiguration conf = YamlConfiguration.loadConfiguration(userFile);
        return new YAMLUserData(
                key, new ArrayList<>(conf.getStringList("pinned")),
                SortFunctions.parse(conf.getInt("sort", 0)),
                conf.getBoolean("reversed", false)
        );
    }

    @Override
    protected void saveData(@NotNull YAMLUserData data) throws Exception {
        File userFile = new File(this.userDataFolder, data.getKey() + ".yaml");
        if (data.isEmpty() && userFile.exists()) {
            userFile.delete(); // Delete empty data files.
            return;
        }

        YamlConfiguration conf;
        if (userFile.exists()) {
            conf = YamlConfiguration.loadConfiguration(userFile);
        } else {
            conf = new YamlConfiguration();
        }

        conf.set("pinned", data.getPinned());
        if (data.getSortFunction() != SortFunctions.NAME) {
            conf.set("sort", data.getSortFunction().ordinal());
        }
        if (data.isSortReversed()) conf.set("reversed", true);
        conf.save(userFile);
    }

    @Override
    public @Unmodifiable @NotNull Set<YAMLResidenceData> listResidences() {
        return Set.copyOf(this.residences.values());
    }

    @Override
    public @Nullable YAMLResidenceData getResidence(@NotNull String name) {
        ClaimedResidence residence = Residence.getInstance().getResidenceManager().getByName(name);
        if (residence == null) return null;

        YAMLResidenceData existed = this.residences.get(name);
        if (existed != null) return existed;

        File dataFile = new File(this.residenceDataFolder, residence.getName() + ".yaml");
        YAMLResidenceData data = new YAMLResidenceData(dataFile, residence);
        this.residences.put(residence.getName(), data);

        return data;
    }

    public int loadAllResidences() {
        String[] filesList = this.residenceDataFolder.list();
        if (filesList == null || filesList.length < 1) return 0;

        List<File> files = Arrays.stream(filesList)
                .map(s -> new File(this.residenceDataFolder, s))
                .filter(File::isFile).toList();

        HashMap<String, YAMLResidenceData> loaded = new HashMap<>();

        if (!files.isEmpty()) {
            for (File file : files) {
                String residenceName = file.getName().substring(0, file.getName().lastIndexOf("."));
                try {
                    YAMLResidenceData data = loadResidence(residenceName, file);
                    Main.debugging("Successfully loaded residence data for '" + residenceName + "' !");
                    loaded.put(residenceName, data);
                } catch (Exception ex) {
                    Main.severe("Error occurred when loading residence data #" + file.getAbsolutePath() + " !");
                    ex.printStackTrace();
                }
            }
        }

        this.residences = loaded;
        return loaded.size();
    }

    public YAMLResidenceData loadResidence(String residenceName, File file) throws Exception {
        ClaimedResidence residence = Residence.getInstance().getResidenceManager().getByName(residenceName);
        if (residence == null) throw new Exception("Residence not found: " + residenceName);
        return new YAMLResidenceData(file, residence);
    }

    @Override
    public YAMLResidenceData loadResidence(String residenceName) throws Exception {
        ClaimedResidence residence = Residence.getInstance().getResidenceManager().getByName(residenceName);
        if (residence == null) throw new Exception("Residence not found: " + residenceName);
        return new YAMLResidenceData(new File(this.residenceDataFolder, residence.getName() + ".yaml"), residence);
    }


    @Override
    public void renameResidence(String oldName, String newName) {
        YAMLResidenceData data = this.residences.remove(oldName);
        if (data == null) return; // No data for this residence yet.

        this.residences.remove(oldName);
        this.residences.put(newName, data);

        File n = new File(this.residenceDataFolder, newName + ".yaml");
        try {
            data.renameTo(n);
            Main.debugging("Successfully renamed residence data for '" + oldName + "' to '" + newName + "' !");
        } catch (Exception e) {
            Main.severe("Error occurred when renaming residence data for '" + oldName + "' to '" + newName + "' !");
            e.printStackTrace();
        }
    }

    @Override
    public boolean updateResidence(@NotNull ResidenceData data,
                                   @NotNull Consumer<ResidenceData> dataConsumer) {
        dataConsumer.accept(data);
        try {
            data.save();
        } catch (Exception e) {
            Main.severe("Error occurred when saving residence data for '" + data.getName() + "' !");
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void saveAllResidences() {
        for (ResidenceData data : this.residences.values()) {
            try {
                data.save();
            } catch (Exception e) {
                Main.severe("Error occurred when saving residence data for '" + data.getName() + "' !");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void saveAllUsers() {
        super.saveAll(); // Use saveAll() provided by UserDataManager.
    }
}
