package com.artformgames.plugin.residencelist.api;

import com.artformgames.plugin.residencelist.api.user.UserListData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface UserManager<U extends UserListData> {

    @Unmodifiable @NotNull Set<U> list();

    @NotNull U get(@NotNull UUID uuid);

    default @NotNull U get(@NotNull Player player) {
        return get(player.getUniqueId());
    }

    @Nullable U getNullable(@NotNull UUID key);

    @NotNull Optional<@Nullable U> getOptional(@NotNull UUID key);

    @NotNull CompletableFuture<U> load(@NotNull UUID key);

    CompletableFuture<Boolean> save(@NotNull U user);

    CompletableFuture<Boolean> unload(@NotNull UUID key, boolean save);

    void saveAllUsers();

}
