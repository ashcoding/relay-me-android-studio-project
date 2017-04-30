package com.tinywebgears.relayme.model;

import java.util.Date;

public class IncomingEmail
{
    private final String messageId;
    private final Date date;
    private final String subject;
    private final String body;

    public IncomingEmail(String messageId, Date date, String subject, String body)
    {
        this.messageId = messageId;
        this.date = date;
        this.subject = subject;
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

    public String getSubject()
    {
        return subject;
    }

    public String getBody()
    {
        return body;
    }

    @Override
    public String toString()
    {
        return "IncomingEmail [messageId=" + messageId + ", date=" + date + ", subject=" + subject + "]";
    }
}
