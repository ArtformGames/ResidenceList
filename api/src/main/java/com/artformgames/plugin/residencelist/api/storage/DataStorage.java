package com.artformgames.plugin.residencelist.api.storage;

import com.artformgames.plugin.residencelist.api.ResidenceManager;
import com.artformgames.plugin.residencelist.api.UserManager;
import com.artformgames.plugin.residencelist.api.residence.ResidenceData;
import com.artformgames.plugin.residencelist.api.user.UserListData;

public interface DataStorage<U extends UserListData, D extends ResidenceData>
        extends UserManager<U>, ResidenceManager<D> {

    void initialize();

    void shutdown();

}
