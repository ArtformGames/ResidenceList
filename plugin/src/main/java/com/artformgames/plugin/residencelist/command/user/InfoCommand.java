package com.artformgames.plugin.residencelist.command.user;

import cc.carm.lib.easyplugin.command.SimpleCompleter;
import cc.carm.lib.easyplugin.command.SubCommand;
import com.artformgames.plugin.residencelist.ResidenceListAPI;
import com.artformgames.plugin.residencelist.api.residence.ResidenceData;
import com.artformgames.plugin.residencelist.command.UserCommands;
import com.artformgames.plugin.residencelist.conf.PluginConfig;
import com.artformgames.plugin.residencelist.conf.PluginMessages;
import com.artformgames.plugin.residencelist.ui.ResidenceInfoUI;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class InfoCommand extends SubCommand<UserCommands> {

    public InfoCommand(@NotNull UserCommands parent, String identifier, String... aliases) {
        super(parent, identifier, aliases);
    }

    @Override
    public Void execute(JavaPlugin plugin, CommandSender sender, String[] args) throws Exception {
        if (!(sender instanceof Player player)) {
            PluginMessages.COMMAND.ONLY_PLAYER.sendTo(sender);
            return null;
        }
        if (args.length < 1) return getParent().noArgs(sender);

        ClaimedResidence residence = ResidenceListAPI.getResidence(args[0]);
        if (residence == null) {
            PluginMessages.COMMAND.NOT_EXISTS.sendTo(sender, args[0]);
            return null;
        }

        ResidenceData data = ResidenceListAPI.getResidenceData(residence);
        ResidenceInfoUI.open(player, data, null);
        PluginConfig.GUI.OPEN_SOUND.playTo(player);
        return null;
    }

    @Override
    public List<String> tabComplete(JavaPlugin plugin, CommandSender sender, String[] args) {
        if (args.length == 1) {
            return SimpleCompleter.objects(
                    args[args.length - 1],
                    ResidenceListAPI.getResidences(sender instanceof Player ? (Player) sender : null)
                            .values().stream().map(ClaimedResidence::getName)
            );
        } else return SimpleCompleter.none();
    }
}
