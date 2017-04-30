package com.tinywebgears.relayme.service.sms;

import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;

import com.codolutions.android.common.util.NumberUtil;
import com.tinywebgears.relayme.app.CustomApplication;
import com.tinywebgears.relayme.common.Constants;
import com.tinywebgears.relayme.service.AbstractBroadcastReceiver;
import com.tinywebgears.relayme.service.BasicIntentService;
import com.tinywebgears.relayme.service.LogStoreHelper;
import com.tinywebgears.relayme.service.MessagingIntentService;

public class SmsReceiver extends AbstractBroadcastReceiver
{
    public static final String SMS_SENT_BROADCAST_ACTION = "com.tinywebgears.relayme.SMS_SENT";
    public static final String SMS_SENT_EXTRA_ID = "MESSAGE_ID";
    public static final String SMS_SENT_EXTRA_PARTS = "MESSAGE_PARTS";
    public static final String SMS_SENT_EXTRA_TRY_DATE = "MESSAGE_TRY_DATE";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (intent.getAction().equals(Constants.SMS_RECEIVED_BROADCAST_ACTION))
        {
            LogStoreHelper.info(SmsReceiver.class, context, "SMS received: " + getResultCode());
            startIntentService(context, MessagingIntentService.ACTION_CODE_INCOMING_SMS, MessagingIntentService.class);
        }
        else if (intent.getAction().equals(SMS_SENT_BROADCAST_ACTION))
        {
            LogStoreHelper.info(SmsReceiver.class, context, "SMS sent, onReceive: " + getResultCode());
            long messageId = intent.getExtras().getLong(SMS_SENT_EXTRA_ID);
            Date dateTried = new Date(intent.getExtras().getLong(SMS_SENT_EXTRA_TRY_DATE));
            Integer numberOfFragments = intent.getExtras().getInt(SMS_SENT_EXTRA_PARTS);
            boolean status = false;
            String reason = "" + getResultCode();
            switch (getResultCode())
            {
            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                reason = "Generic failure";
                break;
            case SmsManager.RESULT_ERROR_NO_SERVICE:
                reason = "No service";
                break;
            case SmsManager.RESULT_ERROR_NULL_PDU:
                reason = "Null PDU";
                break;
            case SmsManager.RESULT_ERROR_RADIO_OFF:
                reason = "Radio off";
                break;
            case Activity.RESULT_OK:
                LogStoreHelper.info(SmsReceiver.class, context, "SMS sent");
                status = true;
                break;
            default:
                LogStoreHelper
                        .info(SmsReceiver.class, context, "SMS failed with unknown error code " + getResultCode());
                break;
            }
            LogStoreHelper.info(SmsReceiver.class, context, "SMS event ID: " + messageId + " fragments: "
                    + numberOfFragments + " date tried: " + dateTried + " result code: " + getResultCode()
                    + " result data: " + getResultData() + " - status? " + status + " reason: " + reason);
            Intent messagingIntent = new Intent(context.getApplicationContext(), MessagingIntentService.class);
            messagingIntent.setAction(MessagingIntentService.ACTION_CODE_MESSAGE_TRY);
            messagingIntent.putExtra(MessagingIntentService.PARAM_IN_MESSAGE_ID, messageId);
            messagingIntent.putExtra(MessagingIntentService.PARAM_IN_MESSAGE_STATUS, status);
            messagingIntent.putExtra(MessagingIntentService.PARAM_IN_MESSAGE_DATE_TRIED, dateTried.getTime());
            long actionId = NumberUtil.getRandomNumber();
            messagingIntent.putExtra(BasicIntentService.PARAM_ACTION_ID, actionId);
            context.getApplicationContext().startService(messagingIntent);
        }
    }

    private void startIntentService(Context context, String action, Class<?> clazz)
    {
        CustomApplication.startWakefulIntentService(context, action, clazz);
    }
}
