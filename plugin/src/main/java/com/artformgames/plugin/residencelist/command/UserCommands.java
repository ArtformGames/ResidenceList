package com.artformgames.plugin.residencelist.command;

import cc.carm.lib.easyplugin.command.CommandHandler;
import com.artformgames.plugin.residencelist.command.user.EditCommand;
import com.artformgames.plugin.residencelist.command.user.InfoCommand;
import com.artformgames.plugin.residencelist.command.user.OpenCommand;
import com.artformgames.plugin.residencelist.conf.PluginMessages;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class UserCommands extends CommandHandler {

    public UserCommands(@NotNull JavaPlugin plugin) {
        super(plugin);
        registerSubCommand(new OpenCommand(this, "open", "o"));
        registerSubCommand(new InfoCommand(this, "info", "i"));
        registerSubCommand(new EditCommand(this, "edit", "e"));
    }

    @Override
    public Void noArgs(CommandSender sender) {
        PluginMessages.COMMAND.USER.sendTo(sender);
        return null;
    }

    @Override
    public Void noPermission(CommandSender sender) {
        PluginMessages.COMMAND.NO_PERMISSION.sendTo(sender);
        return null;
    }

}
