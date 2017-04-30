package com.tinywebgears.relayme.view;

import java.util.Date;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.tinywebgears.relayme.R;
import com.tinywebgears.relayme.service.LogStoreHelper;
import com.tinywebgears.relayme.view.AboutDialogFragment.AboutDialogListener;

@EFragment(R.layout.dialog_about)
public class AboutDialogFragment extends AbstractDialogFragment<AboutDialogListener>
{
    private static final String TAG = AboutDialogFragment.class.getName();
    private static final String UNIQUE_ID_CLIPBOARD_LABEL = "Relay ME - Unique ID";

    @ViewById(R.id.aboutversion)
    TextView versionTextView;

    @ViewById(R.id.aboutdate)
    TextView dateTextView;

    @Override
    protected Class<AboutDialogListener> getListenerClass()
    {
        return AboutDialogListener.class;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        int style = DialogFragment.STYLE_NORMAL, theme = 0;
        setStyle(style, theme);
    }

    @Override
    public void onResume()
    {
        Log.d(TAG, "AboutDialogFragment.onResume");
        super.onResume();

        getDialog().setTitle(R.string.lbl_about_dialog_title);
        String versionNumber = "";
        String dateStr = "";
        try
        {
            versionNumber = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
            long timestamp = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).lastUpdateTime;
            dateStr = DateFormat.getDateFormat(getActivity()).format(new Date(timestamp));
        }
        catch (Exception e)
        {
            LogStoreHelper.warn(this, getActivity(), "Unable to find application version info.", e);
        }
        versionTextView.setText(getActivity().getString(R.string.lbl_about_dialog_version, versionNumber));
        dateTextView.setText(getActivity().getString(R.string.lbl_about_dialog_date, dateStr));
    }

    @Click(R.id.visitwebsite)
    public void onViewWebsiteButtonClick(View v)
    {
        getListener().onFinishAboutDialog(AboutDialogAction.WEB);
        AboutDialogFragment.this.dismiss();
    }

    @Click(R.id.emailus)
    public void onSendEmailButtonClick(View v)
    {
        getListener().onFinishAboutDialog(AboutDialogAction.EMAIL);
        AboutDialogFragment.this.dismiss();
    }

    @Click(R.id.help)
    public void onHelpButtonClick(View v)
    {
        getListener().onFinishAboutDialog(AboutDialogAction.HELP);
        AboutDialogFragment.this.dismiss();
    }

    public enum AboutDialogAction
    {
        WEB, EMAIL, HELP
    }

    public interface AboutDialogListener
    {
        void onFinishAboutDialog(AboutDialogAction action);
    }
}
