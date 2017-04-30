package com.tinywebgears.relayme.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.codolutions.android.common.util.NumberUtil;
import com.codolutions.android.common.util.StringUtil;
import com.tinywebgears.relayme.common.Constants;

public abstract class AbstractBroadcastReceiver extends BroadcastReceiver
{
    public void sendNotification(Context context, String action, String sender, String msgText)
    {
        LogStoreHelper.info(AbstractBroadcastReceiver.class, context, "Notification " + action + " - from: " + sender
                + " text: " + StringUtil.shorten(msgText, Constants.MESSAGE_SHORTENING_LENGTH));
        Intent messagingIntent = new Intent(context, MessagingIntentService.class);
        messagingIntent.setAction(action);
        messagingIntent.putExtra(MessagingIntentService.PARAM_IN_KEY, sender);
        messagingIntent.putExtra(MessagingIntentService.PARAM_IN_NUMBER, sender);
        if (!StringUtil.empty(msgText))
            messagingIntent.putExtra(MessagingIntentService.PARAM_IN_TEXT, msgText);
        long actionId = NumberUtil.getRandomNumber();
        messagingIntent.putExtra(BasicIntentService.PARAM_ACTION_ID, actionId);
        context.startService(messagingIntent);
    }
}
