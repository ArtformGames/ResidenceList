package com.artformgames.plugin.residencelist.conf;

import cc.carm.lib.configuration.core.Configuration;
import cc.carm.lib.easyplugin.utils.ColorParser;
import cc.carm.lib.mineconfiguration.bukkit.builder.message.CraftMessageListBuilder;
import cc.carm.lib.mineconfiguration.bukkit.builder.message.CraftMessageValueBuilder;
import cc.carm.lib.mineconfiguration.bukkit.value.ConfiguredMessage;
import cc.carm.lib.mineconfiguration.bukkit.value.ConfiguredMessageList;
import cc.carm.lib.mineconfiguration.bukkit.value.ConfiguredSound;
import de.themoep.minedown.MineDown;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

public interface PluginMessages extends Configuration {

    static @NotNull CraftMessageValueBuilder<BaseComponent[]> value() {
        return ConfiguredMessage.create(getParser()).whenSend((sender, message) -> sender.spigot().sendMessage(message));
    }

    static @NotNull CraftMessageListBuilder<BaseComponent[]> list() {
        return ConfiguredMessageList.create(getParser()).whenSend((sender, message) -> message.forEach(m -> sender.spigot().sendMessage(m)));
    }

    static @NotNull BiFunction<CommandSender, String, BaseComponent[]> getParser() {
        return PluginMessages::parse;
    }

    static @NotNull BaseComponent[] parse(@NotNull CommandSender sender, @NotNull String message) {
        if (sender instanceof Player player) message = PlaceholderAPI.setPlaceholders(player, message);
        return MineDown.parse(ColorParser.parse(message));
    }

    ConfiguredMessageList<String> LOAD_FAILED = ConfiguredMessageList.asStrings()
            .defaults("&c&lSorry! &fBut your residence data failed to load, please rejoin!")
            .build();

    interface COMMAND extends Configuration {

        ConfiguredMessageList<BaseComponent[]> USER = list()
                .defaults(
                        "&e&lResidenceList &fCommands &7(/reslist)",
                        "&8#&f open &e[player]",
                        "&8-&7 Open the residence list gui.",
                        "&8#&f info &e<residence>",
                        "&8-&7 Display the residence info",
                        "&8#&f edit &e<residence>",
                        "&8-&7 Open the residence edit gui"
                ).build();

        ConfiguredMessageList<BaseComponent[]> ADMIN = list()
                .defaults(
                        "&e&lResidenceList &fAdmin Commands &7(/reslistadmin)",
                        "&8#&f open &e[player]",
                        "&8-&7 Open the admin manage GUI.",
                        "&8#&f edit &e<residence>",
                        "&8-&7 Open the residence edit gui",
                        "&8#&f reload",
                        "&8-&7 Reload the configuration file."
                ).build();

        ConfiguredMessageList<BaseComponent[]> NO_PERMISSION = list()
                .defaults("&c&lSorry! &fBut you dont have enough permissions to do that!")
                .build();

        ConfiguredMessageList<BaseComponent[]> ONLY_PLAYER = list()
                .defaults("&c&lSorry! &fBut this command only can be executed by a player!")
                .build();


        ConfiguredMessageList<BaseComponent[]> NOT_EXISTS = list()
                .defaults("&c&lSorry! &fThere is currently no residence with name &e#%(residence) &f!")
                .params("residence")
                .build();

        ConfiguredMessageList<BaseComponent[]> UNKNOWN_PLAYER = list()
                .defaults("&c&lSorry! &fThere is currently no player named &e#%(name) &f!")
                .params("name")
                .build();


        ConfiguredMessageList<BaseComponent[]> NOT_OWNER = list()
                .defaults("&c&lSorry! &fBut you are not the owner of residence &e#%(residence) &f!")
                .params("residence", "residence_nickname").build();


    }

    interface RELOAD extends Configuration {

        ConfiguredMessageList<BaseComponent[]> START = list()
                .defaults("&fReloading the plugin configurations...")
                .build();

        ConfiguredMessageList<BaseComponent[]> SUCCESS = list()
                .defaults("&a&lSuccess! &fThe plugin configurations has been reloaded, cost &a%(time)&fms.")
                .params("time")
                .build();

        ConfiguredMessageList<BaseComponent[]> FAILED = list()
                .defaults("&c&lFailed! &fThe plugin configurations failed to reload.")
                .build();

    }


    interface PIN extends Configuration {
        ConfiguredSound SOUND = ConfiguredSound.of("BLOCK_ANVIL_PLACE");
        ConfiguredMessageList<BaseComponent[]> MESSAGE = list()
                .defaults("&fYou have successfully pinned the residence &e%(residence)&f!")
                .params("residence")
                .build();
    }


    interface UNPIN extends Configuration {
        ConfiguredSound SOUND = ConfiguredSound.of(Sound.BLOCK_ANVIL_BREAK, 0.5F);
        ConfiguredMessageList<BaseComponent[]> MESSAGE = list()
                .defaults("&fYou have unpinned the residence &e%(residence)&f!")
                .params("residence")
                .build();
    }


    interface TELEPORT extends Configuration {
        ConfiguredSound SOUND = ConfiguredSound.of(Sound.ENTITY_ENDERMAN_TELEPORT, 0.5F);

        ConfiguredMessageList<BaseComponent[]> NO_LOCATION = list()
                .defaults("&c&lSorry! &fBut you cannot teleport to &e%(residence) &fyet!")
                .params("residence")
                .build();
    }


    interface COMMENT extends Configuration {
        ConfiguredSound ASK_SOUND = ConfiguredSound.of(Sound.ENTITY_CHICKEN_EGG, 0.5F);
        ConfiguredSound YES_SOUND = ConfiguredSound.of(Sound.ENTITY_VILLAGER_YES, 0.5F);
        ConfiguredSound NO_SOUND = ConfiguredSound.of(Sound.ENTITY_VILLAGER_NO, 0.5F);

        ConfiguredMessageList<BaseComponent[]> NOTIFY = list()
                .defaults(
                        "&fYou are commenting for residence &e%(residence)&f, please enter your comment in chat.",
                        "&fYou can enter '&e#cancel&f' to cancel this operation."
                ).params("residence").build();
    }

    interface EDIT extends Configuration {
        ConfiguredSound EDIT_SOUND = ConfiguredSound.of(Sound.ENTITY_CHICKEN_EGG, 0.5F);
        ConfiguredSound SUCCESS_SOUND = ConfiguredSound.of(Sound.BLOCK_LEVER_CLICK, 0.5F);
        ConfiguredSound FAILED_SOUND = ConfiguredSound.of(Sound.ENTITY_VILLAGER_NO, 0.5F);

        ConfiguredMessageList<BaseComponent[]> NAME = list()
                .defaults(
                        "&fYou are setting up nickname for residence &e%(residence)&f, please enter in chat.",
                        "&fRemember that a nickname should be &eless than 16 characters&f.",
                        "&fYou can enter '&e#cancel&f' to cancel this operation."
                ).params("residence").build();


        ConfiguredMessageList<BaseComponent[]> DESCRIPTION = list()
                .defaults(
                        "&fYou are editing description for residence &e%(residence)&f, please enter in chat.",
                        "&fRemember that you can use '&e\\\\n&f' to wrap lines.",
                        "&fYou can enter '&e#cancel&f' to cancel this operation."
                ).params("residence").build();

        ConfiguredMessageList<BaseComponent[]> NAME_TOO_LONG = list()
                .defaults(
                        "&c&lSorry! &fBut the nickname that you input is too long,",
                        "&fRemember that a nickname should be &eless than 16 characters&f."
                ).params("residence").build();


        ConfiguredMessageList<BaseComponent[]> NAME_UPDATED = list()
                .defaults("&fYou have successfully updated the nickname of residence &e%(residence)&f!")
                .params("residence")
                .build();

        ConfiguredMessageList<BaseComponent[]> DESCRIPTION_UPDATED = list()
                .defaults("&fYou have successfully updated the description of residence &e%(residence)&f!")
                .params("residence")
                .build();

        ConfiguredMessageList<BaseComponent[]> ICON_UPDATED = list()
                .defaults("&fYou have successfully updated the icon of residence &e%(residence)&f!")
                .params("residence")
                .build();

        ConfiguredMessageList<BaseComponent[]> ICON_BLOCKED = list()
                .defaults("&fYou cannot select this material as residence icon!")
                .build();
    }


}

