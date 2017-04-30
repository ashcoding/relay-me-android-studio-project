package com.tinywebgears.relayme.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemClock;

import com.codolutions.android.common.util.NumberUtil;
import com.tinywebgears.relayme.app.CustomApplication;
import com.tinywebgears.relayme.app.UserData;
import com.tinywebgears.relayme.common.Constants;

public class GlobalEventsReceiver extends BroadcastReceiver
{
    // TODO: Use PollingFactor.
    private static final int PERIOD = 5 * 60 * 1000; // 5 minutes

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (Constants.BOOT_COMPLETED_BROADCAST_ACTION.equals(intent.getAction()))
        {
            startServices(context);
            setAlarm(context);
        }
        else if (Constants.CONNECTIVITY_CHANGED_BROADCAST_ACTION.equals(intent.getAction()))
        {
            LogStoreHelper.info(context, "Connectivity status changed!");
            checkMessages(context);
        }
        else if (Constants.APPLICATION_STARTED_BROADCAST_ACTION.equals(intent.getAction()))
        {
            setAlarm(context);
        }
        else if (Constants.ALARM_BROADCAST_ACTION.equals(intent.getAction()))
        {
            LogStoreHelper.info(context, "Alarm received!");
            checkMessages(context);
        }
    }

    private void startServices(Context context)
    {
        Intent messagingServiceIntent = new Intent(context, TaskSchedulingService.class);
        long actionId = NumberUtil.getRandomNumber();
        messagingServiceIntent.putExtra(BasicIntentService.PARAM_ACTION_ID, actionId);
        context.startService(messagingServiceIntent);
    }

    private void setAlarm(Context context)
    {
        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, GlobalEventsReceiver.class);
        i.setAction(Constants.ALARM_BROADCAST_ACTION);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);

        LogStoreHelper.info(context, "Setting alarm to fire every " + PERIOD + " milliseconds");
        mgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + PERIOD, PERIOD, pi);
    }

    private void checkMessages(Context context)
    {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        boolean online = netInfo != null && netInfo.isConnectedOrConnecting();
        // FIXME: TECHDEBT: This is not the same UserData that the rest of the application is using.
        UserData userData = new UserData(context);
        if (!userData.isOnlineStatusAvailable() || online != userData.isOnline())
        {
            LogStoreHelper.info(context, "Network connectivity changed, online: " + online);
            Intent i = new Intent(Constants.STATUS_UPDATE_BROADCAST_ACTION).putExtra(
                    Constants.STATUS_UPDATE_EXTRA_WHAT, Constants.EVENT_CONNECTION_STATUS_CHANGED);
            context.sendBroadcast(i);
            userData.setOnline(online);
        }
        startIntentService(context, MessagingIntentService.ACTION_CODE_CHECK_FOR_NEW_SMS,
                MessagingIntentService.class);
        if (online)
        {
            LogStoreHelper.info(context, "We are online, let's process pending messages.");
            startIntentService(context, MessagingIntentService.ACTION_CODE_PROCESS_MESSAGES,
                    MessagingIntentService.class);
            startIntentService(context, MessagingIntentService.ACTION_CODE_CHECK_FOR_NEW_EMAILS,
                    MessagingIntentService.class);
        }
    }

    private void startIntentService(Context context, String action, Class<?> clazz)
    {
        CustomApplication.startWakefulIntentService(context, action, clazz);
    }
}
