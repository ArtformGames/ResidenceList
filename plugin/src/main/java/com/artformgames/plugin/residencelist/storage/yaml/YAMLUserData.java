package com.artformgames.plugin.residencelist.storage.yaml;

import cc.carm.lib.easyplugin.user.UserData;
import com.artformgames.plugin.residencelist.api.sort.SortFunctions;
import com.artformgames.plugin.residencelist.api.user.UserListData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class YAMLUserData extends UserData<UUID> implements UserListData {

    protected @NotNull ArrayList<String> pined;
    protected @NotNull SortFunctions sortFunction;
    protected boolean reversed;

    public YAMLUserData(@NotNull UUID key, @NotNull ArrayList<String> pined,
                        @NotNull SortFunctions sort, boolean reversed) {
        super(key);
        this.pined = pined;
        this.sortFunction = sort;
        this.reversed = reversed;
    }

    @Override
    public @NotNull List<String> getPinned() {
        return Collections.unmodifiableList(this.pined);
    }

    @Override
    public boolean isPinned(@NotNull String residence) {
        return this.pined.stream().anyMatch(residence::equalsIgnoreCase);
    }

    @Override
    public void setPin(@NotNull String residence, int index) {
        this.pined.remove(residence);
        if (index < 0) return;

        if (index < this.pined.size()) {
            this.pined.add(index, residence);
        } else {
            this.pined.add(residence);
        }
    }

    @Override
    public @NotNull SortFunctions getSortFunction() {
        return this.sortFunction;
    }

    @Override
    public boolean isSortReversed() {
        return this.reversed;
    }

    @Override
    public void setSortFunction(@NotNull SortFunctions function) {
        this.sortFunction = function;
    }

    @Override
    public void setSortReversed(boolean reversed) {
        this.reversed = reversed;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
