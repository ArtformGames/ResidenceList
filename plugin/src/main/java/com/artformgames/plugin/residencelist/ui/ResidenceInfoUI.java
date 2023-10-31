package com.artformgames.plugin.residencelist.ui;

import cc.carm.lib.configuration.core.ConfigurationRoot;
import cc.carm.lib.easyplugin.gui.GUIItem;
import cc.carm.lib.easyplugin.gui.GUIType;
import cc.carm.lib.easyplugin.gui.paged.AutoPagedGUI;
import cc.carm.lib.mineconfiguration.bukkit.value.ConfiguredMessage;
import cc.carm.lib.mineconfiguration.bukkit.value.ConfiguredMessageList;
import cc.carm.lib.mineconfiguration.bukkit.value.item.ConfiguredItem;
import cc.carm.lib.mineconfiguration.bukkit.value.item.PreparedItem;
import com.artformgames.plugin.residencelist.Main;
import com.artformgames.plugin.residencelist.api.residence.ResidenceData;
import com.artformgames.plugin.residencelist.api.residence.ResidenceRate;
import com.artformgames.plugin.residencelist.api.user.UserListData;
import com.artformgames.plugin.residencelist.conf.PluginConfig;
import com.artformgames.plugin.residencelist.utils.GUIUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ResidenceInfoUI extends AutoPagedGUI {

    protected @NotNull Player viewer;
    protected @NotNull ResidenceData data;
    protected boolean editMode;

    public ResidenceInfoUI(@NotNull Player viewer, @NotNull ResidenceData data, boolean editMode) {
        super(GUIType.SIX_BY_NINE, ResidenceListUI.CONFIG.TITLE.parse(viewer), 10, 34);
        this.viewer = viewer;
        this.data = data;
        this.editMode = editMode;
    }

    public @NotNull Player getViewer() {
        return viewer;
    }

    public UserListData getPlayerData() {
        return Main.getInstance().getUserManager().get(getViewer());
    }

    public ResidenceData getData() {
        return data;
    }

    public void initItems() {
        setPreviousPageSlot(36);
        setNextPageSlot(44);
        setPreviousPageUI(PluginConfig.GUI.PAGE_ITEMS.PREVIOUS_PAGE.get(viewer));
        setNextPageUI(PluginConfig.GUI.PAGE_ITEMS.NEXT_PAGE.get(viewer));

        if (this.editMode || getData().getResidence().isOwner(getViewer())) {

        } else {


        }
    }

    public void loadComments() {
        if (getData().getRates().isEmpty()) {
            setItem(40, new GUIItem(CONFIG.ITEMS.EMPTY.get(getViewer())));
            return;
        }

        for (ResidenceRate value : getData().getRates().values()) {
            ConfiguredItem item = value.recommend() ? CONFIG.ITEMS.COMMENT_LIKE : CONFIG.ITEMS.COMMENT_DISLIKE;
            PreparedItem preparedItem = item.prepare(
                    Optional.ofNullable(value.getAuthorName()).orElse("?"),
                    PluginConfig.DATETIME_FORMATTER.format(value.time())
            );
            preparedItem.setSkullOwner(value.author());
            preparedItem.insertLore("comment", GUIUtils.sortContent(value.content(), 20));
            addItem(new GUIItem(preparedItem.get(getViewer())) {

            });
        }

    }

    public static final class CONFIG extends ConfigurationRoot {

        public static final ConfiguredMessage<String> TITLE = ConfiguredMessage.asString()
                .defaults("&a&lResidence list")
                .build();


        public static final class ITEMS extends ConfigurationRoot {

            public static final ConfiguredItem BACK = ConfiguredItem.create()
                    .defaultType(Material.REDSTONE_TORCH)
                    .defaultName("&cBack").build();

            public static final ConfiguredItem OWNER = ConfiguredItem.create()
                    .defaultType(Material.PLAYER_HEAD)
                    .defaultName("&7Residence owned by &f%(owner)")
                    .defaultLore(
                            "&7",
                            "&a ▶ Click &8|&f See his residences"
                    ).params("owner").build();

            public static final ConfiguredItem TELEPORT = ConfiguredItem.create()
                    .defaultType(Material.ENDER_EYE)
                    .defaultName("&dTeleport to residence")
                    .defaultLore(
                            "&7"
                    ).params("world", "x", "y", "z").build();

            public static final ConfiguredItem COMMENT = ConfiguredItem.create()
                    .defaultType(Material.WRITABLE_BOOK)
                    .defaultName("&eRate & Comment")
                    .defaultLore(
                            "&7"
                    ).build();

            public static final ConfiguredItem EMPTY = ConfiguredItem.create()
                    .defaultType(Material.BARRIER)
                    .defaultName("&7Empty")
                    .defaultLore(
                            "&7There are no comments yet."
                    ).build();

            public static final ConfiguredItem COMMENT_LIKE = ConfiguredItem.create()
                    .defaultType(Material.PLAYER_HEAD)
                    .defaultName("&7From &f%(owner)")
                    .defaultLore(
                            "{  &f&o}#comment#{1}",
                            "&7",
                            "&7Commented at %(date),",
                            "&7This user &arecommend &7this residence.",
                            "#click-lore#{1}"
                    ).params("owner", "date").build();

            public static final ConfiguredItem COMMENT_DISLIKE = ConfiguredItem.create()
                    .defaultType(Material.PLAYER_HEAD)
                    .defaultName("&7From &f%(owner)")
                    .defaultLore(
                            "{  &f&o}#comment#{1}",
                            "&7",
                            "&7Commented at %(date)",
                            "&7This user &cnot recommend &7this residence.",
                            "#click-lore#{1}"
                    ).params("owner", "date").build();

        }

        public static final class ADDITIONAL_LORE extends ConfigurationRoot {

            public static final ConfiguredMessageList<String> CLICK = ConfiguredMessageList.asStrings().defaults(
                    "&a ▶ Click &8|&f View information",
                    "&a ▶ Drop  &8|&f Pin/Unpin residence"
            ).build();

            public static final ConfiguredMessageList<String> TELEPORTABLE = ConfiguredMessageList.asStrings().defaults(
                    "&a ▶ LClick &8|&f View information",
                    "&a ▶ RClick &8|&f Teleport to residence",
                    "&a ▶  Drop  &8|&f Pin/Unpin residence"
            ).build();

        }

    }
}
