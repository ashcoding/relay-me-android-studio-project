package com.tinywebgears.relayme.model;

import java.util.Date;
import java.util.Set;

import org.scribe.model.Token;

import com.tinywebgears.relayme.app.UserData;

public class AutoSavingMessagingConfiguration implements MessagingConfiguration
{
    private final MessagingConfiguration delegate;
    private final UserData userData;

    public AutoSavingMessagingConfiguration(final MessagingConfiguration delegate, final UserData userData)
    {
        this.delegate = delegate;
        this.userData = userData;
    }

    private MessagingConfiguration save(MessagingConfiguration messagingConfiguration)
    {
        userData.setMessagingConfiguration(messagingConfiguration);
        return this;
    }

    @Override
    public GatewayType getGatewayType()
    {
        return delegate.getGatewayType();
    }

    @Override
    public MessagingConfiguration setGatewayType(GatewayType gatewayType)
    {
        return save(delegate.setGatewayType(gatewayType));
    }

    @Override
    public boolean isUsingDeprecatedOauthMethod()
    {
        return delegate.isUsingDeprecatedOauthMethod();
    }

    @Override
    public MessagingConfiguration setUsingDeprecatedOauthMethod(boolean usingDeprecatedOauthMethod)
    {
        return save(delegate.setUsingDeprecatedOauthMethod(usingDeprecatedOauthMethod));
    }

    @Override
    public String getTargetEmailAddress()
    {
        return delegate.getTargetEmailAddress();
    }

    @Override
    public MessagingConfiguration setTargetEmailAddress(String targetEmailAddress)
    {
        return save(delegate.setTargetEmailAddress(targetEmailAddress));
    }

    @Override
    public MessagingConfiguration clearTargetEmailAddress()
    {
        return save(delegate.clearTargetEmailAddress());
    }

    @Override
    public String getCcEmailAddress()
    {
        return delegate.getCcEmailAddress();
    }

    @Override
    public MessagingConfiguration setCcEmailAddress(String ccEmailAddress)
    {
        return save(delegate.setCcEmailAddress(ccEmailAddress));
    }

    @Override
    public MessagingConfiguration clearCcEmailAddress()
    {
        return save(delegate.clearCcEmailAddress());
    }

    @Override
    public Token getOauthAccessToken()
    {
        return delegate.getOauthAccessToken();
    }

    @Override
    public MessagingConfiguration setOauthAccessToken(Token oauthAccessToken)
    {
        return save(delegate.setOauthAccessToken(oauthAccessToken));
    }

    @Override
    public MessagingConfiguration clearOauthAccessToken()
    {
        return save(delegate.clearOauthAccessToken());
    }

    @Override
    public String getOauthEmailAddress()
    {
        return delegate.getOauthEmailAddress();
    }

    @Override
    public MessagingConfiguration setOauthEmailAddress(String oauthEmailAddress)
    {
        return save(delegate.setOauthEmailAddress(oauthEmailAddress));
    }

    @Override
    public MessagingConfiguration clearOauthEmailAddress()
    {
        return save(delegate.clearOauthEmailAddress());
    }

    @Override
    public String getReplyMailboxName()
    {
        return delegate.getReplyMailboxName();
    }

    @Override
    public MessagingConfiguration setReplyMailboxName(String replyMailboxName)
    {
        return save(delegate.setReplyMailboxName(replyMailboxName));
    }

    @Override
    public MessagingConfiguration clearReplyMailboxName()
    {
        return save(delegate.clearReplyMailboxName());
    }

    @Override
    public Set<String> getAllMailboxes()
    {
        return delegate.getAllMailboxes();
    }

    @Override
    public MessagingConfiguration setAllMailboxes(Set<String> allMailboxes)
    {
        return save(delegate.setAllMailboxes(allMailboxes));
    }

    @Override
    public MessagingConfiguration clearAllMailboxes()
    {
        return save(delegate.clearAllMailboxes());
    }

    @Override
    public PollingFactor getPollingFactor()
    {
        return delegate.getPollingFactor();
    }

    @Override
    public MessagingConfiguration setPollingFactor(PollingFactor pollingFactor)
    {
        return save(delegate.setPollingFactor(pollingFactor));
    }

    @Override
    public MessagingConfiguration clearPollingFactor()
    {
        return save(delegate.clearPollingFactor());
    }

    @Override
    public Date getLastCheck()
    {
        return delegate.getLastCheck();
    }

    @Override
    public MessagingConfiguration setLastCheck(Date lastCheck)
    {
        return save(delegate.setLastCheck(lastCheck));
    }

    @Override
    public MessagingConfiguration clearLastCheck()
    {
        return save(delegate.clearLastCheck());
    }

    @Override
    public Date getLastEmailDate()
    {
        return delegate.getLastEmailDate();
    }

    @Override
    public MessagingConfiguration setLastEmailDate(Date lastEmailDate)
    {
        return save(delegate.setLastEmailDate(lastEmailDate));
    }

    @Override
    public MessagingConfiguration clearLastEmailDate()
    {
        return save(delegate.clearLastEmailDate());
    }

    @Override
    public SmtpServer getSmtpServer()
    {
        return delegate.getSmtpServer();
    }

    @Override
    public MessagingConfiguration setSmtpServer(SmtpServer smtpServer)
    {
        return save(delegate.setSmtpServer(smtpServer));
    }

    @Override
    public MessagingConfiguration clearSmtpServer()
    {
        return save(delegate.clearSmtpServer());
    }

    @Override
    public ImapServer getImapServer()
    {
        return delegate.getImapServer();
    }

    @Override
    public MessagingConfiguration setImapServer(ImapServer imapServer)
    {
        return save(delegate.setImapServer(imapServer));
    }

    @Override
    public String getEmailSubjectPrefix()
    {
        return delegate.getEmailSubjectPrefix();
    }

    @Override
    public MessagingConfiguration setEmailSubjectPrefix(String prefix)
    {
        return save(delegate.setEmailSubjectPrefix(prefix));
    }

    @Override
    public MessagingConfiguration clearImapServer()
    {
        return save(delegate.clearImapServer());
    }

    @Override
    public boolean isEmailGatewaySetUp()
    {
        return delegate.isEmailGatewaySetUp();
    }

    @Override
    public boolean isEmailReplyGatewaySetUp()
    {
        return delegate.isEmailReplyGatewaySetUp();
    }

    @Override
    public boolean isSmtpServerSetUp()
    {
        return delegate.isSmtpServerSetUp();
    }

    @Override
    public boolean isImapServerSetUp()
    {
        return delegate.isImapServerSetUp();
    }

    @Override
    public boolean isGmailLinkSetUp()
    {
        return delegate.isGmailLinkSetUp();
    }

    @Override
    public boolean isDestinationSetUp()
    {
        return delegate.isDestinationSetUp();
    }

    @Override
    public boolean isSetUp()
    {
        return delegate.isSetUp();
    }

    @Override
    public String toString()
    {
        return delegate.toString();
    }
}
