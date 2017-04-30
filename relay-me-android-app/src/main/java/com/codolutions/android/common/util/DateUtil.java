package com.codolutions.android.common.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtil {
    public static final int SECOND_IN_MS = 1000;
    public static final int MINUTE_IN_MS = 60 * 1000;
    public static final int HOUR_IN_MS = 60 * 60 * 1000;
    public static final int DAY_IN_MS = 24 * 60 * 60 * 1000;
    public static final int MINUTE_IN_S = 60;
    public static final int HOUR_IN_S = 60 * 60;
    public static final int DAY_IN_S = 24 * 60 * 60;

    private static final DateFormat DATE_FORMAT_ISO_8601 =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.ENGLISH);
    private static final DateFormat DATE_FORMAT_ISO_8601_FALLBACK =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
    private static final DateFormat DATE_FORMAT_ISO_8601_FALLBACK_2 =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH);
    private static final DateFormat SIMPLE_DATE_FORMAT =
            DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT);

    static {
        DATE_FORMAT_ISO_8601.setTimeZone(TimeZone.getTimeZone("GMT"));
        DATE_FORMAT_ISO_8601_FALLBACK.setTimeZone(TimeZone.getTimeZone("GMT"));
        DATE_FORMAT_ISO_8601_FALLBACK_2.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public static Date addSeconds(Date date, int seconds) {
        return new Date(date.getTime() + seconds * SECOND_IN_MS);
    }

    public static Date addMinutes(Date date, int minutes) {
        return new Date(date.getTime() + minutes * MINUTE_IN_MS);
    }

    public static Date addHours(Date date, int hours) {
        return new Date(date.getTime() + hours * HOUR_IN_MS);
    }

    public static Date addDays(Date date, int days) {
        return new Date(date.getTime() + days * DAY_IN_MS);
    }

    public static long getTimePassedInMillis(Date baseDate, Date now) {
        if (baseDate == null)
            baseDate = new Date(0);
        if (now == null)
            now = new Date(0);
        return (now.getTime() - baseDate.getTime());
    }

    public static long getTimePassedInSeconds(Date baseDate, Date now) {
        return getTimePassedInMillis(baseDate, now) / 1000;
    }

    public static boolean isAfter(@Nullable Date baseDate, @Nullable Date date) {
        return getTimePassedInMillis(baseDate, date) > 0;
    }

    public static boolean isAfter(long baseTimestamp, long timestamp) {
        return timestamp > baseTimestamp;
    }

    public static long secondsPassedSince(@NonNull Date baseDate) {
        return secondsPassedSince(baseDate.getTime());
    }

    public static long secondsPassedSince(long baseTimestamp) {
        return millisPassedSince(baseTimestamp) / 1000;
    }

    public static long millisPassedSince(long baseTimestamp) {
        return System.currentTimeMillis() - baseTimestamp;
    }

    public static String getRelativeDateString(Date date) {
        long secs = (date.getTime() - System.currentTimeMillis()) / 1000;
        String dateFormat = secs < 0 ? "%s ago" : "in %s";
        long seconds = Math.abs(secs);
        String dateValue = formatAbsoluteDate(seconds);
        return String.format(dateFormat, dateValue);
    }

    private static String formatAbsoluteDate(long secs) {
        long SECONDS_IN_MINUTE = 60;
        long SECONDS_IN_HOUR = SECONDS_IN_MINUTE * 60;
        long SECONDS_IN_DAY = SECONDS_IN_HOUR * 24;
        long SECONDS_IN_WEEK = SECONDS_IN_DAY * 7;
        long SECONDS_IN_MONTH = SECONDS_IN_DAY * 30;
        long SECONDS_IN_YEAR = SECONDS_IN_DAY * 365;
        int HOURS_IN_A_DAY = 24;
        int DAYS_IN_WEEK = 7;
        int WEEKS_IN_A_YEAR = 52;
        int MONTHS_IN_A_YEAR = 12;
        long yearsAway = secs / SECONDS_IN_YEAR;
        long monthsAway = (secs / SECONDS_IN_MONTH) % MONTHS_IN_A_YEAR;
        long weeksAway = (secs / SECONDS_IN_WEEK) % WEEKS_IN_A_YEAR;
        long daysAway = (secs / SECONDS_IN_DAY) % DAYS_IN_WEEK;
        long hoursAway = (secs / SECONDS_IN_HOUR) % HOURS_IN_A_DAY;
        if (yearsAway > 0)
            return yearsAway > 1 ? yearsAway + " years" : "a year";
        if (monthsAway > 0)
            return monthsAway > 1 ? monthsAway + " months" : "a month";
        if (weeksAway > 0)
            return weeksAway > 1 ? weeksAway + " weeks" : "a week";
        String daysStr = daysAway > 1 ? daysAway + " days" : "a day";
        String hoursStr = (hoursAway > 0) ? (hoursAway > 1 ? hoursAway + " hours" : "an hour") : "";
        if (daysAway > 0) {
            // if (hoursAway > 0)
            // return daysStr + " and " + hoursStr;
            return daysStr;
        }
        int minutesAway = (int) Math.floor((secs % SECONDS_IN_HOUR) / SECONDS_IN_MINUTE);
        String minutesStr = (minutesAway > 0) ? (minutesAway > 1 ? minutesAway + " minutes" : "a minute") : "";
        if (hoursAway > 0) {
            // if (minutesAway > 0)
            // return hoursStr + " and " + minutesStr;
            return hoursStr;
        }
        if (minutesAway > 0)
            return minutesStr;
        return "moments";
    }
}
