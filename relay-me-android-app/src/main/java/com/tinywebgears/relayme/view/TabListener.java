package com.tinywebgears.relayme.view;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBarActivity;

public class TabListener<T extends Fragment> implements ActionBar.TabListener
{
    private final ActionBarActivity mActivity;
    private final String mTag;
    private final Class<T> mClass;

    public TabListener(ActionBarActivity activity, String tag, Class<T> clz)
    {
        mActivity = activity;
        mTag = tag;
        mClass = clz;
    }

    public void onTabSelected(Tab tab, FragmentTransaction ft)
    {
        Fragment preInitializedFragment = (Fragment) mActivity.getSupportFragmentManager().findFragmentByTag(mTag);

        if (preInitializedFragment == null)
        {
            preInitializedFragment = (Fragment) Fragment.instantiate(mActivity, mClass.getName());
            ft.add(android.R.id.content, preInitializedFragment, mTag);
        }
        else
        {
            // If it exists, simply attach it in order to show it
            ft.attach(preInitializedFragment);
        }
    }

    public void onTabUnselected(Tab tab, FragmentTransaction ft)
    {
        Fragment preInitializedFragment = (Fragment) mActivity.getSupportFragmentManager().findFragmentByTag(mTag);
        if (preInitializedFragment != null)
        {
            // Detatch
            ft.detach(preInitializedFragment);
        }
    }

    public void onTabReselected(Tab tab, FragmentTransaction ft)
    {
        // User selected the already selected tab. Usually do nothing.
    }
}
