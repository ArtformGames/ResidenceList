package com.artformgames.plugin.residencelist.scheduler;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/** Schedules work in the context that owns the data it accesses. */
public interface TaskScheduler {

    static TaskScheduler create(Plugin plugin) {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer", false,
                    plugin.getClass().getClassLoader());
            return new FoliaTaskScheduler(plugin);
        } catch (ClassNotFoundException ignored) {
            return new BukkitTaskScheduler(plugin);
        }
    }

    /** Server-wide work that does not touch a player or a particular world region. */
    void runGlobal(Runnable action);

    /** Work independent of the server tick. Bukkit objects must not be accessed here. */
    void runAsync(Runnable action);

    /** Player work, executed on the player's current owning thread on a later tick. */
    void runForPlayer(Player player, Runnable action);

    boolean isFolia();

    void cancelTasks();
}
