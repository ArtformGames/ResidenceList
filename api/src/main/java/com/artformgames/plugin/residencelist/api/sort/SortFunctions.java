package com.artformgames.plugin.residencelist.api.sort;

import cc.carm.lib.easyplugin.utils.ColorParser;
import com.artformgames.plugin.residencelist.ResidenceListAPI;
import com.artformgames.plugin.residencelist.api.residence.ResidenceData;
import com.artformgames.plugin.residencelist.api.residence.ResidenceRate;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.BiFunction;
import java.util.function.Function;

public enum SortFunctions {

    NAME(data -> ColorParser.clear(data.getName()), String::compareTo),
    RATINGS(data -> data.rateRatio(ResidenceRate::recommend), Double::compare, NAME.dataComparator(false)),
    SIZE(data -> data.getResidence().getMainArea().getSize(), Long::compare, NAME.dataComparator(false));

    private final Comparator<ResidenceData> comparator;

    SortFunctions(Comparator<ResidenceData> comparator) {
        this.comparator = comparator;
    }

    <T> SortFunctions(Function<ResidenceData, T> function,
                      BiFunction<T, T, Integer> comparator) {
        this((o1, o2) -> comparator.apply(function.apply(o1), function.apply(o2)));
    }


    <T> SortFunctions(Function<ResidenceData, T> function,
                      BiFunction<T, T, Integer> comparator,
                      Comparator<ResidenceData> thenCompare) {
        this((o1, o2) -> {
            int first = comparator.apply(function.apply(o1), function.apply(o2));
            return first != 0 ? first : thenCompare.compare(o1, o2);
        });
    }

    public Comparator<ResidenceData> dataComparator(boolean reverse) {
        return reverse ? comparator.reversed() : comparator;
    }

    public Comparator<ClaimedResidence> residenceComparator(boolean reverse) {
        return (r1, r2) -> dataComparator(reverse).compare(ResidenceListAPI.getResidenceData(r1), ResidenceListAPI.getResidenceData(r2));
    }

    public SortFunctions next() {
        return next(this);
    }

    public static SortFunctions next(SortFunctions v) {
        return v.ordinal() >= values().length - 1 ? values()[0] : values()[v.ordinal() + 1];
    }

    public static SortFunctions parse(int i) {
        return Arrays.stream(values()).filter(v -> v.ordinal() == i).findFirst().orElse(NAME);
    }

}
