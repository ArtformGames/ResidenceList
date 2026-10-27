package com.artformgames.plugin.residencelist.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Objects;

final class BukkitTaskScheduler implements TaskScheduler {
    private final Plugin plugin;

    BukkitTaskScheduler(Plugin plugin) {
        this.plugin = Objects.requireNonNull(plugin);
    }

    @Override
    public void runGlobal(Runnable action) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (plugin.isEnabled()) action.run();
        });
    }

    @Override
    public void runAsync(Runnable action) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if (plugin.isEnabled()) action.run();
        });
    }

    @Override
    public void runForPlayer(Player player, Runnable action) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (plugin.isEnabled() && player.isOnline()) action.run();
        });
    }

    @Override
    public boolean isFolia() {
        return false;
    }

    @Override
    public void cancelTasks() {
        Bukkit.getScheduler().cancelTasks(plugin);
    }
}
