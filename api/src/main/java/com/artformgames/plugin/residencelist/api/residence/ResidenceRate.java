package com.artformgames.plugin.residencelist.api.residence;

import com.artformgames.plugin.residencelist.ResidenceListAPI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public record ResidenceRate(
        @NotNull UUID author, @NotNull String content,
        boolean recommend, @NotNull LocalDateTime time
) {

    public @Nullable String getAuthorName() {
        return Bukkit.getOfflinePlayer(author).getName();
    }


    public Map<String, Object> serialize() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("time", ResidenceListAPI.format(time));
        data.put("recommend", recommend);
        data.put("content", content);
        return data;
    }

    public static @NotNull ResidenceRate deserialize(@NotNull ConfigurationSection section) {
        return new ResidenceRate(
                UUID.fromString(section.getName()),
                section.getString("content", ""),
                section.getBoolean("recommend"),
                Optional.ofNullable(section.getString("time"))
                        .map(ResidenceListAPI::parse).orElse(LocalDateTime.now())
        );
    }

    public static @NotNull Map<UUID, ResidenceRate> loadFrom(@Nullable ConfigurationSection root) {
        Map<UUID, ResidenceRate> rates = new LinkedHashMap<>();
        if (root == null) return rates;

        for (String key : root.getKeys(false)) {
            ConfigurationSection section = root.getConfigurationSection(key);
            if (section == null) continue;
            try {
                rates.put(UUID.fromString(key), deserialize(section));
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        return rates;
    }

}
