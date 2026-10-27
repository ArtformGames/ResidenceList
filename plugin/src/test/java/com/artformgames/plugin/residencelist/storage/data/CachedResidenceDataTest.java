package com.artformgames.plugin.residencelist.storage.data;

import com.artformgames.plugin.residencelist.api.residence.ResidenceVisitor;
import org.bukkit.entity.Player;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CachedResidenceDataTest {

    @Test
    public void repeatVisitUpdatesNameAndLatestTimeWithoutAddingAnotherVisitor() {
        CachedResidenceData data = new CachedResidenceData(null, ResidenceSnapshot.empty());
        UUID uuid = UUID.randomUUID();
        LocalDateTime first = LocalDateTime.of(2026, 1, 1, 12, 0);
        LocalDateTime later = first.plusDays(1);

        data.recordVisit(uuid, "OldName", first);
        data.recordVisit(uuid, "NewName", later);

        assertEquals(1, data.getVisitorCount());
        assertEquals(new ResidenceVisitor(uuid, "NewName", later), data.getVisitors().get(0));
        assertTrue(data.isDirty());
    }

    @Test
    public void delayedOlderVisitCannotReplaceNewerVisit() {
        CachedResidenceData data = new CachedResidenceData(null, ResidenceSnapshot.empty());
        UUID uuid = UUID.randomUUID();
        LocalDateTime recent = LocalDateTime.of(2026, 2, 2, 12, 0);

        data.recordVisit(uuid, "Current", recent);
        data.recordVisit(uuid, "Stale", recent.minusHours(1));

        assertEquals(List.of(new ResidenceVisitor(uuid, "Current", recent)), data.getVisitors());
    }

    @Test
    public void visitorViewsAndSnapshotsAreImmutableCopies() {
        UUID firstId = UUID.randomUUID();
        ResidenceVisitor first = new ResidenceVisitor(firstId, "First", LocalDateTime.of(2026, 3, 3, 12, 0));
        List<ResidenceVisitor> original = new ArrayList<>(List.of(first));
        ResidenceSnapshot snapshot = new ResidenceSnapshot(null, -1, null, List.of(), Map.of(), original);
        original.clear();
        assertEquals(List.of(first), snapshot.visitors());

        CachedResidenceData data = new CachedResidenceData(null, snapshot);
        List<ResidenceVisitor> view = data.getVisitors();
        ResidenceSnapshot beforeVisit = data.snapshot();
        assertThrows(UnsupportedOperationException.class, () -> view.clear());
        assertThrows(UnsupportedOperationException.class, () -> beforeVisit.visitors().clear());

        data.recordVisit(UUID.randomUUID(), "Second", first.lastTime().plusDays(1));
        assertEquals(List.of(first), view);
        assertEquals(List.of(first), beforeVisit.visitors());
        assertEquals(2, data.getVisitorCount());
    }

    @Test
    public void visitorsSinceIncludesExactBoundary() {
        CachedResidenceData data = new CachedResidenceData(null, ResidenceSnapshot.empty());
        LocalDateTime boundary = LocalDateTime.of(2026, 4, 4, 12, 0);
        UUID before = UUID.randomUUID();
        UUID exact = UUID.randomUUID();
        UUID after = UUID.randomUUID();
        data.recordVisit(before, "Before", boundary.minusNanos(1));
        data.recordVisit(exact, "Exact", boundary);
        data.recordVisit(after, "After", boundary.plusNanos(1));

        assertEquals(List.of(exact, after), data.getVisitorsSince(boundary).stream()
                .map(ResidenceVisitor::uuid).toList());
    }

    @Test
    public void playerHelperSkipsResidenceOwner() {
        Player owner = mock(Player.class);
        Player guest = mock(Player.class);
        when(guest.getUniqueId()).thenReturn(UUID.randomUUID());
        when(guest.getName()).thenReturn("Guest");
        CachedResidenceData data = new CachedResidenceData(null, ResidenceSnapshot.empty()) {
            @Override public boolean isOwner(Player player) { return player == owner; }
        };

        data.recordVisit(owner);
        assertEquals(0, data.getVisitorCount());
        data.recordVisit(guest);
        assertEquals(1, data.getVisitorCount());
        assertEquals("Guest", data.getVisitors().get(0).name());
    }
}
