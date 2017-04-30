package com.tinywebgears.relayme.view;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tinywebgears.relayme.R;
import com.tinywebgears.relayme.view.AlertDialogFragment.AlertDialogListener;

@EFragment
public class AlertDialogFragment extends AbstractDialogFragment<AlertDialogListener>
{
    private static final String TAG = AlertDialogFragment.class.getName();

    @ViewById(R.id.warningtext)
    TextView warningText;

    // Transient fields

    private String title;

    private String message;

    protected AlertDialogFragment setTitle(String title)
    {
        this.title = title;
        return this;
    }

    protected AlertDialogFragment setMessage(String message)
    {
        this.message = message;
        return this;
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
        getDialog().setTitle(R.string.lbl_alert_title);
        if (title != null)
            getDialog().setTitle(title);
        View dialogView = inflater.inflate(R.layout.dialog_alert, container, false);
        return dialogView;
    }

    @Override
    public void onResume()
    {
        Log.d(TAG, "AlertDialogFragment.onResume");
        super.onResume();

        warningText.setText(message);
        warningText.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Click(R.id.cancelwarningdialog)
    public void onCancelDialogButtonClick()
    {
        AlertDialogFragment.this.dismiss();
    }

    @Override
    protected Class<AlertDialogListener> getListenerClass()
    {
        return AlertDialogListener.class;
    }

    public interface AlertDialogListener
    {
        void onFinishAlertDialog();
    }
}
