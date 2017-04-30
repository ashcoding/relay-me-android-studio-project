package com.tinywebgears.relayme.email;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.util.Log;

import com.codolutions.android.common.exception.OperationFailedException;
import com.codolutions.android.common.util.Pair;
import com.tinywebgears.relayme.model.ImapServer;
import com.tinywebgears.relayme.model.IncomingEmail;

public class TestOnlyEmailReceiver implements EmailReceiver
{
    private static final String TAG = TestOnlyEmailReceiver.class.getName();

    private final Context context;

    public TestOnlyEmailReceiver(Context context)
    {
        this.context = context;
    }

    @Override
    public void test(ImapServer imapServer)
    {
    }

    @Override
    public List<String> readMailboxes(ImapServer imapServer) throws OperationFailedException
    {
        return Arrays.asList("INBOX");
    }

    @Override
    public Pair<Date, Set<IncomingEmail>> receiveMail(ImapServer imapServer, SubjectChecker subjectChecker,
            String emailSubjectMagicWord, String emailAddress, Collection<String> fromEmailAddresses,
            Date lastEmailDate, String mailboxName) throws OperationFailedException
    {
        Log.d(TAG, "Pretending to receive email from " + fromEmailAddresses + " in " + mailboxName);
        return Pair.create(
                lastEmailDate,
                generateEmailEveryFewSeconds(subjectChecker, emailSubjectMagicWord, fromEmailAddresses.iterator()
                        .next(), lastEmailDate, mailboxName));
    };

    private Set<IncomingEmail> generateEmailEveryFewSeconds(SubjectChecker subjectChecker,
            String emailSubjectMagicWord, String fromEmailSender, Date lastEmailDate, String mailboxName)
    {
        Set<IncomingEmail> incomingEmails = new HashSet<IncomingEmail>();
        Date now = new Date();
        final int intervalInMs = 300 * 1000;
        final int maxEmails = 8;
        for (int i = 0; i < maxEmails; i++)
        {
            Date messageDate = new Date(now.getTime() - i * intervalInMs);
            if (!messageDate.after(lastEmailDate))
                break;
            long messageSerial = messageDate.getTime() / intervalInMs;
            messageDate = new Date(messageSerial * intervalInMs);
            IncomingEmail incomingEmail = new IncomingEmail("" + messageSerial, messageDate, "Re: [Relay ME] SMS from "
                    + "+61-431-123456", "test " + messageSerial + "\n" + "original text is here.");
            incomingEmails.add(incomingEmail);
        }
        return incomingEmails;
    }
}
