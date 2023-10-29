package com.artformgames.plugin.residencelist.hooker;

import cc.carm.lib.easyplugin.papi.EasyPlaceholder;
import com.artformgames.plugin.residencelist.ResidenceListAPI;
import com.artformgames.plugin.residencelist.api.residence.ResidenceData;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PluginExpansion extends EasyPlaceholder {

    public PluginExpansion(@NotNull JavaPlugin plugin, @NotNull String rootIdentifier) {
        super(plugin, rootIdentifier);

        handle("name", (player, args) -> {
            if (args.length < 1) return "WRONG_ARGS";

            ResidenceData data = ResidenceListAPI.getResidenceManager().getData(args[0]);
            if (data == null) return "WRONG_NAME";

            return data.getDisplayName();
        }, List.of("residence"));

        handle("status", (player, args) -> {
            if (args.length < 1) return "WRONG_ARGS";

            ResidenceData data = ResidenceListAPI.getResidenceManager().getData(args[0]);
            if (data == null) return "WRONG_NAME";

            return data.isPublicDisplayed();
        }, List.of("residence"));

    }

}
