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
import com.tinywebgears.relayme.view.OpenSourceNoticeDialogFragment.OpenSourceDialogListener;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

@EFragment
public class OpenSourceNoticeDialogFragment extends AbstractDialogFragment<OpenSourceDialogListener>
{
    private static final String TAG = OpenSourceNoticeDialogFragment.class.getName();

    @ViewById(R.id.opensourcenoticedialogdetails)
    TextView openSourceNoticeText;

    @Override
    protected Class<OpenSourceDialogListener> getListenerClass()
    {
        return OpenSourceDialogListener.class;
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
        getDialog().setTitle(R.string.lbl_opensource_notice_dialog_title);
        final View dialogView = inflater.inflate(R.layout.dialog_open_source_notice, container, false);
        return dialogView;
    }

    @Override
    public void onResume()
    {
        Log.d(TAG, "OpenSourceNoticeDialogFragment.onResume");
        super.onResume();

        openSourceNoticeText.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Click(R.id.cancelopensourcenoticedialog)
    public void onCancelWelcomeDialogButtonClick(View v)
    {
        getListener().onFinishWelcomeDialog();
        OpenSourceNoticeDialogFragment.this.dismiss();
    }

    public interface OpenSourceDialogListener
    {
        void onFinishWelcomeDialog();
    }
}
