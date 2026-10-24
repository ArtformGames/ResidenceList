package com.artformgames.plugin.residencelist.storage;

import cc.carm.lib.easyplugin.EasyPlugin;
import com.artformgames.plugin.residencelist.api.sort.SortFunctions;
import com.artformgames.plugin.residencelist.storage.data.CachedResidenceData;
import com.artformgames.plugin.residencelist.storage.data.CachedUserData;
import com.artformgames.plugin.residencelist.storage.data.ResidenceSnapshot;
import com.artformgames.plugin.residencelist.storage.data.UserSnapshot;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class AbstractStorageTest {
    private TestStorage storage;

    @Before
    public void setUp() {
        EasyPlugin plugin = mock(EasyPlugin.class);
        when(plugin.getName()).thenReturn("StorageTest");
        when(plugin.getLogger()).thenReturn(Logger.getLogger("StorageTest"));
        storage = new TestStorage(plugin);
    }

    @After
    public void tearDown() throws Exception {
        storage.releaseAll();
        storage.shutdown();
        try { await(storage.flush()); } catch (ExecutionException ignored) { /* failure cases intentionally leave dirty writes */ }
    }

    @Test
    public void hooksRunOnWorkerAndShutdownClosesAfterWrites() throws Exception {
        Thread caller = Thread.currentThread();
        storage.claim("home");
        await(storage.initializeAsync());
        CachedUserData user = await(storage.load(UUID.randomUUID(), true));
        user.setSortFunction(SortFunctions.SIZE);
        storage.saveAll();
        CachedResidenceData residence = storage.getResidence("home");
        assertNotNull(residence);
        residence.setNickname("New name");
        residence.save();
        storage.renameResidence("home", "new-home");
        storage.removeResidence("new-home");
        storage.shutdown();
        await(storage.flush());
        assertTrue(storage.events.contains("close"));
        assertEquals("close", storage.events.get(storage.events.size() - 1));
        for (Thread thread : storage.ioThreads) assertNotSame(caller, thread);
    }

    @Test
    public void queuedSaveCapturesImmutableSnapshotAndLaterRevertWins() throws Exception {
        await(storage.initializeAsync());
        UUID key = UUID.randomUUID();
        CachedUserData user = await(storage.load(key, true));
        storage.pauseFirstUserWrite();
        user.setSortFunction(SortFunctions.RATINGS);
        CompletableFuture<Boolean> first = storage.save(user);
        assertTrue(storage.userWriteStarted.await(5, TimeUnit.SECONDS));
        user.setSortFunction(SortFunctions.NAME);
        storage.saveAll();
        storage.userWriteRelease.countDown();
        await(first);
        await(storage.flush());
        assertEquals(SortFunctions.RATINGS, storage.userWrites.get(0).sortFunction());
        assertEquals(SortFunctions.NAME, storage.userWrites.get(storage.userWrites.size() - 1).sortFunction());
        assertNull(storage.diskUsers.get(key));
    }

    @Test
    public void failedRenameKeepsOldCopyUntilNewCopyPersists() throws Exception {
        ResidenceSnapshot original = new ResidenceSnapshot(null, -1, "Original", List.of(), Map.of());
        storage.diskResidences.put("home", original);
        storage.claim("home");
        await(storage.initializeAsync());
        storage.failNextResidenceWrite.set(true);
        storage.renameResidence("home", "renamed");
        assertTrue(storage.failedResidenceWrite.await(5, TimeUnit.SECONDS));
        assertEquals(original, storage.diskResidences.get("home"));
        assertFalse(storage.diskResidences.containsKey("renamed"));
        await(storage.flush());
        assertFalse(storage.diskResidences.containsKey("home"));
        assertEquals(original, storage.diskResidences.get("renamed"));
    }

    @Test
    public void removedResidenceCannotBeResurrectedByDetachedSave() throws Exception {
        storage.claim("home");
        await(storage.initializeAsync());
        CachedResidenceData residence = storage.getResidence("home");
        residence.setNickname("Saved");
        residence.save();
        await(storage.flush());
        storage.removeResidence("home");
        await(storage.flush());
        residence.setNickname("Detached");
        residence.save();
        await(storage.flush());
        assertFalse(storage.diskResidences.containsKey("home"));
    }

    @Test
    public void pendingLoadUnloadedBeforeCompletionDoesNotEnterCache() throws Exception {
        await(storage.initializeAsync());
        UUID key = UUID.randomUUID();
        storage.pauseNextUserRead();
        CompletableFuture<CachedUserData> loading = storage.load(key, true);
        assertTrue(storage.userReadStarted.await(5, TimeUnit.SECONDS));
        assertFalse(await(storage.unload(key, false)));
        storage.userReadRelease.countDown();
        await(loading);
        assertNull(storage.getNullable(key));
    }

    @Test
    public void failedUserLoadDoesNotCacheDefault() throws Exception {
        await(storage.initializeAsync());
        UUID key = UUID.randomUUID();
        storage.failNextUserRead.set(true);
        try {
            await(storage.load(key, true));
            fail("Backend read failure must reach the caller");
        } catch (ExecutionException expected) {
            assertNull(storage.getNullable(key));
        }
        assertNotNull(await(storage.load(key, true)));
    }

    @Test
    public void shutdownReturnsBeforePausedWriteAndClosesAfterRelease() throws Exception {
        await(storage.initializeAsync());
        CachedUserData user = await(storage.load(UUID.randomUUID(), true));
        user.setSortFunction(SortFunctions.SIZE);
        storage.pauseFirstUserWrite();
        CompletableFuture<Boolean> saving = storage.save(user);
        assertTrue(storage.userWriteStarted.await(5, TimeUnit.SECONDS));

        CountDownLatch shutdownReturned = new CountDownLatch(1);
        Thread caller = new Thread(() -> {
            storage.shutdown();
            shutdownReturned.countDown();
        }, "shutdown-caller");
        caller.start();
        assertTrue("shutdown must not wait for disk IO", shutdownReturned.await(5, TimeUnit.SECONDS));
        assertFalse("termination must wait for disk IO", storage.flush().isDone());
        assertFalse(storage.events.contains("close"));

        storage.userWriteRelease.countDown();
        await(saving);
        await(storage.flush());
        assertEquals("close", storage.events.get(storage.events.size() - 1));
        caller.join(5000);
    }

    @Test
    public void unchangedCachedUserDoesNotOverwriteNewerDiskStateAtShutdown() throws Exception {
        UUID key = UUID.randomUUID();
        UserSnapshot original = new UserSnapshot(key, List.of("Home"), SortFunctions.NAME, false);
        UserSnapshot newer = new UserSnapshot(key, List.of("Home"), SortFunctions.SIZE, true);
        storage.diskUsers.put(key, original);
        await(storage.initializeAsync());
        assertEquals(original, await(storage.load(key, true)).snapshot());
        storage.diskUsers.put(key, newer); // Represents a change made by another server.

        storage.shutdown();
        await(storage.flush());
        assertEquals(newer, storage.diskUsers.get(key));
    }

    private static <T> T await(CompletableFuture<T> future) throws Exception {
        return future.get(5, TimeUnit.SECONDS);
    }

    private static final class TestStorage extends AbstractStorage {
        final Map<String, ResidenceSnapshot> diskResidences = new ConcurrentHashMap<>();
        final Map<UUID, UserSnapshot> diskUsers = new ConcurrentHashMap<>();
        final Map<String, ClaimedResidence> claims = new ConcurrentHashMap<>();
        final List<String> events = java.util.Collections.synchronizedList(new ArrayList<>());
        final List<Thread> ioThreads = java.util.Collections.synchronizedList(new ArrayList<>());
        final List<UserSnapshot> userWrites = java.util.Collections.synchronizedList(new ArrayList<>());
        final AtomicBoolean failNextResidenceWrite = new AtomicBoolean();
        final AtomicBoolean failNextUserRead = new AtomicBoolean();
        final AtomicBoolean blockNextUserWrite = new AtomicBoolean();
        final AtomicBoolean blockNextUserRead = new AtomicBoolean();
        final CountDownLatch failedResidenceWrite = new CountDownLatch(1);
        final CountDownLatch userWriteStarted = new CountDownLatch(1);
        final CountDownLatch userWriteRelease = new CountDownLatch(1);
        final CountDownLatch userReadStarted = new CountDownLatch(1);
        final CountDownLatch userReadRelease = new CountDownLatch(1);

        TestStorage(EasyPlugin plugin) { super(plugin); }

        void claim(String name) {
            try {
                // Residence's constructors require a running server and CMILib. Only getName is used here.
                Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
                Field field = unsafeClass.getDeclaredField("theUnsafe");
                field.setAccessible(true);
                Object unsafe = field.get(null);
                ClaimedResidence claim = (ClaimedResidence) unsafeClass
                        .getMethod("allocateInstance", Class.class).invoke(unsafe, ClaimedResidence.class);
                claim.setName(name);
                claims.put(name, claim);
            } catch (ReflectiveOperationException e) {
                throw new AssertionError("Could not create inert residence", e);
            }
        }

        void pauseFirstUserWrite() { blockNextUserWrite.set(true); }
        void pauseNextUserRead() { blockNextUserRead.set(true); }
        void releaseAll() { userWriteRelease.countDown(); userReadRelease.countDown(); }
        private void io(String event) { ioThreads.add(Thread.currentThread()); events.add(event); }

        @Override protected void runOnServerThread(Runnable action) { action.run(); }
        @Override protected ClaimedResidence resolveResidence(String name) { return claims.get(name); }
        @Override protected void openStorage() { io("open"); }
        @Override protected Map<String, ResidenceSnapshot> readResidences() { io("read-residences"); return Map.copyOf(diskResidences); }
        @Override protected CachedUserData readUser(UUID key) throws Exception {
            io("read-user");
            if (blockNextUserRead.getAndSet(false)) {
                userReadStarted.countDown();
                if (!userReadRelease.await(5, TimeUnit.SECONDS)) throw new AssertionError("Timed out reading user");
            }
            if (failNextUserRead.getAndSet(false)) throw new IllegalStateException("read failed");
            UserSnapshot snapshot = diskUsers.get(key);
            return snapshot == null ? null : new CachedUserData(snapshot);
        }
        @Override protected void writeUsers(List<UserSnapshot> snapshots) throws Exception {
            io("write-users");
            if (blockNextUserWrite.getAndSet(false)) {
                userWriteStarted.countDown();
                if (!userWriteRelease.await(5, TimeUnit.SECONDS)) throw new AssertionError("Timed out writing user");
            }
            for (UserSnapshot snapshot : snapshots) {
                userWrites.add(snapshot);
                if (snapshot.isEmpty()) diskUsers.remove(snapshot.key());
                else diskUsers.put(snapshot.key(), snapshot);
            }
        }
        @Override protected void writeResidences(Map<String, ResidenceSnapshot> snapshots) {
            io("write-residences");
            if (failNextResidenceWrite.getAndSet(false)) {
                failedResidenceWrite.countDown();
                throw new IllegalStateException("write failed");
            }
            diskResidences.putAll(snapshots);
        }
        @Override protected void deleteResidence(String name) { io("delete-residence"); diskResidences.remove(name); }
        @Override protected void closeStorage() { io("close"); }
    }
}
