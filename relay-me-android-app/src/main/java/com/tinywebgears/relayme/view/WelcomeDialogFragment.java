package com.tinywebgears.relayme.view;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tinywebgears.relayme.R;
import com.tinywebgears.relayme.view.WelcomeDialogFragment.WelcomeDialogListener;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

@EFragment
public class WelcomeDialogFragment extends AbstractDialogFragment<WelcomeDialogListener>
{
    private static final String TAG = WelcomeDialogFragment.class.getName();

    @ViewById(R.id.welcomedialogdetails)
    TextView welcomeText;

    @Override
    protected Class<WelcomeDialogListener> getListenerClass()
    {
        return WelcomeDialogListener.class;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        int style = DialogFragment.STYLE_NORMAL, theme = 0;
        setStyle(style, theme);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        getDialog().setTitle(R.string.lbl_welcome_dialog_title);
        final View dialogView = inflater.inflate(R.layout.dialog_welcome, container, false);
        return dialogView;
    }

    @Override
    public void onResume()
    {
        Log.d(TAG, "WelcomeDialogFragment.onResume");
        super.onResume();

        welcomeText.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Click(R.id.cancelwelcomedialog)
    public void onCancelWelcomeDialogButtonClick(View v)
    {
        // getListener().onFinishWelcomeDialog(true);
        WelcomeDialogFragment.this.dismiss();
    }

    public interface WelcomeDialogListener
    {
        void onFinishWelcomeDialog(boolean dontShowAgain);
    }
}
