package com.artformgames.plugin.residencelist.api.user;

import com.artformgames.plugin.residencelist.api.sort.SortFunctions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public interface UserListData {

    @Unmodifiable
    @NotNull List<String> getPinned();

    boolean isPinned(@NotNull String residence);

    void setPin(@NotNull String residence, int index);

    default void removePin(@NotNull String residence) {
        setPin(residence, -1);
    }

    @NotNull SortFunctions getSortFunction();

    boolean isSortReversed();

    void setSortFunction(SortFunctions function);

    void setSortReversed(boolean reversed);


}
