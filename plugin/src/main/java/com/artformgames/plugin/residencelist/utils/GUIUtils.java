package com.artformgames.plugin.residencelist.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class GUIUtils {

    private GUIUtils() {
        throw new UnsupportedOperationException();
    }

    public static List<String> sortContent(String content, int lineLength) {
        List<String> lore = new ArrayList<>();
        if (content == null || content.isBlank()) return lore;

        content = content.replaceAll(Pattern.quote("§"), "&");// Prevent color problems

        int length = content.length();
        int lines = length / lineLength + (length % lineLength == 0 ? 0 : 1);
        for (int i = 0; i < lines; i++) {
            int start = i * lineLength;
            int end = Math.min((i + 1) * lineLength, length);
            lore.add(content.substring(start, end));
        }

        return lore;
    }

}
