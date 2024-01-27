package com.artformgames.plugin.residencelist.ui;

import cc.carm.lib.configuration.core.Configuration;
import cc.carm.lib.easyplugin.gui.GUI;
import cc.carm.lib.easyplugin.gui.GUIItem;
import cc.carm.lib.easyplugin.gui.GUIType;
import cc.carm.lib.mineconfiguration.bukkit.value.ConfiguredMessage;
import cc.carm.lib.mineconfiguration.bukkit.value.item.ConfiguredItem;
import com.artformgames.plugin.residencelist.Main;
import com.artformgames.plugin.residencelist.conf.PluginConfig;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.BiConsumer;

public class SelectIconGUI extends GUI {

    public static void open(@NotNull Player player, @NotNull BiConsumer<Player, ItemStack> callback) {
        new SelectIconGUI(player, callback).openGUI(player);
    }

    protected final @NotNull Player player;
    protected final @NotNull BiConsumer<Player, ItemStack> callback;

    public SelectIconGUI(@NotNull Player player, @NotNull BiConsumer<Player, ItemStack> callback) {
        super(GUIType.ONE_BY_NINE, Objects.requireNonNull(CONFIG.TITLE.parse(player)));
        this.player = player;
        this.callback = callback;

        setItem(4, new GUIItem(CONFIG.ITEMS.SELECT.get(player)));
        setEmptyItem(PluginConfig.ICON.EMPTY.get(player));
    }

    @Override
    public void rawClickListener(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        if (!event.getClickedInventory().equals(player.getInventory())) return;

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        player.closeInventory();
        Main.getInstance().getScheduler().run(() -> callback.accept(player, clickedItem));
    }

    public interface CONFIG extends Configuration {

        ConfiguredMessage<String> TITLE = ConfiguredMessage.asString()
                .defaults("&a&lPlease select icon")
                .build();

        interface ITEMS extends Configuration {

            ConfiguredItem SELECT = ConfiguredItem.create()
                    .defaultType(Material.LIME_DYE)
                    .defaultName("&7Click items in backpack")
                    .defaultLore(
                            " ",
                            "&7Please click items in backpack to select icon.",
                            " "
                    ).build();


        }

    }

}
