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

import com.flurry.android.FlurryAgent;
import com.tinywebgears.relayme.R;
import com.tinywebgears.relayme.common.Constants;
import com.tinywebgears.relayme.view.ConfirmDeleteMessagesDialogFragment.ConfirmDeleteMessagesDialogListener;

// TODO: Generalize as a confirmation dialog
@EFragment
public class ConfirmDeleteMessagesDialogFragment extends AbstractDialogFragment<ConfirmDeleteMessagesDialogListener>
{
    @ViewById(R.id.cancelmessagedelete)
    Button cancelMessageDeleteButton;

    @ViewById(R.id.confirmmessagedelete)
    Button confirmMessageDeleteButton;

    @Override
    protected Class<ConfirmDeleteMessagesDialogListener> getListenerClass()
    {
        return ConfirmDeleteMessagesDialogListener.class;
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
        getDialog().setTitle(R.string.lbl_message_delete_all_dialog_title);
        View dialogView = inflater.inflate(R.layout.dialog_confirm_delete_messages, container, false);
        return dialogView;
    }

    @Click(R.id.cancelmessagedelete)
    public void onCancelMessageDeleteButtonClick()
    {
        getListener().onFinishConfirmDeleteMessagesDialog(false);
        ConfirmDeleteMessagesDialogFragment.this.dismiss();
    }

    @Click(R.id.confirmmessagedelete)
    public void onConfirmMessageDeleteButtonClick(View v)
    {
        FlurryAgent.logEvent(Constants.FLURRY_EVENT_MESSAGES_DELETED);
        getListener().onFinishConfirmDeleteMessagesDialog(true);
        ConfirmDeleteMessagesDialogFragment.this.dismiss();
    }

    public interface ConfirmDeleteMessagesDialogListener
    {
        void onFinishConfirmDeleteMessagesDialog(boolean confirmed);
    }
}