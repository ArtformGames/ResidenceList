package com.artformgames.plugin.residencelist.listener;

import com.artformgames.plugin.residencelist.Main;
import com.artformgames.plugin.residencelist.conf.PluginMessages;
import com.artformgames.plugin.residencelist.manager.UserStorageManager;
import com.artformgames.plugin.residencelist.user.UserListStorage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Optional;

public class UserListener implements Listener {


    protected UserStorageManager getUserManager() {
        return Main.getInstance().getUserManager();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return;
        getUserManager().load(event.getUniqueId(), () -> true).join();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPreLoginMonitor(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            getUserManager().unload(event.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLogin(PlayerLoginEvent e) {
        UserListStorage data = getUserManager().getNullable(e.getPlayer().getUniqueId());
        if (data == null) {
            e.setResult(PlayerLoginEvent.Result.KICK_OTHER);
            Optional.ofNullable(PluginMessages.LOAD_FAILED.parseToLine(e.getPlayer())).ifPresent(e::setKickMessage);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        getUserManager().unload(player.getUniqueId());
    }

}
