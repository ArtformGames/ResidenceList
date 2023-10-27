package com.artformgames.plugin.residencelist;

import com.artformgames.plugin.residencelist.api.ResidenceManager;
import com.artformgames.plugin.residencelist.api.UserManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ResidenceListAPI {

    protected static ResidenceListPlugin plugin;
    protected static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private ResidenceListAPI() {
    }

    public static void openGUI(Player player) {
        plugin.openGUI(player);
    }

    public static ResidenceManager getResidenceManager() {
        return plugin.getResidenceManager();
    }

    public static UserManager getUserManager() {
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
