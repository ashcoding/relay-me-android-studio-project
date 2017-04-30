package com.tinywebgears.relayme.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.codolutions.android.common.util.NumberUtil;
import com.codolutions.android.common.util.StringUtil;
import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.tinywebgears.relayme.common.BaseAsyncTask;
import com.tinywebgears.relayme.service.BasicIntentService;
import com.tinywebgears.relayme.service.LogStoreHelper;

public class CustomApplication extends Application
{
    private static final String TAG = CustomApplication.class.getName();

    // Maps activity class names to the list of currently running AsyncTasks.
    private final Map<String, List<BaseAsyncTask<?, ?, ?>>> activityTaskMap;
    private SystemStatus systemStatus;

    public CustomApplication()
    {
        activityTaskMap = new HashMap<String, List<BaseAsyncTask<?, ?, ?>>>();
    }

    @Override
    public void onCreate()
    {
        LogStoreHelper.info(this, "CustomApplication.onCreate");
        systemStatus = new SystemStatus(this);
    }

    public SystemStatus getSystemStatus()
    {
        return systemStatus;
    }

    public UserData getUserData()
    {
        return systemStatus.getUserData();
    }

    public void removeTask(BaseAsyncTask<?, ?, ?> task)
    {
        for (Entry<String, List<BaseAsyncTask<?, ?, ?>>> entry : activityTaskMap.entrySet())
        {
            List<BaseAsyncTask<?, ?, ?>> tasks = entry.getValue();
            for (int i = 0; i < tasks.size(); i++)
                if (tasks.get(i) == task)
                {
                    tasks.remove(i);
                    break;
                }

            if (tasks.size() == 0)
            {
                activityTaskMap.remove(entry.getKey());
                return;
            }
        }
    }

    public void addTask(Activity activity, BaseAsyncTask<?, ?, ?> task)
    {
        String key = activity.getClass().getCanonicalName();
        List<BaseAsyncTask<?, ?, ?>> tasks = activityTaskMap.get(key);
        if (tasks == null)
        {
            tasks = new ArrayList<BaseAsyncTask<?, ?, ?>>();
            activityTaskMap.put(key, tasks);
        }
        tasks.add(task);
    }

    public void detach(Activity activity)
    {
        List<BaseAsyncTask<?, ?, ?>> tasks = activityTaskMap.get(activity.getClass().getCanonicalName());
        if (tasks != null)
            for (BaseAsyncTask<?, ?, ?> task : tasks)
                task.setActivity(null);
    }

    public void attach(Activity activity)
    {
        List<BaseAsyncTask<?, ?, ?>> tasks = activityTaskMap.get(activity.getClass().getCanonicalName());
        if (tasks != null)
            for (BaseAsyncTask<?, ?, ?> task : tasks)
                task.setActivity(activity);
    }

    public static void startIntentService(Context context, String action, Class<?> clazz)
    {
        startIntentService(context, action, new Intent(context, clazz));
    }

    public static void startIntentService(Context context, String action, Intent intent)
    {
        long actionId = NumberUtil.getRandomNumber();
        if (!StringUtil.empty(action))
            intent.setAction(action);
        intent.putExtra(BasicIntentService.PARAM_ACTION_ID, actionId);
        context.startService(intent);
    }

    public static void startWakefulIntentService(Context context, String action, Class<?> clazz)
    {
        startWakefulIntentService(context, action, new Intent(context, clazz));
    }

    public static void startWakefulIntentService(Context context, String action, Intent intent)
    {
        long actionId = NumberUtil.getRandomNumber();
        if (!StringUtil.empty(action))
            intent.setAction(action);
        intent.putExtra(BasicIntentService.PARAM_ACTION_ID, actionId);
        context.startService(intent);
        WakefulIntentService.sendWakefulWork(context, intent);
    }
}
