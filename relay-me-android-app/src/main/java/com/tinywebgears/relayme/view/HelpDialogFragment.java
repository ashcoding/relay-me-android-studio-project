package com.tinywebgears.relayme.view;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.tinywebgears.relayme.R;
import com.tinywebgears.relayme.view.HelpDialogFragment.HelpDialogListener;

@EFragment
public class HelpDialogFragment extends AbstractDialogFragment<HelpDialogListener>
{
    @ViewById(R.id.checkwiki)
    Button checkWikiButton;

    @ViewById(R.id.contactus)
    Button contactUsButton;

    @Override
    protected Class<HelpDialogListener> getListenerClass()
    {
        return HelpDialogListener.class;
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
        getDialog().setTitle(R.string.lbl_help_dialog_title);
        View dialogView = inflater.inflate(R.layout.dialog_help, container, false);
        return dialogView;
    }

    @Click(R.id.checkwiki)
    public void onCheckWikiButtonClick(View v)
    {
        getListener().onFinishHelpDialog(HelpDialogAction.WIKI);
        HelpDialogFragment.this.dismiss();
    }

    @Click(R.id.contactus)
    public void onContactUsButtonClick(View v)
    {
        getListener().onFinishHelpDialog(HelpDialogAction.CONTACT);
        HelpDialogFragment.this.dismiss();
    }

    public enum HelpDialogAction
    {
        WIKI, CONTACT
    }

    public interface HelpDialogListener
    {
        void onFinishHelpDialog(HelpDialogAction action);
    }
}
