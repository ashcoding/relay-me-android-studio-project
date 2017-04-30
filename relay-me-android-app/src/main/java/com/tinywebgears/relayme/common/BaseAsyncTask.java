package com.tinywebgears.relayme.common;

import com.tinywebgears.relayme.app.CustomApplication;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

public abstract class BaseAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result>
{
    protected CustomApplication app;
    protected Activity activity;
    private Class<?> mCallbackClass;

    public BaseAsyncTask(Activity activity, Class<?> callbackClass)
    {
        this.activity = activity;
        app = (CustomApplication) activity.getApplication();
        mCallbackClass = callbackClass;
    }

    public void setActivity(Activity activity)
    {
        this.activity = activity;
        if (activity == null)
            onActivityDetached();
        else
            onActivityAttached();
    }

    protected void onActivityAttached()
    {
    }

    protected void onActivityDetached()
    {
    }

    @Override
    protected void onPreExecute()
    {
        app.addTask(activity, this);
    }

    @Override
    protected void onCancelled()
    {
        app.removeTask(this);
    }

    @Override
    protected void onPostExecute(Result result)
    {
        app.removeTask(this);
        Log.i(BaseAsyncTask.class.getName(), "Async task result: " + result);
        if (activity == null)
            Log.w(BaseAsyncTask.class.getName(), "The activity wasn't there when I finished!");
        else if (!mCallbackClass.isInstance(activity))
            Log.w(BaseAsyncTask.class.getName(), "The activity " + activity.getClass().getName()
                    + " doesn't implement my callback interface!");
        else
            onPostExecuteCallback(result);
    }

    abstract protected void onPostExecuteCallback(Result result);
}
