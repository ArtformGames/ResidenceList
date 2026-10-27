package com.artformgames.plugin.residencelist.api.sort;

import com.artformgames.plugin.residencelist.api.residence.ResidenceData;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SortFunctionsTest {

    @Test
    public void existingStoredOrdinalsAndCycleRemainStable() {
        assertEquals(SortFunctions.NAME, SortFunctions.parse(0));
        assertEquals(SortFunctions.RATINGS, SortFunctions.parse(1));
        assertEquals(SortFunctions.SIZE, SortFunctions.parse(2));
        assertEquals(SortFunctions.VISITORS, SortFunctions.parse(3));
        assertEquals(SortFunctions.VISITORS, SortFunctions.SIZE.next());
        assertEquals(SortFunctions.NAME, SortFunctions.VISITORS.next());
        assertEquals(SortFunctions.NAME, SortFunctions.parse(99));
    }

    @Test
    public void visitorSortShowsMostVisitedFirstAndNameBreaksTies() {
        ResidenceData one = data("Charlie", 1);
        ResidenceData two = data("Bravo", 2);
        ResidenceData three = data("Alpha", 2);

        List<ResidenceData> input = List.of(one, two, three);
        assertEquals(List.of(three, two, one), sorted(input, false));
        assertEquals(List.of(one, two, three), sorted(input, true));
    }

    private static List<ResidenceData> sorted(List<ResidenceData> data, boolean reverse) {
        return data.stream().sorted(SortFunctions.VISITORS.dataComparator(reverse)).toList();
    }

    private static ResidenceData data(String name, int visitors) {
        ResidenceData result = mock(ResidenceData.class);
        when(result.getDisplayName()).thenReturn(name);
        when(result.getVisitorCount()).thenReturn(visitors);
        return result;
    }
}
