package com.tinywebgears.relayme.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.tinywebgears.relayme.contentprovider.MessagesContentProvider;
import com.tinywebgears.relayme.dao.DataBaseHelper;
import com.tinywebgears.relayme.model.Message;
import com.tinywebgears.relayme.model.MessageStatus;
import com.tinywebgears.relayme.model.MessageType;

public class MessageStoreHelper
{
    private static final String TAG = MessageStoreHelper.class.getName();

    // TODO: Don't insert the same message if we get the SMS broadcast multiple times.
    public static Uri insertMessage(Context context, Message message)
    {
        message.setDateUpdated(new Date());
        ContentValues values = getContentValues(message, false);
        Uri newRecordUri = context.getContentResolver().insert(MessagesContentProvider.CONTENT_URI, values);
        if (newRecordUri == null)
            return null;
        long newRecordId = Long.parseLong(newRecordUri.getPathSegments().get(1));
        LogStoreHelper.info(context, "Message inserted, uri: " + newRecordUri + ", id: " + newRecordId);
        message.updateId(newRecordId);
        notifyChanges(context);
        return newRecordUri;
    }

    public static int updateMessage(Context context, Message message)
    {
        message.setDateUpdated(new Date());
        ContentValues values = getContentValues(message, true);
        int result = context.getContentResolver().update(MessagesContentProvider.CONTENT_URI, values,
                DataBaseHelper.TABLE_MESSAGES_COLUMN_ID + " = ?", new String[] { Long.toString(message.getId()) });
        LogStoreHelper.info(context, "Message updated, result: " + result);
        notifyChanges(context);
        return result;
    }

    public static int deleteAllMessages(Context context)
    {
        int result = context.getContentResolver().delete(MessagesContentProvider.CONTENT_URI, null, null);
        notifyChanges(context);
        return result;
    }

    public static int deleteMessage(Context context, long id)
    {
        Uri singleUri = ContentUris.withAppendedId(MessagesContentProvider.CONTENT_URI, id);
        int result = context.getContentResolver().delete(singleUri, null, null);
        notifyChanges(context);
        return result;
    }

    public static Message findMessageById(Context context, long id)
    {
        Uri singleUri = ContentUris.withAppendedId(MessagesContentProvider.CONTENT_URI, id);
        Cursor cursor = null;
        try
        {
            cursor = context.getContentResolver().query(singleUri, DataBaseHelper.TABLE_MESSAGES_ALL_COLUMNS, null,
                    null, null);
            if (cursor.getCount() != 1)
            {
                LogStoreHelper.warn(context, "Unable to find a message in database with ID: " + id, null);
                return null;
            }
            cursor.moveToFirst();
            Message message = readFromCursor(cursor);
            return message;
        }
        finally
        {
            if (cursor != null)
                cursor.close();
        }
    }

    public static Message findUniqueMessageByCriteria(Context context, String selectionCriteria, String[] values)
    {
        Cursor cursor = null;
        try
        {
            cursor = context.getContentResolver().query(MessagesContentProvider.CONTENT_URI,
                    DataBaseHelper.TABLE_MESSAGES_ALL_COLUMNS, selectionCriteria, values, null);
            if (cursor.getCount() != 1)
            {
                LogStoreHelper.warn(context, "Unable to find a message in database with criteria: " + selectionCriteria
                        + " and values: " + values, null);
                return null;
            }
            cursor.moveToFirst();
            Message message = readFromCursor(cursor);
            return message;
        }
        finally
        {
            if (cursor != null)
                cursor.close();
        }
    }

    public static List<Message> getPendingMessages(Context context)
    {
        List<Message> pendingMessages = new ArrayList<Message>();
        Cursor cursor = null;
        try
        {
            cursor = context.getContentResolver().query(
                    MessagesContentProvider.CONTENT_URI,
                    DataBaseHelper.TABLE_MESSAGES_ALL_COLUMNS,
                    DataBaseHelper.TABLE_MESSAGES_COLUMN_STATUS + " = ? OR "
                            + DataBaseHelper.TABLE_MESSAGES_COLUMN_STATUS + " = ?",
                    new String[] { MessageStatus.NEW.toString(), MessageStatus.FAILED.toString() },
                    DataBaseHelper.TABLE_MESSAGES_COLUMN_DATE_UPDATED + " ASC");
            cursor.moveToFirst();
            if (cursor.getCount() > 0)
            {
                do
                    pendingMessages.add(readFromCursor(cursor));
                while (cursor.moveToNext());
            }
            return pendingMessages;
        }
        finally
        {
            if (cursor != null)
                cursor.close();
        }
    }

    public static Date getLastTimeMessagesSent(Context context)
    {
        Cursor cursor = null;
        try
        {
            cursor = context.getContentResolver().query(MessagesContentProvider.CONTENT_URI,
                    new String[] { DataBaseHelper.TABLE_MESSAGES_COLUMN_DATE_UPDATED },
                    DataBaseHelper.TABLE_MESSAGES_COLUMN_STATUS + " = ?",
                    new String[] { MessageStatus.SENT.toString() },
                    DataBaseHelper.TABLE_MESSAGES_COLUMN_TIMESTAMP + " DESC");
            cursor.moveToFirst();
            if (cursor.getCount() > 0)
            {
                long dateLong = cursor.getLong(cursor
                        .getColumnIndexOrThrow(DataBaseHelper.TABLE_MESSAGES_COLUMN_DATE_UPDATED));
                return dateLong > 0 ? new Date(dateLong) : new Date(0);

            }
            return new Date(0);
        }
        finally
        {
            if (cursor != null)
                cursor.close();
        }
    }

    public static int getNumberOfTextsSent(Context context)
    {
        Cursor cursor = null;
        try
        {
            cursor = context.getContentResolver().query(
                    MessagesContentProvider.CONTENT_URI,
                    new String[] { DataBaseHelper.TABLE_MESSAGES_COLUMN_ID },
                    DataBaseHelper.TABLE_MESSAGES_COLUMN_MESSAGE_TYPE + " = ? AND "
                            + DataBaseHelper.TABLE_MESSAGES_COLUMN_STATUS + " = ?",
                    new String[] { MessageType.OUTGOING.toString(), MessageStatus.SENT.toString() }, null);
            return cursor.getCount();
        }
        finally
        {
            if (cursor != null)
                cursor.close();
        }
    }

    public static int getNumberOfTextsReceived(Context context)
    {
        Cursor cursor = null;
        try
        {
            cursor = context.getContentResolver().query(MessagesContentProvider.CONTENT_URI,
                    new String[] { DataBaseHelper.TABLE_MESSAGES_COLUMN_ID },
                    DataBaseHelper.TABLE_MESSAGES_COLUMN_MESSAGE_TYPE + " = ?",
                    new String[] { MessageType.INCOMING.toString() }, null);
            return cursor.getCount();
        }
        finally
        {
            if (cursor != null)
                cursor.close();
        }
    }

    public static List<Message> getProgressingMessages(Context context)
    {
        List<Message> progressingMessages = new ArrayList<Message>();
        Cursor cursor = null;
        try
        {
            cursor = context.getContentResolver().query(MessagesContentProvider.CONTENT_URI,
                    DataBaseHelper.TABLE_MESSAGES_ALL_COLUMNS, DataBaseHelper.TABLE_MESSAGES_COLUMN_STATUS + " = ?",
                    new String[] { MessageStatus.SENDING.toString() },
                    DataBaseHelper.TABLE_MESSAGES_COLUMN_DATE_UPDATED + " ASC");
            cursor.moveToFirst();
            if (cursor.getCount() > 0)
            {
                do
                    progressingMessages.add(readFromCursor(cursor));
                while (cursor.moveToNext());
            }
            return progressingMessages;
        }
        finally
        {
            if (cursor != null)
                cursor.close();
        }
    }

    public static Message readFromCursor(Cursor cursor)
    {
        return MessagesContentProvider.readFromCursor(cursor);
    }

    public static ContentValues getContentValues(Message message, boolean includeId)
    {
        return MessagesContentProvider.getContentValues(message, includeId);
    }

    public static long getIdFromUri(Uri uri)
    {
        return Long.parseLong(uri.getPathSegments().get(1));
    }

    private static void notifyChanges(Context context)
    {
        context.getContentResolver().notifyChange(MessagesContentProvider.CONTENT_URI, null);
    }

    public static void purgeOldRecords(Context context)
    {
        // TODO: Delete records older than 1 week.
        // TODO: Delete oldest records if total number of records exceed some limit.
    }
}
