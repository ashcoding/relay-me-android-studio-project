package com.tinywebgears.relayme.view;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tinywebgears.relayme.R;
import com.tinywebgears.relayme.view.OauthDialogFragment.OauthDialogListener;

@EFragment
public class OauthDialogFragment extends AbstractDialogFragment<OauthDialogListener>
{
    @Override
    protected Class<OauthDialogListener> getListenerClass()
    {
        return OauthDialogListener.class;
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
        getDialog().setTitle(R.string.lbl_oauth_dialog_title);
        final View dialogView = inflater.inflate(R.layout.dialog_oauth, container, false);
        return dialogView;
    }

    @Click(R.id.canceloauth)
    public void onCancelOauthButtonClick(View v)
    {
        OauthDialogFragment.this.dismiss();
    }

    @Click(R.id.gotogoogle)
    public void onGoToGoogleButtonClick(View v)
    {
        getListener().onFinishOauthDialog(OauthDialogAction.GO_TO_GOOGLE);
        OauthDialogFragment.this.dismiss();
    }

    public enum OauthDialogAction
    {
        GO_TO_GOOGLE
    }

    public interface OauthDialogListener
    {
        void onFinishOauthDialog(OauthDialogAction action);
    }
}