package com.artformgames.plugin.residencelist.utils;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class ResidenceUtils {
    public static boolean isServerLand(ClaimedResidence residence) {
        return Residence.getInstance().getServerUUID().equals(residence.getOwnerUUID())
                || Residence.getInstance().getEmptyUserUUID().equals(residence.getOwnerUUID());
    }
}
