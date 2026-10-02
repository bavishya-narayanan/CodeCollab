package com.codecollab.client.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/** Humanised time-formatting utilities. */
public final class TimeFormat {

    private static final DateTimeFormatter FULL =
            DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm").withZone(ZoneId.systemDefault());

    private TimeFormat() {}

    public static String relative(Instant instant) {
        if (instant == null) return "";
        long minutes = ChronoUnit.MINUTES.between(instant, Instant.now());
        if (minutes < 1)   return "just now";
        if (minutes < 60)  return minutes + "m ago";
        long hours = minutes / 60;
        if (hours < 24)    return hours + "h ago";
        long days = hours / 24;
        if (days < 30)     return days + "d ago";
        return FULL.format(instant);
    }

    public static String full(Instant instant) {
        return instant == null ? "" : FULL.format(instant);
    }
}
