package com.artformgames.plugin.residencelist.conf;

import cc.carm.lib.configuration.core.ConfigurationRoot;
import cc.carm.lib.configuration.core.annotation.HeaderComment;
import cc.carm.lib.configuration.core.value.type.ConfiguredList;
import cc.carm.lib.configuration.core.value.type.ConfiguredValue;
import cc.carm.lib.mineconfiguration.bukkit.value.ConfiguredSound;
import cc.carm.lib.mineconfiguration.bukkit.value.item.ConfiguredItem;
import com.artformgames.plugin.residencelist.ui.*;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Material;
import org.bukkit.Sound;

import java.time.format.DateTimeFormatter;
import java.util.Objects;

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


    public static final class SETTINGS extends ConfigurationRoot {

        public static final ConfiguredValue<Integer> LETTERS_PRE_LINE = ConfiguredValue.of(Integer.class, 35);

        public static final ConfiguredList<Material> BLOCKED_ICON_TYPES = ConfiguredList.builderOf(Material.class).fromString()
                .parseValue(s -> Objects.requireNonNull(XMaterial.matchXMaterial(s).orElseThrow().parseMaterial()))
                .serializeValue(d -> XMaterial.matchXMaterial(d).name())
                .defaults(Material.CHEST, Material.CHAIN, Material.REDSTONE).build();

    }

    public static final class ICON extends ConfigurationRoot {

        public static final ConfiguredItem INFO = ConfiguredItem.create()
                .defaultType(Material.GRASS_BLOCK)
                .defaultName("&7# &f%(name)")
                .defaultLore(
                        "{  &f&o}#description#{1,1}",
                        "&7Owner: &f%(owner)",
                        "&7Size: &f%(size) &7block(s)",
                        "&7Members: &f%(members)",
                        "&7Rates: &a%(likes) &7likes, &c%(dislikes) &7dislikes.",
                        "#click-lore#{1,0}"
                ).params("name", "owner", "members", "size", "likes", "dislikes")
                .build();

        public static final class RATE extends ConfigurationRoot {

            public static final ConfiguredItem LIKE = ConfiguredItem.create()
                    .defaultType(Material.PLAYER_HEAD)
                    .defaultName("&7From &f%(owner)")
                    .defaultLore(
                            "{&7-  &f&o}#comment#{1,1}",
                            "&7Commented at %(date),",
                            "&7This user &arecommended &7this residence.",
                            "#click-lore#{1,0}"
                    ).params("owner", "date").build();

            public static final ConfiguredItem DISLIKE = ConfiguredItem.create()
                    .defaultType(Material.PLAYER_HEAD)
                    .defaultName("&7From &f%(owner)")
                    .defaultLore(
                            "{  &f&o}#comment#{1}",
                            "&7",
                            "&7Commented at %(date)",
                            "&7This user &cnot recommended &7this residence.",
                            "#click-lore#{1,0}"
                    ).params("owner", "date").build();

        }


        public static final ConfiguredItem EMPTY = ConfiguredItem.create()
                .defaultType(Material.BLACK_STAINED_GLASS_PANE)
                .defaultName("&7  ").build();


        public static final class PAGE extends ConfigurationRoot {

            public static final ConfiguredItem PREVIOUS_PAGE = ConfiguredItem.create()
                    .defaults(Material.ARROW, "&fPrevious page")
                    .defaultLore(
                            " ",
                            "&f  Left click to view the previous page.",
                            "&f  Right click to view the first page.",
                            " ")
                    .build();


            public static final ConfiguredItem NO_PREVIOUS_PAGE = ConfiguredItem.create()
                    .defaults(Material.ARROW, "&fPrevious page")
                    .defaultLore(
                            " ",
                            "&f  There is no previous page.",
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

            public static final ConfiguredItem NO_NEXT_PAGE = ConfiguredItem.create()
                    .defaults(Material.ARROW, "&fNext page")
                    .defaultLore(
                            " ",
                            "&f  There is no next page.",
                            " ")
                    .build();

        }

    }

    public static final class GUI extends ConfigurationRoot {

        public static final ConfiguredSound OPEN_SOUND = ConfiguredSound.of(Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
        public static final ConfiguredSound CLICK_SOUND = ConfiguredSound.of(Sound.UI_BUTTON_CLICK);

        public static final Class<?> LIST = ResidenceListUI.CONFIG.class;
        public static final Class<?> INFO = ResidenceInfoUI.CONFIG.class;
        public static final Class<?> MANAGE = ResidenceManageUI.CONFIG.class;
        public static final Class<?> ADMIN = ResidenceAdminUI.CONFIG.class;
        public static final Class<?> SELECT = SelectIconGUI.CONFIG.class;

    }

}
