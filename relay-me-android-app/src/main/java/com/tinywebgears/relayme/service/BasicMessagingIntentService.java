package com.tinywebgears.relayme.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.telephony.SmsManager;
import android.util.Log;

import com.codolutions.android.common.util.DateUtil;
import com.codolutions.android.common.util.StringUtil;
import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.tinywebgears.relayme.R;
import com.tinywebgears.relayme.app.SystemStatus;
import com.tinywebgears.relayme.app.UserData;
import com.tinywebgears.relayme.common.Constants;
import com.tinywebgears.relayme.contentprovider.MessagesContentProvider;
import com.tinywebgears.relayme.model.EventType;
import com.tinywebgears.relayme.model.Message;
import com.tinywebgears.relayme.model.MessageStatus;
import com.tinywebgears.relayme.model.MessageType;
import com.tinywebgears.relayme.model.MessagingConfiguration;
import com.tinywebgears.relayme.model.ServiceStatus;
import com.tinywebgears.relayme.service.ContactsIntentService.ContactsLoader;
import com.tinywebgears.relayme.service.sms.SmsReceiver;

// TODO: Later: Share code with BasicIntentService
public abstract class BasicMessagingIntentService extends WakefulIntentService
{
    private static final String TAG = BasicMessagingIntentService.class.getName();

    private static final String TELEPHON_NUMBER_FIELD_NAME = "address";
    private static final String MESSAGE_BODY_FIELD_NAME = "body";
    private static final Uri SENT_MSGS_CONTET_PROVIDER = Uri.parse("content://sms/sent");

    public static final String PARAM_IN_KEY = "in-key";
    public static final String PARAM_IN_NUMBER = "in-number";
    public static final String PARAM_IN_TEXT = "in-text";
    public static final String PARAM_IN_MESSAGE_ID = "in-message-id";
    public static final String PARAM_IN_MESSAGE_STATUS = "in-message-status";
    public static final String PARAM_IN_MESSAGE_DATE_TRIED = "in-message-date-tried";

    private static final int MAX_MESSAGE_SEND_TRIES = 8;
    private static final int MESSAGE_LOCK_TIMEOUT_IN_SECONDS = 60;

    // FIXME: TECHDEBT: Fix this hack. These fields are are initialized in the onHandleIntent() method of subtypes.
    protected SystemStatus systemStatus;
    protected UserData userData;
    protected EmailService emailService;

    public BasicMessagingIntentService(String name)
    {
        super(name);
    }

    // A new event arrived. Store and try sending it right away.
    protected void onAction(final EventType eventType, final String key, final Date date, final String sender,
            final String text)
    {
        LogStoreHelper.info(this, "Event " + eventType + ", key: " + key + ", sender: " + sender + " date: " + date);
        // Is this a control command?
        if (userData.getServiceConfiguration().getServiceStatus() != ServiceStatus.ENABLED)
        {
            if (eventType == EventType.SMS
                    && StringUtil.nonEmpty(text)
                    && text.toUpperCase(Locale.US).contains(
                            userData.getServiceConfiguration().getActivationCode().toUpperCase(Locale.US)))
            {
                userData.getServiceConfiguration().setServiceStatus(ServiceStatus.ENABLED);
                LogStoreHelper.info(this, "The application is activated now.");
                Intent i = new Intent(Constants.STATUS_UPDATE_BROADCAST_ACTION).putExtra(
                        Constants.STATUS_UPDATE_EXTRA_WHAT, Constants.EVENT_SERVICE_STATUS_CHANGED);
                sendBroadcast(i);
            }
        }
        // Check pre-conditions.
        if (!userData.getServiceConfiguration().isActive())
        {
            LogStoreHelper.info(this, "The application is not active now.");
            return;
        }
        if ((eventType == EventType.MISSED_CALL)
                && !userData.getServiceConfiguration().isMissedCallNotificationEnabled())
        {
            LogStoreHelper.info(this, "Ignoring missed call notifications.");
            return;
        }
        try
        {
            Message message = new Message(key, eventType, MessageType.INCOMING, sender, text, date);
            Uri uri = insertMessage(message);
            if (uri != null)
            {
                LogStoreHelper.info(this, "New message inserted: " + uri);
                sendEmailMessage(message, false);
            }
            // Update the UI reflecting the fact that we were able to receive texts.
            userData.checkMessagesAndSetFlags();
        }
        catch (Exception e)
        {
            LogStoreHelper.warn(this, "Unable to insert new incoming message.", e);
        }
    }

    protected void sendEmailMessage(Message message, boolean force)
    {
        // Check pre-conditions.
        if (!systemStatus.isOnline())
        {
            LogStoreHelper.info(this, "No Internet connection.");
            return;
        }
        if ((message.getEventType() == EventType.MISSED_CALL)
                && !userData.getServiceConfiguration().isMissedCallNotificationEnabled())
        {
            LogStoreHelper.info(this, "Ignoring missed call notifications.");
            return;
        }
        MessagingConfiguration messagingConfiguration = userData.getMessagingConfiguration();
        if (!messagingConfiguration.isSetUp())
        {
            LogStoreHelper.warn(this, "The application is not set up yet.", null);
            return;
        }
        try
        {
            LogStoreHelper.info(this, "Sending email message...");
            String subject = "";
            ContactsLoader contactsLoader = new ContactsLoader(getApplicationContext(), userData);
            contactsLoader.loadContacts();
            if (StringUtil.empty(message.getPhoneNumber()))
            {
                Log.d(TAG, "Ignoring message with empty phone number.");
                return;
            }
            String contactName = ContactsIntentService
                    .findContactName(userData.getContacts(), message.getPhoneNumber());
            if (message.getMessageType() == MessageType.INCOMING)
            {
                String eventType = getString(message.getEventType().getNameResource());
                subject = getString(R.string.str_email_subject, eventType, message.getPhoneNumber());
                if (StringUtil.nonEmpty(contactName))
                    subject = getString(R.string.str_email_subject_with_name, eventType, message.getPhoneNumber(),
                            contactName);
                String prefix = userData.getMessagingConfiguration().getEmailSubjectPrefix();
                if (!StringUtil.empty(prefix))
                    subject = prefix + " " + subject;
            }
            else if (message.getMessageType() == MessageType.STATUS)
            {
                subject = getString(R.string.str_email_subject_status, message.getPhoneNumber());
                if (StringUtil.nonEmpty(contactName))
                    subject = getString(R.string.str_email_subject_status_name, message.getPhoneNumber(), contactName);
                String prefix = userData.getMessagingConfiguration().getEmailSubjectPrefix();
                if (!StringUtil.empty(prefix))
                    subject = prefix + " " + subject;
            }
            message = lockForSending(message.getId(), getValidStartStatesForLocking(force));
            if (message == null)
            {
                Log.d(TAG, "Unable to lock the message for sending.");
                return;
            }
            // Try to send the message.
            String targetEmailAddress = userData.getMessagingConfiguration().getTargetEmailAddress();
            String ccEmailAddress = userData.getMessagingConfiguration().getCcEmailAddress();
            String timestamps = getString(R.string.txt_message_timestamps, message.getTimestamp());
            emailService.sendEmail(userData, targetEmailAddress, ccEmailAddress, message.getPhoneNumber(), subject, "",
                    message.getBody() + timestamps);
            unlockSending(message.getId(), true, message.getDateTried(), null);
        }
        catch (Exception e)
        {
            LogStoreHelper.warn(this, "Error sending emails: " + e.getMessage(), e);
            unlockSending(message.getId(), false, message.getDateTried(), e.getMessage());
        }
    }

    protected void sendSmsMessage(final Message msg, boolean force)
    {
        // Check pre-conditions.
        if (!systemStatus.isNetworkAvailable(true))
        {
            LogStoreHelper.info(this, "No network.");
            return;
        }
        if (!userData.getServiceConfiguration().isActive())
        {
            LogStoreHelper.info(this, "The application is not active now.");
            return;
        }
        if (!userData.getServiceConfiguration().isReplyingEnabled())
        {
            LogStoreHelper.info(this, "Replying is disabled.");
            return;
        }

        Message message = lockForSending(msg.getId(), getValidStartStatesForLocking(force));
        if (message == null)
        {
            LogStoreHelper.info(this, "Unable to lock the message " + msg.getId()
                    + " for sending, maybe it is being sent.");
            return;
        }
        long messageId = message.getId();
        String number = message.getPhoneNumber();
        String textToSend = message.getBody();
        LogStoreHelper.info(this, "Sending SMS to: " + number + " message ID: " + messageId);
        SmsManager smsManager = SmsManager.getDefault();
        ArrayList<String> messageTextParts = smsManager.divideMessage(textToSend);
        Intent sentIntent = new Intent(SmsReceiver.SMS_SENT_BROADCAST_ACTION);
        sentIntent.putExtra(SmsReceiver.SMS_SENT_EXTRA_ID, messageId);
        sentIntent.putExtra(SmsReceiver.SMS_SENT_EXTRA_TRY_DATE, message.getDateTried().getTime());
        sentIntent.putExtra(SmsReceiver.SMS_SENT_EXTRA_PARTS, messageTextParts.size());
        int requestCode = (int) (Long.reverse(messageId) ^ message.getDateTried().getTime());
        if (messageTextParts.size() > 1)
        {
            ArrayList<PendingIntent> sentPIs = new ArrayList<PendingIntent>();
            for (int i = 0; i < messageTextParts.size(); i++)
                sentPIs.add(PendingIntent.getBroadcast(getApplicationContext(), (int) requestCode, sentIntent, 0));
            smsManager.sendMultipartTextMessage(number, null, messageTextParts, sentPIs, null);
        }
        else
        {
            PendingIntent sentPI = PendingIntent
                    .getBroadcast(getApplicationContext(), (int) requestCode, sentIntent, 0);
            smsManager.sendTextMessage(number, null, textToSend, sentPI, null);
        }

        ContentValues sentSms = new ContentValues();
        sentSms.put(TELEPHON_NUMBER_FIELD_NAME, number);
        sentSms.put(MESSAGE_BODY_FIELD_NAME, textToSend);

        ContentResolver contentResolver = getApplicationContext().getContentResolver();
        contentResolver.insert(SENT_MSGS_CONTET_PROVIDER, sentSms);
    }

    protected Set<MessageStatus> getValidStartStatesForLocking(boolean force)
    {
        if (!force)
            return new HashSet<MessageStatus>(Arrays.asList(MessageStatus.NEW, MessageStatus.FAILED));
        return new HashSet<MessageStatus>(Arrays.asList(MessageStatus.NEW, MessageStatus.SENT, MessageStatus.FAILED,
                MessageStatus.PERMANENTLY_FAILED));
    }

    protected Message getMessageIfNotBeingSent(Context context, long messageId)
    {
        Message messageFound = MessageStoreHelper.findMessageById(this, messageId);
        if (messageFound == null)
        {
            LogStoreHelper.info(context, "No message found for ID: " + messageId);
            return null;
        }
        if (messageFound.getStatus() == MessageStatus.SENDING)
        {
            if (DateUtil.secondsPassedSince(messageFound.getDateTried()) > MESSAGE_LOCK_TIMEOUT_IN_SECONDS)
            {
                LogStoreHelper.warn(context, "The lock on message " + messageId + " is stale, timing out.", null);
                return messageFound;
            }
            else
            {
                LogStoreHelper.info(context, "The message " + messageId
                        + " is currently being sent and cannot be worked on.");
                return null;
            }
        }
        return messageFound;
    }

    protected Message lockForSending(long messageId, Set<MessageStatus> validStatuses)
    {
        LogStoreHelper.info(this, "Locking message for sending, ID: " + messageId);
        Message messageFound = MessageStoreHelper.findMessageById(this, messageId);
        if (!validStatuses.contains(messageFound.getStatus()))
        {
            Log.d(TAG, "Message status: " + messageFound.getStatus() + ", not in " + validStatuses);
            return null;
        }
        messageFound.setStatus(MessageStatus.SENDING);
        messageFound.incrementTries();
        messageFound.setDateTried(new Date());
        updateMessage(messageFound);
        LogStoreHelper.info(this, "Locking " + messageId + " done.");
        getContentResolver().notifyChange(MessagesContentProvider.CONTENT_URI, null);
        sendBroadcast(new Intent(Constants.STATUS_UPDATE_BROADCAST_ACTION).putExtra(Constants.STATUS_UPDATE_EXTRA_WHAT,
                Constants.EVENT_STATUS_OF_MESSAGES_CHANGED));
        return messageFound;
    }

    protected boolean unlockSending(long messageId, boolean result, Date dateTried, String error)
    {
        LogStoreHelper.info(this, "Unlocking message, ID: " + messageId + " status:" + result + " date tried: "
                + dateTried + " error: " + error);
        Message message = MessageStoreHelper.findMessageById(this, messageId);
        if (message == null)
        {
            LogStoreHelper.info(this, "We received receipt for a non-existing message, ID: " + messageId);
            return false;
        }
        if (dateTried == null || message.getDateTried() == null || !message.getDateTried().equals(dateTried))
        {
            LogStoreHelper.info(this, "Date tried we received was " + dateTried + " but we have this in database: "
                    + message.getDateTried());
            return false;
        }
        if (result && message.getStatus() != MessageStatus.SENDING)
        {
            LogStoreHelper.info(this,
                    "Ignoring extra confirmation while we have already got the status: " + message.getStatus());
            return false;
        }
        int tries = message.getTries();
        MessageStatus newStatus = result ? MessageStatus.SENT
                : tries >= MAX_MESSAGE_SEND_TRIES ? MessageStatus.PERMANENTLY_FAILED : MessageStatus.FAILED;
        message.setStatus(newStatus);
        boolean updateResult = updateMessage(message) > 0;
        boolean userWantsStatusReport = userData.getServiceConfiguration().isReplyingStatusReportEnabled();
        if (userWantsStatusReport
                && message.getMessageType() == MessageType.OUTGOING
                && (message.getStatus() == MessageStatus.SENT || message.getStatus() == MessageStatus.PERMANENTLY_FAILED))
        {
            String messageText = "";
            if (message.getStatus() == MessageStatus.SENT)
                messageText = getString(R.string.str_email_status_body_succeeded, message.getBody());
            else if (message.getStatus() == MessageStatus.PERMANENTLY_FAILED)
                messageText = getString(R.string.str_email_status_body_failed, message.getBody());
            Message statusMessage = new Message("", EventType.SMS, MessageType.STATUS, message.getPhoneNumber(),
                    messageText, new Date());
            Uri uri = insertMessage(statusMessage);
            if (uri != null)
            {
                LogStoreHelper.info(this, "New message inserted: " + uri);
                sendEmailMessage(statusMessage, false);
            }
        }
        LogStoreHelper.info(this, "Unlocking " + messageId + " done.");
        getContentResolver().notifyChange(MessagesContentProvider.CONTENT_URI, null);
        sendBroadcast(new Intent(Constants.STATUS_UPDATE_BROADCAST_ACTION).putExtra(Constants.STATUS_UPDATE_EXTRA_WHAT,
                Constants.EVENT_STATUS_OF_MESSAGES_CHANGED));
        // We might need to update the trial state if all tries are used.
        userData.checkMessagesAndSetFlags();
        return updateResult;
    }

    protected Uri insertMessage(Message message)
    {
        return MessageStoreHelper.insertMessage(this, message);
    }

    protected int updateMessage(Message message)
    {
        return MessageStoreHelper.updateMessage(this, message);
    }
}
