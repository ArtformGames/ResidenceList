package com.artformgames.plugin.residencelist;

import com.artformgames.plugin.residencelist.api.InformationManager;
import com.artformgames.plugin.residencelist.api.UserManager;
import org.bukkit.entity.Player;

public class ResidenceListAPI {

    protected static ResidenceListPlugin plugin;

    private ResidenceListAPI() {
    }

    public static void openGUI(Player player) {
        plugin.openGUI(player);
    }

    public static InformationManager getInformationManager() {
        return plugin.getInformationManager();
    }

    public static UserManager getUserManager() {
        return plugin.getUserManager();
    }


}
