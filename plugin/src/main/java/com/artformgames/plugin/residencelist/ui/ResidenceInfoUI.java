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
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public class ResidenceInfoUI extends AutoPagedGUI {

    public static void open(@NotNull Player player, @NotNull ResidenceData data, @Nullable GUI previousGUI) {
        new ResidenceInfoUI(player, data, previousGUI).openGUI(player);
    }

    protected @NotNull Player viewer;
    protected @NotNull ResidenceData data;
    protected @Nullable GUI previousGUI;

    public ResidenceInfoUI(@NotNull Player viewer, @NotNull ResidenceData data, @Nullable GUI previousGUI) {
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

        setItem(14, new GUIItem(CONFIG.ITEMS.OWNER.prepare(getData().getOwner())
                .setSkullOwner(getData().getResidence().getOwnerUUID())
                .get(getViewer())) {
            @Override
            public void onClick(Player clicker, ClickType type) {
                ResidenceListUI.open(getViewer(), getData().getOwner());
                PluginConfig.GUI.CLICK_SOUND.playTo(getViewer());
            }
        });


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
            preparedItem.insertLore("comment", GUIUtils.sortContent(value.content()));
            addItem(new GUIItem(preparedItem.get(getViewer())) {

            });
        }
    }

    public void loadIcon() {
        setItem(11, generateIcon(getPlayerData(), getData().getResidence()));
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
        if (getData().getIconMaterial() != null) icon.handleItem((i, p) -> i.setType(getData().getIconMaterial()));
        return new GUIItem(icon.get(viewer)) {
            @Override
            public void onClick(Player clicker, ClickType type) {
                if (!type.isLeftClick()) return;
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

            ConfiguredMessageList<String> CLICK = ConfiguredMessageList.asStrings().defaults(
                    "&a ▶ Click &8|&f Pin/Unpin residence"
            ).build();

        }

    }
}
