package com.tinywebgears.relayme.email;

import android.content.Context;
import android.util.Log;

import com.codolutions.android.common.exception.OperationFailedException;
import com.tinywebgears.relayme.model.SmtpServer;

public class TestOnlyEmailSender implements EmailSender
{
    private static final String TAG = TestOnlyEmailSender.class.getName();

    public TestOnlyEmailSender(Context context)
    {
    }

    @Override
    public void test(SmtpServer smtpServer)
    {
    }

    @Override
    public void sendMail(SmtpServer smtpServer, String senderId, String subject, String textHeading, String body,
            String recipient, String cc) throws OperationFailedException
    {
        Log.d(TAG, "Pretending to send email from " + smtpServer.getEmail() + " to " + recipient);
    }
}
