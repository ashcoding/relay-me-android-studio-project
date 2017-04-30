package com.tinywebgears.relayme.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.tinywebgears.relayme.app.CustomApplication;
import com.tinywebgears.relayme.app.UserData;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TaskSchedulingService extends Service
{
    private static final String TAG = TaskSchedulingService.class.getName();

    private static final int SCHEDULER_STARTUP_DELAY_IN_SECONDS = 10;
    private static final int SCHEDULER_RESEND_MESSAGES_INTERVAL_IN_SECONDS = 60;
    private static final int SCHEDULER_STATUS_WATCH_INTERVAL_IN_SECONDS = 60;
    private static final int SCHEDULER_HOUSEKEEPING_INTERVAL_IN_SECONDS = 15 * 60;

    // Transient state
    private ScheduledExecutorService scheduledTaskExecutor;

    @Override
    public void onCreate()
    {
        Log.d(TAG, "TaskSchedulingService: onCreate");
        startScheduler();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Log.d(TAG, "TaskSchedulingService: onDestroy");
        stopScheduler();
    }

    private void stopScheduler()
    {
        if (scheduledTaskExecutor != null)
            scheduledTaskExecutor.shutdown();
        scheduledTaskExecutor = null;
    }

    private void startScheduler()
    {
        scheduledTaskExecutor = Executors.newScheduledThreadPool(1);
        scheduledTaskExecutor.scheduleWithFixedDelay(new StatusWatchTask(), SCHEDULER_STARTUP_DELAY_IN_SECONDS,
                SCHEDULER_STATUS_WATCH_INTERVAL_IN_SECONDS, TimeUnit.SECONDS);
        scheduledTaskExecutor.scheduleWithFixedDelay(new HouseKeepingTask(), SCHEDULER_STARTUP_DELAY_IN_SECONDS,
                SCHEDULER_HOUSEKEEPING_INTERVAL_IN_SECONDS, TimeUnit.SECONDS);
        scheduledTaskExecutor.scheduleWithFixedDelay(new MessageResendTask(), SCHEDULER_STARTUP_DELAY_IN_SECONDS,
                SCHEDULER_RESEND_MESSAGES_INTERVAL_IN_SECONDS, TimeUnit.SECONDS);
        UserData userData = ((CustomApplication) getApplication()).getUserData();
        int checkMessagesInterval = userData.getMessagingConfiguration().getPollingFactor()
                .getSchedulerIntervalInSeconds();
        scheduledTaskExecutor.scheduleWithFixedDelay(new SmsCheckTask(), SCHEDULER_STARTUP_DELAY_IN_SECONDS,
                checkMessagesInterval, TimeUnit.SECONDS);
        scheduledTaskExecutor.scheduleWithFixedDelay(new EmailCheckTask(), SCHEDULER_STARTUP_DELAY_IN_SECONDS,
                checkMessagesInterval, TimeUnit.SECONDS);
    }

    private class SmsCheckTask implements Runnable
    {
        @Override
        public void run()
        {
            Log.d(TAG, "SmsCheckTask.run()");
            CustomApplication.startIntentService(getApplicationContext(),
                    MessagingIntentService.ACTION_CODE_CHECK_FOR_NEW_SMS, MessagingIntentService.class);
        }
    }

    private class MessageResendTask implements Runnable
    {
        @Override
        public void run()
        {
            Log.d(TAG, "MessageResendTask.run()");
            CustomApplication.startIntentService(getApplicationContext(),
                    MessagingIntentService.ACTION_CODE_PROCESS_MESSAGES, MessagingIntentService.class);
        }
    }

    private class EmailCheckTask implements Runnable
    {
        @Override
        public void run()
        {
            Log.d(TAG, "EmailCheckTask.run()");
            CustomApplication.startIntentService(getApplicationContext(),
                    MessagingIntentService.ACTION_CODE_CHECK_FOR_NEW_EMAILS, MessagingIntentService.class);
        }
    }

    private class HouseKeepingTask implements Runnable
    {
        @Override
        public void run()
        {
            Log.d(TAG, "HouseKeepingTask.run()");
            MessageStoreHelper.purgeOldRecords(getApplicationContext());
            LogStoreHelper.purgeOldRecords(getApplicationContext());
            startIntentService(ServerSideIntentService.ACTION_CODE_SYNC, ServerSideIntentService.class);
            if (((CustomApplication) getApplication()).getUserData().isInSyncWithServer())
                startIntentService(ServerSideIntentService.ACTION_CODE_CHECK_UPDATES, ServerSideIntentService.class);
        }

        private void startIntentService(String action, Class<?> clazz)
        {
            CustomApplication.startIntentService(getApplicationContext(), action, clazz);
        }
    }

    private class StatusWatchTask implements Runnable
    {
        @Override
        public void run()
        {
            Log.d(TAG, "StatusWatchTask.run()");
            ((CustomApplication) getApplication()).getUserData().checkMessagesAndSetFlags();
        }
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }
}
