package com.artformgames.plugin.residencelist.listener;

import cc.carm.lib.easyplugin.user.UserDataRegistry;
import com.artformgames.plugin.residencelist.Main;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class UserListener implements Listener {

    private final Map<UUID, Object> sessions = new ConcurrentHashMap<>();
    private final Map<UUID, CompletableFuture<Boolean>> pendingUnloads = new ConcurrentHashMap<>();

    protected static UserDataRegistry<UUID, ?> users() {
        return Main.getInstance().getUserManager();
    }

    public void loadOnlineUsers() {
        Bukkit.getOnlinePlayers().forEach(player -> loadUser(player.getUniqueId()));
    }

    private void loadUser(UUID userId) {
        Object session = new Object();
        sessions.put(userId, session);
        CompletableFuture<Boolean> pendingUnload = pendingUnloads.get(userId);
        if (pendingUnload == null) {
            loadSession(userId, session);
        } else {
            pendingUnload.whenComplete((saved, error) -> {
                if (sessions.get(userId) == session) {
                    loadSession(userId, session);
                }
            });
        }
    }

    private void loadSession(UUID userId, Object session) {
        users().load(userId, () -> sessions.get(userId) == session).whenComplete((data, error) -> {
            if (error != null) {
                Main.getInstance().getLogger().log(Level.SEVERE,
                        "Could not load user data for " + userId + "; existing data was not replaced.", error);
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLogin(PlayerLoginEvent e) {
        if (e.getResult() != PlayerLoginEvent.Result.ALLOWED) return;
        loadUser(e.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        UUID userId = event.getPlayer().getUniqueId();
        sessions.remove(userId);
        CompletableFuture<Boolean> pendingUnload = users().unload(userId, true);
        pendingUnloads.put(userId, pendingUnload);
        pendingUnload.whenComplete((saved, error) -> {
            pendingUnloads.remove(userId, pendingUnload);
            if (error != null) {
                Main.getInstance().getLogger().log(Level.SEVERE,
                        "Could not save user data for " + userId + "; dirty data remains cached for retry.", error);
            }
        });
    }

}
