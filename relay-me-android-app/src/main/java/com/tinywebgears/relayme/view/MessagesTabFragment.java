package com.tinywebgears.relayme.view;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.codolutions.android.common.util.StringUtil;
import com.tinywebgears.relayme.R;
import com.tinywebgears.relayme.contentprovider.MessagesContentProvider;
import com.tinywebgears.relayme.dao.DataBaseHelper;
import com.tinywebgears.relayme.model.Message;
import com.tinywebgears.relayme.model.MessageStatus;
import com.tinywebgears.relayme.model.MessageType;
import com.tinywebgears.relayme.service.ContactsIntentService;
import com.tinywebgears.relayme.service.LogStoreHelper;
import com.tinywebgears.relayme.service.MessageStoreHelper;
import com.tinywebgears.relayme.service.MessagingIntentService;
import com.tinywebgears.relayme.view.ConfirmDeleteMessagesDialogFragment.ConfirmDeleteMessagesDialogListener;

@SuppressLint("NewApi")
@EFragment
public class MessagesTabFragment extends TabFragment implements LoaderManager.LoaderCallbacks<Cursor>,
        ConfirmDeleteMessagesDialogListener
{
    private static final String TAG = MessagesTabFragment.class.getName();

    // Transient state

    @ViewById(R.id.messageslist)
    ListView messagesListView;

    private MyCursorAdapter listViewAdapter;

    private final DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

    private ActionMode actionMode;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG, "MessagesTabFragment.onCreate");
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.messages_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.messages, container, false);
        return view;
    }

    @Override
    public void onAttach(Activity activity)
    {
        Log.d(TAG, "MessagesTabFragment.onAttach");
        super.onAttach(activity);

        listViewAdapter = new MyCursorAdapter(activity, R.layout.messageslistitem, null, new String[] {
                DataBaseHelper.TABLE_MESSAGES_COLUMN_PHONE_NUMBER, DataBaseHelper.TABLE_MESSAGES_COLUMN_TIMESTAMP,
                DataBaseHelper.TABLE_MESSAGES_COLUMN_BODY, DataBaseHelper.TABLE_MESSAGES_COLUMN_NUMBER_OF_TRIES,
                DataBaseHelper.TABLE_MESSAGES_COLUMN_MESSAGE_TYPE, DataBaseHelper.TABLE_MESSAGES_COLUMN_STATUS },
                new int[] { R.id.messagelistitemcontact, R.id.messagelistitemdate, R.id.messageslistitemdetails,
                        R.id.messageslistitemtries, R.id.messageslistdirectionicon, R.id.messagesliststatusicon }, 0);
        getLoaderManager().initLoader(MessagesContentProvider.UNIQUE_ID, null, this);
    }

    @Override
    public void onDetach()
    {
        Log.d(TAG, "MessagesTabFragment.onDetach");
        super.onDetach();
    }

    @Override
    public void onResume()
    {
        Log.d(TAG, "MessagesTabFragment.onResume");
        super.onResume();

        // Using activity is safe here.
        messagesListView.setAdapter(listViewAdapter);
        setListViewClickListeners();
        reloadContacts();
    }

    private void setListViewClickListeners()
    {
        messagesListView.setOnItemClickListener(new OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if (actionMode == null)
                {
                    Log.d(TAG, "Message clicked, row " + position);
                    showMessageDetailsAtPosition(position);
                }
                else
                {
                    // add or remove selection for current list item
                    onListItemSelect(position);
                }
            }
        });
        messagesListView.setOnItemLongClickListener(new OnItemLongClickListener()
        {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
            {
                onListItemSelect(position);
                return true;
            }
        });
    }

    private void onListItemSelect(int position)
    {
        boolean hasCheckedItems = listViewAdapter.getSelectedCount() > 0;
        if (!hasCheckedItems && actionMode == null)
            listViewAdapter.removeSelection();

        listViewAdapter.toggleSelection(position);
        hasCheckedItems = listViewAdapter.getSelectedCount() > 0;

        if (hasCheckedItems && actionMode == null)
            actionMode = myActivity.startSupportActionMode(new ActionModeCallback());
        else if (!hasCheckedItems && actionMode != null)
            actionMode.finish();

        if (actionMode != null)
            actionMode.setTitle(String.valueOf(listViewAdapter.getSelectedCount()) + " selected");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
        case R.id.action_delete_all_messages:
            showConfirmDeleteMessagesDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showConfirmDeleteMessagesDialog()
    {
        DialogFragment confirmDialog = new ConfirmDeleteMessagesDialogFragment_();
        confirmDialog.setTargetFragment(this, 0);
        confirmDialog.show(getChildFragmentManager(), "confirm_delete_messages_dialog");
    }

    private void reloadContacts()
    {
        ((BaseTabActivity) getActivity()).startIntentService(ContactsIntentService.ACTION_CODE_LOAD_CONTACTS,
                ContactsIntentService.class);
    }

    private Message getMessageAtIndex(int index)
    {
        if (index < 0)
            return null;
        Cursor cursor = (Cursor) listViewAdapter.getItem(index);
        return MessageStoreHelper.readFromCursor(cursor);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        Uri baseUri = MessagesContentProvider.CONTENT_URI;
        // Create and return a CursorLoader that will take care of creating a Cursor for the data being displayed.
        return new CursorLoader(applicationContext, baseUri, DataBaseHelper.TABLE_MESSAGES_ALL_COLUMNS,
                DataBaseHelper.TABLE_MESSAGES_COLUMN_MESSAGE_TYPE + "!= ?",
                new String[] { MessageType.STATUS.toString() }, DataBaseHelper.TABLE_MESSAGES_COLUMN_TIMESTAMP
                        + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data)
    {
        // Swap in a new Cursor, returning the old Cursor.
        listViewAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {
        // Swap in a new Cursor, returning the old Cursor.
        listViewAdapter.swapCursor(null);
    }

    private void showMessageDetailsAtPosition(int index)
    {
        Message message = getMessageAtIndex(index);
        DialogFragment newFragment = MessageDetailsDialogFragment.newInstance(message.getMessageType(),
                message.getPhoneNumber(), message.getTimestamp(), message.getDateUpdated(), message.getBody());
        newFragment.setTargetFragment(this, 0);
        newFragment.show(getFragmentManager(), "message_details_dialog");
    }

    private void resendMessage(Message message)
    {
        Intent messagingIntent = new Intent(applicationContext, MessagingIntentService.class);
        messagingIntent.putExtra(MessagingIntentService.PARAM_IN_MESSAGE_ID, message.getId());
        ((BaseTabActivity) getActivity()).startIntentService(MessagingIntentService.ACTION_CODE_RESEND_MESSAGE,
                messagingIntent);
    }

    @Override
    public void onFinishConfirmDeleteMessagesDialog(boolean confirmed)
    {
        if (confirmed)
        {
            MessageStoreHelper.deleteAllMessages(applicationContext);
            Toast.makeText(myActivity.getApplicationContext(), R.string.lbl_messages_deleted, Toast.LENGTH_LONG).show();
        }
    }

    private class ActionModeCallback implements ActionMode.Callback
    {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu)
        {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.messages_context_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu)
        {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item)
        {
            List<Message> selectedItems = new ArrayList<Message>();
            SparseBooleanArray selected = listViewAdapter.getSelectedIds();
            for (int i = (selected.size() - 1); i >= 0; i--)
            {
                if (selected.valueAt(i))
                    selectedItems.add(MessageStoreHelper.readFromCursor((Cursor) listViewAdapter.getItem(selected
                            .keyAt(i))));
            }
            LogStoreHelper.info(MessagesTabFragment.this, applicationContext, "Selected Items: " + selectedItems);
            mode.finish();
            switch (item.getItemId())
            {
            case R.id.action_delete_message:
                // TODO: Bulk delete
                for (Message message : selectedItems)
                    MessageStoreHelper.deleteMessage(applicationContext, message.getId());
                return true;
            case R.id.action_resend_message:
                // TODO: Bulk send
                for (Message message : selectedItems)
                    resendMessage(message);
                return true;
            default:
                return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode)
        {
            listViewAdapter.removeSelection();
            actionMode = null;
        }
    };

    protected static class RowViewHolder
    {
        public View mRowContents;
    }

    public class MyCursorAdapter extends SimpleCursorAdapter
    {
        // TODO: How does it react to updates to the underlying data?
        private SparseBooleanArray selectedItemsIds = new SparseBooleanArray();

        public MyCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags)
        {
            super(context, layout, c, from, to, flags);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            View view = super.getView(position, convertView, parent);
            // TODO: No hard-coded UI constant
            view.setBackgroundColor(selectedItemsIds.get(position) ? 0x9934B5E4 : Color.TRANSPARENT);
            return view;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent)
        {
            View view = View.inflate(context, R.layout.messageslistitem, null);
            RowViewHolder holder = new RowViewHolder();
            holder.mRowContents = view.findViewById(R.id.messageslistitemouterlayout);
            view.setTag(holder);
            return view;
        }

        @Override
        public void setViewText(TextView v, String text)
        {
            if (v.getId() == R.id.messageslistitemtries)
            {
                if (Integer.parseInt(text) > 1)
                {
                    v.setVisibility(View.VISIBLE);
                    v.setText(getString(R.string.lbl_message_header_tried, text));
                }
                else
                    v.setVisibility(View.GONE);
            }
            else
            {
                String transformedText = text;
                if (v.getId() == R.id.messagelistitemdate)
                    transformedText = dateFormat.format(new Date(Long.parseLong(text)));
                else if (v.getId() == R.id.messagelistitemcontact)
                {
                    String contactName = ContactsIntentService.findContactName(getUserData().getContacts(), text);
                    if (!StringUtil.empty(contactName))
                        transformedText = contactName;
                }
                super.setViewText(v, transformedText);
            }
        }

        @Override
        public void setViewImage(ImageView v, String value)
        {
            if (v.getId() == R.id.messageslistdirectionicon)
                v.setImageResource(MessageType.fromString(value).getIcon());
            else if (v.getId() == R.id.messagesliststatusicon)
                v.setImageResource(MessageStatus.fromString(value).getIcon());
            else
                super.setViewImage(v, value);
        }

        public void toggleSelection(int position)
        {
            selectView(position, !selectedItemsIds.get(position));
        }

        public void removeSelection()
        {
            selectedItemsIds = new SparseBooleanArray();
            notifyDataSetChanged();
        }

        public void selectView(int position, boolean value)
        {
            if (value)
                selectedItemsIds.put(position, value);
            else
                selectedItemsIds.delete(position);

            notifyDataSetChanged();
        }

        public int getSelectedCount()
        {
            return selectedItemsIds.size();
        }

        public SparseBooleanArray getSelectedIds()
        {
            return selectedItemsIds;
        }
    }
}
