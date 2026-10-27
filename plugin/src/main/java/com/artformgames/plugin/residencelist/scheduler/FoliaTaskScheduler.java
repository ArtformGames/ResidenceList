package com.artformgames.plugin.residencelist.scheduler;

import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Objects;

/** Folia-only code: never instantiated on a Bukkit server without Folia's API. */
final class FoliaTaskScheduler implements TaskScheduler {
    private final Plugin plugin;
    private final GlobalRegionScheduler global;
    private final AsyncScheduler async;

    FoliaTaskScheduler(Plugin plugin) {
        this(plugin, Bukkit.getGlobalRegionScheduler(), Bukkit.getAsyncScheduler());
    }

    FoliaTaskScheduler(Plugin plugin, GlobalRegionScheduler global, AsyncScheduler async) {
        this.plugin = Objects.requireNonNull(plugin);
        this.global = Objects.requireNonNull(global);
        this.async = Objects.requireNonNull(async);
    }

    @Override
    public void runGlobal(Runnable action) {
        global.execute(plugin, () -> {
            if (plugin.isEnabled()) action.run();
        });
    }

    @Override
    public void runAsync(Runnable action) {
        async.runNow(plugin, ignored -> {
            if (plugin.isEnabled()) action.run();
        });
    }

    @Override
    public void runForPlayer(Player player, Runnable action) {
        // A moving player may change regions before this task runs. Its own scheduler follows it.
        // The retired callback must do no world/entity work and intentionally does nothing.
        player.getScheduler().execute(plugin, () -> {
            if (plugin.isEnabled() && player.isOnline()) action.run();
        }, () -> { }, 1L);
    }

    @Override
    public boolean isFolia() {
        return true;
    }

    @Override
    public void cancelTasks() {
        global.cancelTasks(plugin);
        async.cancelTasks(plugin);
    }
}
