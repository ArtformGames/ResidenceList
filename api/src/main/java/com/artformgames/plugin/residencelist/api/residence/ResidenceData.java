package com.artformgames.plugin.residencelist.api.residence;

import com.artformgames.plugin.residencelist.ResidenceListAPI;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface ResidenceData {

    @NotNull ClaimedResidence getResidence();

    @Nullable Material getIconMaterial();

    void setIconMaterial(@NotNull Material material);

    default @NotNull String getDisplayName() {
        return Optional.ofNullable(getAliasName()).orElse(getName());
    }

    @Nullable String getAliasName();

    void setNickname(@NotNull String name);

    @Unmodifiable
    @NotNull List<String> getDescription();

    void setDescription(@NotNull List<String> description);

    default void setDescription(@NotNull String... descriptions) {
        setDescription(List.of(descriptions));
    }

    boolean isPublicDisplayed();

    void setPublicDisplayed(boolean publicDisplayed);

    Map<UUID, ResidenceRate> getRates();

    void setRates(Map<UUID, ResidenceRate> rates);

    void setRate(UUID uuid, ResidenceRate rate);

    default void addRate(ResidenceRate rate) {
        setRate(rate.author(), rate);
    }

    default void addRate(String content, boolean recommend, UUID author, LocalDateTime time) {
        addRate(new ResidenceRate(author, content, recommend, time));
    }

    default void addRate(String content, boolean recommend, UUID author) {
        addRate(content, recommend, author, LocalDateTime.now());
    }

    void removeRate(UUID author);

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
        return getTeleportLocation(player, null);
    }

    @Contract("_,!null->!null")
    default @Nullable Location getTeleportLocation(Player player, Location defaults) {
        return Optional.ofNullable(getResidence().getTeleportLocation(player, false)).orElse(defaults);
    }

    default int countRate(Predicate<ResidenceRate> predicate) {
        return (int) getRates().values().stream().filter(predicate).count();
    }

    default double rateRatio(Predicate<ResidenceRate> predicate) {
        return getRates().isEmpty() ? 0 : (double) countRate(predicate) / getRates().size();
    }

    default boolean canTeleport(Player player) {
        return isOwner(player) || checkPermission(player, Flags.tp, true);
    }

    default boolean checkPermission(Player player, Flags flags, boolean defaults) {
        return getResidence().getPermissions().playerHas(player, flags, defaults);
    }


    void renameTo(@NotNull File newFile) throws Exception;

    void save() throws Exception;

    default void modify(Consumer<ResidenceData> modifier) {
        ResidenceListAPI.getResidenceManager().updateResidence(this, modifier);
    }

}
