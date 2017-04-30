package com.codolutions.android.common.util;

public class DataSerializationException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public DataSerializationException(String detailMessage)
    {
        super(detailMessage);
    }

    public DataSerializationException(String detailMessage, Throwable throwable)
    {
        super(detailMessage, throwable);
    }
}
