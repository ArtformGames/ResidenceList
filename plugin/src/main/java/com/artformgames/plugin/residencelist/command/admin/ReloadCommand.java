package com.artformgames.plugin.residencelist.command.admin;

import cc.carm.lib.easyplugin.command.SubCommand;
import com.artformgames.plugin.residencelist.Main;
import com.artformgames.plugin.residencelist.command.AdminCommands;
import com.artformgames.plugin.residencelist.conf.PluginMessages;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class ReloadCommand extends SubCommand<AdminCommands> {

    public ReloadCommand(@NotNull AdminCommands parent, String identifier, String... aliases) {
        super(parent, identifier, aliases);
    }

    @Override
    public Void execute(JavaPlugin plugin, CommandSender sender, String[] args) throws Exception {

        try {
            PluginMessages.RELOAD.START.sendTo(sender);
            long s1 = System.currentTimeMillis();
            Main.getInstance().getTaskScheduler().runAsync(() -> {
                try {
                    Main.getInstance().getConfiguration().reload();
                    long elapsed = System.currentTimeMillis() - s1;
                    reply(sender, () -> PluginMessages.RELOAD.SUCCESS.sendTo(sender, elapsed));
                } catch (Exception e) {
                    e.printStackTrace();
                    reply(sender, () -> PluginMessages.RELOAD.FAILED.sendTo(sender));
                }
            });
        } catch (Exception e) {
            PluginMessages.RELOAD.FAILED.sendTo(sender);
            e.printStackTrace();
        }

        return null;
    }

    private void reply(CommandSender sender, Runnable action) {
        if (sender instanceof Player player) {
            Main.getInstance().getTaskScheduler().runForPlayer(player, action);
        } else {
            Main.getInstance().getTaskScheduler().runGlobal(action);
        }
    }

}
