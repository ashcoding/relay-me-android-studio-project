package com.tinywebgears.relayme.email;

import com.codolutions.android.common.exception.OperationFailedException;
import com.tinywebgears.relayme.model.SmtpServer;

public interface EmailSender
{
    void test(SmtpServer smtpServer) throws OperationFailedException;

    void sendMail(SmtpServer smtpServer, String senderId, String subject, String textHeading, String body,
            String recipient, String cc) throws OperationFailedException;
}
