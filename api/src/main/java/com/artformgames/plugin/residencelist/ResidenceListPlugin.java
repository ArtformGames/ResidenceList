package com.artformgames.plugin.residencelist;

import com.artformgames.plugin.residencelist.api.ResidenceManager;
import com.artformgames.plugin.residencelist.api.UserManager;
import com.artformgames.plugin.residencelist.storage.DataStorage;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ResidenceListPlugin {

    void openGUI(@NotNull Player player, @Nullable String owner);

    @NotNull DataStorage<?, ?> getStorage();

    void setStorage(DataStorage<?, ?> storage);

    default @NotNull ResidenceManager<?> getResidenceManager() {
        return getStorage();
    }

    default @NotNull UserManager<?> getUserManager() {
        return getStorage();
    }

}
