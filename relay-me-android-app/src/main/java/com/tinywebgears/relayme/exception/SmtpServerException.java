package com.tinywebgears.relayme.exception;

public class SmtpServerException extends Exception
{
    private static final long serialVersionUID = 1L;

    public SmtpServerException(String detailMessage)
    {
        super(detailMessage);
    }

    public SmtpServerException(String detailMessage, Throwable throwable)
    {
        super(detailMessage, throwable);
    }
}
