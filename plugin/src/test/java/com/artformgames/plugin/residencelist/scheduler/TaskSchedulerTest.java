package com.artformgames.plugin.residencelist.scheduler;

import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.EntityScheduler;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class TaskSchedulerTest {

    @Test
    public void foliaPlayerCallbackRunsOnEntitySchedulerAndNotOnGlobalScheduler() {
        Plugin plugin = mock(Plugin.class);
        GlobalRegionScheduler global = mock(GlobalRegionScheduler.class);
        AsyncScheduler async = mock(AsyncScheduler.class);
        Player player = mock(Player.class);
        EntityScheduler entity = mock(EntityScheduler.class);
        when(plugin.isEnabled()).thenReturn(true);
        when(player.isOnline()).thenReturn(true);
        when(player.getScheduler()).thenReturn(entity);
        AtomicInteger calls = new AtomicInteger();

        TaskScheduler scheduler = new FoliaTaskScheduler(plugin, global, async);
        scheduler.runForPlayer(player, calls::incrementAndGet);

        ArgumentCaptor<Runnable> run = ArgumentCaptor.forClass(Runnable.class);
        ArgumentCaptor<Runnable> retired = ArgumentCaptor.forClass(Runnable.class);
        verify(entity).execute(eq(plugin), run.capture(), retired.capture(), eq(1L));
        verifyNoInteractions(global, async);
        assertTrue(scheduler.isFolia());
        assertEquals(0, calls.get());
        run.getValue().run();
        assertEquals(1, calls.get());
        retired.getValue().run();
        assertEquals(1, calls.get());
    }

    @Test
    public void foliaPlayerCallbackSkipsOfflineOrDisabledPlugin() {
        Plugin plugin = mock(Plugin.class);
        Player player = mock(Player.class);
        EntityScheduler entity = mock(EntityScheduler.class);
        when(player.getScheduler()).thenReturn(entity);
        AtomicInteger calls = new AtomicInteger();
        TaskScheduler scheduler = new FoliaTaskScheduler(plugin,
                mock(GlobalRegionScheduler.class), mock(AsyncScheduler.class));

        scheduler.runForPlayer(player, calls::incrementAndGet);
        ArgumentCaptor<Runnable> run = ArgumentCaptor.forClass(Runnable.class);
        verify(entity).execute(eq(plugin), run.capture(), any(Runnable.class), eq(1L));
        when(plugin.isEnabled()).thenReturn(true);
        when(player.isOnline()).thenReturn(false);
        run.getValue().run();
        when(player.isOnline()).thenReturn(true);
        when(plugin.isEnabled()).thenReturn(false);
        run.getValue().run();
        assertEquals(0, calls.get());
    }

    @Test
    public void foliaGlobalAndAsyncCallbacksUseTheirOwnSchedulers() {
        Plugin plugin = mock(Plugin.class);
        GlobalRegionScheduler global = mock(GlobalRegionScheduler.class);
        AsyncScheduler async = mock(AsyncScheduler.class);
        when(plugin.isEnabled()).thenReturn(true);
        TaskScheduler scheduler = new FoliaTaskScheduler(plugin, global, async);
        AtomicInteger calls = new AtomicInteger();

        scheduler.runGlobal(calls::incrementAndGet);
        scheduler.runAsync(calls::incrementAndGet);

        ArgumentCaptor<Runnable> globalRun = ArgumentCaptor.forClass(Runnable.class);
        @SuppressWarnings("rawtypes") ArgumentCaptor<Consumer> asyncRun = ArgumentCaptor.forClass(Consumer.class);
        verify(global).execute(eq(plugin), globalRun.capture());
        verify(async).runNow(eq(plugin), asyncRun.capture());
        globalRun.getValue().run();
        asyncRun.getValue().accept(null);
        assertEquals(2, calls.get());
        scheduler.cancelTasks();
        verify(global).cancelTasks(plugin);
        verify(async).cancelTasks(plugin);
    }

    @Test
    public void bukkitFallbackUsesLegacySchedulerAndChecksPlayerAtExecution() {
        Plugin plugin = mock(Plugin.class);
        Player player = mock(Player.class);
        BukkitScheduler bukkit = mock(BukkitScheduler.class);
        when(plugin.isEnabled()).thenReturn(true);
        AtomicInteger calls = new AtomicInteger();

        try (MockedStatic<Bukkit> server = mockStatic(Bukkit.class)) {
            server.when(Bukkit::getScheduler).thenReturn(bukkit);
            TaskScheduler scheduler = new BukkitTaskScheduler(plugin);
            assertFalse(scheduler.isFolia());
            scheduler.runForPlayer(player, calls::incrementAndGet);
            ArgumentCaptor<Runnable> run = ArgumentCaptor.forClass(Runnable.class);
            verify(bukkit).runTask(eq(plugin), run.capture());
            when(player.isOnline()).thenReturn(false, true);
            run.getValue().run();
            run.getValue().run();
            assertEquals(1, calls.get());
            scheduler.runAsync(calls::incrementAndGet);
            verify(bukkit).runTaskAsynchronously(eq(plugin), any(Runnable.class));
            scheduler.cancelTasks();
            verify(bukkit).cancelTasks(plugin);
        }
    }
}
