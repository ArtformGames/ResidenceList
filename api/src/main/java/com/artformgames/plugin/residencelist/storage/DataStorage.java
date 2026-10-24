package com.artformgames.plugin.residencelist.storage;

import cc.carm.lib.easyplugin.user.UserDataRegistry;
import com.artformgames.plugin.residencelist.api.ResidenceManager;
import com.artformgames.plugin.residencelist.api.residence.ResidenceData;
import com.artformgames.plugin.residencelist.api.user.UserListData;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface DataStorage<U extends UserListData, D extends ResidenceData>
        extends UserDataRegistry<UUID, U>, ResidenceManager<D> {

    void initialize();

    /** Completes when persisted data is available to synchronous cache readers. */
    default CompletableFuture<Void> initializeAsync() {
        initialize();
        return CompletableFuture.completedFuture(null);
    }

    /** Completes after all previously queued writes. Never wait for this on the server thread. */
    default CompletableFuture<Void> flush() {
        saveAll();
        saveAllResidences();
        return CompletableFuture.completedFuture(null);
    }

    void shutdown();

}
