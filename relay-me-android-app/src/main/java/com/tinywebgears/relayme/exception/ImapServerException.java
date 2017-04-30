package com.tinywebgears.relayme.exception;

public class ImapServerException extends Exception
{
    private static final long serialVersionUID = 1L;

    public ImapServerException(String detailMessage)
    {
        super(detailMessage);
    }

    public ImapServerException(String detailMessage, Throwable throwable)
    {
        super(detailMessage, throwable);
    }
}
