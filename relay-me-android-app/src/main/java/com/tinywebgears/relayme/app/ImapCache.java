package com.tinywebgears.relayme.app;

import java.util.HashSet;
import java.util.Set;

import com.codolutions.android.common.util.StringUtil;

// FIXME: TECHDEBT: This is a hack and should be refactored.
public class ImapCache
{
    private String mailboxName;
    private String emailAddress;
    // FIXME: TECHDEBT: This can go away without notice, this is not the best place to place a cache.
    private final Set<String> visitedMessageIds = new HashSet<String>();

    private void resetVisitedMessagesIfNecessary(String emailAddress, String mailboxName)
    {
        if (!StringUtil.empty(this.emailAddress) && !StringUtil.empty(this.mailboxName)
                && this.emailAddress.equals(emailAddress) && this.mailboxName.equals(mailboxName))
            return;
        this.emailAddress = emailAddress;
        this.mailboxName = mailboxName;
        this.visitedMessageIds.clear();
    }

    public Set<String> getVisitedMessageIds(String emailAddress, String mailboxName)
    {
        resetVisitedMessagesIfNecessary(emailAddress, mailboxName);
        return visitedMessageIds;
    }
    
    public void removeOldEntries(Set<String> currentMessageIds) {
        visitedMessageIds.retainAll(currentMessageIds);
    }
}
