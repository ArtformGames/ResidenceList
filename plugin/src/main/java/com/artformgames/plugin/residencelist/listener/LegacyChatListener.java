package com.artformgames.plugin.residencelist.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/** Chat input for Spigot servers without Paper's modern chat event. */
final class LegacyChatListener implements Listener {

    @EventHandler
    public void onMessage(AsyncPlayerChatEvent event) {
        EditHandler.consume(event.getPlayer(), event.getMessage(), () -> event.setCancelled(true));
    }

}
