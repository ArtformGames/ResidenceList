package com.artformgames.plugin.residencelist.ui;

import cc.carm.lib.configuration.core.ConfigurationRoot;
import cc.carm.lib.easyplugin.gui.GUI;
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
import com.artformgames.plugin.residencelist.conf.PluginMessages;
import com.artformgames.plugin.residencelist.listener.EditHandler;
import com.artformgames.plugin.residencelist.utils.GUIUtils;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public class ResidenceManageUI extends AutoPagedGUI {

    public static void open(@NotNull Player player, @NotNull ResidenceData data, @Nullable GUI previousGUI) {
        new ResidenceManageUI(player, data, previousGUI).openGUI(player);
    }

    protected @NotNull Player viewer;
    protected @NotNull ResidenceData data;
    protected @Nullable GUI previousGUI;

    public ResidenceManageUI(@NotNull Player viewer, @NotNull ResidenceData data, @Nullable GUI previousGUI) {
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
        loadStatus();
        loadRates();
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

    public void loadIcon() {
        setItem(11, generateIcon(getPlayerData(), getData().getResidence()));
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
        Location teleportLocation = getData().getTeleportLocation(getViewer(), getViewer().getLocation());
        setItem(13, new GUIItem(CONFIG.ITEMS.TELEPORT.prepare(
                getData().getResidence().getMainArea().getWorldName(),
                teleportLocation.getBlockX(), teleportLocation.getBlockY(), teleportLocation.getBlockZ()
        ).get(getViewer())) {
            @Override
            public void onClick(Player clicker, ClickType type) {
                if (type.isLeftClick()) {
                    clicker.teleport(teleportLocation);
                    PluginMessages.TELEPORT.SOUND.playTo(clicker);
                } else if (type.isRightClick()) {
                    getData().getResidence().setTpLoc(clicker, clicker.hasPermission("residence.admin"));
                    PluginConfig.GUI.CLICK_SOUND.playTo(clicker);
                }
            }
        });

        setItem(14, new GUIItem(CONFIG.ITEMS.INFORMATION.get(getViewer())) {
            @Override
            public void onClick(Player clicker, ClickType type) {
                if (type.isLeftClick()) {
                    clicker.closeInventory();
                    PluginMessages.EDIT.EDIT_SOUND.playTo(getViewer());
                    PluginMessages.EDIT.NAME.send(getViewer(), getData().getDisplayName());
                    EditHandler.start(getViewer(), (player, content) -> {
                        setItem(11, generateIcon(getPlayerData(), getData().getResidence()));
                        if (content.length() > 16) {
                            PluginMessages.EDIT.NAME_TOO_LONG.send(player);
                            PluginMessages.EDIT.FAILED_SOUND.playTo(player);
                            return;
                        }
                        getData().modify(d -> d.setNickname(content));
                        PluginMessages.EDIT.NAME_UPDATED.send(player, getData().getDisplayName());
                        PluginMessages.EDIT.SUCCESS_SOUND.playTo(player);
                        loadIcon();
                        openGUI(player);
                    });
                } else if (type.isRightClick()) {
                    clicker.closeInventory();
                    PluginMessages.EDIT.EDIT_SOUND.playTo(getViewer());
                    PluginMessages.EDIT.DESCRIPTION.send(getViewer(), getData().getDisplayName());
                    EditHandler.start(getViewer(), (player, content) -> {
                        getData().modify(d -> d.setDescription(content.split("\\\\n")));
                        PluginMessages.EDIT.DESCRIPTION_UPDATED.send(player, getData().getDisplayName());
                        PluginMessages.EDIT.SUCCESS_SOUND.playTo(player);
                        loadIcon();
                        openGUI(player);
                    });
                } else if (type == ClickType.MIDDLE) {
                    clicker.closeInventory();
                    PluginMessages.EDIT.EDIT_SOUND.playTo(getViewer());
                    SelectIconGUI.open(clicker, ((player, itemStack) -> {
                        getData().modify(d -> d.setIcon(itemStack.getType()));
                        PluginMessages.EDIT.ICON_UPDATED.send(player, getData().getDisplayName());
                        PluginMessages.EDIT.SUCCESS_SOUND.playTo(player);
                        loadIcon();
                        openGUI(player);
                    }));
                }
            }
        });
    }

    public void loadStatus() {
        if (getData().isPublicDisplayed()) {
            setItem(15, new GUIItem(CONFIG.ITEMS.PUBLIC.get(getViewer())) {
                @Override
                public void onClick(Player clicker, ClickType type) {
                    getData().modify(d -> d.setPublicDisplayed(false));
                    PluginMessages.EDIT.SUCCESS_SOUND.playTo(clicker);
                    loadStatus();
                    updateView();
                }
            });
        } else {
            setItem(15, new GUIItem(CONFIG.ITEMS.PRIVATE.get(getViewer())) {
                @Override
                public void onClick(Player clicker, ClickType type) {
                    getData().modify(d -> d.setPublicDisplayed(true));
                    PluginMessages.EDIT.SUCCESS_SOUND.playTo(clicker);
                    loadStatus();
                    updateView();
                }
            });
        }
    }

    public void loadRates() {
        if (getData().getRates().isEmpty()) {
            setItem(40, new GUIItem(CONFIG.ITEMS.EMPTY.get(getViewer())));
            return;
        }

        for (ResidenceRate value : getData().getRates().values()) {
            ConfiguredItem item = value.recommend() ? PluginConfig.ICON.RATE.LIKE : PluginConfig.ICON.RATE.DISLIKE;
            PreparedItem preparedItem = item.prepare(
                    Optional.ofNullable(value.getAuthorName()).orElse("?"),
                    PluginConfig.DATETIME_FORMATTER.format(value.time())
            );
            preparedItem.setSkullOwner(value.author());
            preparedItem.insertLore("comment", GUIUtils.sortContent(value.content(), 25));
            if (getViewer().hasPermission("residence.admin")) {
                preparedItem.insertLore("click-lore", CONFIG.ADDITIONAL_LORE.REMOVE);
            }
            addItem(new GUIItem(preparedItem.get(getViewer())) {
                @Override
                public void onClick(Player clicker, ClickType type) {
                    if (getViewer().hasPermission("residence.admin")) {
                        getData().removeRate(value.author());
                        PluginMessages.EDIT.SUCCESS_SOUND.playTo(clicker);
                        open(getViewer(), data, previousGUI);
                    }
                }
            });
        }
    }

    protected GUIItem generateIcon(UserListData userData, ClaimedResidence residence) {
        ResidenceData residenceData = Main.getInstance().getResidenceManager().getData(residence);
        PreparedItem icon = PluginConfig.ICON.INFO.prepare(
                data.getDisplayName(), data.getOwner(),
                residence.getTrustedPlayers().size() + 1, residence.getMainArea().getSize(),
                data.countRate(ResidenceRate::recommend), data.countRate(r -> !r.recommend())
        );
        icon.insertLore("click-lore", CONFIG.ADDITIONAL_LORE.CLICK);
        if (!getData().getDescription().isEmpty()) icon.insertLore("description", getData().getDescription());
        if (userData.isPinned(residence.getName())) icon.glow();
        if (getData().getIcon() != null) icon.handleItem((i, p) -> i.setType(getData().getIcon()));
        return new GUIItem(icon.get(viewer)) {
            @Override
            public void onClick(Player clicker, ClickType type) {
                if (type.isLeftClick()) {      // Pin/Unpin
                    if (userData.isPinned(residence.getName())) {
                        userData.removePin(residence.getName());
                        PluginMessages.UNPIN.SOUND.playTo(clicker);
                        PluginMessages.UNPIN.MESSAGE.send(clicker, residenceData.getDisplayName());
                    } else {
                        userData.setPin(residence.getName(), 0);
                        PluginMessages.PIN.SOUND.playTo(clicker);
                        PluginMessages.PIN.MESSAGE.send(clicker, residenceData.getDisplayName());
                    }
                    loadIcon();
                    updateView();
                }
            }
        };
    }

    public static final class CONFIG extends ConfigurationRoot {

        public static final ConfiguredMessage<String> TITLE = ConfiguredMessage.asString()
                .defaults("&a&lDetails &7#&f%(name)")
                .params("name").build();

        public static final class ITEMS extends ConfigurationRoot {

            public static final ConfiguredItem BACK = ConfiguredItem.create()
                    .defaultType(Material.REDSTONE_TORCH)
                    .defaultName("&cBack").build();

            public static final ConfiguredItem TELEPORT = ConfiguredItem.create()
                    .defaultType(Material.ENDER_EYE)
                    .defaultName("&d&lTeleport")
                    .defaultLore(
                            "&7",
                            "&7Residence location:",
                            "&f%(world)&7@&f%(x)&7,&f%(y),&f%(z)",
                            "",
                            "&a ▶ LClick &8|&f Teleport to residence.",
                            "&a ▶ RClick &8|&f Set current location for teleportation."
                    ).params("world", "x", "y", "z").build();

            public static final ConfiguredItem INFORMATION = ConfiguredItem.create()
                    .defaultType(Material.OAK_SIGN)
                    .defaultName("&e&lEdit information")
                    .defaultLore(
                            "",
                            "&a ▶ LClick &8|&f Set residence's nickname.",
                            "&a ▶ RClick &8|&f Set residence's description.",
                            "&a ▶ Middle &8|&f Edit residence's icon"
                    ).build();

            public static final ConfiguredItem PUBLIC = ConfiguredItem.create()
                    .defaultType(Material.LIME_DYE)
                    .defaultName("&7Current: &a&lPublic")
                    .defaultLore(
                            " ",
                            "&7Now all players can see this residence in list.",
                            " ",
                            "&a ▶ Click &8|&f Change to &c&lPrivate"
                    ).build();

            public static final ConfiguredItem PRIVATE = ConfiguredItem.create()
                    .defaultType(Material.GRAY_DYE)
                    .defaultName("&7Current: &c&lPrivate")
                    .defaultLore(
                            " ",
                            "&7Now only you can see this residence in list.",
                            "&7Others can't see it.",
                            " ",
                            "&a ▶ Click &8|&f Change to &a&lPublic"
                    ).build();


            public static final ConfiguredItem EMPTY = ConfiguredItem.create()
                    .defaultType(Material.BARRIER)
                    .defaultName("&7Empty")
                    .defaultLore(
                            "&7There are no comments yet."
                    ).build();

        }

        public static final class ADDITIONAL_LORE extends ConfigurationRoot {

            public static final ConfiguredMessageList<String> CLICK = ConfiguredMessageList.asStrings().defaults(
                    "&a ▶ LClick &8|&f Pin/Unpin residence",
                    "&a ▶ RClick &8|&f Set residence's nickname",
                    "&a ▶ Middle &8|&f Edit residence's icon"
            ).build();

            public static final ConfiguredMessageList<String> REMOVE = ConfiguredMessageList.asStrings().defaults(
                    "&a ▶ LClick &8|&f Delete this comment &c(ADMIN)"
            ).build();

        }

    }
}
