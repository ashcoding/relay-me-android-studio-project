package com.tinywebgears.relayme.service;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import org.scribe.model.Token;

import android.content.Context;

import com.codolutions.android.common.exception.OperationFailedException;
import com.codolutions.android.common.util.Pair;
import com.codolutions.android.common.util.StringUtil;
import com.tinywebgears.relayme.R;
import com.tinywebgears.relayme.app.SystemStatus;
import com.tinywebgears.relayme.app.UserData;
import com.tinywebgears.relayme.common.Constants;
import com.tinywebgears.relayme.email.EmailReceiver;
import com.tinywebgears.relayme.email.EmailReceiver.SubjectChecker;
import com.tinywebgears.relayme.email.EmailSender;
import com.tinywebgears.relayme.email.ImapEmailReceiver;
import com.tinywebgears.relayme.email.ServerCredentials;
import com.tinywebgears.relayme.email.SmtpEmailSender;
import com.tinywebgears.relayme.email.TestOnlyEmailReceiver;
import com.tinywebgears.relayme.email.TestOnlyEmailSender;
import com.tinywebgears.relayme.model.GatewayType;
import com.tinywebgears.relayme.model.ImapServer;
import com.tinywebgears.relayme.model.IncomingEmail;
import com.tinywebgears.relayme.model.MessagingConfiguration;
import com.tinywebgears.relayme.model.ServerSecurity;
import com.tinywebgears.relayme.model.SmtpServer;
import com.tinywebgears.relayme.oauth.GoogleOauth2Helper;
import com.tinywebgears.relayme.oauth.OauthHelper;

public class EmailService
{
    private final EmailSender sender;
    private final EmailReceiver receiver;
    private Context context;

    public EmailService(Context context, SystemStatus systemStatus, UserData userData)
    {
        this.context = context;
        if (Constants.EMAIL_TEST_MODE)
        {
            sender = new TestOnlyEmailSender(context);
            receiver = new TestOnlyEmailReceiver(context);
        }
        else
        {
            sender = new SmtpEmailSender(context);
            receiver = new ImapEmailReceiver(context, systemStatus.getGmailImapCache());
        }
    }

    public void sendEmail(UserData userData, String email, String cc, String senderId, String subject,
            String textHeading, String text) throws OperationFailedException
    {
        try
        {
            MessagingConfiguration messagingConfiguration = userData.getMessagingConfiguration();
            if (!messagingConfiguration.isEmailGatewaySetUp())
                throw new OperationFailedException("Email gateway is not set up.");
            if (messagingConfiguration.getGatewayType() == GatewayType.BUILT_IN)
                throw new OperationFailedException("Built-in gateway not implemented yet.");
            if (messagingConfiguration.getGatewayType() == GatewayType.CUSTOM)
            {
                SmtpServer smtpServer = messagingConfiguration.getSmtpServer();
                sender.sendMail(smtpServer, senderId, subject, textHeading, text, email, cc);
            }
            else if (messagingConfiguration.getGatewayType() == GatewayType.GMAIL)
            {
                String fromEmail = messagingConfiguration.getOauthEmailAddress();
                if (messagingConfiguration.isUsingDeprecatedOauthMethod())
                    throw new OperationFailedException("OAuth 1.0 is not supported anymore.");
                OauthHelper oauthHelper = GoogleOauth2Helper.get(userData);
                Pair<String, Token> result = oauthHelper.buildXOAuthSmtp(messagingConfiguration.getOauthEmailAddress(),
                        messagingConfiguration.getOauthAccessToken());
                messagingConfiguration.setOauthAccessToken(result.second);
                SmtpServer smtpServer = new SmtpServer(fromEmail, "smtp.gmail.com", 587, ServerSecurity.STARTTLS, true,
                        new ServerCredentials(result.first));
                sender.sendMail(smtpServer, senderId, subject, textHeading, text, email, cc);
            }
            else
                throw new OperationFailedException("Unkonwn gateway: " + messagingConfiguration.getGatewayType());
        }
        catch (Exception e)
        {
            throw new OperationFailedException(e.getMessage(), e);
        }
    }

    public List<String> readMailboxes(UserData userData) throws OperationFailedException
    {
        MessagingConfiguration messagingConfiguration = userData.getMessagingConfiguration();
        if (!messagingConfiguration.isEmailReplyGatewaySetUp())
            throw new OperationFailedException("Reply gateway is not set up.");
        if (messagingConfiguration.getGatewayType() == GatewayType.BUILT_IN)
            throw new OperationFailedException("Built-in gateway not implemented yet.");
        if (messagingConfiguration.getGatewayType() == GatewayType.CUSTOM)
        {
            ImapServer imapServer = messagingConfiguration.getImapServer();
            return receiver.readMailboxes(imapServer);
        }
        else if (messagingConfiguration.getGatewayType() == GatewayType.GMAIL)
        {
            if (messagingConfiguration.isUsingDeprecatedOauthMethod())
                throw new OperationFailedException("OAuth 1.0 is not supported anymore.");
            OauthHelper oauthHelper = GoogleOauth2Helper.get(userData);
            Pair<String, Token> result = oauthHelper.buildXOAuthImap(messagingConfiguration.getOauthEmailAddress(),
                    messagingConfiguration.getOauthAccessToken());
            messagingConfiguration.setOauthAccessToken(result.second);
            ImapServer imapServer = new ImapServer("imap.gmail.com", 993, ServerSecurity.SSL, true,
                    new ServerCredentials(result.first));
            return receiver.readMailboxes(imapServer);
        }
        else
            throw new OperationFailedException("Unkonwn gateway: " + messagingConfiguration.getGatewayType());
    }

    public Pair<Date, Set<IncomingEmail>> receiveEmail(final UserData userData, Date lastEmailDate,
            Collection<String> fromAddresses, String mailboxName) throws OperationFailedException
    {
        SubjectChecker subjectChecker = new SubjectChecker()
        {
            @Override
            public boolean validSubject(String subject)
            {
                try
                {
                    if (StringUtil.nonEmpty(getNumber(context, userData, subject)))
                        return true;
                    LogStoreHelper.info(EmailService.class, context, "Invalid subject (no number): " + subject);
                    return false;
                }
                catch (OperationFailedException e)
                {
                    LogStoreHelper.info(EmailService.class, context, "Invalid subject (couldn't find numbers): "
                            + subject);
                    return false;
                }
            }
        };
        try
        {
            MessagingConfiguration messagingConfiguration = userData.getMessagingConfiguration();
            if (!messagingConfiguration.isEmailReplyGatewaySetUp())
                throw new OperationFailedException("Reply gateway is not set up.");
            if (messagingConfiguration.getGatewayType() == GatewayType.BUILT_IN)
                throw new OperationFailedException("Built-in gateway not implemented yet.");
            if (messagingConfiguration.getGatewayType() == GatewayType.CUSTOM)
            {
                SmtpServer smtpServer = messagingConfiguration.getSmtpServer();
                ImapServer imapServer = messagingConfiguration.getImapServer();
                return receiver.receiveMail(imapServer, subjectChecker, userData.getMessagingConfiguration()
                        .getEmailSubjectPrefix(), smtpServer.getEmail(), fromAddresses, lastEmailDate, mailboxName);
            }
            else if (messagingConfiguration.getGatewayType() == GatewayType.GMAIL)
            {
                String emailAddress = messagingConfiguration.getOauthEmailAddress();
                if (messagingConfiguration.isUsingDeprecatedOauthMethod())
                    throw new OperationFailedException("OAuth 1.0 is not supported anymore.");
                OauthHelper oauthHelper = GoogleOauth2Helper.get(userData);
                Pair<String, Token> result = oauthHelper.buildXOAuthImap(messagingConfiguration.getOauthEmailAddress(),
                        messagingConfiguration.getOauthAccessToken());
                messagingConfiguration.setOauthAccessToken(result.second);
                ImapServer imapServer = new ImapServer("imap.gmail.com", 993, ServerSecurity.SSL, true,
                        new ServerCredentials(result.first));
                return receiver.receiveMail(imapServer, subjectChecker, userData.getMessagingConfiguration()
                        .getEmailSubjectPrefix(), emailAddress, fromAddresses, lastEmailDate, mailboxName);
            }
            else
                throw new OperationFailedException("Unkonwn gateway: " + messagingConfiguration.getGatewayType());
        }
        catch (Exception e)
        {
            throw new OperationFailedException(e.getMessage(), e);
        }
    }

    static String getNumber(Context context, UserData userData, String subject) throws OperationFailedException
    {
        // TODO: Formats probably don't belong to strings.
        String prefix = userData.getMessagingConfiguration().getEmailSubjectPrefix();
        if (!subject.contains(prefix))
            throw new OperationFailedException("Invalid subject: " + subject);
        String[] patterns = {
                String.format(context.getString(R.string.str_email_subject_pattern_1), Pattern.quote(prefix)),
                String.format(context.getString(R.string.str_email_subject_pattern_2), Pattern.quote(prefix)),
                String.format(context.getString(R.string.str_email_subject_pattern_3), Pattern.quote(prefix)),
                String.format(context.getString(R.string.str_email_subject_pattern_4), Pattern.quote(prefix)) };
        String numberStr = StringUtil.stripMatchingRegex(patterns, subject);
        if (numberStr == null)
            throw new OperationFailedException("Invalid subject: " + subject);
        if (numberStr.toLowerCase(Locale.US).startsWith("from "))
            throw new OperationFailedException("Invalid subject: " + subject);
        numberStr = numberStr.trim().replaceAll("[^\\w()+-]+.*", "").trim();
        if (StringUtil.empty(numberStr))
            throw new OperationFailedException("Invalid subject: " + subject);
        return numberStr;
    }
}
