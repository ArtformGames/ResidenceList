package com.artformgames.plugin.residencelist;

import com.artformgames.plugin.residencelist.api.ResidenceManager;
import com.artformgames.plugin.residencelist.api.UserManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

interface ResidenceListPlugin {

    void openGUI(@NotNull Player player, @Nullable String owner);

    @NotNull ResidenceManager getResidenceManager();

    @NotNull UserManager getUserManager();

}
