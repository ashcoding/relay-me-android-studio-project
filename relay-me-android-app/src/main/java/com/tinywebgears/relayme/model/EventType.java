package com.tinywebgears.relayme.model;

import java.util.HashMap;
import java.util.Map;

import com.tinywebgears.relayme.R;

public enum EventType
{
    UNKNOWN("unknown", R.string.lbl_unknown, false), SMS("sms", R.string.str_email_subject_sms, true), MMS("mms",
            R.string.str_email_subject_mms, true), MISSED_CALL("missed_call", R.string.str_email_subject_missed_call,
            true);

    private static final Map<String, EventType> stringToEnum = new HashMap<String, EventType>();

    static
    {
        for (EventType e : values())
            stringToEnum.put(e.toString(), e);
    }

    private final String messageDirectionId;
    private final int nameResource;
    private final boolean replyPossible;

    EventType(String messageDirectionId, int nameResource, boolean replyPossible)
    {
        this.messageDirectionId = messageDirectionId;
        this.nameResource = nameResource;
        this.replyPossible = replyPossible;
    }

    public int getNameResource()
    {
        return nameResource;
    }

    public boolean isReplyPossible()
    {
        return replyPossible;
    }

    public String toString()
    {
        return messageDirectionId;
    }

    public static EventType defaultValue()
    {
        return UNKNOWN;
    }

    public static EventType fromString(String str)
    {
        return stringToEnum.containsKey(str) ? stringToEnum.get(str) : defaultValue();
    }
}
