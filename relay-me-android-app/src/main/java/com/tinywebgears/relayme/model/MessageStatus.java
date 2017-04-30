package com.tinywebgears.relayme.model;

import java.util.HashMap;
import java.util.Map;

import com.tinywebgears.relayme.R;

public enum MessageStatus
{
    NEW("new", R.drawable.msg_new), SENDING("sending", R.drawable.msg_sending), FAILED("failed", R.drawable.msg_failed), SENT(
            "sent", R.drawable.msg_sent), PERMANENTLY_FAILED("perm_failed", R.drawable.msg_failed_permanently);

    private static final Map<String, MessageStatus> stringToEnum = new HashMap<String, MessageStatus>();

    static
    {
        for (MessageStatus e : values())
            stringToEnum.put(e.toString(), e);
    }

    private final String messageStatusId;
    private final int icon;

    MessageStatus(String messageStatusId, int icon)
    {
        this.messageStatusId = messageStatusId;
        this.icon = icon;
    }

    public int getIcon()
    {
        return icon;
    }

    public String toString()
    {
        return messageStatusId;
    }

    public static MessageStatus defaultValue()
    {
        return NEW;
    }

    public static MessageStatus fromString(String str)
    {
        return stringToEnum.containsKey(str) ? stringToEnum.get(str) : defaultValue();
    }
}
