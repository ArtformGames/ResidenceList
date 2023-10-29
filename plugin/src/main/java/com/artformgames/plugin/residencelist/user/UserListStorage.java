package com.artformgames.plugin.residencelist.user;

import cc.carm.lib.easyplugin.user.UserData;
import com.artformgames.plugin.residencelist.api.user.UserListData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class UserListStorage extends UserData<UUID> implements UserListData {

    protected @NotNull ArrayList<String> pined;

    public UserListStorage(@NotNull UUID key, @NotNull ArrayList<String> pined) {
        super(key);
        this.pined = pined;
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

}
