package com.tinywebgears.relayme;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import android.util.Log;

import com.tinywebgears.relayme.dao.DataBaseHelper;
import com.tinywebgears.relayme.model.EventType;
import com.tinywebgears.relayme.model.Message;
import com.tinywebgears.relayme.model.MessageType;
import com.tinywebgears.relayme.service.MessageStoreHelper;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class MessagesContentProviderTest
{
    private static final String TAG = "MsgContentProviderTest";
    private static final Message sTestMessage = new Message("0123", EventType.SMS, MessageType.INCOMING,
            "+61-411-123456", "Hello, World!", new Date());

    private Context context;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getTargetContext();
    }

    @Test
    public void testMessagesGetDeleted() throws Exception
    {
        try
        {
            pauseMessageProcessing(context);

            Log.d(TAG, "Inserting a new item...");
            // FIXME: Insertion doesn't work.
            Uri newRecordUri = MessageStoreHelper.insertMessage(context, sTestMessage);
            long newRecordId = Long.parseLong(newRecordUri.getPathSegments().get(1));
            Log.i(TAG, "New recored created: " + newRecordUri + ", ID is: " + newRecordId);

            Log.d(TAG, "Deleting all the existing records...");
            int count = MessageStoreHelper.deleteMessage(context, newRecordId);
            Log.i(TAG, "Number of existing records deleted: " + count);

            Log.d(TAG, "Retrieving all the items...");
            Message message = MessageStoreHelper.findMessageById(context, newRecordId);
            assertNull(message);
        }
        finally
        {
            resumeMessageProcessing(context);
        }
    }

    @Test
    public void testNewMessageGetsAdded() throws Exception
    {
        try
        {
            pauseMessageProcessing(context);

            Log.d(TAG, "Inserting a new item...");
            // FIXME: Insertion doesn't work.
            Uri newRecordUri = MessageStoreHelper.insertMessage(context, sTestMessage);
            long newRecordId = Long.parseLong(newRecordUri.getPathSegments().get(1));
            Log.i(TAG, "New recored created: " + newRecordUri + ", ID is: " + newRecordId);

            Log.d(TAG, "Retrieving the newly inserted item via its unique url...");
            Message message = MessageStoreHelper.findMessageById(context, newRecordId);
            message.setDateUpdated(sTestMessage.getDateUpdated());
            assertEquals(message, sTestMessage);

            Log.d(TAG, "Retrieving the newly inserted intem via a query...");
            String selectionCriteria = DataBaseHelper.TABLE_MESSAGES_COLUMN_ID + " = ?";
            String[] values = new String[] { Long.toString(newRecordId) };
            Message message2 = MessageStoreHelper.findUniqueMessageByCriteria(context, selectionCriteria, values);
            assertEquals(message2.getId(), sTestMessage.getId());
        }
        finally
        {
            resumeMessageProcessing(context);
        }
    }

    private void pauseMessageProcessing(Context context)
    {
        setGlobalSwitch(context, false);
    }

    private void resumeMessageProcessing(Context context)
    {
        setGlobalSwitch(context, true);
    }

    private void setGlobalSwitch(Context context, boolean globalSwitch)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean("GLOBAL_SWITCH", globalSwitch);
        edit.commit();
    }
}