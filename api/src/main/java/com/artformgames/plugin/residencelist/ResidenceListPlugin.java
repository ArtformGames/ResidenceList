package com.artformgames.plugin.residencelist;

import cc.carm.lib.easyplugin.user.UserDataRegistry;
import com.artformgames.plugin.residencelist.api.ResidenceManager;
import com.artformgames.plugin.residencelist.api.user.UserListData;
import com.artformgames.plugin.residencelist.storage.DataStorage;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface ResidenceListPlugin {

    void openGUI(@NotNull Player player, @Nullable String owner);

    @NotNull DataStorage<?, ?> getStorage();

    void setStorage(DataStorage<?, ?> storage);

    default @NotNull ResidenceManager<?> getResidenceManager() {
        return getStorage();
    }

    default @NotNull UserDataRegistry<UUID, ? extends UserListData> getUserManager() {
        return getStorage();
    }

}
