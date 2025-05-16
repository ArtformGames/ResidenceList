package com.artformgames.plugin.residencelist.listener;

import cc.carm.lib.easyplugin.user.UserDataRegistry;
import com.artformgames.plugin.residencelist.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class UserListener implements Listener {

    protected static UserDataRegistry<UUID, ?> users() {
        return Main.getInstance().getUserManager();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLogin(PlayerLoginEvent e) {
        users().load(e.getPlayer().getUniqueId(), () -> true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        users().unload(event.getPlayer().getUniqueId(), true);
    }

}
