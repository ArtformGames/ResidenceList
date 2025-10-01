package com.artformgames.plugin.residencelist.command.user;

import cc.carm.lib.easyplugin.command.SimpleCompleter;
import cc.carm.lib.easyplugin.command.SubCommand;
import com.artformgames.plugin.residencelist.command.UserCommands;
import com.artformgames.plugin.residencelist.conf.PluginConfig;
import com.artformgames.plugin.residencelist.conf.PluginMessages;
import com.artformgames.plugin.residencelist.ui.ResidenceListUI;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class OpenCommand extends SubCommand<UserCommands> {

    public OpenCommand(@NotNull UserCommands parent, String identifier, String... aliases) {
        super(parent, identifier, aliases);
    }

    @Override
    public Void execute(JavaPlugin plugin, CommandSender sender, String[] args) throws Exception {
        if (!(sender instanceof Player player)) {
            PluginMessages.COMMAND.ONLY_PLAYER.sendTo(sender);
            return null;
        }

        Player owner = null;
        if (args.length > 0) {
            owner = Bukkit.getOnlinePlayers().stream()
                    .filter(s -> s.getName() != null && s.getName().equals(args[0]))
                    .findFirst().orElse(null);
            if (owner == null) {
                PluginMessages.COMMAND.UNKNOWN_PLAYER.sendTo(sender, args[0]);
                return null;
            }
        }

        ResidenceListUI.open(player, Optional.ofNullable(owner).map(Player::getName).orElse(null));
        PluginConfig.GUI.OPEN_SOUND.playTo(player);

        return null;
    }

    @Override
    public List<String> tabComplete(JavaPlugin plugin, CommandSender sender, String[] args) {
        if (args.length == 1) {
            return SimpleCompleter.onlinePlayers(args[args.length - 1], 10);
        } else return SimpleCompleter.none();
    }
}
