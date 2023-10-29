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
import com.artformgames.plugin.residencelist.api.user.UserListData;
import com.artformgames.plugin.residencelist.conf.PluginConfig;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ResidenceListUI extends AutoPagedGUI {

    public static void open(@NotNull Player player, @Nullable String owner) {
        new ResidenceListUI(player, owner).openGUI(player);
    }

    protected @NotNull Player viewer;

    protected @Nullable String owner;

    public ResidenceListUI(@NotNull Player viewer, @Nullable String owner) {
        super(GUIType.SIX_BY_NINE, CONFIG.TITLE.parse(viewer), 10, 34);
        this.viewer = viewer;
        this.owner = owner;

        loadResidences();
    }

    public @NotNull Player getViewer() {
        return viewer;
    }

    public UserListData getPlayerData() {
        return Main.getInstance().getUserManager().get(getViewer());
    }

    public boolean shouldDisplay(ClaimedResidence residence) {
        return owner != null && residence.isOwner(owner);
    }

    public void initItems() {
        setPreviousPageSlot(48);
        setNextPageSlot(50);
        setPreviousPageUI(PluginConfig.GUI.PAGE_ITEMS.PREVIOUS_PAGE.get(viewer));
        setNextPageUI(PluginConfig.GUI.PAGE_ITEMS.NEXT_PAGE.get(viewer));

        if (this.owner != null) {
            setItem(49, new GUIItem(CONFIG.ITEMS.OWNED.prepare(owner).get(getViewer())) {
                @Override
                public void onClick(Player clicker, ClickType type) {
                    clicker.closeInventory();
                    open(clicker, null);
                }
            });
        } else {
            setItem(49, new GUIItem(CONFIG.ITEMS.ALL.get(getViewer())) {
                @Override
                public void onClick(Player clicker, ClickType type) {
                    clicker.closeInventory();
                    open(clicker, clicker.getName());
                }
            });
        }
    }

    public void loadResidences() {
        UserListData data = getPlayerData();
        List<ClaimedResidence> display = new ArrayList<>();

        data.getPinned().stream()
                .map(residenceName -> Residence.getInstance().getResidenceManager().getByName(residenceName))
                .filter(residence -> residence != null && shouldDisplay(residence))
                .forEach(display::add);

        Residence.getInstance().getResidenceManager().getResidences().values().stream()
                .filter(residence -> !display.contains(residence) && shouldDisplay(residence))
                .forEach(display::add);

        display.forEach(residence -> addItem(generateIcon(data, residence)));
    }

    protected GUIItem generateIcon(UserListData userData, ClaimedResidence residence) {
        ResidenceData data = Main.getInstance().getResidenceManager().getData(residence);
        PreparedItem icon = PluginConfig.INFORMATION.ICON.prepare(
                data.getDisplayName(), data.getOwner(),
                residence.getTrustedPlayers().size() + 1,
                residence.getAreaCount()
        );
        if (data.canTeleport(viewer)) {
            icon.insertLore("click-lore", CONFIG.ADDITIONAL_LORE.TELEPORTABLE);
        } else {
            icon.insertLore("click-lore", CONFIG.ADDITIONAL_LORE.CLICK);
        }
        if (userData.isPinned(residence.getName())) {
            icon.glow();
        }

        return new GUIItem(icon.get(viewer)) {
            @Override
            public void onClick(Player clicker, ClickType type) {
                if (type == ClickType.DROP || type == ClickType.CONTROL_DROP) {      // Pin/Unpin
                    if (userData.isPinned(residence.getName())) {
                        userData.removePin(residence.getName());
                    } else {
                        userData.setPin(residence.getName(), 0);
                    }
                    getViewer().closeInventory();
                    //TODO SEND MESSAGE
                } else if (type.isLeftClick()) { // View information
                    //TODO Information GUI
                } else if (type.isRightClick()) { // Teleport to residence (If allowed)
                    if (!data.canTeleport(viewer)) return;

                    Location target = data.getTeleportLocation(viewer);
                    if (target == null) {
                        //TODO SEND MESSAGE
                        return;
                    }
                    getViewer().teleport(target);
                }
            }
        };
    }


    public static final class CONFIG extends ConfigurationRoot {

        public static final ConfiguredMessage<String> TITLE = ConfiguredMessage.asString()
                .defaults("&a&lResidence list")
                .build();


        public static final class ITEMS extends ConfigurationRoot {

            public static final ConfiguredItem ALL = ConfiguredItem.create()
                    .defaultType(Material.CHEST)
                    .defaultName("&a&lAll residences")
                    .defaultLore(
                            "&7",
                            "&7Now all residences are displayed.",
                            "&7",
                            "&a ▶ Click &8|&f See only personal residences"
                    ).build();
            public static final ConfiguredItem OWNED = ConfiguredItem.create()
                    .defaultType(Material.PLAYER_HEAD)
                    .defaultName("&7Residence owned by &f%(owner)")
                    .defaultLore(
                            "&7",
                            "&a ▶ Click &8|&f See all residences"
                    ).params("owner").build();

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
