package com.artformgames.plugin.residencelist.command;

import cc.carm.lib.easyplugin.command.CommandHandler;
import com.artformgames.plugin.residencelist.command.user.OpenCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class UserCommands extends CommandHandler {

    public UserCommands(@NotNull JavaPlugin plugin) {
        super(plugin);
        registerSubCommand(new OpenCommand(this, "open", "o"));
    }

    @Override
    public Void noArgs(CommandSender sender) {
        return null;
    }

    @Override
    public Void noPermission(CommandSender sender) {
        return null;
    }
}
