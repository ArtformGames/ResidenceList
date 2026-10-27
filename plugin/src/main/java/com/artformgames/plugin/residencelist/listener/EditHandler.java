package com.artformgames.plugin.residencelist.listener;

import com.artformgames.plugin.residencelist.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * This class is used to handle the edit process based on chat messages.
 */
public class EditHandler implements Listener {

    private static final Map<UUID, BiConsumer<Player, String>> callbackMap = new ConcurrentHashMap<>();

    /** Paper/Folia use their native chat event; Spigot uses the legacy Bukkit event. */
    public static Listener chatListener() {
        try {
            Class.forName("io.papermc.paper.event.player.AsyncChatEvent", false, EditHandler.class.getClassLoader());
            return new PaperChatListener();
        } catch (ClassNotFoundException ignored) {
            return new LegacyChatListener();
        }
    }

    static boolean consume(Player player, String input, Runnable cancelChat) {
        return consume(player, input, task -> Main.getInstance().getTaskScheduler().runForPlayer(player, task), cancelChat);
    }

    static boolean consume(Player player, String input, Consumer<Runnable> schedule, Runnable cancelChat) {
        BiConsumer<Player, String> callback = callbackMap.remove(player.getUniqueId());
        if (callback == null) return false;

        // Cancel before scheduling: a rejected task must not disclose an edit in public chat.
        cancelChat.run();
        if (!input.isBlank() && !input.equalsIgnoreCase("#cancel")) {
            schedule.accept(() -> {
                if (player.isOnline()) callback.accept(player, input);
            });
        }
        return true;
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

    public static void clear() {
        callbackMap.clear();
    }

}
