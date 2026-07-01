package com.bardock.discordvoicebot.util;

public final class TimeFormatterUtil {

    private TimeFormatterUtil() {
        throw new UnsupportedOperationException("This is a utility class and can`t be instanciated.");
    }

    public static String formatTime(Long totalSeconds) {
        if (totalSeconds == null || totalSeconds < 0) {
            return "0h 0m";
        }

        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        return hours + "h " + minutes + "m";
    }

}
