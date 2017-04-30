package com.tinywebgears.relayme.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.Manifest;
import android.app.IntentService;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

import com.codolutions.diagnostics.LogCollector;
import com.tinywebgears.relayme.app.CustomApplication;
import com.tinywebgears.relayme.app.SystemStatus;
import com.tinywebgears.relayme.model.LogEntry;

//FIXME: TECHDEBT: Extend from BasicIntentService
public class LogsIntentService extends IntentService
{
    private static final String TAG = LogsIntentService.class.getName();
    public static final String ACTION_CODE_DETECT_CRASH = "detect-crash";
    public static final String ACTION_RESPONSE_DETECT_CRASH = LogsIntentService.class.getCanonicalName()
            + ".RESPONSE_DETECT_CRASH";
    public static final String ACTION_CODE_SEND_LOGS = "send-logs";
    public static final String PARAM_IN_ISSUE_CATEGORY = "issue-category";
    public static final String PARAM_IN_USER_COMMENTS = "user-comments";
    public static final String PARAM_IN_INCLUDE_SETTINGS = "include-settings";

    private static final String TARGET_EMAIL_ADDRESS = "relayme+logs@codolutions.com";
    private static final String TARGET_EMAIL_SUBJECT = "Logs from Relay ME for Android";
    private static final String TARGET_EMAIL_HEADING = "Here are the logs from Relay ME %s (%s - %s)\n\nProblem: %s\nComments: %s\n\n";
    private static final String FILE_PROVIDER_AUTHORITIES = "com.tinywebgears.relayme.fileprovider";

    private LogCollector collector;

    public LogsIntentService()
    {
        super("LogsIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        boolean lowMemory = SystemStatus.isLowMemory(this);
        collector = new LogCollector(this, lowMemory);
        String action = intent.getAction();
        if (ACTION_CODE_DETECT_CRASH.equalsIgnoreCase(action))
        {
            Log.d(TAG, "Checking to see if the app has crashed...");
            if (collector.hasForceCloseHappened())
            {
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(ACTION_RESPONSE_DETECT_CRASH);
                broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
                sendBroadcast(broadcastIntent);
            }
        }
        else if (ACTION_CODE_SEND_LOGS.equalsIgnoreCase(action))
        {
            Log.d(TAG, "Sending logs...");

            boolean result = collector.collect();
            if (result)
            {
                String versionName = "";
                int versionCode = 0;
                String updateDateString = "";
                try
                {
                    versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
                    versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
                    updateDateString = new Date(getApplication().getPackageManager().getPackageInfo(
                            getApplication().getPackageName(), 0).lastUpdateTime).toString();
                }
                catch (NameNotFoundException e)
                {
                    LogStoreHelper.warn(this, "Couldn't find version info of the app.", e);
                }
                boolean includeSettings = intent.getBooleanExtra(PARAM_IN_INCLUDE_SETTINGS, false);
                String issueCategory = intent.getStringExtra(PARAM_IN_ISSUE_CATEGORY);
                String userComments = intent.getStringExtra(PARAM_IN_USER_COMMENTS);
                StringBuilder heading = new StringBuilder();
                heading.append(String.format(TARGET_EMAIL_HEADING, versionName, versionCode, updateDateString,
                        issueCategory, userComments));
                if (includeSettings)
                {
                    Map<String, ?> allData = ((CustomApplication) getApplication()).getSystemStatus()
                            .getAllForLogging();
                    for (Entry<String, ?> entry : allData.entrySet())
                        heading.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
                    heading.append("\n").append("Applications receiving SMS broadcasts:").append("\n");
                    for (String appName : SystemStatus.getAppsWithPermission(this, Manifest.permission.RECEIVE_SMS,
                            true))
                        heading.append(appName).append("\n");
                }
                StringBuilder extraInfo = new StringBuilder();
                List<LogEntry> logs = LogStoreHelper.getAllLogsToSend(this, lowMemory);
                for (int i = logs.size() - 1; i >= 0; i--)
                {
                    LogEntry log = logs.get(i);
                    extraInfo.append(log).append("\n");
                }
                collector.sendLogs(TARGET_EMAIL_ADDRESS, TARGET_EMAIL_SUBJECT, heading.toString(),
                        extraInfo.toString(), FILE_PROVIDER_AUTHORITIES);
            }
        }
    }
}
