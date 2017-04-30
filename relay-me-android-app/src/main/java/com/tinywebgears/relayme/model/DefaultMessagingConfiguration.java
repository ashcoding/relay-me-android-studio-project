package com.tinywebgears.relayme.model;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.scribe.model.Token;

import com.codolutions.android.common.util.StringUtil;

public class DefaultMessagingConfiguration implements MessagingConfiguration
{
    // TODO: Later: Move to strings.xml
    private static final String DEFAULT_EMAIL_SUBJECT_PREFIX = "[Relay ME]";

    private GatewayType gatewayType;
    private boolean usingDeprecatedOauthMethod;
    private String targetEmailAddress;
    private String ccEmailAddress;
    private Token oauthAccessToken;
    private String oauthEmailAddress;
    private String replyMailboxName;
    private Set<String> allMailboxes = new HashSet<String>();
    private PollingFactor pollingFactor;
    private Date lastCheck;
    private Date lastEmailDate;
    private SmtpServer smtpServer;
    private SmtpServer draftSmtpServer;
    private ImapServer imapServer;
    private ImapServer draftImapServer;
    private String emailSubjectPrefix;

    @Override
    public GatewayType getGatewayType()
    {
        return gatewayType == null ? GatewayType.defaultValue() : gatewayType;
    }

    @Override
    public DefaultMessagingConfiguration setGatewayType(GatewayType gatewayType)
    {
        this.gatewayType = gatewayType;
        return this;
    }

    @Override
    public boolean isUsingDeprecatedOauthMethod()
    {
        return usingDeprecatedOauthMethod;
    }

    @Override
    public DefaultMessagingConfiguration setUsingDeprecatedOauthMethod(boolean usingDeprecatedOauthMethod)
    {
        this.usingDeprecatedOauthMethod = usingDeprecatedOauthMethod;
        return this;
    }

    @Override
    public String getTargetEmailAddress()
    {
        return targetEmailAddress;
    }

    @Override
    public DefaultMessagingConfiguration setTargetEmailAddress(String targetEmailAddress)
    {
        this.targetEmailAddress = targetEmailAddress;
        return this;
    }

    @Override
    public DefaultMessagingConfiguration clearTargetEmailAddress()
    {
        return setTargetEmailAddress(null);
    }

    @Override
    public String getCcEmailAddress()
    {
        return ccEmailAddress;
    }

    @Override
    public DefaultMessagingConfiguration setCcEmailAddress(String ccEmailAddress)
    {
        this.ccEmailAddress = ccEmailAddress;
        return this;
    }

    @Override
    public DefaultMessagingConfiguration clearCcEmailAddress()
    {
        return setCcEmailAddress(null);
    }

    @Override
    public Token getOauthAccessToken()
    {
        return oauthAccessToken;
    }

    @Override
    public DefaultMessagingConfiguration setOauthAccessToken(Token oauthAccessToken)
    {
        this.oauthAccessToken = oauthAccessToken;
        return this;
    }

    @Override
    public DefaultMessagingConfiguration clearOauthAccessToken()
    {
        return setOauthAccessToken(null);
    }

    @Override
    public String getOauthEmailAddress()
    {
        return oauthEmailAddress;
    }

    @Override
    public DefaultMessagingConfiguration setOauthEmailAddress(String oauthEmailAddress)
    {
        this.oauthEmailAddress = oauthEmailAddress;
        return this;
    }

    @Override
    public DefaultMessagingConfiguration clearOauthEmailAddress()
    {
        return setOauthEmailAddress(null);
    }

    @Override
    public String getReplyMailboxName()
    {
        return replyMailboxName;
    }

    @Override
    public DefaultMessagingConfiguration setReplyMailboxName(String replyMailboxName)
    {
        this.replyMailboxName = replyMailboxName;
        return this;
    }

    @Override
    public DefaultMessagingConfiguration clearReplyMailboxName()
    {
        return setReplyMailboxName(null);
    }

    @Override
    public Set<String> getAllMailboxes()
    {
        return allMailboxes;
    }

    @Override
    public DefaultMessagingConfiguration setAllMailboxes(Set<String> allMailboxes)
    {
        this.allMailboxes = allMailboxes == null ? new HashSet<String>() : allMailboxes;
        return this;
    }

    @Override
    public DefaultMessagingConfiguration clearAllMailboxes()
    {
        Set<String> emptySet = Collections.emptySet();
        return setAllMailboxes(emptySet);
    }

    @Override
    public PollingFactor getPollingFactor()
    {
        return pollingFactor == null ? PollingFactor.defaultValue() : pollingFactor;
    }

    @Override
    public MessagingConfiguration setPollingFactor(PollingFactor pollingFactor)
    {
        this.pollingFactor = pollingFactor;
        return this;
    }

    @Override
    public MessagingConfiguration clearPollingFactor()
    {
        this.pollingFactor = null;
        return this;
    }

    @Override
    public Date getLastCheck()
    {
        return lastCheck;
    }

    @Override
    public DefaultMessagingConfiguration setLastCheck(Date lastCheck)
    {
        this.lastCheck = lastCheck;
        return this;
    }

    @Override
    public DefaultMessagingConfiguration clearLastCheck()
    {
        return setLastCheck(null);
    }

    @Override
    public Date getLastEmailDate()
    {
        return lastEmailDate;
    }

    @Override
    public DefaultMessagingConfiguration setLastEmailDate(Date lastEmailDate)
    {
        this.lastEmailDate = lastEmailDate;
        return this;
    }

    @Override
    public DefaultMessagingConfiguration clearLastEmailDate()
    {
        return setLastEmailDate(null);
    }

    @Override
    public SmtpServer getSmtpServer()
    {
        return smtpServer;
    }

    @Override
    public MessagingConfiguration setSmtpServer(SmtpServer smtpServer)
    {
        this.smtpServer = smtpServer;
        return this;
    }

    @Override
    public MessagingConfiguration clearSmtpServer()
    {
        return setSmtpServer(null);
    }

    @Override
    public ImapServer getImapServer()
    {
        return imapServer;
    }

    @Override
    public MessagingConfiguration setImapServer(ImapServer imapServer)
    {
        this.imapServer = imapServer;
        return this;
    }

    @Override
    public MessagingConfiguration clearImapServer()
    {
        return setImapServer(null);
    }

    @Override
    public String getEmailSubjectPrefix()
    {
        return (emailSubjectPrefix == null) ? DEFAULT_EMAIL_SUBJECT_PREFIX : emailSubjectPrefix;
    }

    @Override
    public MessagingConfiguration setEmailSubjectPrefix(String prefix)
    {
        this.emailSubjectPrefix = prefix;
        return this;
    }

    @Override
    public boolean isEmailGatewaySetUp()
    {
        if (gatewayType == GatewayType.GMAIL)
            return isGmailLinkSetUp();
        else if (gatewayType == GatewayType.CUSTOM)
            return isSmtpServerSetUp();
        return false;
    }

    @Override
    public boolean isEmailReplyGatewaySetUp()
    {
        if (gatewayType == GatewayType.GMAIL)
            return isGmailLinkSetUp();
        else if (gatewayType == GatewayType.CUSTOM)
            return isImapServerSetUp();
        return false;
    }

    @Override
    public boolean isSmtpServerSetUp()
    {
        return smtpServer != null && smtpServer.isValid();
    }

    @Override
    public boolean isImapServerSetUp()
    {
        return imapServer != null && imapServer.isValid();
    }

    @Override
    public boolean isGmailLinkSetUp()
    {
        return oauthAccessToken != null && !StringUtil.empty(oauthEmailAddress);
    }

    @Override
    public boolean isDestinationSetUp()
    {
        return validateEmailAddress(targetEmailAddress);
    }

    @Override
    public boolean isSetUp()
    {
        return isEmailGatewaySetUp() && isDestinationSetUp();
    }

    private boolean validateEmailAddress(String emailAddress)
    {
        if (emailAddress == null || emailAddress.length() < 1)
            return false;
        int indexOfAt = emailAddress.indexOf("@");
        if (indexOfAt <= 0 || indexOfAt >= emailAddress.length() - 1 || indexOfAt != emailAddress.lastIndexOf("@"))
            return false;
        String domainPart = emailAddress.substring(indexOfAt + "@".length());
        int indexOfDot = domainPart.indexOf(".");
        if (indexOfDot <= 0 || indexOfDot >= domainPart.length() - 1)
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        return "MessagingConfiguration [gatewayType=" + gatewayType + ", usingDeprecatedOauthMethod="
                + usingDeprecatedOauthMethod + ", targetEmailAddress=" + targetEmailAddress + ", ccEmailAddress="
                + ccEmailAddress + ", oauthEmailAddress=" + oauthEmailAddress + ", replyMailboxName="
                + replyMailboxName + ", allMailboxes=" + allMailboxes + ", lastCheck=" + lastCheck + ", lastEmailDate="
                + lastEmailDate + ", emailSubjectPrefix=" + emailSubjectPrefix + "]";
    }
}
