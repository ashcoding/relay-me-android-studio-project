package com.tinywebgears.relayme.exception;

import com.codolutions.android.common.exception.OperationFailedException;

public class MessagingServiceNotStartedException extends OperationFailedException
{
    private static final long serialVersionUID = 1L;

    public MessagingServiceNotStartedException(String detailMessage)
    {
        super(detailMessage);
    }

    public MessagingServiceNotStartedException(String detailMessage, Throwable throwable)
    {
        super(detailMessage, throwable);
    }
}
