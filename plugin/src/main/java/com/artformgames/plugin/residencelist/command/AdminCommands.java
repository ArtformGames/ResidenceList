package com.artformgames.plugin.residencelist.command;

import cc.carm.lib.easyplugin.command.CommandHandler;
import com.artformgames.plugin.residencelist.command.admin.EditCommand;
import com.artformgames.plugin.residencelist.command.admin.OpenCommand;
import com.artformgames.plugin.residencelist.command.admin.ReloadCommand;
import com.artformgames.plugin.residencelist.conf.PluginMessages;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class AdminCommands extends CommandHandler {

    public AdminCommands(@NotNull JavaPlugin plugin) {
        super(plugin);
        registerSubCommand(new OpenCommand(this, "open", "o"));
        registerSubCommand(new EditCommand(this, "edit", "e"));
        registerSubCommand(new ReloadCommand(this, "reload", "r"));
    }

    @Override
    public Void noArgs(CommandSender sender) {
        PluginMessages.COMMAND.ADMIN.sendTo(sender);
        return null;
    }

    @Override
    public Void noPermission(CommandSender sender) {
        PluginMessages.COMMAND.NO_PERMISSION.sendTo(sender);
        return null;
    }

    @Override
    public boolean hasPermission(@NotNull CommandSender sender) {
        return sender.hasPermission("ResidenceList.admin");
    }
}
