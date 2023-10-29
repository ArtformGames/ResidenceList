package com.artformgames.plugin.residencelist.api;

import com.artformgames.plugin.residencelist.api.residence.ResidenceData;
import com.bekvon.bukkit.residence.Residence;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public interface ResidenceManager {

    @Unmodifiable
    @NotNull Set<ResidenceData> listData();

    @Nullable ResidenceData getData(@NotNull String name);

    default @NotNull ResidenceData getData(@NotNull Residence residence) {
        return Objects.requireNonNull(getData(residence.getName()));
    }

    @NotNull ResidenceData updateData(@NotNull String name, @NotNull Consumer<ResidenceData> dataConsumer);

}
