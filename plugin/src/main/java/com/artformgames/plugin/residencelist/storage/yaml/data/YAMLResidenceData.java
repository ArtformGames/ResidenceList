package com.artformgames.plugin.residencelist.storage.yaml.data;

import com.artformgames.plugin.residencelist.ResidenceListAPI;
import com.artformgames.plugin.residencelist.api.residence.ResidenceData;
import com.artformgames.plugin.residencelist.api.residence.ResidenceRate;
import com.artformgames.plugin.residencelist.conf.PluginConfig;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;

public class YAMLResidenceData implements ResidenceData {

    protected @NotNull File file;
    protected final @NotNull FileConfiguration conf;

    protected final @NotNull ClaimedResidence residence;

    protected @Nullable Material icon;
    protected int customModelData; // <= 0 means no custom model data.

    protected @Nullable String aliasName;
    protected @NotNull List<String> description;

    protected final Map<UUID, ResidenceRate> rates;

    public YAMLResidenceData(@NotNull File file, @NotNull ClaimedResidence residence) {
        this.file = file;
        this.conf = file.exists() ? YamlConfiguration.loadConfiguration(file) : new YamlConfiguration();
        this.residence = residence;

        String iconData = conf.getString("icon");

        if (iconData != null) {
            String[] args = iconData.split(":");
            this.icon = XMaterial.matchXMaterial(args[0]).map(XMaterial::parseMaterial).orElse(null);
            try {
                this.customModelData = args.length > 1 ? Integer.parseInt(args[1]) : -1;
            } catch (NumberFormatException | NullPointerException ignored) {
            }
        }

        this.aliasName = conf.getString("nickname", residence.getName());
        this.description = conf.getStringList("description");
        this.rates = loadRatesFrom(conf.getConfigurationSection("rates"));
    }

    public @NotNull FileConfiguration getConfiguration() {
        return conf;
    }

    public @NotNull ClaimedResidence getResidence() {
        return residence;
    }

    public @Nullable Material getIconMaterial() {
        return icon;
    }

    public int getCustomModelData() {
        return customModelData;
    }

    public void setIconMaterial(@NotNull Material material, int customModelData) {
        this.icon = material;
        this.customModelData = customModelData;

        if (customModelData > 0) {
            this.conf.set("icon", XMaterial.matchXMaterial(material).name() + ":" + customModelData);
        } else {
            this.conf.set("icon", XMaterial.matchXMaterial(material).name());
        }
    }

    public @NotNull String getDisplayName() {
        return Optional.ofNullable(getAliasName()).orElse(getName());
    }

    public @Nullable String getAliasName() {
        return this.aliasName;
    }

    public void setNickname(@NotNull String name) {
        this.aliasName = name;
        this.conf.set("nickname", name);
    }

    public @NotNull List<String> getDescription() {
        return description;
    }

    public void setDescription(@NotNull List<String> description) {
        this.description = description;
        this.conf.set("description", description);
    }

    public void setDescription(@NotNull String... descriptions) {
        setDescription(List.of(descriptions));
    }

    @Override
    public boolean isPublicDisplayed() {
        return !getResidence().getPermissions().has(Flags.hidden, !PluginConfig.SETTINGS.DEFAULT_STATUS.resolve());
    }

    @Override
    public void setPublicDisplayed(boolean enabled) {
        getResidence().getPermissions().setFlag(
                Flags.hidden.getName(),
                enabled ? FlagPermissions.FlagState.FALSE : FlagPermissions.FlagState.TRUE
        );
    }

    public Map<UUID, ResidenceRate> getRates() {
        return rates;
    }

    public void setRates(Map<UUID, ResidenceRate> rates) {
        this.rates.clear();
        this.rates.putAll(rates);

        this.conf.set("rates", null); // Clear existing rates.
        rates.forEach((k, v) -> this.conf.set(k.toString(), serializeRate(v)));
    }

    public void setRate(UUID uuid, ResidenceRate rate) {
        this.rates.put(uuid, rate);
        this.conf.set("rates." + uuid.toString(), serializeRate(rate));
    }

    public void removeRate(UUID author) {
        this.rates.remove(author);
        this.conf.set(author.toString(), null);
    }

    public void renameTo(@NotNull File newFile) throws Exception {
        if (this.file.exists()) this.file.delete(); // Delete old file.
        this.file = newFile;
        save();
    }

    public boolean delete() throws Exception {
        return this.file.exists() && this.file.delete();
    }

    public void save() throws Exception {
        if (!this.file.exists()) {
            this.file.createNewFile();
        }
        this.conf.save(this.file);
    }


    public Map<String, Object> serializeRate(@NotNull ResidenceRate rate) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("time", ResidenceListAPI.format(rate.time()));
        data.put("recommend", rate.recommend());
        data.put("content", rate.content());
        return data;
    }

    public static @NotNull Map<UUID, ResidenceRate> loadRatesFrom(@Nullable ConfigurationSection root) {
        Map<UUID, ResidenceRate> rates = new LinkedHashMap<>();
        if (root == null) return rates;

        for (String key : root.getKeys(false)) {
            ConfigurationSection section = root.getConfigurationSection(key);
            if (section == null) continue;
            try {
                ResidenceRate rate = new ResidenceRate(
                        UUID.fromString(section.getName()),
                        section.getString("content", ""),
                        section.getBoolean("recommend"),
                        Optional.ofNullable(section.getString("time"))
                                .map(ResidenceListAPI::parse).orElse(LocalDateTime.now())
                );
                rates.put(rate.author(), rate);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        return rates;
    }

}
