package com.codolutions.android.common.exception;

@SuppressWarnings("serial")
public class OperationFailedException extends Exception {
    public OperationFailedException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public OperationFailedException(String detailMessage) {
        super(detailMessage);
    }
}
