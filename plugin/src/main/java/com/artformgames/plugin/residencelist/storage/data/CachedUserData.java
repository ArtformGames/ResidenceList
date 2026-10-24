package com.artformgames.plugin.residencelist.storage.data;

import cc.carm.lib.easyplugin.user.AbstractUserData;
import com.artformgames.plugin.residencelist.api.sort.SortFunctions;
import com.artformgames.plugin.residencelist.api.user.UserListData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** User operations only change memory; the storage owns persistence. */
public class CachedUserData extends AbstractUserData<UUID> implements UserListData {
    private final List<String> pinned;
    private SortFunctions sortFunction;
    private boolean reversed;
    private UserSnapshot saved;

    public CachedUserData(UserSnapshot data) {
        super(data.key());
        this.pinned = new ArrayList<>(data.pinned());
        this.sortFunction = data.sortFunction();
        this.reversed = data.reversed();
        this.saved = data;
    }

    public synchronized UserSnapshot snapshot() {
        return new UserSnapshot(key(), pinned, sortFunction, reversed);
    }

    public synchronized boolean isDirty() {
        return !snapshot().equals(saved);
    }

    public synchronized void markSaved(UserSnapshot snapshot) {
        this.saved = snapshot;
    }

    @Override
    public synchronized @NotNull List<String> getPinned() {
        return List.copyOf(pinned);
    }

    @Override
    public synchronized boolean isPinned(@NotNull String residence) {
        return pinned.stream().anyMatch(residence::equalsIgnoreCase);
    }

    @Override
    public synchronized void setPin(@NotNull String residence, int index) {
        pinned.removeIf(residence::equalsIgnoreCase);
        if (index >= 0) pinned.add(Math.min(index, pinned.size()), residence);
    }

    @Override
    public synchronized @NotNull SortFunctions getSortFunction() {
        return sortFunction;
    }

    @Override
    public synchronized boolean isSortReversed() {
        return reversed;
    }

    @Override
    public synchronized void setSortFunction(@NotNull SortFunctions function) {
        this.sortFunction = Objects.requireNonNull(function);
    }

    @Override
    public synchronized void setSortReversed(boolean reversed) {
        this.reversed = reversed;
    }

    @Override
    public synchronized void setDropping(boolean dropping) {
        super.setDropping(dropping);
    }

    @Override
    public synchronized boolean isDropping() {
        return super.isDropping();
    }
}
