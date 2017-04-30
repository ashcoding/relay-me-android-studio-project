package com.tinywebgears.relayme.view;

import org.androidannotations.annotations.EFragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.tinywebgears.relayme.R;
import com.tinywebgears.relayme.common.DialogParams;
import com.tinywebgears.relayme.view.ConfirmationDialogFragment.ConfirmationDialogListener;

@EFragment
public class ConfirmationDialogFragment extends AbstractDialogFragment<ConfirmationDialogListener>
{
    public static String ARGUMENT_DIALOG_DATA = "dialog-data";
    public static String ARGUMENT_ICON = "dialog-icon";
    public static String ARGUMENT_TITLE = "dialog-title";
    public static String ARGUMENT_MESSAGE = "dialog-message";

    @Override
    protected Class<ConfirmationDialogListener> getListenerClass()
    {
        return ConfirmationDialogListener.class;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        final DialogParams<?> dialogData = getArguments().getParcelable(ARGUMENT_DIALOG_DATA);
        final int iconResId = getArguments().getInt(ARGUMENT_ICON);
        final String title = getArguments().getString(ARGUMENT_TITLE);
        final String message = getArguments().getString(ARGUMENT_MESSAGE);
        return new AlertDialog.Builder(getActivity()).setIcon(iconResId).setTitle(title).setMessage(message)
                .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        getListener().onFinishConfirmationDialog(dialogData);
                        dismiss();
                    }
                }).setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        getListener().onCancelConfirmationDialog(dialogData);
                        dismiss();
                    }
                }).create();
    }

    public interface ConfirmationDialogListener
    {
        void onFinishConfirmationDialog(DialogParams<?> param);

        void onCancelConfirmationDialog(DialogParams<?> param);
    }
}
