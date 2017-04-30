package com.tinywebgears.relayme.service.mms;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.codolutions.android.common.util.Pair;
import com.tinywebgears.relayme.common.Constants;
import com.tinywebgears.relayme.service.AbstractBroadcastReceiver;
import com.tinywebgears.relayme.service.LogStoreHelper;
import com.tinywebgears.relayme.service.MessagingIntentService;

public class MmsReceiver extends AbstractBroadcastReceiver
{
    private static final String TAG = MmsReceiver.class.getName();

    private static final String MMS_DATA_TYPE = "application/vnd.wap.mms-message";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (intent.getAction().equals(Constants.WAP_PUSH_RECEIVED_BROADCAST_ACTION)
                && MMS_DATA_TYPE.equals(intent.getType()))
        {
            Map<String, Pair<String, String>> msg = retrieveMessages(context, intent);
            if (msg == null || msg.keySet() == null)
            {
                LogStoreHelper.warn(MmsReceiver.class, context, "Message is null or empty!", null);
                return;
            }
            for (String msgId : msg.keySet())
                sendNotification(context, MessagingIntentService.ACTION_CODE_INCOMING_MMS, msg.get(msgId).first,
                        msg.get(msgId).second);
        }
    }

    private static Map<String, Pair<String, String>> retrieveMessages(Context context, Intent intent)
    {
        Map<String, Pair<String, String>> messagesMap = new HashMap<String, Pair<String, String>>();

        // Parse the WAP push contents
        PduParser parser = new PduParser();
        PduHeaders headers = parser.parseHeaders(intent.getByteArrayExtra("data"));
        if (headers == null)
        {
            LogStoreHelper.warn(MmsReceiver.class, context, "Couldn't parse headers for WAP PUSH.", null);
            return messagesMap;
        }

        int type = headers.getMessageType();
        Log.d(TAG, "WAP PUSH message type: 0x" + Integer.toHexString(type));

        // Check if it's a MMS notification
        if (type == PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND)
        {
            String fromStr = null;
            EncodedStringValue encodedFrom = headers.getFrom();
            if (encodedFrom != null)
                fromStr = encodedFrom.getString();

            messagesMap.put(fromStr, new Pair<String, String>(fromStr, null));
        }
        return messagesMap;
    }
}
