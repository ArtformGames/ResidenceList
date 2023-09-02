package com.artformgames.plugin.residencelist.api.residence;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ResidenceInformation {

    @NotNull ClaimedResidence getResidence();

    @NotNull Material getDisplayIcon();

    void setDisplayIcon(@NotNull Material material);

    @NotNull String getDisplayName();

    void setDisplayName(@NotNull String name);

    @NotNull List<String> getDescription();

    void setDescription(@NotNull List<String> description);

    boolean isPublicDisplayed();

    void setPublicDisplayed(boolean displayed);

    Map<UUID, ResidenceRate> getRates();

    void addRate(ResidenceRate rate);

    default void addRate(List<String> content, boolean recommend, UUID author, LocalDateTime time) {
        addRate(new ResidenceRate(content, recommend, author, time));
    }

    default void addRate(List<String> content, boolean recommend, UUID author) {
        addRate(content, recommend, author, LocalDateTime.now());
    }

    void removeRate(UUID author);

    void save();

    default String getName() {
        return getResidence().getName();
    }

    default String getOwner() {
        return getResidence().getOwner();
    }

    default boolean isOwner(@NotNull Player player) {
        return getResidence().isOwner(player);
    }

    default @Nullable Location getTeleportLocation(Player player) {
        return getResidence().getTeleportLocation(player, false);
    }


}
