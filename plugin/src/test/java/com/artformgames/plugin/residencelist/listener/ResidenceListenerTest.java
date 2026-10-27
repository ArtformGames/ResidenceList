package com.artformgames.plugin.residencelist.listener;

import com.artformgames.plugin.residencelist.api.ResidenceManager;
import com.artformgames.plugin.residencelist.api.residence.ResidenceData;
import com.bekvon.bukkit.residence.event.ResidenceChangedEvent;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.entity.Player;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.mockito.Mockito.*;

public class ResidenceListenerTest {

    @Test
    public void enteringTopLevelResidenceRecordsVisitWithoutSaving() throws Exception {
        ClaimedResidence home = residence("home", null);
        Player player = mock(Player.class);
        ResidenceManager<ResidenceData> manager = mock(ResidenceManager.class);
        ResidenceData data = mock(ResidenceData.class);
        when(manager.getResidence("home")).thenReturn(data);

        ResidenceListener.recordEntry(change(null, home, player), manager);

        verify(data).recordVisit(player);
        verify(data, never()).save();
    }

    @Test
    public void crossingSubzonesOfSameResidenceIsNotAnotherVisit() {
        ClaimedResidence home = residence("home", null);
        ClaimedResidence first = residence("home.first", home);
        ClaimedResidence second = residence("home.second", home);
        ResidenceManager<ResidenceData> manager = mock(ResidenceManager.class);

        ResidenceListener.recordEntry(change(first, second, mock(Player.class)), manager);

        verifyNoInteractions(manager);
    }

    @Test
    public void enteringSubzoneRecordsVisitForTopLevelResidence() {
        ClaimedResidence home = residence("home", null);
        ClaimedResidence subzone = residence("home.subzone", home);
        Player player = mock(Player.class);
        ResidenceManager<ResidenceData> manager = mock(ResidenceManager.class);
        ResidenceData data = mock(ResidenceData.class);
        when(manager.getResidence("home")).thenReturn(data);

        ResidenceListener.recordEntry(change(null, subzone, player), manager);

        verify(data).recordVisit(player);
    }

    @Test
    public void leavingResidenceOrUnknownDestinationDoesNotRecord() {
        ResidenceManager<ResidenceData> manager = mock(ResidenceManager.class);
        ResidenceListener.recordEntry(change(residence("home", null), null, mock(Player.class)), manager);
        ResidenceListener.recordEntry(change(null, residence("unknown", null), null), manager);

        verifyNoInteractions(manager);
    }

    private static ClaimedResidence residence(String name, ClaimedResidence parent) {
        try {
            // Residence constructors require a running server. The fixture only exposes the two
            // methods the listener needs and does not inspect Residence's CMILib-backed fields.
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            Field instanceField = unsafeClass.getDeclaredField("theUnsafe");
            instanceField.setAccessible(true);
            Object unsafe = instanceField.get(null);
            TestResidence residence = (TestResidence) unsafeClass
                    .getMethod("allocateInstance", Class.class).invoke(unsafe, TestResidence.class);
            residence.name = name;
            residence.top = parent == null ? residence : parent.getTopParent();
            return residence;
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Could not create inert residence", e);
        }
    }

    private static ResidenceChangedEvent change(ClaimedResidence from, ClaimedResidence to, Player player) {
        return new ResidenceChangedEvent(from, to, player);
    }

    private static final class TestResidence extends ClaimedResidence {
        private String name;
        private ClaimedResidence top;

        @Override public String getName() { return name; }
        @Override public ClaimedResidence getTopParent() { return top; }
    }
}
