package com.artformgames.plugin.residencelist.api.sort;

import cc.carm.lib.easyplugin.utils.ColorParser;
import com.artformgames.plugin.residencelist.ResidenceListAPI;
import com.artformgames.plugin.residencelist.api.residence.ResidenceData;
import com.artformgames.plugin.residencelist.api.residence.ResidenceRate;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Function;

public enum SortFunctions {

    NAME(comparingData(data -> ColorParser.clear(data.getDisplayName()), String::compareToIgnoreCase)),
    RATINGS(comparingData(d -> d.rateRatio(ResidenceRate::recommend), Double::compare)
            .thenComparing(d -> d.countRate(ResidenceRate::recommend), Integer::compare)
            .thenComparing(NAME.comparator)),
    SIZE(comparingData(d -> d.getResidence().getMainArea().getSize(), Long::compare).thenComparing(NAME.comparator));

    private final Comparator<ResidenceData> comparator;

    SortFunctions(Comparator<ResidenceData> comparator) {
        this.comparator = comparator;
    }

    public Comparator<ResidenceData> dataComparator(boolean reverse) {
        return reverse ? comparator.reversed() : comparator;
    }

    public Comparator<ClaimedResidence> residenceComparator(boolean reverse) {
        return Comparator.comparing(ResidenceListAPI::getResidenceData, dataComparator(reverse));
    }

    public SortFunctions next() {
        return next(this);
    }

    public static <U> Comparator<ResidenceData> comparingData(Function<ResidenceData, ? extends U> keyExtractor,
                                                              Comparator<? super U> keyComparator) {
        return (c1, c2) -> keyComparator.compare(keyExtractor.apply(c1), keyExtractor.apply(c2));
    }

    public static SortFunctions next(SortFunctions v) {
        return v.ordinal() >= values().length - 1 ? values()[0] : values()[v.ordinal() + 1];
    }

    public static SortFunctions parse(int i) {
        return Arrays.stream(values()).filter(v -> v.ordinal() == i).findFirst().orElse(NAME);
    }

}
