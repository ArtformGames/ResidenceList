package com.artformgames.plugin.residencelist.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/** Chat input for Paper and Folia, which expose the Adventure chat event. */
final class PaperChatListener implements Listener {

    @EventHandler
    public void onMessage(AsyncChatEvent event) {
        String input = PlainTextComponentSerializer.plainText().serialize(event.message());
        EditHandler.consume(event.getPlayer(), input, () -> event.setCancelled(true));
    }

}
