package com.tinywebgears.relayme.service.call;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.codolutions.android.common.util.StringUtil;
import com.tinywebgears.relayme.service.AbstractBroadcastReceiver;
import com.tinywebgears.relayme.service.LogStoreHelper;
import com.tinywebgears.relayme.service.MessagingIntentService;

public class MissedCallListener extends PhoneStateListener
{
    private static final String NO_CALLER_ID = "private number";

    private Context context;
    private boolean isRinging = false;
    private String callerPhoneNumber;
    private final AbstractBroadcastReceiver broadcastReceiver;

    public MissedCallListener(Context context, final AbstractBroadcastReceiver broadcastReceiver)
    {
        this.context = context;
        this.broadcastReceiver = broadcastReceiver;
    }

    private String getState(int state)
    {
        switch (state)
        {
        case TelephonyManager.CALL_STATE_RINGING:
            return "RINGING";
        case TelephonyManager.CALL_STATE_OFFHOOK:
            return "OFFHOOK";
        case TelephonyManager.CALL_STATE_IDLE:
            return "IDLE";
        default:
            return "UNKNOWN";
        }
    }

    @Override
    public void onCallStateChanged(int state, String incomingNumber)
    {
        super.onCallStateChanged(state, incomingNumber);

        LogStoreHelper.info(MissedCallListener.class, context, "PhoneStateReceiver - state: " + getState(state)
                + " ringing: " + isRinging);

        switch (state)
        {
        case TelephonyManager.CALL_STATE_RINGING:
            isRinging = true;
            callerPhoneNumber = incomingNumber;
            LogStoreHelper.info(MissedCallListener.class, context, "Incoming call from: " + callerPhoneNumber);
            break;
        case TelephonyManager.CALL_STATE_OFFHOOK:
            isRinging = false;
            break;
        case TelephonyManager.CALL_STATE_IDLE:
            if (isRinging == true)
            {
                LogStoreHelper.info(MissedCallListener.class, context, "Missed call detected from: "
                        + callerPhoneNumber);
                String phoneNumber = StringUtil.empty(callerPhoneNumber) ? NO_CALLER_ID : callerPhoneNumber;
                broadcastReceiver.sendNotification(context, MessagingIntentService.ACTION_CODE_MISSED_CALL,
                        phoneNumber, null);
            }
            break;
        default:
        }
    }
}
