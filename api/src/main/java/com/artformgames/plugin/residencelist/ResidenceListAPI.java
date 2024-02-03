package com.artformgames.plugin.residencelist;

import com.artformgames.plugin.residencelist.api.ResidenceManager;
import com.artformgames.plugin.residencelist.api.UserManager;
import com.artformgames.plugin.residencelist.api.residence.ResidenceData;
import com.artformgames.plugin.residencelist.api.storage.DataStorage;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class ResidenceListAPI {

    protected static ResidenceListPlugin plugin;
    protected static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private ResidenceListAPI() {
    }

    public static void openGUI(Player player, @Nullable String owner) {
        plugin.openGUI(player, owner);
    }

    public static @NotNull ResidenceData getResidenceData(@NotNull ClaimedResidence residence) {
        return getResidenceManager().getResidence(residence);
    }

    public static @Nullable ClaimedResidence getResidence(@NotNull String name) {
        return Residence.getInstance().getResidenceManager().getByName(name);
    }

    @Unmodifiable
    public static @NotNull Map<String, ClaimedResidence> getResidences() {
        return Collections.unmodifiableMap(Residence.getInstance().getResidenceManager().getResidences());
    }

    @Unmodifiable
    public static @NotNull Set<ClaimedResidence> listResidences() {
        return Set.copyOf(getResidences().values());
    }

    public static DataStorage<?, ?> getStorage() {
        return plugin.getStorage();
    }

    public static ResidenceManager<?> getResidenceManager() {
        return plugin.getResidenceManager();
    }

    public static UserManager<?> getUserManager() {
        return plugin.getUserManager();
    }

    public static DateTimeFormatter getFormatter() {
        return formatter;
    }

    public static String format(@NotNull LocalDateTime time) {
        return time.format(formatter);
    }

    @Contract("null -> null;!null -> !null")
    public static LocalDateTime parse(@Nullable String time) {
        return time == null ? null : LocalDateTime.parse(time, formatter);
    }

}
