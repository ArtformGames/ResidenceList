package com.artformgames.plugin.residencelist.storage.data;

import com.artformgames.plugin.residencelist.api.residence.ResidenceRate;
import com.artformgames.plugin.residencelist.api.residence.ResidenceVisitor;
import org.bukkit.Material;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Contains no live Bukkit objects and can safely be persisted on a worker. */
public record ResidenceSnapshot(Material icon, int customModelData, String aliasName,
                                List<String> description, Map<UUID, ResidenceRate> rates,
                                List<ResidenceVisitor> visitors) {
    public ResidenceSnapshot {
        description = List.copyOf(description);
        rates = Collections.unmodifiableMap(new LinkedHashMap<>(rates));
        visitors = List.copyOf(visitors);
    }

    public ResidenceSnapshot(Material icon, int customModelData, String aliasName,
                             List<String> description, Map<UUID, ResidenceRate> rates) {
        this(icon, customModelData, aliasName, description, rates, List.of());
    }

    public static ResidenceSnapshot empty() {
        return new ResidenceSnapshot(null, -1, null, List.of(), Map.of());
    }
}
