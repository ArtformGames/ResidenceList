package com.artformgames.plugin.residencelist.storage;

import cc.carm.lib.easyplugin.user.UserDataRegistry;
import com.artformgames.plugin.residencelist.api.ResidenceManager;
import com.artformgames.plugin.residencelist.api.residence.ResidenceData;
import com.artformgames.plugin.residencelist.api.user.UserListData;

import java.util.UUID;

public interface DataStorage<U extends UserListData, D extends ResidenceData>
        extends UserDataRegistry<UUID, U>, ResidenceManager<D> {

    void initialize();

    void shutdown();

}
