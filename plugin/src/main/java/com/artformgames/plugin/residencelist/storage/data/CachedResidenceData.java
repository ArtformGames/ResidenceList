package com.artformgames.plugin.residencelist.storage.data;

import com.artformgames.plugin.residencelist.api.residence.ResidenceData;
import com.artformgames.plugin.residencelist.api.residence.ResidenceRate;
import com.artformgames.plugin.residencelist.api.residence.ResidenceVisitor;
import com.artformgames.plugin.residencelist.conf.PluginConfig;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Cached metadata. No getter, setter or save performs IO. */
public class CachedResidenceData implements ResidenceData {
    private final ClaimedResidence residence;
    private Material icon;
    private int customModelData;
    private String aliasName;
    private List<String> description;
    private final Map<UUID, ResidenceRate> rates = new LinkedHashMap<>();
    private final Map<UUID, ResidenceVisitor> visitors = new LinkedHashMap<>();
    private ResidenceSnapshot saved;
    private Runnable saveAction;

    public CachedResidenceData(ClaimedResidence residence, ResidenceSnapshot snapshot) {
        this.residence = residence;
        this.icon = snapshot.icon();
        this.customModelData = snapshot.customModelData();
        this.aliasName = snapshot.aliasName();
        this.description = snapshot.description();
        this.rates.putAll(snapshot.rates());
        snapshot.visitors().forEach(visitor -> visitors.merge(visitor.uuid(), visitor,
                (old, latest) -> old.lastTime().isAfter(latest.lastTime()) ? old : latest));
        this.saved = snapshot;
    }

    public synchronized void bindPersistence(Runnable save) {
        this.saveAction = save;
    }

    public synchronized ResidenceSnapshot snapshot() {
        return new ResidenceSnapshot(icon, customModelData, aliasName, description, rates, List.copyOf(visitors.values()));
    }

    public synchronized boolean isDirty() {
        return !snapshot().equals(saved);
    }

    public synchronized void markSaved(ResidenceSnapshot snapshot) {
        this.saved = snapshot;
    }

    @Override
    public @NotNull ClaimedResidence getResidence() {
        return residence;
    }

    @Override
    public synchronized @Nullable Material getIconMaterial() {
        return icon;
    }

    @Override
    public synchronized int getCustomModelData() {
        return customModelData;
    }

    @Override
    public synchronized void setIconMaterial(@NotNull Material material, int customModelData) {
        this.icon = material;
        this.customModelData = customModelData;
    }

    @Override
    public synchronized @NotNull String getDisplayName() {
        return aliasName == null ? getName() : aliasName;
    }

    @Override
    public synchronized @Nullable String getAliasName() {
        return aliasName;
    }

    @Override
    public synchronized void setNickname(@NotNull String name) {
        this.aliasName = name;
    }

    @Override
    public synchronized @NotNull List<String> getDescription() {
        return List.copyOf(description);
    }

    @Override
    public synchronized void setDescription(@NotNull List<String> description) {
        this.description = List.copyOf(description);
    }

    @Override
    public boolean isPublicDisplayed() {
        return !residence.getPermissions().has(Flags.hidden, !PluginConfig.SETTINGS.DEFAULT_STATUS.getNotNull());
    }

    @Override
    public void setPublicDisplayed(boolean enabled) {
        residence.getPermissions().setFlag(Flags.hidden.name(),
                enabled ? FlagPermissions.FlagState.FALSE : FlagPermissions.FlagState.TRUE);
    }

    @Override
    public synchronized Map<UUID, ResidenceRate> getRates() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(rates));
    }

    @Override
    public synchronized void setRates(Map<UUID, ResidenceRate> rates) {
        Map<UUID, ResidenceRate> copy = new LinkedHashMap<>(rates);
        this.rates.clear();
        this.rates.putAll(copy);
    }

    @Override
    public synchronized void setRate(UUID uuid, ResidenceRate rate) {
        rates.put(uuid, rate);
    }

    @Override
    public synchronized void removeRate(UUID author) {
        rates.remove(author);
    }

    @Override
    public synchronized @NotNull List<ResidenceVisitor> getVisitors() {
        return List.copyOf(visitors.values());
    }

    @Override
    public synchronized int getVisitorCount() {
        return visitors.size();
    }

    @Override
    public synchronized void recordVisit(@NotNull UUID uuid, @NotNull String name, @NotNull LocalDateTime time) {
        ResidenceVisitor visitor = new ResidenceVisitor(uuid, name, time);
        visitors.merge(uuid, visitor, (old, latest) -> old.lastTime().isAfter(latest.lastTime()) ? old : latest);
    }

    @Override
    public void save() {
        Runnable action;
        synchronized (this) { action = saveAction; }
        if (action == null) throw new IllegalStateException("Residence data is not attached to a storage");
        action.run();
    }
}
