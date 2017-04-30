package com.tinywebgears.relayme.email;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.codolutions.android.common.exception.OperationFailedException;
import com.codolutions.android.common.util.Pair;
import com.tinywebgears.relayme.model.ImapServer;
import com.tinywebgears.relayme.model.IncomingEmail;

public interface EmailReceiver
{
    void test(ImapServer imapServer) throws OperationFailedException;

    List<String> readMailboxes(ImapServer imapServer) throws OperationFailedException;

    Pair<Date, Set<IncomingEmail>> receiveMail(ImapServer imapServer, SubjectChecker subjectChecker,
            String emailSubjectMagicWord, String emailAddress, Collection<String> fromAddresses, Date lastEmailDate,
            String mailboxName) throws OperationFailedException;

    interface SubjectChecker
    {
        boolean validSubject(String subject);
    }
}
