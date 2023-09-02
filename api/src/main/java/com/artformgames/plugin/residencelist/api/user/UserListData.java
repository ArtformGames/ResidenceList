package com.artformgames.plugin.residencelist.api.user;

import java.util.List;

public interface UserListData {

    List<String> getPinned();

    void setPin(String residence, int index);

    default void removePin(String residence) {
        setPin(residence, -1);
    }

}
