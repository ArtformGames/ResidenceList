package com.artformgames.plugin.residencelist.api;

import com.artformgames.plugin.residencelist.api.residence.ResidenceData;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public interface ResidenceManager<D extends ResidenceData> {

    @Unmodifiable
    @NotNull Set<D> listResidences();

    @Nullable D getResidence(@NotNull String name);

    default @NotNull D getResidence(@NotNull ClaimedResidence residence) {
        return Objects.requireNonNull(getResidence(residence.getName()));
    }

    @NotNull D loadResidence(String residenceName) throws Exception;

    void renameResidence(String oldName, String newName);

    void removeResidence(@NotNull String name);

    boolean updateResidence(@NotNull ResidenceData data, @NotNull Consumer<ResidenceData> dataConsumer);

    default boolean updateResidence(@NotNull String name, @NotNull Consumer<ResidenceData> dataConsumer) {
        ResidenceData data = getResidence(name);
        if (data == null) return false;
        return updateResidence(data, dataConsumer);
    }

    void saveAllResidences();

}
