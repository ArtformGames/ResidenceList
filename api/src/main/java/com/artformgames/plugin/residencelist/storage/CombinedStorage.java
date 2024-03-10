package com.artformgames.plugin.residencelist.storage;

import com.artformgames.plugin.residencelist.api.ResidenceManager;
import com.artformgames.plugin.residencelist.api.UserManager;
import com.artformgames.plugin.residencelist.api.residence.ResidenceData;
import com.artformgames.plugin.residencelist.api.user.UserListData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class CombinedStorage<U extends UserListData, D extends ResidenceData> implements DataStorage<U, D> {

    protected final @NotNull UserManager<U> userManager;
    protected final @NotNull ResidenceManager<D> residenceManager;

    protected CombinedStorage(@NotNull UserManager<U> userManager,
                              @NotNull ResidenceManager<D> residenceManager) {
        this.userManager = userManager;
        this.residenceManager = residenceManager;
    }

    @Override
    public abstract void initialize();

    @Override
    public abstract void shutdown();

    @Override
    public @Unmodifiable
    @NotNull Set<D> listResidences() {
        return this.residenceManager.listResidences();
    }

    @Override
    public @Nullable D getResidence(@NotNull String name) {
        return this.residenceManager.getResidence(name);
    }

    @Override
    public @NotNull D loadResidence(String residenceName) throws Exception {
        return this.residenceManager.loadResidence(residenceName);
    }

    @Override
    public void renameResidence(String oldName, String newName) {
        this.residenceManager.renameResidence(oldName, newName);
    }

    @Override
    public void removeResidence(@NotNull String name) {
        this.residenceManager.removeResidence(name);
    }

    @Override
    public @Nullable U getNullable(@NotNull UUID key) {
        return null;
    }

    @Override
    public @NotNull Optional<@Nullable U> getOptional(@NotNull UUID key) {
        return Optional.empty();
    }

    @Override
    public boolean updateResidence(@NotNull ResidenceData data, @NotNull Consumer<ResidenceData> dataConsumer) {
        return this.residenceManager.updateResidence(data, dataConsumer);
    }

    @Override
    public void saveAllResidences() {
        this.residenceManager.saveAllResidences();
    }

    @Override
    public @Unmodifiable
    @NotNull Set<U> list() {
        return this.userManager.list();
    }

    @Override
    public @NotNull U get(@NotNull UUID uuid) {
        return this.userManager.get(uuid);
    }

    @Override
    public @NotNull CompletableFuture<U> load(@NotNull UUID key, @NotNull Supplier<Boolean> cacheCondition) {
        return this.userManager.load(key, cacheCondition);
    }

    @Override
    public CompletableFuture<Boolean> save(@NotNull U user) {
        return this.userManager.save(user);
    }

    @Override
    public CompletableFuture<Boolean> unload(@NotNull UUID key, boolean save) {
        return this.userManager.unload(key, save);
    }

    @Override
    public void saveAllUsers() {
        this.userManager.saveAllUsers();
    }

}
