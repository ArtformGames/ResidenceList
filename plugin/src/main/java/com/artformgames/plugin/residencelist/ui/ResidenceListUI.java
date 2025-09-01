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
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ResidenceListUI extends AutoPagedGUI {

    public static void open(@NotNull Player player, @Nullable String owner) {
        UserListData data = ResidenceListAPI.getUserManager().getNullable(player.getUniqueId());
        if (data == null) {
            PluginMessages.LOAD_FAILED.sendTo(player);
            return;
        }
        new ResidenceListUI(player, data, owner).openGUI(player);
    }

    protected final @NotNull Player viewer;
    protected final @NotNull UserListData data;
    protected @Nullable String owner;

    public ResidenceListUI(@NotNull Player viewer, @NotNull UserListData data, @Nullable String owner) {
        super(GUIType.SIX_BY_NINE, "", 10, 34);
        this.viewer = viewer;
        this.data = data;
        this.owner = owner;

        setPreviousPageSlot(47);
        setNextPageSlot(51);
        setPreviousPageUI(PluginConfig.ICON.PAGE.PREVIOUS_PAGE.get(viewer));
        setNextPageUI(PluginConfig.ICON.PAGE.NEXT_PAGE.get(viewer));
        setNoPreviousPageUI(PluginConfig.ICON.PAGE.NO_PREVIOUS_PAGE.get(viewer));
        setNoNextPageUI(PluginConfig.ICON.PAGE.NO_NEXT_PAGE.get(viewer));
        setEmptyItem(PluginConfig.ICON.EMPTY.get(viewer));

        initItems();
        loadResidences();
        this.title = CONFIG.TITLE.parseLine(viewer, 1, getLastPageNumber());
    }

    public @NotNull Player getViewer() {
        return viewer;
    }

    public @NotNull UserListData getPlayerData() {
        return data;
    }

    public boolean checkOwner(ClaimedResidence residence) {
        return owner == null || residence.isOwner(owner);
    }

    @SuppressWarnings("deprecation")
    public void initItems() {

        if (this.owner != null) {
            setItem(49, new GUIItem(CONFIG.ITEMS.OWNED.prepare(owner).setSkullOwner(this.owner).get(getViewer())) {
                @Override
                public void onClick(Player clicker, ClickType type) {
                    clicker.closeInventory();
                    open(clicker, null);
                    PluginConfig.GUI.CLICK_SOUND.playTo(getViewer());
                }
            });
        } else {
            setItem(49, new GUIItem(CONFIG.ITEMS.ALL.get(getViewer())) {
                @Override
                public void onClick(Player clicker, ClickType type) {
                    clicker.closeInventory();
                    open(clicker, clicker.getName());
                    PluginConfig.GUI.CLICK_SOUND.playTo(getViewer());
                }
            });
        }

        ConfiguredItem sortItem = switch (getPlayerData().getSortFunction()) {
            case NAME -> CONFIG.ITEMS.SORT_BY_NAME;
            case SIZE -> CONFIG.ITEMS.SORT_BY_SIZE;
            case RATINGS -> CONFIG.ITEMS.SORT_BY_RATINGS;
        };

        setItem(53, new GUIItem(sortItem.get(getViewer(), (getPlayerData().isSortReversed() ? "⬇" : "⬆"))) {
            @Override
            public void onClick(Player clicker, ClickType type) {
                if (type.isRightClick()) {
                    PluginConfig.GUI.CLICK_SOUND.playTo(getViewer());
                    getPlayerData().setSortReversed(!getPlayerData().isSortReversed());
                    open(clicker, owner);
                } else if (type.isLeftClick()) {
                    PluginConfig.GUI.CLICK_SOUND.playTo(getViewer());
                    getPlayerData().setSortFunction(getPlayerData().getSortFunction().next());
                    open(clicker, owner);
                }
            }
        });
    }

    @Override
    public void onPageChange(int pageNum) {
        PluginConfig.GUI.CLICK_SOUND.playTo(getViewer());
        updateTitle(CONFIG.TITLE.parseLine(viewer, pageNum, getLastPageNumber()));
    }

    public void loadResidences() {
        this.container.clear();
        UserListData data = getPlayerData();
        List<ClaimedResidence> display = new ArrayList<>();

        Comparator<ClaimedResidence> comparator = data.getSortFunction().residenceComparator(data.isSortReversed());

        data.getPinned().stream()
                .map(ResidenceListAPI::getResidence)
                .filter(residence -> residence != null && checkOwner(residence))
                .sorted(comparator).forEach(display::add);
        ResidenceListAPI.listResidences(this.viewer).stream()
                .filter(residence -> !display.contains(residence) && checkOwner(residence))
                .sorted(comparator).forEach(display::add);

        display.stream().filter(r -> {
            ResidenceData d = Main.getInstance().getResidenceManager().getResidence(r);
            return d.isPublicDisplayed() || (d.isOwner(getViewer()));
        }).forEach(residence -> addItem(generateIcon(getViewer(), this, owner, data, residence)));
    }

    protected static GUIItem generateIcon(Player viewer, GUI previous, String filterOwner,
                                          UserListData userData, ClaimedResidence residence) {
        ResidenceData data = Main.getInstance().getResidenceManager().getResidence(residence);
        PreparedItem icon = PluginConfig.ICON.INFO.prepare(
                data.getDisplayName(), data.getOwner(),
                residence.getTrustedPlayers().size() + 1, residence.getMainArea().getSize(),
                data.countRate(ResidenceRate::recommend), data.countRate(r -> !r.recommend())
        );
        if (data.canTeleport(viewer)) {
            icon.insert("click-lore", CONFIG.ADDITIONAL_LORE.TELEPORTABLE);
        } else {
            icon.insert("click-lore", CONFIG.ADDITIONAL_LORE.NORMAL);
        }
        if (!data.getDescription().isEmpty()) icon.insert("description", data.getDescription());
        if (userData.isPinned(residence.getName())) {
            icon.glow();
        }
        if (data.getIconMaterial() != null) {
            icon.handleItem((i, p) -> i.setType(data.getIconMaterial()));
            if (data.getCustomModelData() > 0) {
                icon.handleMeta((itemMeta, player) -> itemMeta.setCustomModelData(data.getCustomModelData()));
            }
        }
        return new GUIItem(icon.get(viewer)) {
            @Override
            public void onClick(Player clicker, ClickType type) {
                if (type == ClickType.DROP || type == ClickType.CONTROL_DROP) {      // Pin/Unpin
                    if (userData.isPinned(residence.getName())) {
                        userData.removePin(residence.getName());
                        PluginMessages.UNPIN.SOUND.playTo(clicker);
                        PluginMessages.UNPIN.MESSAGE.sendTo(clicker, data.getDisplayName());
                    } else {
                        userData.setPin(residence.getName(), 0);
                        PluginMessages.PIN.SOUND.playTo(clicker);
                        PluginMessages.PIN.MESSAGE.sendTo(clicker, data.getDisplayName());
                    }
                    open(viewer, filterOwner);
                } else if (type.isLeftClick()) { // View information
                    PluginConfig.GUI.CLICK_SOUND.playTo(viewer);
                    if (data.isOwner(clicker)) {
                        ResidenceManageUI.open(viewer, data, previous);
                    } else {
                        ResidenceInfoUI.open(viewer, data, previous);
                    }
                } else if (type.isRightClick()) { // Teleport to residence (If allowed)
                    if (!data.canTeleport(viewer)) return;
                    Location target = data.getTeleportLocation(viewer);
                    if (target == null) {
                        PluginMessages.TELEPORT.NO_LOCATION.sendTo(clicker, data.getDisplayName());
                        return;
                    }
                    data.getResidence().tpToResidence(clicker, clicker, clicker.hasPermission("residence.admin"));
                    PluginMessages.TELEPORT.SOUND.playTo(clicker);
                }
            }
        };
    }


    public interface CONFIG extends Configuration {

        ConfiguredMessage<String> TITLE = ConfiguredMessage.asString().defaults("&a&lResidence list &7(&f%(current_page)&7/%(total_page))").params("current_page", "total_page").build();

        interface ITEMS extends Configuration {

            ConfiguredItem ALL = ConfiguredItem.create()
                    .defaultType(Material.CHEST)
                    .defaultName("&a&lAll residences")
                    .defaultLore(
                            "&7", "&7Now all residences are displayed.",
                            "&7",
                            "&a ▶ Click &8|&f See only personal residences"
                    ).build();

            ConfiguredItem OWNED = ConfiguredItem.create()
                    .defaultType(Material.PLAYER_HEAD)
                    .defaultName("&7Residence owned by &f%(owner)")
                    .defaultLore("&7", "&a ▶ Click &8|&f See all residences")
                    .params("owner").build();

            ConfiguredItem SORT_BY_RATINGS = ConfiguredItem.create()
                    .defaultType(Material.LADDER)
                    .defaultName("&fSort by &e&lRATINGS %(order)")
                    .defaultLore(
                            "&7",
                            "&fSort order: %(order)",
                            "&fSort functions:",
                            "&7 &a➥ &e&lRATINGS",
                            "&7     &fNAME",
                            "&7     &fSIZE",
                            " ",
                            "&a ▶ LClick &8|&f Switch sort function",
                            "&a ▶ RClick &8|&f Toggle sort reverse"
                    ).params("order").build();

            ConfiguredItem SORT_BY_NAME = ConfiguredItem.create()
                    .defaultType(Material.LADDER)
                    .defaultName("&fSort by &2&lNAME %(order)")
                    .defaultLore(
                            "&7",
                            "&fSort order: %(order)",
                            "&fSort functions:",
                            "&7     &fRATINGS",
                            "&7 &a➥ &2&lNAME",
                            "&7     &fSIZE",
                            " ",
                            "&a ▶ LClick &8|&f Switch sort function",
                            "&a ▶ RClick &8|&f Toggle sort reverse"
                    ).params("order").build();

            ConfiguredItem SORT_BY_SIZE = ConfiguredItem.create()
                    .defaultType(Material.LADDER)
                    .defaultName("&fSort by &d&lSIZE %(order)")
                    .defaultLore(
                            "&7",
                            "&fSort order: %(order)",
                            "&fSort functions:",
                            "&7     &fRATINGS",
                            "&7     &fNAME",
                            "&7  &a➥ &d&lSIZE",
                            " ",
                            "&a ▶ LClick &8|&f Switch sort function",
                            "&a ▶ RClick &8|&f Toggle sort reverse"
                    ).params("order").build();

        }

        interface ADDITIONAL_LORE extends Configuration {

            ConfiguredMessage<String> NORMAL = ConfiguredMessage.asString()
                    .defaults("&a ▶ Click &8|&f View information", "&a ▶ Drop &8|&f Pin/Unpin residence")
                    .build();

            ConfiguredMessage<String> TELEPORTABLE = ConfiguredMessage.asString().defaults(
                    "&a ▶ LClick &8|&f View information",
                    "&a ▶ RClick &8|&f Teleport to residence",
                    "&a ▶  Drop  &8|&f Pin/Unpin residence"
            ).build();

        }

    }
}
