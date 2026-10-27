package com.artformgames.plugin.residencelist.utils;

import com.artformgames.plugin.residencelist.Main;
import com.artformgames.plugin.residencelist.api.residence.ResidenceData;
import com.artformgames.plugin.residencelist.conf.PluginConfig;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ResidenceUtils {
    private static final UUID TEMP_UUID = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");

    public record TeleportPreview(String world, int x, int y, int z, boolean approximate) {
        /** Keep the existing world/x/y/z item parameters on every server platform. */
        public Object[] parameters() {
            return new Object[]{world, formatCoordinate(x), formatCoordinate(y), formatCoordinate(z)};
        }

        private String formatCoordinate(int coordinate) {
            return (approximate ? "≈" : "") + coordinate;
        }
    }

    /** Folia previews use stored cuboid bounds; resolving a safe teleport spot reads remote blocks. */
    public static @Nullable TeleportPreview teleportPreview(@NotNull ResidenceData data,
                                                             @NotNull Player viewer,
                                                             @Nullable Location fallback) {
        if (Main.getInstance().getTaskScheduler().isFolia()) {
            return approximateTeleportPreview(data.getResidence().getMainArea());
        }
        Location location = data.getTeleportLocation(viewer, fallback);
        if (location == null) return null;
        return new TeleportPreview(data.getResidence().getMainArea().getWorldName(),
                location.getBlockX(), location.getBlockY(), location.getBlockZ(), false);
    }

    public static @Nullable TeleportPreview approximateTeleportPreview(@Nullable CuboidArea area) {
        if (area == null) return null;
        Vector low = area.getLowVector();
        Vector high = area.getHighVector();
        return new TeleportPreview(area.getWorldName(),
                (low.getBlockX() + high.getBlockX()) / 2,
                (low.getBlockY() + high.getBlockY()) / 2,
                (low.getBlockZ() + high.getBlockZ()) / 2, true);
    }

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
