package com.tinywebgears.relayme.model;

import java.util.HashMap;
import java.util.Map;

public enum LogLevel
{
    DEBUG("debug"), INFO("info"), WARN("warn"), ERROR("error");

    private static final Map<String, LogLevel> stringToEnum = new HashMap<String, LogLevel>();

    static
    {
        for (LogLevel e : values())
            stringToEnum.put(e.toString(), e);
    }

    private final String mLogLevelId;

    LogLevel(String logLevelId)
    {
        this.mLogLevelId = logLevelId;
    }

    public String toString()
    {
        return mLogLevelId;
    }

    public static LogLevel defaultValue()
    {
        return INFO;
    }

    public static LogLevel fromString(String str)
    {
        return stringToEnum.containsKey(str) ? stringToEnum.get(str) : defaultValue();
    }
}
