package com.artformgames.plugin.residencelist.utils;

import com.bekvon.bukkit.residence.protection.CuboidArea;
import org.bukkit.util.Vector;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class ResidenceUtilsTest {

    @Test
    public void foliaPreviewUsesStoredAreaBoundsWithoutResolvingWorld() {
        CuboidArea area = mock(CuboidArea.class);
        when(area.getWorldName()).thenReturn("remote_world");
        when(area.getLowVector()).thenReturn(new Vector(-11, 60, 20));
        when(area.getHighVector()).thenReturn(new Vector(9, 80, 40));

        ResidenceUtils.TeleportPreview preview = ResidenceUtils.approximateTeleportPreview(area);

        assertEquals("remote_world", preview.world());
        assertEquals(-1, preview.x());
        assertEquals(70, preview.y());
        assertEquals(30, preview.z());
        assertTrue(preview.approximate());
        verify(area).getWorldName();
        verify(area).getLowVector();
        verify(area).getHighVector();
        verifyNoMoreInteractions(area);
    }
}
