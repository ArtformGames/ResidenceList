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

import java.util.Optional;

public class ResidenceInfoUI extends AutoPagedGUI {

    public static void open(@NotNull Player player, @NotNull ResidenceData data, @Nullable GUI previousGUI) {
        new ResidenceInfoUI(player, data, previousGUI).openGUI(player);
    }

    protected @NotNull Player viewer;
    protected @NotNull ResidenceData data;
    protected @Nullable GUI previousGUI;

    public ResidenceInfoUI(@NotNull Player viewer, @NotNull ResidenceData data, @Nullable GUI previousGUI) {
        super(GUIType.SIX_BY_NINE, CONFIG.TITLE.parseLine(viewer, data.getDisplayName()), 28, 52);
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
        displayRates(); // By default, show rates.
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
                    .insert("comment", GUIUtils.sortContent(rated.content()))
                    .get(getViewer());
        }
        setItem(15, new GUIItem(rateIcon) {
            @Override
            public void onClick(Player clicker, ClickType type) {
                if (!(type.isLeftClick() || type.isRightClick())) return;
                clicker.closeInventory();
                boolean recommend = type.isLeftClick();
                PluginMessages.COMMENT.NOTIFY.sendTo(clicker, getData().getDisplayName());
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

    public void displayRates() {
        this.container.clear();
        this.page = 1;

        setItem(16, new GUIItem(CONFIG.ITEMS.MEMBERS
                .prepare(getData().getResidence().getTrustedPlayers().size())
                .get(viewer)) {
            @Override
            public void onClick(Player clicker, ClickType type) {
                displayMembers();
                updateView();
                PluginConfig.GUI.CLICK_SOUND.playTo(getViewer());
            }
        });

        if (getData().getRates().isEmpty()) {
            goFirstPage();
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
            preparedItem.insert("comment", GUIUtils.sortContent(value.content()));
            addItem(new GUIItem(preparedItem.get(getViewer())) {

            });
        }
        goFirstPage();
    }

    public void displayMembers() {
        this.container.clear();

        setItem(16, new GUIItem(CONFIG.ITEMS.RATES
                .prepare(getData().getRates().size())
                .get(viewer)) {
            @Override
            public void onClick(Player clicker, ClickType type) {
                displayRates();
                updateView();
                PluginConfig.GUI.CLICK_SOUND.playTo(getViewer());
            }
        });

        ClaimedResidence residence = getData().getResidence();
        if (!ResidenceUtils.isServerLand(getData().getResidence())) {
            addItem(new GUIItem(CONFIG.ITEMS.OWNER.prepare(getData().getOwner())
                    .setSkullOwner(getData().getResidence().getOwnerUUID())
                    .get(getViewer())) {
                @Override
                public void onClick(Player clicker, ClickType type) {
                    ResidenceListUI.open(getViewer(), getData().getOwner());
                    PluginConfig.GUI.CLICK_SOUND.playTo(getViewer());
                }
            });
        }
        for (ResidencePlayer trustedPlayer : residence.getTrustedPlayers()) {
            addItem(new GUIItem(
                    CONFIG.ITEMS.PLAYER.prepare(trustedPlayer.getName(), trustedPlayer.getUniqueId())
                            .setSkullOwner(trustedPlayer.getName())
                            .get(viewer)
            ));
        }
        goFirstPage();
    }

    public void loadIcon() {
        setItem(11, generateIcon(getPlayerData(), getData().getResidence()));
    }

    protected GUIItem generateIcon(UserListData userData, ClaimedResidence residence) {
        ResidenceData residenceData = Main.getInstance().getResidenceManager().getResidence(residence);
        PreparedItem icon = PluginConfig.ICON.INFO.prepare(
                data.getDisplayName(), data.getOwner(),
                residence.getTrustedPlayers().size() + 1, residence.getMainArea().getSize(),
                data.countRate(ResidenceRate::recommend), data.countRate(r -> !r.recommend())
        );
        icon.insert("click-lore", CONFIG.ADDITIONAL_LORE.CLICK);
        if (!getData().getDescription().isEmpty()) icon.insert("description", getData().getDescription());
        if (userData.isPinned(residence.getName())) icon.glow();
        if (data.getIconMaterial() != null) {
            icon.handleItem((i, p) -> i.setType(data.getIconMaterial()));
            if (data.getCustomModelData() > 0) {
                icon.handleMeta((itemMeta, player) -> itemMeta.setCustomModelData(data.getCustomModelData()));
            }
        }
        return new GUIItem(icon.get(viewer)) {
            @Override
            public void onClick(Player clicker, ClickType type) {
                if (!type.isLeftClick()) return;
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

            ConfiguredItem OWNER = ConfiguredItem.create()
                    .defaultType(Material.PLAYER_HEAD)
                    .defaultName("&7Residence owned by &f%(owner)")
                    .defaultLore(
                            "&7",
                            "&a ▶ Click &8|&f See all his residences"
                    ).params("owner").build();

            ConfiguredItem SERVER = ConfiguredItem.create()
                    .defaultType(Material.CREEPER_HEAD)
                    .defaultName("&e&oServer Residence")
                    .defaultLore(
                            "&7"
                    ).build();

            ConfiguredItem TELEPORT_TO = ConfiguredItem.create()
                    .defaultType(Material.ENDER_EYE)
                    .defaultName("&dTeleport to residence")
                    .defaultLore(
                            "&7",
                            "&7Residence location:",
                            "&f%(world)&7@&f%(x)&7,&f%(y),&f%(z)",
                            "",
                            "&a ▶ Click &8|&f Teleport to residence."
                    ).params("world", "x", "y", "z").build();


            ConfiguredItem TELEPORT_DISABLED = ConfiguredItem.create()
                    .defaultType(Material.ENDER_EYE)
                    .defaultName("&d&mTeleport")
                    .defaultLore(
                            "&7",
                            "&cThis residence cannot be teleported to.",
                            ""
                    ).build();

            ConfiguredItem PLAYER = ConfiguredItem.create()
                    .defaultType(Material.PLAYER_HEAD)
                    .defaultName("&a%(name)")
                    .defaultLore(
                            "&7"
                    ).params("name", "uuid").build();


            ConfiguredItem MEMBERS = ConfiguredItem.create()
                    .defaultType(Material.FURNACE)
                    .defaultName("&eMembers")
                    .defaultLore(
                            "&7",
                            "&7This residence has &f%(members) &7members.",
                            "&7",
                            "&a ▶ Click &8|&f See all members."
                    ).params("members").build();

            ConfiguredItem RATES = ConfiguredItem.create()
                    .defaultType(Material.COPPER_BLOCK)
                    .defaultName("&eRates")
                    .defaultLore(
                            "&7",
                            "&7This residence has &f%(size) &7rates.",
                            "&7",
                            "&a ▶ Click &8|&f See all rates."
                    ).params("size").build();

            ConfiguredItem RATE = ConfiguredItem.create()
                    .defaultType(Material.WRITABLE_BOOK)
                    .defaultName("&eRate && Comment")
                    .defaultLore(
                            "&7",
                            "&7You can rate and comment on this residence.",
                            "&7",
                            "&a ▶ LClick &8|&f Like && comment.",
                            "&a ▶ RClick &8|&f Dislike && comment."
                    ).build();

            ConfiguredItem RATED = ConfiguredItem.create()
                    .defaultType(Material.WRITTEN_BOOK)
                    .defaultName("&eRate && Comment")
                    .defaultLore(
                            "&7",
                            "&7You have already rated and commented:",
                            "{&7- &f&o}#comment#",
                            "&7Rate at &f%(date)",
                            " ",
                            "&7You can still update your comment.",
                            "&7",
                            "&a ▶ LClick &8|&f Like && comment.",
                            "&a ▶ RClick &8|&f Dislike && comment."
                    ).params("date").build();

            ConfiguredItem EMPTY = ConfiguredItem.create()
                    .defaultType(Material.BARRIER)
                    .defaultName("&7Empty")
                    .defaultLore(
                            "&7There are no comments yet."
                    ).build();

        }

        interface ADDITIONAL_LORE extends Configuration {

            ConfiguredMessage<String> CLICK = ConfiguredMessage.asString().defaults(
                    "&a ▶ Click &8|&f Pin/Unpin residence"
            ).build();

        }

    }
}
