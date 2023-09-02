package com.artformgames.plugin.residencelist.api;

import com.artformgames.plugin.residencelist.api.user.UserListData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface UserManager {

    @NotNull UserListData get(@NotNull UUID uuid);

    default @NotNull UserListData get(@NotNull Player player) {
        return get(player.getUniqueId());
    }

}
