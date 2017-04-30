package com.tinywebgears.relayme.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import android.content.Intent;

import com.codolutions.android.common.exception.OperationFailedException;
import com.tinywebgears.relayme.R;
import com.tinywebgears.relayme.app.CustomApplication;
import com.tinywebgears.relayme.app.SystemStatus;
import com.tinywebgears.relayme.app.UserData;
import com.tinywebgears.relayme.email.EmailReceiver;
import com.tinywebgears.relayme.email.EmailSender;
import com.tinywebgears.relayme.email.ImapEmailReceiver;
import com.tinywebgears.relayme.email.SmtpEmailSender;
import com.tinywebgears.relayme.model.ImapServer;
import com.tinywebgears.relayme.model.SmtpServer;

public class EmailIntentService extends BasicIntentService
{
    private static final String TAG = EmailIntentService.class.getName();
    public static final String ACTION_CODE_LOAD_MAILBOX_NAMES = "load-mailbox-names";
    public static final String ACTION_RESPONSE_LOAD_MAILBOX_NAMES = EmailIntentService.class.getCanonicalName()
            + ".RESPONSE_MAILBOX_NAMES";
    public static final String ACTION_CODE_CHECK_SMTP_SERVER = "check-smtp-server";
    public static final String PARAM_IN_SMTP_SERVER = "smtp-server";
    public static final String ACTION_RESPONSE_CHECK_SMTP_SERVER = MessagingIntentService.class.getCanonicalName()
            + ".RESPONSE_CHECK_SMTP_SERVER";
    public static final String ACTION_CODE_CHECK_IMAP_SERVER = "check-imap-server";
    public static final String PARAM_IN_IMAP_SERVER = "imap-server";
    public static final String ACTION_RESPONSE_CHECK_IMAP_SERVER = MessagingIntentService.class.getCanonicalName()
            + ".RESPONSE_CHECK_IMAP_SERVER";
    public static final Map<String, String> ACTIONS = new HashMap<String, String>()
    {
        {
            put(ACTION_CODE_LOAD_MAILBOX_NAMES, ACTION_RESPONSE_LOAD_MAILBOX_NAMES);
            put(ACTION_CODE_CHECK_SMTP_SERVER, ACTION_RESPONSE_CHECK_SMTP_SERVER);
            put(ACTION_CODE_CHECK_IMAP_SERVER, ACTION_RESPONSE_CHECK_IMAP_SERVER);
        }
    };

    public static final String PARAM_IN_VERIFIER_URI = "verifier-uri";
    public static final String PARAM_OUT_AUTHORIZATION_URI = "authorization-uri";

    protected SystemStatus systemStatus;
    protected UserData userData;
    protected EmailService emailService;

    public EmailIntentService()
    {
        super("EmailIntentService");
    }

    @Override
    protected ActionResponse handleAction(String action, long actionId, Intent intent)
    {
        LogStoreHelper.info(this, "A new intent received by EmailIntentService: " + action);
        systemStatus = ((CustomApplication) getApplication()).getSystemStatus();
        userData = ((CustomApplication) getApplication()).getUserData();
        emailService = new EmailService(this, systemStatus, userData);

        if (ACTION_CODE_LOAD_MAILBOX_NAMES.equalsIgnoreCase(action))
        {
            LogStoreHelper.info(this, "Loading list of mailboxes...");
            if (!userData.getMessagingConfiguration().isEmailReplyGatewaySetUp())
            {
                LogStoreHelper.info(this, "Reply gateway is not set up.");
                return ActionResponse.DROP;
            }
            try
            {
                List<String> mailboxNames = emailService.readMailboxes(userData);
                LogStoreHelper.info(this, "Mailboxes: " + mailboxNames);
                userData.getMessagingConfiguration().setAllMailboxes(new HashSet<String>(mailboxNames));
                return ActionResponse.SUCCESS;
            }
            catch (OperationFailedException e)
            {
                LogStoreHelper.warn(this, "Unable to read name of mailboxes: " + e.getMessage(), e);
                return ActionResponse.FAILURE;
            }
        }
        else if (ACTION_CODE_CHECK_SMTP_SERVER.equalsIgnoreCase(action))
        {
            SmtpServer smtpServer = intent.getParcelableExtra(PARAM_IN_SMTP_SERVER);
            if (smtpServer == null || !smtpServer.isValid())
                return ActionResponse
                        .FAILURE_WITH_ERROR_MESSAGE(getString(R.string.lbl_invalid_smtp_server_configuration));
            else
            {
                EmailSender sender = new SmtpEmailSender(this);
                try
                {
                    sender.test(smtpServer);
                    return ActionResponse.SUCCESS;
                }
                catch (OperationFailedException e)
                {
                    return ActionResponse.FAILURE_WITH_ERROR_MESSAGE(getString(
                            R.string.lbl_smtp_server_connection_failed, e.getMessage()));
                }
            }
        }
        else if (ACTION_CODE_CHECK_IMAP_SERVER.equalsIgnoreCase(action))
        {
            ImapServer imapServer = intent.getParcelableExtra(PARAM_IN_IMAP_SERVER);
            if (imapServer == null || !imapServer.isValid())
                return ActionResponse
                        .FAILURE_WITH_ERROR_MESSAGE(getString(R.string.lbl_invalid_imap_server_configuration));
            else
            {
                EmailReceiver receiver = new ImapEmailReceiver(this, null);
                try
                {
                    receiver.test(imapServer);
                    return ActionResponse.SUCCESS;
                }
                catch (OperationFailedException e)
                {
                    return ActionResponse.FAILURE_WITH_ERROR_MESSAGE(getString(
                            R.string.lbl_imap_server_connection_failed, e.getMessage()));
                }
            }
        }
        return ActionResponse.DROP;
    }

    @Override
    protected String getResponseAction(String requestAction)
    {
        return ACTIONS.get(requestAction);
    }
}
