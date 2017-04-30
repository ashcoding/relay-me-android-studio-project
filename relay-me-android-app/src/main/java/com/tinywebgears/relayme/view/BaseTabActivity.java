package com.tinywebgears.relayme.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.flurry.android.FlurryAgent;
import com.tinywebgears.relayme.app.CustomApplication;
import com.tinywebgears.relayme.app.SystemStatus;
import com.tinywebgears.relayme.app.UserData;
import com.tinywebgears.relayme.common.Constants;
import com.tinywebgears.relayme.service.GlobalEventsReceiver;

public class BaseTabActivity extends ActionBarActivity
{
    // Singleton context
    private SystemStatus systemStatus;
    private UserData userData;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        systemStatus = ((CustomApplication) getApplication()).getSystemStatus();
        userData = ((CustomApplication) getApplication()).getUserData();
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        Intent intent = new Intent(this, GlobalEventsReceiver.class);
        intent.setAction(Constants.APPLICATION_STARTED_BROADCAST_ACTION);
        sendBroadcast(intent);
    }

    protected void startIntentService(String action, Class<?> clazz)
    {
        CustomApplication.startIntentService(getApplicationContext(), action, clazz);
    }

    protected void startIntentService(String action, Intent intent)
    {
        CustomApplication.startIntentService(getApplicationContext(), action, intent);
    }

    @Override
    protected void onStop()
    {
        FlurryAgent.onEndSession(this);
        super.onStop();
    }

    protected SystemStatus getSystemStatus()
    {
        return systemStatus;
    }

    protected UserData getUserData()
    {
        return userData;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        ((CustomApplication) getApplication()).detach(this);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);

        ((CustomApplication) getApplication()).attach(this);
    }
}
