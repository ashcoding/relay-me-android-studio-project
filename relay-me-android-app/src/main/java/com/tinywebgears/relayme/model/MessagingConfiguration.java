package com.tinywebgears.relayme.model;

import java.util.Date;
import java.util.Set;

import org.scribe.model.Token;

public interface MessagingConfiguration
{
    GatewayType getGatewayType();

    MessagingConfiguration setGatewayType(GatewayType gatewayType);

    boolean isUsingDeprecatedOauthMethod();

    MessagingConfiguration setUsingDeprecatedOauthMethod(boolean usingDeprecatedOauthMethod);

    String getTargetEmailAddress();

    MessagingConfiguration setTargetEmailAddress(String targetEmailAddress);

    MessagingConfiguration clearTargetEmailAddress();

    String getCcEmailAddress();

    MessagingConfiguration setCcEmailAddress(String ccEmailAddress);

    MessagingConfiguration clearCcEmailAddress();

    Token getOauthAccessToken();

    MessagingConfiguration setOauthAccessToken(Token oauthAccessToken);

    MessagingConfiguration clearOauthAccessToken();

    String getOauthEmailAddress();

    MessagingConfiguration setOauthEmailAddress(String oauthEmailAddress);

    MessagingConfiguration clearOauthEmailAddress();

    String getReplyMailboxName();

    MessagingConfiguration setReplyMailboxName(String replyMailboxName);

    MessagingConfiguration clearReplyMailboxName();

    Set<String> getAllMailboxes();

    MessagingConfiguration setAllMailboxes(Set<String> allMailboxes);

    MessagingConfiguration clearAllMailboxes();

    PollingFactor getPollingFactor();

    MessagingConfiguration setPollingFactor(PollingFactor pollingFactor);

    MessagingConfiguration clearPollingFactor();

    Date getLastCheck();

    MessagingConfiguration setLastCheck(Date lastCheck);

    MessagingConfiguration clearLastCheck();

    Date getLastEmailDate();

    MessagingConfiguration setLastEmailDate(Date lastEmailDate);

    MessagingConfiguration clearLastEmailDate();

    SmtpServer getSmtpServer();

    MessagingConfiguration setSmtpServer(SmtpServer smtpServer);

    MessagingConfiguration clearSmtpServer();

    ImapServer getImapServer();

    MessagingConfiguration setImapServer(ImapServer imapServer);

    MessagingConfiguration clearImapServer();

    String getEmailSubjectPrefix();

    MessagingConfiguration setEmailSubjectPrefix(String prefix);

    boolean isEmailGatewaySetUp();

    boolean isEmailReplyGatewaySetUp();

    boolean isSmtpServerSetUp();

    boolean isImapServerSetUp();

    boolean isGmailLinkSetUp();

    boolean isDestinationSetUp();

    boolean isSetUp();
}
