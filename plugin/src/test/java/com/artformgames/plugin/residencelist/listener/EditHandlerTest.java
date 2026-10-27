package com.artformgames.plugin.residencelist.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class EditHandlerTest {

    @Test
    public void editIsConsumedOnceAndRunsOnScheduledPlayerTask() {
        Player player = player();
        when(player.isOnline()).thenReturn(true);
        List<Runnable> scheduled = new ArrayList<>();
        AtomicReference<String> saved = new AtomicReference<>();
        EditHandler.start(player, (ignored, input) -> saved.set(input));

        assertTrue(EditHandler.consume(player, "new name", scheduled::add, () -> { }));
        assertNull(saved.get());
        assertEquals(1, scheduled.size());
        assertFalse(EditHandler.consume(player, "duplicate", scheduled::add, () -> { }));

        scheduled.get(0).run();
        assertEquals("new name", saved.get());
    }

    @Test
    public void cancelAndQuitDiscardPendingEdits() {
        Player player = player();
        List<Runnable> scheduled = new ArrayList<>();
        EditHandler.start(player, (ignored, input) -> fail("cancelled edit ran"));
        assertTrue(EditHandler.consume(player, "#cancel", scheduled::add, () -> { }));
        assertTrue(scheduled.isEmpty());

        EditHandler.start(player, (ignored, input) -> fail("quit edit ran"));
        new EditHandler().onQuit(new PlayerQuitEvent(player, ""));
        assertFalse(EditHandler.consume(player, "new name", scheduled::add, () -> { }));
    }

    @Test
    public void scheduledEditDoesNotRunAfterPlayerLeaves() {
        Player player = player();
        List<Runnable> scheduled = new ArrayList<>();
        EditHandler.start(player, (ignored, input) -> fail("offline edit ran"));
        assertTrue(EditHandler.consume(player, "new name", scheduled::add, () -> { }));
        scheduled.get(0).run();
    }

    @Test
    public void chatIsCancelledBeforeSchedulingCanFail() {
        Player player = player();
        AtomicBoolean cancelled = new AtomicBoolean();
        EditHandler.start(player, (ignored, input) -> fail("rejected edit ran"));

        assertThrows(IllegalStateException.class, () -> EditHandler.consume(player, "private edit",
                task -> { throw new IllegalStateException("plugin disabled"); },
                () -> cancelled.set(true)));
        assertTrue(cancelled.get());
    }

    @Test
    public void clearDiscardsPendingEdits() {
        Player player = player();
        EditHandler.start(player, (ignored, input) -> fail("cleared edit ran"));
        EditHandler.clear();
        assertFalse(EditHandler.consume(player, "new name", task -> fail("task scheduled"), () -> { }));
    }

    @Test
    public void paperChatConsumesAndCancelsEditMessage() {
        Player player = player();
        AsyncChatEvent event = mock(AsyncChatEvent.class);
        when(event.getPlayer()).thenReturn(player);
        when(event.message()).thenReturn(Component.text("#cancel"));
        EditHandler.start(player, (ignored, input) -> fail("cancelled edit ran"));

        new PaperChatListener().onMessage(event);

        verify(event).setCancelled(true);
    }

    @Test
    public void legacyChatConsumesAndCancelsEditMessage() {
        Player player = player();
        AsyncPlayerChatEvent event = mock(AsyncPlayerChatEvent.class);
        when(event.getPlayer()).thenReturn(player);
        when(event.getMessage()).thenReturn("#cancel");
        EditHandler.start(player, (ignored, input) -> fail("cancelled edit ran"));

        new LegacyChatListener().onMessage(event);

        verify(event).setCancelled(true);
    }

    private static Player player() {
        Player player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        return player;
    }

}
