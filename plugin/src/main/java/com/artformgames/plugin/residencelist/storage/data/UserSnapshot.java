package com.artformgames.plugin.residencelist.storage.data;

import com.artformgames.plugin.residencelist.api.sort.SortFunctions;

import java.util.List;
import java.util.UUID;

/** An immutable value transferred from the cache to the storage worker. */
public record UserSnapshot(UUID key, List<String> pinned, SortFunctions sortFunction, boolean reversed) {
    public UserSnapshot {
        pinned = List.copyOf(pinned);
    }

    public static UserSnapshot empty(UUID key) {
        return new UserSnapshot(key, List.of(), SortFunctions.NAME, false);
    }

    public boolean isEmpty() {
        return pinned.isEmpty() && sortFunction == SortFunctions.NAME && !reversed;
    }
}
