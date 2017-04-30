package com.codolutions.android.common.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionUtil {
    public static Throwable getCause(Throwable e, Class<?> exceptionClass) {
        return getCause(e, exceptionClass, 8);
    }

    private static Throwable getCause(Throwable e, Class<?> exceptionClass, int depth) {
        if (e == null)
            return null;
        if (exceptionClass == e.getClass())
            return e;
        return getCause(e.getCause(), exceptionClass, depth - 1);
    }

    public static String getStackTrace(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}
