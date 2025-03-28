package com.artformgames.plugin.residencelist.conf;

import cc.carm.lib.configuration.Configuration;
import cc.carm.lib.configuration.annotation.ConfigPath;
import cc.carm.lib.configuration.annotation.HeaderComments;
import cc.carm.lib.configuration.value.standard.ConfiguredList;
import cc.carm.lib.configuration.value.standard.ConfiguredValue;
import cc.carm.lib.mineconfiguration.bukkit.value.ConfiguredSound;
import cc.carm.lib.mineconfiguration.bukkit.value.item.ConfiguredItem;
import com.artformgames.plugin.residencelist.ui.ResidenceInfoUI;
import com.artformgames.plugin.residencelist.ui.ResidenceListUI;
import com.artformgames.plugin.residencelist.ui.ResidenceManageUI;
import com.artformgames.plugin.residencelist.ui.SelectIconGUI;
import com.artformgames.plugin.residencelist.ui.admin.ResidenceAdminUI;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Material;
import org.bukkit.Sound;

import java.time.format.DateTimeFormatter;
import java.util.Objects;


@ConfigPath(root = true)
public interface PluginConfig extends Configuration {

    DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    ConfiguredValue<Boolean> DEBUG = ConfiguredValue.of(Boolean.class, false);

    @HeaderComments({
            "Statistics Settings",
            "This option is used to help developers count plug-in versions and usage, and it will never affect performance and user experience.",
            "Of course, you can also choose to turn it off here for this plugin,",
            "or turn it off for all plugins in the configuration file under \"plugins/bStats\"."
    })
    ConfiguredValue<Boolean> METRICS = ConfiguredValue.of(Boolean.class, true);

    @HeaderComments({
            "Check update settings",
            "This option is used by the plug-in to determine whether to check for updates.",
            "If you do not want the plug-in to check for updates and prompt you, you can choose to close.",
            "Checking for updates is an asynchronous operation that will never affect performance and user experience."
    })
    ConfiguredValue<Boolean> CHECK_UPDATE = ConfiguredValue.of(Boolean.class, true);


    interface SETTINGS extends Configuration {

        @HeaderComments({
                "Remove info data when failed to load.",
                "  true = Delete the info data, false = Only notify, keep the info data."
        })
        ConfiguredValue<Boolean> AUTO_REMOVE = ConfiguredValue.of(Boolean.class, true);

        @HeaderComments("Default residence status (Public = true / Private = false)")
        ConfiguredValue<Boolean> DEFAULT_STATUS = ConfiguredValue.of(Boolean.class, true);

        @HeaderComments("How many letters are displayed per line in the residence description.")
        ConfiguredValue<Integer> LETTERS_PRE_LINE = ConfiguredValue.of(Integer.class, 35);

        @HeaderComments("The unsuitable icon types for the residence list.")
        ConfiguredList<Material> BLOCKED_ICON_TYPES = ConfiguredList.builderOf(Material.class).fromString()
                .parse(s -> Objects.requireNonNull(XMaterial.matchXMaterial(s).orElseThrow().parseMaterial()))
                .serialize(d -> XMaterial.matchXMaterial(d).name())
                .defaults(Material.CHEST, Material.CHAIN, Material.REDSTONE).build();

    }

    interface ICON extends Configuration {

        @HeaderComments("The icon of the residence list.")
        ConfiguredItem INFO = ConfiguredItem.create()
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

        interface RATE extends Configuration {

            ConfiguredItem LIKE = ConfiguredItem.create()
                    .defaultType(Material.PLAYER_HEAD)
                    .defaultName("&7From &f%(owner)")
                    .defaultLore(
                            "{&7-  &f&o}#comment#{1,1}",
                            "&7Commented at %(date),",
                            "&7This user &arecommended &7this residence.",
                            "#click-lore#{1,0}"
                    ).params("owner", "date").build();

            ConfiguredItem DISLIKE = ConfiguredItem.create()
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


        ConfiguredItem EMPTY = ConfiguredItem.create()
                .defaultType(Material.BLACK_STAINED_GLASS_PANE)
                .defaultName("&7  ").build();


        interface PAGE extends Configuration {

            ConfiguredItem PREVIOUS_PAGE = ConfiguredItem.create()
                    .defaultType(Material.ARROW)
                    .defaultName("&fPrevious page")
                    .defaultLore(
                            " ",
                            "&f  Left click to view the previous page.",
                            "&f  Right click to view the first page.",
                            " ")
                    .build();


            ConfiguredItem NO_PREVIOUS_PAGE = ConfiguredItem.create()
                    .defaultType(Material.ARROW)
                    .defaultName("&fPrevious page")
                    .defaultLore(
                            " ",
                            "&f  There is no previous page.",
                            " ")
                    .build();

            ConfiguredItem NEXT_PAGE = ConfiguredItem.create()
                    .defaultType(Material.ARROW)
                    .defaultName("&fNext page")
                    .defaultLore(
                            " ",
                            "&f  Left click to view the next page.",
                            "&f  Right click to view the last page.",
                            " "
                    ).build();

            ConfiguredItem NO_NEXT_PAGE = ConfiguredItem.create()
                    .defaultType(Material.ARROW)
                    .defaultName("&fNext page")
                    .defaultLore(
                            " ",
                            "&f  There is no next page.",
                            " ")
                    .build();

        }

    }

    interface GUI extends Configuration {

        @HeaderComments("The sound played when the GUI is opened.")
        ConfiguredSound OPEN_SOUND = ConfiguredSound.of(Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
        @HeaderComments("The sound played when the player click the buttons in GUI.")
        ConfiguredSound CLICK_SOUND = ConfiguredSound.of(Sound.UI_BUTTON_CLICK);

        Class<?> LIST = ResidenceListUI.CONFIG.class;
        Class<?> INFO = ResidenceInfoUI.CONFIG.class;
        Class<?> MANAGE = ResidenceManageUI.CONFIG.class;
        Class<?> ADMIN = ResidenceAdminUI.CONFIG.class;
        Class<?> SELECT = SelectIconGUI.CONFIG.class;

    }

}
