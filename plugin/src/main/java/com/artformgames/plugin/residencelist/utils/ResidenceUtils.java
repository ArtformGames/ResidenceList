package com.artformgames.plugin.residencelist.utils;

import com.artformgames.plugin.residencelist.conf.PluginConfig;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ResidenceUtils {
    private static final UUID TEMP_UUID = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");

    public static boolean isServerLand(ClaimedResidence residence) {
        return residence.getOwnerUUID() == null
                || TEMP_UUID.equals(residence.getOwnerUUID())
                || Residence.getInstance().getServerUUID().equals(residence.getOwnerUUID());
    }

    public static boolean hiddenDefault() {
        return !PluginConfig.SETTINGS.DEFAULT_STATUS.resolve();
    }

    public static boolean viewable(@NotNull ClaimedResidence residence, @NotNull Player viewer) {
        if (isServerLand(residence)) return true;
        if (!residence.getPermissions().has(Flags.hidden, hiddenDefault())) return true;
        return residence.isOwner(viewer);
    }

}
