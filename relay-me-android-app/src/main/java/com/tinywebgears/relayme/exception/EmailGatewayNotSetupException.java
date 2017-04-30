package com.tinywebgears.relayme.exception;

import com.codolutions.android.common.exception.OperationFailedException;

public class EmailGatewayNotSetupException extends OperationFailedException
{
    private static final long serialVersionUID = 1L;

    public EmailGatewayNotSetupException(String detailMessage)
    {
        super(detailMessage);
    }

    public EmailGatewayNotSetupException(String detailMessage, Throwable throwable)
    {
        super(detailMessage, throwable);
    }
}
