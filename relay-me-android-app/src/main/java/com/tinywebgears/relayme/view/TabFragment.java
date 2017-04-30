package com.tinywebgears.relayme.view;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;

import com.tinywebgears.relayme.app.CustomApplication;
import com.tinywebgears.relayme.app.SystemStatus;
import com.tinywebgears.relayme.app.UserData;

public abstract class TabFragment extends Fragment
{
    // Transient context
    protected SystemStatus systemStatus;
    protected ActionBarActivity myActivity;
    protected Context applicationContext;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    public UserData getUserData()
    {
        return systemStatus.getUserData();
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);

        systemStatus = ((CustomApplication) activity.getApplication()).getSystemStatus();
        myActivity = (ActionBarActivity) activity;
        applicationContext = activity.getApplicationContext();
    }

    @Override
    public void onDetach()
    {
        myActivity = null;

        super.onDetach();
    }
}