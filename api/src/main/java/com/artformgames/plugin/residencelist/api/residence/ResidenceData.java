package com.artformgames.plugin.residencelist.api.residence;

import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class ResidenceData {

    protected @NotNull File file;
    protected final @NotNull FileConfiguration conf;

    protected final @NotNull ClaimedResidence residence;

    protected @Nullable Material icon;
    protected @Nullable String aliasName;
    protected @NotNull List<String> description;

    protected boolean publicDisplayed;
    protected final Map<UUID, ResidenceRate> rates;

    public ResidenceData(@NotNull File file, @NotNull ClaimedResidence residence) {
        this.file = file;
        this.conf = file.exists() ? YamlConfiguration.loadConfiguration(file) : new YamlConfiguration();
        this.residence = residence;

        this.icon = Optional.ofNullable(conf.getString("icon"))
                .flatMap(XMaterial::matchXMaterial)
                .map(XMaterial::parseMaterial).orElse(null);
        this.aliasName = conf.getString("alias", residence.getName());
        this.description = conf.getStringList("description");

        this.publicDisplayed = conf.getBoolean("public", true);
        this.rates = ResidenceRate.loadFrom(conf.getConfigurationSection("rates"));
    }

    public @NotNull FileConfiguration getConfiguration() {
        return conf;
    }

    public @NotNull ClaimedResidence getResidence() {
        return residence;
    }

    public @Nullable Material getIcon() {
        return icon;
    }

    public void setIcon(@NotNull Material material) {
        this.icon = material;
        this.conf.set("icon", XMaterial.matchXMaterial(material).name());
    }

    public @NotNull String getDisplayName() {
        return Optional.ofNullable(getAliasName()).orElse(getName());
    }

    public @Nullable String getAliasName() {
        return this.aliasName;
    }

    public void setAliasName(@NotNull String name) {
        this.aliasName = name;
        this.conf.set("alias", name);
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

    public boolean isPublicDisplayed() {
        return publicDisplayed;
    }

    public void setPublicDisplayed(boolean publicDisplayed) {
        this.publicDisplayed = publicDisplayed;
        this.conf.set("public", publicDisplayed);
    }

    public Map<UUID, ResidenceRate> getRates() {
        return rates;
    }

    public void setRates(Map<UUID, ResidenceRate> rates) {
        this.rates.clear();
        this.rates.putAll(rates);

        this.conf.set("rates", null); // Clear existing rates.
        rates.forEach((k, v) -> this.conf.set(k.toString(), v.serialize()));
    }

    public void setRate(UUID uuid, ResidenceRate rate) {
        this.rates.put(uuid, rate);
        this.conf.set(uuid.toString(), rate.serialize());
    }

    public void addRate(ResidenceRate rate) {
        setRate(rate.author(), rate);
    }

    public void addRate(String content, boolean recommend, UUID author, LocalDateTime time) {
        addRate(new ResidenceRate(author, content, recommend, time));
    }

    public void addRate(String content, boolean recommend, UUID author) {
        addRate(content, recommend, author, LocalDateTime.now());
    }

    public void removeRate(UUID author) {
        this.rates.remove(author);
        this.conf.set(author.toString(), null);
    }

    public String getName() {
        return getResidence().getName();
    }

    public String getOwner() {
        return getResidence().getOwner();
    }

    public boolean isOwner(@NotNull Player player) {
        return getResidence().isOwner(player);
    }

    public @Nullable Location getTeleportLocation(Player player) {
        return getResidence().getTeleportLocation(player, false);
    }

    public boolean canTeleport(Player player) {
        return checkPermission(player, Flags.tp, true)
                && checkPermission(player, Flags.move, true);
    }

    public boolean checkPermission(Player player, Flags flags, boolean defaults) {
        return getResidence().getPermissions().playerHas(player, flags, defaults);
    }


    public void renameTo(@NotNull File newFile) throws Exception {
        if (this.file.exists()) this.file.delete(); // Delete old file.
        this.file = newFile;
        save();
    }

    public void save() throws Exception {
        if (!this.file.exists()) {
            this.file.createNewFile();
        }
        this.conf.save(this.file);
    }

}
