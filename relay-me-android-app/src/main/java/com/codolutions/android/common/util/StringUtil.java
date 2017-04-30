package com.codolutions.android.common.util;

import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {
    private static final String TAG = StringUtil.class.getName();
    private static final String ELLIPSIS = "...";

    public static boolean empty(String s) {
        return s == null || s.length() < 1;
    }

    public static boolean nonEmpty(String s) {
        return !empty(s);
    }

    public static boolean stringsEqual(String s1, String s2) {
        if (s1 == null && s2 == null)
            return true;
        if (s1 == null || s2 == null)
            return false;
        return s1.equals(s2);
    }

    public static String shorten(String text, Integer length) {
        if (empty(text))
            return text;
        String shortenedText = substringByCharacters(text, 0, length);
        return shortenedText.length() < text.length() ? (shortenedText + ELLIPSIS) : shortenedText;
    }

    public static String stripMatchingRegex(String[] regexes, String text) {
        if (empty(text))
            return text;
        for (String regex : regexes) {
            Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);
            boolean found = matcher.find();
            if (found)
                return text.substring(matcher.end());
        }
        return null;
    }

    public static Set<String> splitString(String text) {
        Set<String> result = new HashSet<String>();
        if (empty(text))
            return result;
        for (String token : text.split("\\s+"))
            if (token.length() > 0)
                result.add(token);
        return result;
    }

    public static String join(Collection<?> collection, String separator) {
        return join(collection, separator, separator, separator, "", "");
    }

    public static String join(Collection<?> collection, String separator, String quote) {
        return join(collection, separator, separator, separator, quote, quote);
    }

    public static String join(Collection<?> collection, String separator, String lastSeparator, String twoSeparator,
                              String quoteLeft, String quoteRight) {
        Object[] array = collection.toArray();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                if (array.length == 2)
                    builder.append(twoSeparator);
                else if (i == array.length - 1)
                    builder.append(lastSeparator);
                else
                    builder.append(separator);
            }
            if (quoteLeft != null)
                builder.append(quoteLeft);
            builder.append(array[i]);
            if (quoteRight != null)
                builder.append(quoteRight);
        }
        return builder.toString();
    }

    public static String getStackTrace(Throwable throwable) {
        StringWriter stackTraceWriter = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stackTraceWriter));
        return stackTraceWriter.toString();
    }

    public static String getStackTrace(Throwable throwable, int length) {
        String stackTrace = getStackTrace(throwable);
        int sectionLength = length / 3;
        if (stackTrace.length() <= length || length < 0)
            return stackTrace;
        String header = stackTrace.substring(0, sectionLength * 2);
        String footer = stackTrace.substring(stackTrace.length() - sectionLength, stackTrace.length());
        return header + "\n" + ELLIPSIS + "\n" + footer;
    }

    public static String unaliasEmailAddress(String emailAddress) {
        if (emailAddress == null)
            return null;
        String newEmailAddress = emailAddress;
        while (newEmailAddress.matches(".*\\+.*@.*"))
            newEmailAddress = emailAddress.replaceAll("\\+.*@", "@");
        return newEmailAddress;
    }

    public static String substringByCharacters(String str, int start, int length) {
        int startOffset = 0;
        int endOffset = str.length();
        try {
            startOffset = str.offsetByCodePoints(0, start);
            endOffset = str.offsetByCodePoints(startOffset, length);
        } catch (IndexOutOfBoundsException e) {
            Log.i(TAG,
                    String.format("Out of bound exception when finding %d long substring from %d of string: %s", length,
                            start, str));
        }
        return str.substring(startOffset, endOffset);
    }
}
