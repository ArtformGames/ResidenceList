package com.artformgames.plugin.residencelist.command.admin;

import cc.carm.lib.easyplugin.command.SubCommand;
import com.artformgames.plugin.residencelist.Main;
import com.artformgames.plugin.residencelist.command.AdminCommands;
import com.artformgames.plugin.residencelist.conf.PluginMessages;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class ReloadCommand extends SubCommand<AdminCommands> {

    public ReloadCommand(@NotNull AdminCommands parent, String identifier, String... aliases) {
        super(parent, identifier, aliases);
    }

    @Override
    public Void execute(JavaPlugin plugin, CommandSender sender, String[] args) throws Exception {

        try {
            PluginMessages.RELOAD.START.send(sender);
            long s1 = System.currentTimeMillis();
            Main.getInstance().getConfiguration().reload();
            PluginMessages.RELOAD.SUCCESS.send(sender, System.currentTimeMillis() - s1);
        } catch (Exception e) {
            PluginMessages.RELOAD.FAILED.send(sender);
            e.printStackTrace();
        }

        return null;
    }

}
