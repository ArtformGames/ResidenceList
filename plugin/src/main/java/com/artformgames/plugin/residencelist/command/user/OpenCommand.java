package com.artformgames.plugin.residencelist.command.user;

import cc.carm.lib.easyplugin.command.SubCommand;
import com.artformgames.plugin.residencelist.command.UserCommands;
import com.artformgames.plugin.residencelist.conf.PluginConfig;
import com.artformgames.plugin.residencelist.ui.ResidenceListUI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class OpenCommand extends SubCommand<UserCommands> {

    public OpenCommand(@NotNull UserCommands parent, String identifier, String... aliases) {
        super(parent, identifier, aliases);
    }

    @Override
    public Void execute(JavaPlugin plugin, CommandSender sender, String[] args) throws Exception {
        if (sender instanceof Player player) {
            ResidenceListUI.open(player, null);
            PluginConfig.GUI.OPEN_SOUND.playTo(player);
        }

        return null;
    }
}
