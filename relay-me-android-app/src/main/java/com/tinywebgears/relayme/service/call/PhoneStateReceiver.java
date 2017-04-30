package com.tinywebgears.relayme.service.call;

import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

import com.tinywebgears.relayme.service.AbstractBroadcastReceiver;
import com.tinywebgears.relayme.service.LogStoreHelper;

public class PhoneStateReceiver extends AbstractBroadcastReceiver
{
    static MissedCallListener missedCallListener;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (missedCallListener == null)
        {
            LogStoreHelper.info(context, "Registring missed call listener.");
            missedCallListener = new MissedCallListener(context, this);
            manager.listen(missedCallListener, android.telephony.PhoneStateListener.LISTEN_CALL_STATE);
        }
    }
}
