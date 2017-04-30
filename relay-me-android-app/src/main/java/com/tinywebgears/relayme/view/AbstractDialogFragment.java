package com.tinywebgears.relayme.view;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

public abstract class AbstractDialogFragment<T> extends DialogFragment
{
    private static final String TAG = AbstractDialogFragment.class.getName();

    public static interface ListenerHolder<T>
    {
        T getListener();
    }

    abstract protected Class<T> getListenerClass();

    private T listener;

    @SuppressWarnings("unchecked")
    protected T getListener()
    {
        // Return the listener if explicitly set
        if (listener != null)
            return listener;
        // Guess who it might be, a fragment, or an activity?
        if (getTargetFragment() != null && getListenerClass().isAssignableFrom(getTargetFragment().getClass()))
            return (T) getTargetFragment();
        else
            return (T) getActivity();
    }

    /**
     * Some times when both fragment and activity implement this dialog's callback, the callback listener has to be
     * explicitly specified.
     */
    // FIXME: TECHDEBT: This is very error prone.
    public void setListener(T listener)
    {
        this.listener = listener;
    }

    /**
     * We don't want our dialogs to linger around.
     */
    @Override
    public void onPause()
    {
        super.onPause();
        dismiss();
    }

    @Override
    public void show(FragmentManager manager, String tag)
    {
        Log.d(TAG, "Showing dialog fragment " + getClass().getName());
        super.show(manager, tag);
    }

    @Override
    public int show(FragmentTransaction transaction, String tag)
    {
        Log.d(TAG, "Showing dialog fragment (in transaction) " + getClass().getName());
        return super.show(transaction, tag);
    }
}
