package com.tinywebgears.relayme.view;

import java.util.List;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import android.Manifest;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.codolutions.android.common.util.StringUtil;
import com.tinywebgears.relayme.R;
import com.tinywebgears.relayme.app.SystemStatus;
import com.tinywebgears.relayme.view.SmsHelpDialogFragment.SmsHelpDialogListener;

//FIXME: TECHDEBT: Reuse the code in HelpDialogFragment 
@EFragment
public class SmsHelpDialogFragment extends AbstractDialogFragment<SmsHelpDialogListener>
{
    @ViewById(R.id.checkwiki)
    Button checkWikiButton;

    @ViewById(R.id.contactus)
    Button contactUsButton;

    @ViewById(R.id.smshelpdialogheader)
    TextView dialogHeader;

    @ViewById(R.id.smshelpdialogappslist)
    TextView appsList;

    @Override
    protected Class<SmsHelpDialogListener> getListenerClass()
    {
        return SmsHelpDialogListener.class;
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
        getDialog().setTitle(R.string.lbl_sms_help_dialog_title);
        View dialogView = inflater.inflate(R.layout.dialog_sms_help, container, false);
        return dialogView;
    }

    public void onResume()
    {
        super.onResume();

        List<String> appNames = SystemStatus.getAppsWithPermission(getActivity(), Manifest.permission.RECEIVE_SMS,
                false);
        String appNamesString = StringUtil.join(appNames, " - ");
        appsList.setVisibility(View.GONE);
        if (appNames.isEmpty())
            dialogHeader.setText(R.string.lbl_sms_help_dialog_header);
        else
        {
            dialogHeader.setText(getString(R.string.lbl_sms_help_dialog_header_apps_found));
            appsList.setVisibility(View.VISIBLE);
            appsList.setText(Html.fromHtml(appNamesString));
        }
    }

    @Click(R.id.checkwiki)
    public void onCheckWikiButtonClick(View v)
    {
        getListener().onFinishSmsHelpDialog(SmsHelpDialogAction.WIKI);
        SmsHelpDialogFragment.this.dismiss();
    }

    @Click(R.id.contactus)
    public void onContactUsButtonClick(View v)
    {
        getListener().onFinishSmsHelpDialog(SmsHelpDialogAction.CONTACT);
        SmsHelpDialogFragment.this.dismiss();
    }

    public enum SmsHelpDialogAction
    {
        WIKI, CONTACT
    }

    public interface SmsHelpDialogListener
    {
        void onFinishSmsHelpDialog(SmsHelpDialogAction action);
    }
}
