package com.artformgames.plugin.residencelist.api.residence;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/** The most recent visit by a unique player; contains no live Bukkit objects. */
public record ResidenceVisitor(@NotNull UUID uuid, @NotNull String name, @NotNull LocalDateTime lastTime) {
    public ResidenceVisitor {
        Objects.requireNonNull(uuid, "uuid");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(lastTime, "lastTime");
    }
}
