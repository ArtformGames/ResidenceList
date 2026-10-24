package com.artformgames.plugin.residencelist.storage;

import cc.carm.lib.easyplugin.EasyPlugin;
import cc.carm.lib.easyplugin.user.UserDataManager;
import com.artformgames.plugin.residencelist.api.residence.ResidenceData;
import com.artformgames.plugin.residencelist.conf.PluginConfig;
import com.artformgames.plugin.residencelist.storage.data.CachedResidenceData;
import com.artformgames.plugin.residencelist.storage.data.CachedUserData;
import com.artformgames.plugin.residencelist.storage.data.ResidenceSnapshot;
import com.artformgames.plugin.residencelist.storage.data.UserSnapshot;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * Synchronous gameplay operations only touch caches. One worker orders all backend IO,
 * including login loads, snapshots, renames, deletions and final shutdown writes.
 */
public abstract class AbstractStorage extends UserDataManager<UUID, CachedUserData>
        implements DataStorage<CachedUserData, CachedResidenceData> {
    private final Object stateLock = new Object();
    private final Map<String, CachedResidenceData> residences = new LinkedHashMap<>();
    private final Map<UUID, Long> generations = new HashMap<>();
    private final Map<UUID, Integer> pendingUsers = new HashMap<>();
    private final Map<String, Integer> pendingResidences = new HashMap<>();
    // Accessed only by the worker. Failed writes stay at the head, so a later delete
    // can never overtake a failed rename or a failed save.
    private final ArrayDeque<IOOperation<Void>> pendingWrites = new ArrayDeque<>();
    private CompletableFuture<Void> initialization;
    private CompletableFuture<Void> termination = CompletableFuture.completedFuture(null);
    private boolean ready;
    private boolean closing;
    private final Thread shutdownHook;

    protected AbstractStorage(@NotNull EasyPlugin plugin) {
        super(plugin, Executors.newSingleThreadExecutor(task -> {
            Thread worker = new Thread(task, plugin.getName() + "-Storage");
            // Let queued final writes finish during an orderly JVM shutdown.
            worker.setDaemon(false);
            return worker;
        }));
        this.shutdownHook = new Thread(() -> {
            shutdown();
            try {
                if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                    getLogger().severe("Storage did not finish saving within the JVM shutdown timeout.");
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }, plugin.getName() + "-StorageShutdown");
    }

    protected abstract void openStorage() throws Exception;
    protected abstract Map<String, ResidenceSnapshot> readResidences() throws Exception;
    protected abstract @Nullable CachedUserData readUser(UUID key) throws Exception;
    protected abstract void writeUsers(List<UserSnapshot> snapshots) throws Exception;
    protected abstract void writeResidences(Map<String, ResidenceSnapshot> snapshots) throws Exception;
    protected abstract void deleteResidence(String name) throws Exception;
    protected void closeStorage() throws Exception { }

    /** Overridable for a test scheduler; live Bukkit objects are attached on the server thread. */
    protected void runOnServerThread(Runnable action) {
        Bukkit.getScheduler().runTask(plugin, action);
    }

    protected @Nullable ClaimedResidence resolveResidence(String name) {
        return Residence.getInstance().getResidenceManager().getByName(name);
    }

    @Override
    public final void initialize() {
        initializeAsync();
    }

    @Override
    public final CompletableFuture<Void> initializeAsync() {
        synchronized (stateLock) {
            if (initialization != null) return initialization;
            if (closing) return CompletableFuture.failedFuture(new IllegalStateException("Storage is closed"));
            initialization = new CompletableFuture<>();
            Runtime.getRuntime().addShutdownHook(shutdownHook);
            submit(() -> {
                openStorage();
                return readResidences();
            }).whenComplete((loaded, failure) -> {
                if (failure != null) {
                    initialization.completeExceptionally(failure);
                    return;
                }
                try {
                    runOnServerThread(() -> {
                        try {
                            synchronized (stateLock) {
                                if (closing) throw new IllegalStateException("Storage closed while loading");
                                for (Map.Entry<String, ResidenceSnapshot> entry : loaded.entrySet()) {
                                    ClaimedResidence residence = resolveResidence(entry.getKey());
                                    if (residence == null) {
                                        if (PluginConfig.SETTINGS.AUTO_REMOVE.getNotNull()) {
                                            queueDelete(entry.getKey());
                                        }
                                    } else {
                                        residences.put(residence.getName(), createResidence(residence, entry.getValue()));
                                    }
                                }
                                ready = true;
                            }
                            initialization.complete(null);
                        } catch (Exception ex) {
                            initialization.completeExceptionally(ex);
                        }
                    });
                } catch (Exception ex) {
                    initialization.completeExceptionally(ex);
                }
            });
            return initialization;
        }
    }

    @Override
    public @NotNull CachedUserData emptyUser(@NotNull UUID key) {
        return new CachedUserData(UserSnapshot.empty(key));
    }

    @Override
    protected @Nullable CachedUserData loadData(@NotNull UUID key) throws Exception {
        return readUser(key);
    }

    @Override
    protected void saveData(@NotNull CachedUserData data) throws Exception {
        UserSnapshot snapshot = data.snapshot();
        writeUsers(List.of(snapshot));
        data.markSaved(snapshot);
    }

    @Override
    public @NotNull CompletableFuture<CachedUserData> load(@NotNull UUID key, @NotNull Supplier<Boolean> condition) {
        synchronized (stateLock) {
            if (!ready || closing) return unavailable();
            long generation = generations.getOrDefault(key, 0L);
            CachedUserData cached = dataCache.get(key);
            if (cached != null) {
                cached.setDropping(false);
                return CompletableFuture.completedFuture(cached);
            }
            return submit(() -> {
                // A preceding load may have populated the cache while this task was queued.
                CachedUserData data = dataCache.get(key);
                if (data == null) {
                    drainWrites();
                    data = readUser(key);
                    if (data == null) data = emptyUser(key);
                }
                synchronized (stateLock) {
                    if (!closing && generations.getOrDefault(key, 0L) == generation && condition.get()) {
                        dataCache.put(key, data);
                    }
                }
                return data;
            });
        }
    }

    @Override
    public @NotNull CompletableFuture<Boolean> save(@NotNull CachedUserData user) {
        synchronized (stateLock) {
            if (!ready || closing) return unavailable();
            return saveUsers(List.of(user)).thenApply(unused -> true);
        }
    }

    @Override
    public @NotNull CompletableFuture<Boolean> unload(@NotNull UUID key, boolean save) {
        synchronized (stateLock) {
            generations.merge(key, 1L, Long::sum);
            CachedUserData data = dataCache.get(key);
            if (data == null) return CompletableFuture.completedFuture(false);
            if (!save || (!data.isDirty() && !pendingUsers.containsKey(key))) {
                dataCache.remove(key, data);
                return CompletableFuture.completedFuture(true);
            }
            if (closing) return unavailable();
            data.setDropping(true);
            return saveUsers(List.of(data)).thenApply(unused -> {
                synchronized (stateLock) {
                    if (data.isDropping()) dataCache.remove(key, data);
                }
                return true;
            });
        }
    }

    @Override
    public @NotNull CompletableFuture<Boolean> modify(@NotNull UUID key, @NotNull Consumer<CachedUserData> consumer) {
        synchronized (stateLock) {
            if (!ready || closing) return unavailable();
            CachedUserData cached = dataCache.get(key);
            if (cached != null) {
                synchronized (cached) { consumer.accept(cached); }
                return CompletableFuture.completedFuture(true);
            }
            return load(key, false).thenCompose(data -> {
                synchronized (data) { consumer.accept(data); }
                return save(data);
            });
        }
    }

    @Override
    public void saveAll() {
        synchronized (stateLock) {
            if (ready && !closing) report(saveUsers(dirtyUsers()));
        }
    }

    @Override
    public int unloadAll(boolean save) {
        synchronized (stateLock) {
            List<UUID> keys = new ArrayList<>(dataCache.keySet());
            keys.forEach(key -> unload(key, save));
            return keys.size();
        }
    }

    private List<CachedUserData> dirtyUsers() {
        return dataCache.values().stream()
                .filter(data -> data.isDirty() || pendingUsers.containsKey(data.key())).toList();
    }

    private CompletableFuture<Void> saveUsers(List<CachedUserData> users) {
        Map<CachedUserData, UserSnapshot> snapshots = new LinkedHashMap<>();
        users.forEach(data -> {
            snapshots.put(data, data.snapshot());
            pendingUsers.merge(data.key(), 1, Integer::sum);
        });
        return persist(() -> {
            if (!snapshots.isEmpty()) writeUsers(List.copyOf(snapshots.values()));
            snapshots.forEach(CachedUserData::markSaved);
            synchronized (stateLock) {
                snapshots.keySet().forEach(data -> decrement(pendingUsers, data.key()));
            }
            return null;
        });
    }

    @Override
    public @NotNull Set<CachedResidenceData> listResidences() {
        synchronized (stateLock) { return Set.copyOf(residences.values()); }
    }

    @Override
    public @Nullable CachedResidenceData getResidence(@NotNull String name) {
        synchronized (stateLock) {
            if (!ready || closing) return null;
            CachedResidenceData existing = residences.get(name);
            if (existing != null) return existing;
            ClaimedResidence residence = resolveResidence(name);
            if (residence == null) return null;
            return residences.computeIfAbsent(residence.getName(), key ->
                    createResidence(residence, ResidenceSnapshot.empty()));
        }
    }

    @Override
    public @NotNull CachedResidenceData loadResidence(String name) {
        CachedResidenceData data = getResidence(name);
        if (data == null) throw new IllegalArgumentException("Residence not available: " + name);
        return data;
    }

    private CachedResidenceData createResidence(ClaimedResidence residence, ResidenceSnapshot snapshot) {
        CachedResidenceData data = new CachedResidenceData(residence, snapshot);
        data.bindPersistence(() -> saveResidence(data));
        return data;
    }

    private String keyOf(CachedResidenceData data) {
        return residences.entrySet().stream().filter(entry -> entry.getValue() == data)
                .map(Map.Entry::getKey).findFirst().orElse(null);
    }

    private void saveResidence(CachedResidenceData data) {
        synchronized (stateLock) {
            String name = keyOf(data);
            if (name != null && ready && !closing) report(saveResidences(Map.of(name, data)));
        }
    }

    @Override
    public boolean updateResidence(@NotNull ResidenceData data, @NotNull Consumer<ResidenceData> consumer) {
        synchronized (stateLock) {
            if (!ready || closing || !(data instanceof CachedResidenceData cached)) return false;
            String name = keyOf(cached);
            if (name == null) return false;
            synchronized (data) { consumer.accept(data); }
            report(saveResidences(Map.of(name, residences.get(name))));
            return true;
        }
    }

    @Override
    public void renameResidence(String oldName, String newName) {
        synchronized (stateLock) {
            if (!ready || closing || oldName.equals(newName)) return;
            CachedResidenceData data = residences.remove(oldName);
            if (data == null) return;
            residences.put(newName, data);
            ResidenceSnapshot snapshot = data.snapshot();
            pendingResidences.merge(newName, 1, Integer::sum);
            report(persist(() -> {
                writeResidences(Map.of(newName, snapshot));
                deleteResidence(oldName);
                data.markSaved(snapshot);
                synchronized (stateLock) { decrement(pendingResidences, newName); }
                return null;
            }));
        }
    }

    @Override
    public void removeResidence(@NotNull String name) {
        synchronized (stateLock) {
            if (!ready || closing) return;
            residences.remove(name);
            queueDelete(name);
        }
    }

    private void queueDelete(String name) {
        report(persist(() -> {
            deleteResidence(name);
            return null;
        }));
    }

    private Map<String, CachedResidenceData> dirtyResidences() {
        Map<String, CachedResidenceData> dirty = new LinkedHashMap<>();
        residences.forEach((name, data) -> {
            if (data.isDirty() || pendingResidences.containsKey(name)) {
                dirty.put(name, data);
            }
        });
        return dirty;
    }

    private CompletableFuture<Void> saveResidences(Map<String, CachedResidenceData> data) {
        Map<String, ResidenceSnapshot> snapshots = new LinkedHashMap<>();
        data.forEach((name, value) -> {
            snapshots.put(name, value.snapshot());
            pendingResidences.merge(name, 1, Integer::sum);
        });
        return persist(() -> {
            if (!snapshots.isEmpty()) writeResidences(snapshots);
            data.forEach((name, value) -> value.markSaved(snapshots.get(name)));
            synchronized (stateLock) {
                snapshots.keySet().forEach(name -> decrement(pendingResidences, name));
            }
            return null;
        });
    }

    @Override
    public void saveAllResidences() {
        synchronized (stateLock) {
            if (ready && !closing) report(saveResidences(dirtyResidences()));
        }
    }

    @Override
    public CompletableFuture<Void> flush() {
        synchronized (stateLock) {
            if (closing) return termination;
            if (!ready) return unavailable();
            return flushQueued();
        }
    }

    private CompletableFuture<Void> flushQueued() {
        report(saveUsers(dirtyUsers()));
        report(saveResidences(dirtyResidences()));
        // The barrier reflects the final result, including successful retries of older failures.
        return submit(() -> {
            drainWrites();
            return null;
        });
    }

    @Override
    public void shutdown() {
        synchronized (stateLock) {
            if (closing) return;
            closing = true;
            CompletableFuture<Void> writes = ready ? flushQueued() : CompletableFuture.completedFuture(null);
            CompletableFuture<Void> close = submit(() -> {
                try {
                    drainWrites();
                } finally {
                    try {
                        closeStorage();
                    } finally {
                        try {
                            Runtime.getRuntime().removeShutdownHook(shutdownHook);
                        } catch (IllegalStateException ignored) {
                            // JVM shutdown already started; the hook waits off the server thread.
                        }
                    }
                }
                return null;
            });
            termination = CompletableFuture.allOf(writes, close);
            report(termination);
            // Do not wait on the Bukkit thread. The independent worker drains before closing the pool.
            executor.shutdown();
            if (initialization != null && !initialization.isDone()) {
                initialization.completeExceptionally(new IllegalStateException("Storage closed while loading"));
            }
        }
    }

    private static <K> void decrement(Map<K, Integer> counts, K key) {
        counts.computeIfPresent(key, (ignored, count) -> count <= 1 ? null : count - 1);
    }

    private <T> CompletableFuture<T> unavailable() {
        return CompletableFuture.failedFuture(new IllegalStateException("Storage is not ready or is closing"));
    }

    private CompletableFuture<Void> persist(IOOperation<Void> operation) {
        return submit(() -> {
            pendingWrites.addLast(operation);
            drainWrites();
            return null;
        });
    }

    private void drainWrites() throws Exception {
        while (!pendingWrites.isEmpty()) {
            pendingWrites.getFirst().run();
            pendingWrites.removeFirst();
        }
    }

    private <T> CompletableFuture<T> submit(IOOperation<T> operation) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return operation.run();
            } catch (Exception ex) {
                throw new CompletionException(ex);
            }
        }, executor);
    }

    private void report(CompletableFuture<?> future) {
        future.whenComplete((ignored, error) -> {
            if (error != null) getLogger().log(Level.SEVERE, "Storage operation failed; unsaved data remains dirty.", error);
        });
    }

    @FunctionalInterface
    private interface IOOperation<T> {
        T run() throws Exception;
    }
}
