package com.artformgames.plugin.residencelist.ui;

import cc.carm.lib.configuration.core.ConfigurationRoot;
import cc.carm.lib.easyplugin.gui.GUIType;
import cc.carm.lib.easyplugin.gui.paged.AutoPagedGUI;
import cc.carm.lib.mineconfiguration.bukkit.value.ConfiguredMessage;
import cc.carm.lib.mineconfiguration.bukkit.value.ConfiguredMessageList;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ResidenceListUI extends AutoPagedGUI {

    public ResidenceListUI(@NotNull Player player) {
        super(GUIType.SIX_BY_NINE, CONFIG.TITLE.parse(player), 10, 34);
    }




    public static final class CONFIG extends ConfigurationRoot {

        public static final ConfiguredMessage<String> TITLE = ConfiguredMessage.asString()
                .defaults("&a&lAll Requests")
                .build();

        public static final class ADDITIONAL_LORE extends ConfigurationRoot {

            public static final ConfiguredMessageList<String> CLICK = ConfiguredMessageList.asStrings().defaults(
                    "&a ▶ Click &8|&f View details"
            ).build();

        }

    }
}
