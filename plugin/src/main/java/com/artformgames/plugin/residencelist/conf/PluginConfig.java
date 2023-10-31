package com.artformgames.plugin.residencelist.conf;

import cc.carm.lib.configuration.core.ConfigurationRoot;
import cc.carm.lib.configuration.core.annotation.HeaderComment;
import cc.carm.lib.configuration.core.value.type.ConfiguredValue;
import cc.carm.lib.mineconfiguration.bukkit.value.item.ConfiguredItem;
import org.bukkit.Material;

import java.time.format.DateTimeFormatter;

public class PluginConfig extends ConfigurationRoot {

    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static final ConfiguredValue<Boolean> DEBUG = ConfiguredValue.of(Boolean.class, false);

    @HeaderComment({
            "Statistics Settings",
            "This option is used to help developers count plug-in versions and usage, and it will never affect performance and user experience.",
            "Of course, you can also choose to turn it off here for this plugin,",
            "or turn it off for all plugins in the configuration file under \"plugins/bStats\"."
    })
    public static final ConfiguredValue<Boolean> METRICS = ConfiguredValue.of(Boolean.class, true);

    @HeaderComment({
            "Check update settings",
            "This option is used by the plug-in to determine whether to check for updates.",
            "If you do not want the plug-in to check for updates and prompt you, you can choose to close.",
            "Checking for updates is an asynchronous operation that will never affect performance and user experience."
    })
    public static final ConfiguredValue<Boolean> CHECK_UPDATE = ConfiguredValue.of(Boolean.class, true);


    public static final class INFORMATION extends ConfigurationRoot {


        public static final ConfiguredItem ICON = ConfiguredItem.create()
                .defaultType(Material.GRASS_BLOCK)
                .defaultName("&7# &f%(name)")
                .defaultLore(
                        "{  &f&o}#description#{1,1}",
                        "&7Owner: &f%(owner)",
                        "&7Size: &f%(size) &7block(s)",
                        "&7Members: &f%(members)",
                        "#click-lore#{1}"
                ).params("name", "owner", "members", "create_time", "size")
                .build();

    }

    public static final class GUI extends ConfigurationRoot {


        public static final class PAGE_ITEMS extends ConfigurationRoot {

            public static final ConfiguredItem PREVIOUS_PAGE = ConfiguredItem.create()
                    .defaults(Material.ARROW, "&fPrevious page")
                    .defaultLore(
                            " ",
                            "&f  Left click to view the previous page.",
                            "&f  Right click to view the first page.",
                            " ")
                    .build();

            public static final ConfiguredItem NEXT_PAGE = ConfiguredItem.create()
                    .defaults(Material.ARROW, "&fNext page")
                    .defaultLore(
                            " ",
                            "&f  Left click to view the next page.",
                            "&f  Right click to view the last page.",
                            " "
                    ).build();

        }

    }

}
