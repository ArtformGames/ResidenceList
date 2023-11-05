package com.artformgames.plugin.residencelist.listener;

import com.artformgames.plugin.residencelist.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

/**
 * This class is used to handle the edit process based on chat messages.
 */
public class EditHandler implements Listener {

    protected static final Map<UUID, BiConsumer<Player, String>> callbackMap = new HashMap<>();

    @EventHandler
    public void onMessage(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        BiConsumer<Player, String> target = callbackMap.remove(player.getUniqueId());
        if (target == null) return;

        event.setCancelled(true);
        String input = event.getMessage();

        if (input.isBlank() || input.equalsIgnoreCase("#cancel")) return;
        Main.getInstance().getScheduler().run(() -> target.accept(player, event.getMessage()));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        callbackMap.remove(event.getPlayer().getUniqueId());
    }

    public static void start(@NotNull Player player, BiConsumer<@NotNull Player, @NotNull String> callback) {
        start(player.getUniqueId(), callback);
    }

    public static void start(@NotNull UUID player, BiConsumer<@NotNull Player, @NotNull String> callback) {
        callbackMap.put(player, callback);
    }

}
