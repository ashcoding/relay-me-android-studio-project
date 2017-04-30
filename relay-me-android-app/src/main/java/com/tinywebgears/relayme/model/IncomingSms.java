package com.tinywebgears.relayme.model;

import java.util.Date;

public class IncomingSms
{
    private final String messageId;
    private final Date date;
    private final String body;

    public IncomingSms(String messageId, Date date, String subject, String body)
    {
        this.messageId = messageId;
        this.date = date;
        this.body = body;
    }

    public String getMessageId()
    {
        return messageId;
    }

    public Date getDate()
    {
        return date;
    }

    public String getBody()
    {
        return body;
    }
}
