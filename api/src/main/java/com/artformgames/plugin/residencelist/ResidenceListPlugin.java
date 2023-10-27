package com.artformgames.plugin.residencelist;

import com.artformgames.plugin.residencelist.api.ResidenceManager;
import com.artformgames.plugin.residencelist.api.UserManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

interface ResidenceListPlugin {

    void openGUI(@NotNull Player player);

    @NotNull ResidenceManager getResidenceManager();

    @NotNull UserManager getUserManager();
    
}
