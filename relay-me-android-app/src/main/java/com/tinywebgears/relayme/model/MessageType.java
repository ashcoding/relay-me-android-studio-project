package com.tinywebgears.relayme.model;

import java.util.HashMap;
import java.util.Map;

import com.tinywebgears.relayme.R;

public enum MessageType
{
    UNKNOWN("unknown", 0), INCOMING("in", R.drawable.msg_dir_incoming), OUTGOING("out", R.drawable.msg_dir_outgoing), STATUS(
            "status", 0);

    private static final Map<String, MessageType> stringToEnum = new HashMap<String, MessageType>();

    static
    {
        for (MessageType e : values())
            stringToEnum.put(e.toString(), e);
    }

    private final String messageDirectionId;
    private final int icon;

    MessageType(String messageDirectionId, int icon)
    {
        this.messageDirectionId = messageDirectionId;
        this.icon = icon;
    }

    public int getIcon()
    {
        return icon;
    }

    public String toString()
    {
        return messageDirectionId;
    }

    public static MessageType defaultValue()
    {
        return UNKNOWN;
    }

    public static MessageType fromString(String str)
    {
        return stringToEnum.containsKey(str) ? stringToEnum.get(str) : defaultValue();
    }
}
