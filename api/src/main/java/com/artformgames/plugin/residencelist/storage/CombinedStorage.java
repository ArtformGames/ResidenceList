package com.artformgames.plugin.residencelist.storage;

import cc.carm.lib.easyplugin.user.UserDataRegistry;
import com.artformgames.plugin.residencelist.api.ResidenceManager;
import com.artformgames.plugin.residencelist.api.residence.ResidenceData;
import com.artformgames.plugin.residencelist.api.user.UserListData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public abstract class CombinedStorage<U extends UserListData, D extends ResidenceData> implements DataStorage<U, D> {

    protected final @NotNull UserDataRegistry<UUID, U> userManager;
    protected final @NotNull ResidenceManager<D> residenceManager;

    protected CombinedStorage(@NotNull UserDataRegistry<UUID, U> userManager,
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
    public boolean updateResidence(@NotNull ResidenceData data, @NotNull Consumer<ResidenceData> dataConsumer) {
        return this.residenceManager.updateResidence(data, dataConsumer);
    }

    @Override
    public void saveAllResidences() {
        this.residenceManager.saveAllResidences();
    }

}
