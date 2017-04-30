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
import android.widget.CheckBox;

import com.tinywebgears.relayme.R;
import com.tinywebgears.relayme.view.ConfirmClearOauthDialogFragment.ConfirmClearOauthDialogListener;

// TODO: Generalize as a confirmation dialog
@EFragment
public class ConfirmClearOauthDialogFragment extends AbstractDialogFragment<ConfirmClearOauthDialogListener>
{
    @ViewById(R.id.cancelclearoauth)
    Button cancelClearOauthButton;

    @ViewById(R.id.confirmclearoauth)
    Button confirmClearOauthButton;

    @ViewById(R.id.opengoogleaccountdashboard)
    CheckBox openGoogleAccountDashboardCheckbox;

    @Override
    protected Class<ConfirmClearOauthDialogListener> getListenerClass()
    {
        return ConfirmClearOauthDialogListener.class;
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
        getDialog().setTitle(R.string.lbl_clear_oauth_dialog_title);
        final View dialogView = inflater.inflate(R.layout.dialog_confirm_clear_oauth, container, false);
        return dialogView;
    }

    @Click(R.id.cancelclearoauth)
    public void onCancelClearOauthButtonClick()
    {
        ConfirmClearOauthDialogFragment.this.dismiss();
    }

    @Click(R.id.confirmclearoauth)
    public void onConfirmClearOauthButtonClick()
    {
        getListener().onFinishConfirmClearOauthDialog(true, openGoogleAccountDashboardCheckbox.isChecked());
        ConfirmClearOauthDialogFragment.this.dismiss();
    }

    public interface ConfirmClearOauthDialogListener
    {
        void onFinishConfirmClearOauthDialog(boolean confirmed, boolean goToGoogle);
    }
}