package com.artformgames.plugin.residencelist.ui;

import cc.carm.lib.configuration.core.Configuration;
import cc.carm.lib.easyplugin.gui.GUI;
import cc.carm.lib.easyplugin.gui.GUIItem;
import cc.carm.lib.easyplugin.gui.GUIType;
import cc.carm.lib.easyplugin.gui.paged.AutoPagedGUI;
import cc.carm.lib.mineconfiguration.bukkit.value.ConfiguredMessage;
import cc.carm.lib.mineconfiguration.bukkit.value.ConfiguredMessageList;
import cc.carm.lib.mineconfiguration.bukkit.value.item.ConfiguredItem;
import cc.carm.lib.mineconfiguration.bukkit.value.item.PreparedItem;
import com.artformgames.plugin.residencelist.Main;
import com.artformgames.plugin.residencelist.ResidenceListAPI;
import com.artformgames.plugin.residencelist.api.residence.ResidenceData;
import com.artformgames.plugin.residencelist.api.residence.ResidenceRate;
import com.artformgames.plugin.residencelist.api.user.UserListData;
import com.artformgames.plugin.residencelist.conf.PluginConfig;
import com.artformgames.plugin.residencelist.conf.PluginMessages;
import com.artformgames.plugin.residencelist.listener.EditHandler;
import com.artformgames.plugin.residencelist.utils.GUIUtils;
import com.artformgames.plugin.residencelist.utils.ResidenceUtils;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ResidenceMembersUI extends AutoPagedGUI {

    public static void open(@NotNull Player player, @NotNull ResidenceData data, @Nullable GUI previousGUI) {
        new ResidenceMembersUI(player, data, previousGUI).openGUI(player);
    }

    protected @NotNull Player viewer;
    protected @NotNull ResidenceData data;
    protected @Nullable GUI previousGUI;

    public ResidenceMembersUI(@NotNull Player viewer, @NotNull ResidenceData data, @Nullable GUI previousGUI) {
        super(GUIType.SIX_BY_NINE, Objects.requireNonNull(CONFIG.TITLE.parse(viewer, data.getDisplayName())), 28, 52);
        this.viewer = viewer;
        this.data = data;
        this.previousGUI = previousGUI;

        setPreviousPageSlot(36);
        setNextPageSlot(44);
        setPreviousPageUI(PluginConfig.ICON.PAGE.PREVIOUS_PAGE.get(viewer));
        setNextPageUI(PluginConfig.ICON.PAGE.NEXT_PAGE.get(viewer));
        setNoPreviousPageUI(PluginConfig.ICON.PAGE.NO_PREVIOUS_PAGE.get(viewer));
        setNoNextPageUI(PluginConfig.ICON.PAGE.NO_NEXT_PAGE.get(viewer));
        setEmptyItem(PluginConfig.ICON.EMPTY.get(viewer));

        initItems();
        loadIcon();
        loadMembers();
    }

    public @NotNull Player getViewer() {
        return viewer;
    }

    public UserListData getPlayerData() {
        return Main.getInstance().getUserManager().get(getViewer());
    }

    public @NotNull ResidenceData getData() {
        return data;
    }

    public void initItems() {

        if (this.previousGUI != null) {
            setItem(0, new GUIItem(CONFIG.ITEMS.BACK.get(viewer)) {
                @Override
                public void onClick(Player player, ClickType clickType) {
                    PluginConfig.GUI.CLICK_SOUND.playTo(player);
                    previousGUI.openGUI(player);
                }
            });
        }

        Location teleportLocation = getData().getTeleportLocation(getViewer());
        if (teleportLocation != null && getData().canTeleport(getViewer())) {
            setItem(13, new GUIItem(CONFIG.ITEMS.TELEPORT_TO.prepare(
                    getData().getResidence().getMainArea().getWorldName(),
                    teleportLocation.getBlockX(),
                    teleportLocation.getBlockY(),
                    teleportLocation.getBlockZ()
            ).get(getViewer())) {
                @Override
                public void onClick(Player clicker, ClickType type) {
                    data.getResidence().tpToResidence(clicker, clicker, clicker.hasPermission("residence.admin"));
                    PluginMessages.TELEPORT.SOUND.playTo(clicker);
                }
            });
        } else {
            setItem(13, new GUIItem(CONFIG.ITEMS.TELEPORT_DISABLED.prepare().get(getViewer())));
        }

        if (ResidenceUtils.isServerLand(getData().getResidence())) {
            setItem(14, new GUIItem(CONFIG.ITEMS.SERVER.prepare().get(getViewer())));
        } else {
            setItem(14, new GUIItem(CONFIG.ITEMS.OWNER.prepare(getData().getOwner())
                    .setSkullOwner(getData().getResidence().getOwnerUUID())
                    .get(getViewer())) {
                @Override
                public void onClick(Player clicker, ClickType type) {
                    ResidenceListUI.open(getViewer(), getData().getOwner());
                    PluginConfig.GUI.CLICK_SOUND.playTo(getViewer());
                }
            });
        }

        ResidenceRate rated = getData().getRates().get(getViewer().getUniqueId());
        ItemStack rateIcon;
        if (rated == null) {
            rateIcon = CONFIG.ITEMS.RATE.get(getViewer());
        } else {
            rateIcon = CONFIG.ITEMS.RATED.prepare(ResidenceListAPI.format(rated.time()))
                    .insertLore("comment", GUIUtils.sortContent(rated.content()))
                    .get(getViewer());
        }
        setItem(15, new GUIItem(rateIcon) {
            @Override
            public void onClick(Player clicker, ClickType type) {
                if (!(type.isLeftClick() || type.isRightClick())) return;
                clicker.closeInventory();
                boolean recommend = type.isLeftClick();
                PluginMessages.COMMENT.NOTIFY.send(clicker, getData().getDisplayName());
                PluginMessages.COMMENT.ASK_SOUND.playTo(clicker);
                EditHandler.start(clicker, (player, content) -> {
                    getData().modify(d -> d.addRate(content, recommend, getViewer().getUniqueId()));
                    open(player, getData(), previousGUI);
                    if (recommend) {
                        PluginMessages.COMMENT.YES_SOUND.playTo(player);
                    } else {
                        PluginMessages.COMMENT.NO_SOUND.playTo(player);
                    }
                });
            }
        });
    }

    public void loadMembers() {
        if (getData().getRates().isEmpty()) {
            setItem(40, new GUIItem(CONFIG.ITEMS.EMPTY.get(getViewer())));
            return;
        }

        ClaimedResidence residence = getData().getResidence();
        for (ResidencePlayer trustedPlayer : residence.getTrustedPlayers()) {
            trustedPlayer.getPlayer();
        }

    }

    public void loadIcon() {
        setItem(11, generateIcon(getPlayerData(), getData().getResidence()));
    }

    public interface CONFIG extends Configuration {

        ConfiguredMessage<String> TITLE = ConfiguredMessage.asString()
                .defaults("&a&lMembers &7#&f%(name)")
                .params("name").build();

    }
}
