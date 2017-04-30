package com.tinywebgears.relayme.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Set;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony;

import com.codolutions.android.common.exception.OperationFailedException;
import com.codolutions.android.common.util.DateUtil;
import com.codolutions.android.common.util.Pair;
import com.codolutions.android.common.util.StringUtil;
import com.tinywebgears.relayme.R;
import com.tinywebgears.relayme.app.CustomApplication;
import com.tinywebgears.relayme.app.UserData;
import com.tinywebgears.relayme.model.EventType;
import com.tinywebgears.relayme.model.IncomingEmail;
import com.tinywebgears.relayme.model.Message;
import com.tinywebgears.relayme.model.MessageStatus;
import com.tinywebgears.relayme.model.MessageType;
import com.tinywebgears.relayme.model.MessagingConfiguration;
import com.tinywebgears.relayme.model.ServiceConfiguration;

public class MessagingIntentService extends BasicMessagingIntentService
{
    private static final String TAG = MessagingIntentService.class.getName();

    public static final String ACTION_CODE_INCOMING_SMS = "relay-incoming-sms";
    public static final String ACTION_CODE_INCOMING_MMS = "relay-incoming-mms";
    public static final String ACTION_CODE_MISSED_CALL = "relay-missed-call";
    public static final String ACTION_CODE_MESSAGE_TRY = "record-message-try";
    public static final String ACTION_CODE_PROCESS_MESSAGES = "resend-pending-message";
    public static final String ACTION_CODE_RESEND_MESSAGE = "resend-message";
    public static final String ACTION_CODE_CHECK_FOR_NEW_EMAILS = "check-for-new-emails";
    public static final String ACTION_CODE_CHECK_FOR_NEW_SMS = "check-for-new-sms";

    private static final int RETRY_WAIT_BASE_TIME_IN_SECONDS = 30;
    private static final int MAX_SMS_MESSAGE_LENGTH = 465;
    private static final int MESSAGE_SENDING_TIMEOUT_IN_SECONDS = 3 * 60;
    private static final int MAX_MESSAGE_AGE_IN_HOURS = 8;
    private static final int MAX_MESSAGE_AGE_IN_SECONDS = MAX_MESSAGE_AGE_IN_HOURS * DateUtil.HOUR_IN_S;

    public MessagingIntentService()
    {
        super("MessagingIntentService");
    }

    @Override
    protected void doWakefulWork(Intent intent)
    {
        systemStatus = ((CustomApplication) getApplication()).getSystemStatus();
        userData = ((CustomApplication) getApplication()).getUserData();
        emailService = new EmailService(this, systemStatus, userData);

        if (intent == null)
            return;
        String action = intent.getAction();
        LogStoreHelper.info(this, "A new intent received by MessagingIntentService:" + action);
        if (ACTION_CODE_INCOMING_MMS.equalsIgnoreCase(action))
        {
            String key = intent.getStringExtra(PARAM_IN_KEY);
            String sender = intent.getStringExtra(PARAM_IN_NUMBER);
            String messageText = getString(R.string.str_you_received_mms);
            onAction(EventType.MMS, key, new Date(), sender, messageText);
        }
        else if (ACTION_CODE_MISSED_CALL.equalsIgnoreCase(action))
        {
            String key = intent.getStringExtra(PARAM_IN_KEY);
            String caller = intent.getStringExtra(PARAM_IN_NUMBER);
            String messageText = getString(R.string.str_you_missed_call);
            onAction(EventType.MISSED_CALL, key, new Date(), caller, messageText);
        }
        else if (ACTION_CODE_MESSAGE_TRY.equalsIgnoreCase(action))
        {
            final long messageId = intent.getLongExtra(PARAM_IN_MESSAGE_ID, 0);
            final boolean status = intent.getBooleanExtra(PARAM_IN_MESSAGE_STATUS, false);
            final Date dateTried = new Date(intent.getLongExtra(PARAM_IN_MESSAGE_DATE_TRIED, 0));
            unlockSending(messageId, status, dateTried, null);
        }
        else if (ACTION_CODE_PROCESS_MESSAGES.equalsIgnoreCase(action))
        {
            updateProgressingMessagesAction();
            sendAllPendingMessagesAction();
        }
        else if (ACTION_CODE_RESEND_MESSAGE.equalsIgnoreCase(action))
        {
            final long messageId = intent.getLongExtra(PARAM_IN_MESSAGE_ID, 0);
            sendMessageAction(messageId);
        }
        else if (ACTION_CODE_INCOMING_SMS.equalsIgnoreCase(action))
        {
            Date lastSmsTimestamp = getLastSmsTimestamp();
            int round = 1;
            // Allow up to 15 seconds for the new message to become available in the content provider.
            for (; round <= 3; round++)
            {
                checkForNewSms();
                Date lastSmsTimestampNow = getLastSmsTimestamp();
                if (DateUtil.isAfter(lastSmsTimestamp, lastSmsTimestampNow))
                {
                    LogStoreHelper.info(this, "Found new SMS, last timestamp: " + lastSmsTimestampNow);
                    break;
                }
                try
                {
                    Thread.sleep(5000 * round);
                }
                catch (InterruptedException e)
                {
                    LogStoreHelper.info(this, "I was interrupted when checking new SMS");
                }
            }
            if (round > 3)
                LogStoreHelper.info(this, "Couldn't get the new SMS after 3 tries");
        }
        else if (ACTION_CODE_CHECK_FOR_NEW_SMS.equalsIgnoreCase(action))
        {
            checkForNewSms();
        }
        else if (ACTION_CODE_CHECK_FOR_NEW_EMAILS.equalsIgnoreCase(action))
        {
            Date lastCheckDate = userData.getMessagingConfiguration().getLastCheck();
            if (lastCheckDate == null)
            {
                lastCheckDate = new Date();
                userData.getMessagingConfiguration().setLastCheck(lastCheckDate);
            }
            if (DateUtil.secondsPassedSince(lastCheckDate) > MAX_MESSAGE_AGE_IN_SECONDS)
                lastCheckDate = DateUtil.addHours(new Date(), -MAX_MESSAGE_AGE_IN_HOURS);
            Date lastEmailDate = userData.getMessagingConfiguration().getLastEmailDate();
            if (lastEmailDate == null)
            {
                lastEmailDate = new Date();
                userData.getMessagingConfiguration().setLastEmailDate(lastEmailDate);
            }
            if (DateUtil.secondsPassedSince(lastEmailDate) > MAX_MESSAGE_AGE_IN_SECONDS)
                lastEmailDate = DateUtil.addHours(new Date(), -MAX_MESSAGE_AGE_IN_HOURS);
            android.util.Pair<Date, Date> result = checkIncomingEmailsAction(lastCheckDate, lastEmailDate);
            if (result.first.after(lastCheckDate))
                userData.getMessagingConfiguration().setLastCheck(result.first);
            if (result.second.after(lastEmailDate))
                userData.getMessagingConfiguration().setLastEmailDate(result.second);
        }
    }

    // Send all pending messages.
    private void sendAllPendingMessagesAction()
    {
        LogStoreHelper.info(this, "Resending all pending messages...");
        // Check pre-conditions.
        if (!userData.getServiceConfiguration().isActive())
        {
            LogStoreHelper.info(this, "The application is not active now.");
            return;
        }
        if (!systemStatus.isThereAnyPendingMessage())
            return;
        try
        {
            userData.setSendingMessagesNow(true);
            for (Message m : MessageStoreHelper.getPendingMessages(this))
            {
                Message message = getMessageIfNotBeingSent(this, m.getId());
                if (message == null)
                    continue;
                if (DateUtil.secondsPassedSince(message.getTimestamp()) > MAX_MESSAGE_AGE_IN_SECONDS)
                {
                    LogStoreHelper.info(this, "Message is too old, ID: " + message.getId());
                    message.setStatus(MessageStatus.PERMANENTLY_FAILED);
                    boolean updateResult = updateMessage(message) > 0;
                    LogStoreHelper.info(this, "Message updated: " + updateResult);
                    continue;
                }
                LogStoreHelper.info(this, "Resending message, ID: " + message.getId());
                long timePassed = DateUtil.secondsPassedSince(message.getDateUpdated());
                long timeRequiredToPass = (long) Math.pow(2, message.getTries()) * RETRY_WAIT_BASE_TIME_IN_SECONDS;
                if (timePassed < timeRequiredToPass)
                {
                    LogStoreHelper.info(this, "Too early to retry, will wait for another "
                            + (timeRequiredToPass - timePassed) + " seconds.");
                    continue;
                }
                if (message.getMessageType() == MessageType.INCOMING || message.getMessageType() == MessageType.STATUS)
                    sendEmailMessage(message, false);
                else if (message.getMessageType() == MessageType.OUTGOING)
                    sendSmsMessage(message, false);
            }
        }
        catch (Exception e)
        {
            LogStoreHelper.warn(this, "Error sending messages: " + e.getMessage(), e);
        }
        finally
        {
            userData.setSendingMessagesNow(false);
        }
    }

    // Send all pending messages.
    private void updateProgressingMessagesAction()
    {
        LogStoreHelper.info(this, "Updating all progressing messages...");
        Date now = new Date();
        for (Message message : MessageStoreHelper.getProgressingMessages(this))
        {
            if (DateUtil.getTimePassedInSeconds(message.getDateUpdated(), now) > MESSAGE_SENDING_TIMEOUT_IN_SECONDS)
            {
                LogStoreHelper.info(this, "This message has been in flux for so long: " + message
                        + ", assuming it has failed.");
                unlockSending(message.getId(), false, message.getDateTried(), "Timed Out");
            }
        }
    }

    // Send/resend a particular message.
    private void sendMessageAction(long messageId)
    {
        LogStoreHelper.info(this, "Resending message, ID: " + messageId);
        Message message = getMessageIfNotBeingSent(this, messageId);
        if (message == null)
            return;
        if (message.getMessageType() == MessageType.INCOMING || message.getMessageType() == MessageType.STATUS)
            sendEmailMessage(message, true);
        else if (message.getMessageType() == MessageType.OUTGOING)
            sendSmsMessage(message, true);
    }

    private void checkForNewSms()
    {
        Date lastSmsTimestamp = getLastSmsTimestamp();
        Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"),
                new String[] { "_id", "type", "address", "date", "body" }, "date > ?",
                new String[] { Long.toString(lastSmsTimestamp.getTime()) }, "date ASC");
        try
        {
            int count = cursor.getCount();
            LogStoreHelper
                    .info(this, "Number of messages found since " + DateUtil.getRelativeDateString(lastSmsTimestamp)
                            + ": " + count);
            if (count > 0)
            {
                if (cursor.moveToFirst())
                {
                    for (int i = 0; i < count; i++)
                    {
                        long id = cursor.getLong(cursor.getColumnIndexOrThrow(Telephony.Sms.Inbox._ID));
                        String sender = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                        Date date = new Date(cursor.getLong(cursor.getColumnIndexOrThrow("date")));
                        String text = cursor.getString(cursor.getColumnIndexOrThrow("body"));
                        LogStoreHelper.info(this, "SMS " + id + " from " + sender + " arrived on " + date);
                        onAction(EventType.SMS, Long.toString(id), date, sender, text);
                        if (DateUtil.isAfter(lastSmsTimestamp, date))
                            userData.setLastSmsTimestamp(date);
                        cursor.moveToNext();
                    }
                }
                else
                    LogStoreHelper.warn(this, "Couldn't read from SMS content provider", null);
            }
        }
        finally
        {
            cursor.close();
        }
    }

    private Date getLastSmsTimestamp()
    {
        Date lastSmsTimestamp = userData.getLastSmsTimestamp();
        if (lastSmsTimestamp == null)
        {
            lastSmsTimestamp = new Date();
            userData.setLastSmsTimestamp(lastSmsTimestamp);
        }
        return lastSmsTimestamp;
    }

    private Date getMinTimeToCheckIncomingEmails(Date lastCheckDate)
    {
        Date lastMessageSentDate = MessageStoreHelper.getLastTimeMessagesSent(getApplicationContext());
        long secondsSinceLastMessageSent = Math.max(0, DateUtil.secondsPassedSince(lastMessageSentDate));
        // We calculate a range between 1 to 20, for the time passed since last message sent. It stays under 4 for times
        // up to 2 minutes, then slows down so that it hardly moves above 16 after 120 minutes.
        double rawRange = 2 * Math.log(Math.max(secondsSinceLastMessageSent, 1)) / Math.log(2) - 10;
        double normalizedRange = Math.max(1, Math.min(20, rawRange));
        UserData userData = ((CustomApplication) getApplication()).getUserData();
        int factor = userData.getMessagingConfiguration().getPollingFactor().getDurationCoefficientInSeconds();
        // Return a date that is at least 30 seconds bigger than last check date.
        int interval = Math.max((int) (factor * normalizedRange), 30);
        return DateUtil.addSeconds(lastCheckDate, interval);
    }

    // Check for incoming emails, add anything found, and try sending them right away.
    private android.util.Pair<Date, Date> checkIncomingEmailsAction(Date lastCheckDate, Date lastEmailDate)
    {
        LogStoreHelper.info(this, "Checking for incoming emails...");
        Date now = new Date();
        // Check pre-conditions.
        MessagingConfiguration messagingConfiguration = userData.getMessagingConfiguration();
        if (!messagingConfiguration.isSetUp())
        {
            LogStoreHelper.warn(this, "The application is not set up yet.", null);
            return new Pair<Date, Date>(now, lastEmailDate);
        }
        if (!messagingConfiguration.isEmailReplyGatewaySetUp())
        {
            LogStoreHelper.warn(this, "Reply gateway is not set up yet.", null);
            return new Pair<Date, Date>(now, lastEmailDate);
        }
        ServiceConfiguration serviceConfiguration = userData.getServiceConfiguration();
        if (!serviceConfiguration.isActive())
        {
            LogStoreHelper.info(this, "The application is not active now.");
            return new Pair<Date, Date>(now, lastEmailDate);
        }
        if (!serviceConfiguration.isReplyingEnabled())
        {
            LogStoreHelper.info(this, "Replying is disabled.");
            return new Pair<Date, Date>(now, lastEmailDate);
        }
        if (!systemStatus.isOnline())
        {
            LogStoreHelper.info(this, "No Internet connection.");
            return new Pair<Date, Date>(now, lastEmailDate);
        }
        Date lastMessageSentDate = MessageStoreHelper.getLastTimeMessagesSent(getApplicationContext());
        Date minTimeToCheck = getMinTimeToCheckIncomingEmails(lastCheckDate);
        LogStoreHelper.info(this, String.format("Last message sent: %d seconds ago, last time checked: %d "
                + "seconds ago, last email found: %d seconds ago", DateUtil.secondsPassedSince(lastMessageSentDate),
                DateUtil.secondsPassedSince(lastCheckDate), DateUtil.secondsPassedSince(lastEmailDate)));
        if (!now.after(minTimeToCheck))
        {
            long secondsToWait = (minTimeToCheck.getTime() - now.getTime()) / 1000;
            LogStoreHelper.info(this, String.format(
                    "Not enough time passed to check for replies. Will try in %d seconds.", secondsToWait));
            return new Pair<Date, Date>(lastCheckDate, lastEmailDate);
        }
        try
        {
            LogStoreHelper.info(this, "Checking now");
            // Allow some time (a few minutes) for time mismatch in email dates.
            String mailbox = userData.getMessagingConfiguration().getReplyMailboxName();
            Collection<String> fromAddresses = new ArrayList<String>();
            fromAddresses.add(StringUtil.unaliasEmailAddress(userData.getMessagingConfiguration()
                    .getTargetEmailAddress()));
            if (!StringUtil.empty(userData.getMessagingConfiguration().getCcEmailAddress()))
                fromAddresses.add(StringUtil.unaliasEmailAddress(userData.getMessagingConfiguration()
                        .getCcEmailAddress()));
            boolean replyingFromDifferentEmail = userData.getServiceConfiguration().isReplyingFromDifferentEmail();
            if (replyingFromDifferentEmail
                    && !StringUtil.empty(userData.getServiceConfiguration().getReplySourceEmailAddress()))
                fromAddresses.add(StringUtil.unaliasEmailAddress(userData.getServiceConfiguration()
                        .getReplySourceEmailAddress()));
            Pair<Date, Set<IncomingEmail>> receiveResult = emailService.receiveEmail(userData, lastEmailDate,
                    fromAddresses, mailbox);
            Set<IncomingEmail> incomingEmails = receiveResult.second;
            onIncomingEmails(incomingEmails);
            return new Pair<Date, Date>(now, receiveResult.first);
        }
        catch (OperationFailedException e)
        {
            LogStoreHelper.warn(this, "Error checking for incoming emails: " + e.getMessage(), e, false);
            return new Pair<Date, Date>(now, lastEmailDate);
        }
    }

    private void onIncomingEmails(Set<IncomingEmail> incomingEmails)
    {
        for (IncomingEmail incomingEmail : incomingEmails)
        {
            LogStoreHelper.info(
                    this,
                    "Email received, key: " + incomingEmail.getMessageId() + " date: " + incomingEmail.getDate()
                            + " subject: " + incomingEmail.getSubject() + " body: "
                            + StringUtil.shorten(incomingEmail.getBody(), 16));
            try
            {
                String text = incomingEmail.getBody();
                String number = EmailService.getNumber(this, userData, incomingEmail.getSubject());
                String messageText = text;
                if (messageText.length() > MAX_SMS_MESSAGE_LENGTH)
                    messageText = text.substring(0, MAX_SMS_MESSAGE_LENGTH);
                Message message = new Message(incomingEmail.getMessageId(), EventType.SMS, MessageType.OUTGOING,
                        number, messageText, incomingEmail.getDate());
                Uri uri = insertMessage(message);
                if (uri != null)
                {
                    LogStoreHelper.info(this, "Message inserted: " + uri);
                    sendSmsMessage(message, false);
                }
            }
            catch (Exception e)
            {
                LogStoreHelper.warn(this, "Unable to process email: " + e.getMessage(), e);
            }
        }
    }
}
