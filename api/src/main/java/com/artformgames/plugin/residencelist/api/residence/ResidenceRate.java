package com.artformgames.plugin.residencelist.api.residence;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.UUID;

public record ResidenceRate(
        @NotNull UUID author, @NotNull String content,
        boolean recommend, @NotNull LocalDateTime time
) {

    public @Nullable String getAuthorName() {
        return Bukkit.getOfflinePlayer(author).getName();
    }

}
