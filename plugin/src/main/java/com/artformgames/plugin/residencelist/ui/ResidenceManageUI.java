package com.artformgames.plugin.residencelist.ui;

import cc.carm.lib.configuration.Configuration;
import cc.carm.lib.easyplugin.gui.GUI;
import cc.carm.lib.easyplugin.gui.GUIItem;
import cc.carm.lib.easyplugin.gui.GUIType;
import cc.carm.lib.easyplugin.gui.paged.AutoPagedGUI;
import cc.carm.lib.mineconfiguration.bukkit.value.ConfiguredMessage;
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
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ResidenceManageUI extends AutoPagedGUI {

    public static void open(@NotNull Player player, @NotNull ResidenceData data, @Nullable GUI previousGUI) {
        UserListData user = ResidenceListAPI.getUserManager().getNullable(player.getUniqueId());
        if (user == null) {
            PluginMessages.LOAD_FAILED.sendTo(player);
            return;
        }
        new ResidenceManageUI(player, user, data, previousGUI).openGUI(player);
    }

    protected @NotNull Player viewer;
    protected final @NotNull UserListData userData;
    protected final @NotNull ResidenceData residenceData;
    protected @Nullable GUI previousGUI;

    public ResidenceManageUI(@NotNull Player viewer,
                             @NotNull UserListData userData, @NotNull ResidenceData residenceData,
                             @Nullable GUI previousGUI) {
        super(GUIType.SIX_BY_NINE, CONFIG.TITLE.parseLine(viewer, residenceData.getDisplayName()), 28, 52);
        this.viewer = viewer;
        this.userData = userData;
        this.residenceData = residenceData;
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
        return userData;
    }

    public @NotNull ResidenceData getResidenceData() {
        return residenceData;
    }

    public void loadIcon() {
        setItem(11, generateIcon(getPlayerData(), getResidenceData().getResidence()));
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
        Location teleportLocation = getResidenceData().getTeleportLocation(getViewer(), getViewer().getLocation());
        setItem(13, new GUIItem(CONFIG.ITEMS.TELEPORT.prepare(
                getResidenceData().getResidence().getMainArea().getWorldName(),
                teleportLocation.getBlockX(), teleportLocation.getBlockY(), teleportLocation.getBlockZ()
        ).get(getViewer())) {
            @Override
            public void onClick(Player clicker, ClickType type) {
                if (type.isLeftClick()) {
                    getResidenceData().getResidence().tpToResidence(clicker, clicker, clicker.hasPermission("residence.admin"));
                    PluginMessages.TELEPORT.SOUND.playTo(clicker);
                } else if (type.isRightClick()) {
                    getResidenceData().getResidence().setTpLoc(clicker, clicker.hasPermission("residence.admin"));
                    PluginConfig.GUI.CLICK_SOUND.playTo(clicker);
                }
            }
        });

        setItem(14, new GUIItem(CONFIG.ITEMS.INFORMATION.get(getViewer())) {
            @Override
            public void onClick(Player clicker, ClickType type) {
                if (type.isShiftClick()) {
                    clicker.closeInventory();
                    PluginMessages.EDIT.EDIT_SOUND.playTo(getViewer());
                    SelectIconGUI.open(clicker, ((player, itemStack) -> {
                        Material material = itemStack.getType();
                        if (PluginConfig.SETTINGS.BLOCKED_ICON_TYPES.contains(material)) {
                            PluginMessages.EDIT.ICON_BLOCKED.sendTo(player, getResidenceData().getDisplayName());
                            PluginMessages.EDIT.FAILED_SOUND.playTo(player);
                        } else {
                            getResidenceData().modify(d -> d.setIconMaterial(itemStack));
                            PluginMessages.EDIT.ICON_UPDATED.sendTo(player, getResidenceData().getDisplayName());
                            PluginMessages.EDIT.SUCCESS_SOUND.playTo(player);
                        }

                        loadIcon();
                        openGUI(player);
                    }));
                } else if (type.isLeftClick()) {
                    clicker.closeInventory();
                    PluginMessages.EDIT.EDIT_SOUND.playTo(getViewer());
                    PluginMessages.EDIT.NAME.sendTo(getViewer(), getResidenceData().getDisplayName());
                    EditHandler.start(getViewer(), (player, content) -> {
                        setItem(11, generateIcon(getPlayerData(), getResidenceData().getResidence()));
                        if (content.length() > 16) {
                            PluginMessages.EDIT.NAME_TOO_LONG.sendTo(player);
                            PluginMessages.EDIT.FAILED_SOUND.playTo(player);
                            return;
                        }
                        getResidenceData().modify(d -> d.setNickname(content));
                        PluginMessages.EDIT.NAME_UPDATED.sendTo(player, getResidenceData().getDisplayName());
                        PluginMessages.EDIT.SUCCESS_SOUND.playTo(player);
                        loadIcon();
                        openGUI(player);
                    });
                } else if (type.isRightClick()) {
                    clicker.closeInventory();
                    PluginMessages.EDIT.EDIT_SOUND.playTo(getViewer());
                    PluginMessages.EDIT.DESCRIPTION.sendTo(getViewer(), getResidenceData().getDisplayName());
                    EditHandler.start(getViewer(), (player, content) -> {
                        getResidenceData().modify(d -> d.setDescription(content.split("\\\\n")));
                        PluginMessages.EDIT.DESCRIPTION_UPDATED.sendTo(player, getResidenceData().getDisplayName());
                        PluginMessages.EDIT.SUCCESS_SOUND.playTo(player);
                        loadIcon();
                        openGUI(player);
                    });
                }
            }
        });
    }

    public void loadStatus() {
        if (getResidenceData().isPublicDisplayed()) {
            setItem(15, new GUIItem(CONFIG.ITEMS.PUBLIC.get(getViewer())) {
                @Override
                public void onClick(Player clicker, ClickType type) {
                    getResidenceData().modify(d -> d.setPublicDisplayed(false));
                    PluginMessages.EDIT.SUCCESS_SOUND.playTo(clicker);
                    loadStatus();
                    updateView();
                }
            });
        } else {
            setItem(15, new GUIItem(CONFIG.ITEMS.PRIVATE.get(getViewer())) {
                @Override
                public void onClick(Player clicker, ClickType type) {
                    getResidenceData().modify(d -> d.setPublicDisplayed(true));
                    PluginMessages.EDIT.SUCCESS_SOUND.playTo(clicker);
                    loadStatus();
                    updateView();
                }
            });
        }
    }

    public void loadRates() {
        if (getResidenceData().getRates().isEmpty()) {
            setItem(40, new GUIItem(CONFIG.ITEMS.EMPTY.get(getViewer())));
            return;
        }

        for (ResidenceRate value : getResidenceData().getRates().values()) {
            ConfiguredItem item = value.recommend() ? PluginConfig.ICON.RATE.LIKE : PluginConfig.ICON.RATE.DISLIKE;
            PreparedItem preparedItem = item.prepare(
                    Optional.ofNullable(value.getAuthorName()).orElse("?"),
                    PluginConfig.DATETIME_FORMATTER.format(value.time())
            );
            preparedItem.setSkullOwner(value.author());
            preparedItem.insert("comment", GUIUtils.sortContent(value.content()));
            if (getViewer().hasPermission("residence.admin")) {
                preparedItem.insert("click-lore", CONFIG.ADDITIONAL_LORE.REMOVE);
            }
            addItem(new GUIItem(preparedItem.get(getViewer())) {
                @Override
                public void onClick(Player clicker, ClickType type) {
                    if (getViewer().hasPermission("residence.admin")) {
                        getResidenceData().removeRate(value.author());
                        PluginMessages.EDIT.SUCCESS_SOUND.playTo(clicker);
                        open(getViewer(), residenceData, previousGUI);
                    }
                }
            });
        }
    }

    protected GUIItem generateIcon(UserListData userData, ClaimedResidence residence) {
        ResidenceData residenceData = Main.getInstance().getResidenceManager().getResidence(residence);
        PreparedItem icon = PluginConfig.ICON.INFO.prepare(
                this.residenceData.getDisplayName(), this.residenceData.getOwner(),
                residence.getTrustedPlayers().size() + 1, residence.getMainArea().getSize(),
                this.residenceData.countRate(ResidenceRate::recommend), this.residenceData.countRate(r -> !r.recommend())
        );
        icon.insert("click-lore", CONFIG.ADDITIONAL_LORE.CLICK);
        if (!getResidenceData().getDescription().isEmpty())
            icon.insert("description", getResidenceData().getDescription());
        if (userData.isPinned(residence.getName())) icon.glow();
        if (this.residenceData.getIconMaterial() != null) {
            icon.handleItem((i, p) -> i.setType(this.residenceData.getIconMaterial()));
            if (this.residenceData.getCustomModelData() > 0) {
                icon.handleMeta((itemMeta, player) -> itemMeta.setCustomModelData(this.residenceData.getCustomModelData()));
            }
        }
        return new GUIItem(icon.get(viewer)) {
            @Override
            public void onClick(Player clicker, ClickType type) {
                if (type.isLeftClick()) {      // Pin/Unpin
                    if (userData.isPinned(residence.getName())) {
                        userData.removePin(residence.getName());
                        PluginMessages.UNPIN.SOUND.playTo(clicker);
                        PluginMessages.UNPIN.MESSAGE.sendTo(clicker, residenceData.getDisplayName());
                    } else {
                        userData.setPin(residence.getName(), 0);
                        PluginMessages.PIN.SOUND.playTo(clicker);
                        PluginMessages.PIN.MESSAGE.sendTo(clicker, residenceData.getDisplayName());
                    }
                    loadIcon();
                    updateView();
                }
            }
        };
    }

    public interface CONFIG extends Configuration {

        ConfiguredMessage<String> TITLE = ConfiguredMessage.asString()
                .defaults("&a&lDetails &7#&f%(name)")
                .params("name").build();

        interface ITEMS extends Configuration {

            ConfiguredItem BACK = ConfiguredItem.create()
                    .defaultType(Material.REDSTONE_TORCH)
                    .defaultName("&cBack").build();

            ConfiguredItem TELEPORT = ConfiguredItem.create()
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

            ConfiguredItem INFORMATION = ConfiguredItem.create()
                    .defaultType(Material.OAK_SIGN)
                    .defaultName("&e&lEdit information")
                    .defaultLore(
                            "",
                            "&a ▶ LClick &8|&f Set residence's nickname.",
                            "&a ▶ RClick &8|&f Set residence's description.",
                            "&a ▶ Shift+Click &8|&f Edit residence's icon"
                    ).build();

            ConfiguredItem PUBLIC = ConfiguredItem.create()
                    .defaultType(Material.LIME_DYE)
                    .defaultName("&7Current: &a&lPublic")
                    .defaultLore(
                            " ",
                            "&7Now all players can see this residence in list.",
                            " ",
                            "&a ▶ Click &8|&f Change to &c&lPrivate"
                    ).build();

            ConfiguredItem PRIVATE = ConfiguredItem.create()
                    .defaultType(Material.GRAY_DYE)
                    .defaultName("&7Current: &c&lPrivate")
                    .defaultLore(
                            " ",
                            "&7Now only you can see this residence in list.",
                            "&7Others can't see it.",
                            " ",
                            "&a ▶ Click &8|&f Change to &a&lPublic"
                    ).build();


            ConfiguredItem EMPTY = ConfiguredItem.create()
                    .defaultType(Material.BARRIER)
                    .defaultName("&7Empty")
                    .defaultLore(
                            "&7There are no comments yet."
                    ).build();

        }

        interface ADDITIONAL_LORE extends Configuration {

            ConfiguredMessage<String> CLICK = ConfiguredMessage.asString().defaults(
                    "&a ▶ LClick &8|&f Pin/Unpin residence"
            ).build();

            ConfiguredMessage<String> REMOVE = ConfiguredMessage.asString().defaults(
                    "&a ▶ LClick &8|&f Delete this comment &c(ADMIN)"
            ).build();

        }

    }
}
