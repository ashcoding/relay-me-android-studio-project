package com.tinywebgears.relayme.view;

import java.util.Date;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tinywebgears.relayme.R;
import com.tinywebgears.relayme.model.MessageType;
import com.tinywebgears.relayme.view.MessageDetailsDialogFragment.MessageDetailsDialogListener;

@EFragment
public class MessageDetailsDialogFragment extends AbstractDialogFragment<MessageDetailsDialogListener>
{
    private static final String TAG = MessageDetailsDialogFragment.class.getName();

    @ViewById(R.id.msgdetailsnumber)
    TextView contactNameView;

    @ViewById(R.id.msgdetailsdatecreated)
    TextView messageDateCreatedView;

    @ViewById(R.id.msgdetailsdateupdated)
    TextView messageDateUpdatedView;

    @ViewById(R.id.msgdetailstext)
    TextView messageBodyView;

    static MessageDetailsDialogFragment newInstance(MessageType messageType, String contactName,
            Date messageDateCreated, Date messageDateUpdated, String messageBody)
    {
        MessageDetailsDialogFragment f = new MessageDetailsDialogFragment_();
        Bundle args = new Bundle();
        args.putString("messageType", messageType.toString());
        args.putString("contact", contactName);
        args.putLong("datecreated", messageDateCreated.getTime());
        args.putLong("dateupdated", messageDateUpdated.getTime());
        args.putString("messagebody", messageBody);
        f.setArguments(args);
        return f;
    }

    // Transient fields

    private MessageType messageType;

    private String contactName;

    private Date messageDateCreated;

    private Date messageDateUpdated;

    private String messageBody;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        messageType = MessageType.fromString(getArguments().getString("messageType"));
        contactName = getArguments().getString("contact");
        messageDateCreated = new Date(getArguments().getLong("datecreated"));
        messageDateUpdated = new Date(getArguments().getLong("dateupdated"));
        messageBody = getArguments().getString("messagebody");

        int style = DialogFragment.STYLE_NORMAL, theme = 0;
        setStyle(style, theme);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        getDialog().setTitle(R.string.lbl_message_details_dialog_title);
        View dialogView = inflater.inflate(R.layout.dialog_message_details, container, false);
        return dialogView;
    }

    @Override
    public void onResume()
    {
        Log.d(TAG, "MessageDetailsDialogFragment.onResume");
        super.onResume();

        String contactStr = messageType == MessageType.INCOMING ? getString(R.string.lbl_message_from, contactName)
                : getString(R.string.lbl_message_to, contactName);
        contactNameView.setText(contactStr);
        // TODO: Date format
        messageDateCreatedView.setText(getString(R.string.lbl_message_date_created, messageDateCreated.toString()));
        // TODO: Date format
        messageDateUpdatedView.setText(getString(R.string.lbl_message_date_updated, messageDateUpdated.toString()));
        messageBodyView.setText(messageBody);
    }

    @Override
    protected Class<MessageDetailsDialogListener> getListenerClass()
    {
        return MessageDetailsDialogListener.class;
    }

    public interface MessageDetailsDialogListener
    {
        void onFinishMessageDetailsDialog();
    }
}
