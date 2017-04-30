package com.tinywebgears.relayme.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.codolutions.android.common.util.StringUtil;
import com.tinywebgears.relayme.contentprovider.LogEntriesContentProvider;
import com.tinywebgears.relayme.dao.DataBaseHelper;
import com.tinywebgears.relayme.model.LogEntry;
import com.tinywebgears.relayme.model.LogLevel;

/**
 * Helper utility that makes calls to content provider to manage log entries.
 */
public class LogStoreHelper
{
    // These constants are public and non-final to facilitate testing.
    public static int LIMIT_LOGS_TO_SEND = 1000;
    public static int LIMIT_LOGS_TO_KEEP = 2000;
    public static int DELETE_BATCH_SIZE = 100;
    public static int STACKTRACE_MAX_LENGTH = 600;

    public static void error(Context context, String message, Exception e)
    {
        error(context, context, message, e);
    }

    public static void error(Object o, Context context, String message, Exception e)
    {
        if (e != null)
            Log.e(getLogTag(o), message, e);
        else
            Log.e(getLogTag(o), message);
        if (context != null)
        {
            String stackTrace = (e != null) ? "\n" + StringUtil.getStackTrace(e, STACKTRACE_MAX_LENGTH) : "";
            insertLogEntry(context, new LogEntry(LogLevel.ERROR, true, message + stackTrace));
        }
    }

    public static void warn(Context context, String message, Exception e)
    {
        warn(context, context, message, e);
    }

    public static void warn(Context context, String message, Exception e, boolean visible)
    {
        warn(context, context, message, e, visible);
    }

    public static void warn(Object o, Context context, String message, Exception e)
    {
        warn(o, context, message, e, true);
    }

    public static void warn(Object o, Context context, String message, Exception e, boolean visible)
    {
        if (e != null)
            Log.w(getLogTag(o), message, e);
        else
            Log.w(getLogTag(o), message);
        if (context != null)
        {
            String stackTrace = (e != null) ? "\n" + StringUtil.getStackTrace(e, STACKTRACE_MAX_LENGTH) : "";
            insertLogEntry(context, new LogEntry(LogLevel.WARN, visible, message + stackTrace));
        }
    }

    public static void info(Context context, String message)
    {
        info(context, context, message);
    }

    public static void info(Context context, String message, Exception e)
    {
        info(context, context, message, e);
    }

    public static void info(Object o, Context context, String message)
    {
        info(o, context, message, null);
    }

    public static void info(Object o, Context context, String message, Exception e)
    {
        if (e != null)
            Log.i(getLogTag(o), message, e);
        else
            Log.i(getLogTag(o), message);
        if (context != null)
        {
            String stackTrace = (e != null) ? "\n" + StringUtil.getStackTrace(e, STACKTRACE_MAX_LENGTH) : "";
            insertLogEntry(context, new LogEntry(LogLevel.INFO, false, message + stackTrace));
        }
    }

    private static String getLogTag(Object o)
    {
        if (o instanceof Class)
            return ((Class<?>) o).getName();
        return o.getClass().getName();
    }

    public static Uri insertLogEntry(Context context, LogEntry logEntry)
    {
        logEntry.setDateUpdated(new Date());
        ContentValues values = getContentValues(logEntry, false);
        Uri newRecordUri = context.getContentResolver().insert(LogEntriesContentProvider.CONTENT_URI, values);
        if (newRecordUri == null)
            return null;
        long newRecordId = Long.parseLong(newRecordUri.getPathSegments().get(1));
        logEntry.updateId(newRecordId);
        notifyChanges(context);
        return newRecordUri;
    }

    public static int updateLogEntry(Context context, LogEntry logEntry)
    {
        logEntry.setDateUpdated(new Date());
        ContentValues values = getContentValues(logEntry, true);
        int result = context.getContentResolver().update(LogEntriesContentProvider.CONTENT_URI, values,
                DataBaseHelper.TABLE_LOGENTRIES_COLUMN_ID + " = ?", new String[] { Long.toString(logEntry.getId()) });
        Log.d(LogStoreHelper.class.getName(), "Recored updated, result" + result);
        notifyChanges(context);
        return result;
    }

    public static int deleteAllLogEntries(Context context)
    {
        int result = context.getContentResolver().delete(LogEntriesContentProvider.CONTENT_URI, null, null);
        notifyChanges(context);
        return result;
    }

    public static int hideAllLogEntries(Context context)
    {
        ContentValues values = new ContentValues();
        values.put(DataBaseHelper.TABLE_LOGENTRIES_COLUMN_VISIBLE_TO_USER, DataBaseHelper.BOOLEAN_INT_VALUE_FALSE);
        int result = context.getContentResolver().update(LogEntriesContentProvider.CONTENT_URI, values, null, null);
        notifyChanges(context);
        return result;
    }

    public static int getNumberOfRecords(Context context)
    {
        Cursor cursor = null;
        try
        {
            cursor = context.getContentResolver().query(LogEntriesContentProvider.CONTENT_URI,
                    DataBaseHelper.TABLE_LOGENTRIES_ALL_COLUMNS, null, null, null);
            int numberOfRecords = cursor.getCount();
            return numberOfRecords;
        }
        finally
        {
            if (cursor != null)
                cursor.close();
        }
    }

    public static LogEntry getOldestToKeep(Context context)
    {
        Cursor cursor = null;
        try
        {
            cursor = context.getContentResolver().query(LogEntriesContentProvider.CONTENT_URI,
                    DataBaseHelper.TABLE_LOGENTRIES_ALL_COLUMNS, null, null,
                    DataBaseHelper.TABLE_LOGENTRIES_COLUMN_DATE_UPDATED + " ASC");
            LogEntry logEntry = null;
            if (cursor.getCount() > DELETE_BATCH_SIZE)
            {
                cursor.move(DELETE_BATCH_SIZE);
                logEntry = readFromCursor(cursor);
            }
            return logEntry;
        }
        finally
        {
            if (cursor != null)
                cursor.close();
        }
    }

    public static List<LogEntry> getAllLogsToSend(Context context, boolean lowMemory)
    {
        List<LogEntry> logs = new ArrayList<LogEntry>();
        Cursor cursor = null;
        try
        {
            cursor = context.getContentResolver().query(LogEntriesContentProvider.CONTENT_URI,
                    DataBaseHelper.TABLE_LOGENTRIES_ALL_COLUMNS, null, null,
                    DataBaseHelper.TABLE_LOGENTRIES_COLUMN_DATE_UPDATED + " DESC");
            int recordsToSend = LIMIT_LOGS_TO_SEND;
            if (lowMemory)
                recordsToSend /= 10;
            int numberOfRecords = 0;
            if (cursor.getCount() > 0)
            {
                cursor.moveToFirst();
                do
                    logs.add(readFromCursor(cursor));
                while (cursor.moveToNext() && numberOfRecords++ < recordsToSend);
            }
            return logs;
        }
        finally
        {
            if (cursor != null)
                cursor.close();
        }
    }

    public static LogEntry readFromCursor(Cursor cursor)
    {
        return LogEntriesContentProvider.readFromCursor(cursor);
    }

    public static LogEntry readFromValues(ContentValues values)
    {
        return LogEntriesContentProvider.readFromValues(values);
    }

    public static ContentValues getContentValues(LogEntry logEntry, boolean includeId)
    {
        return LogEntriesContentProvider.getContentValues(logEntry, includeId);
    }

    public static long getIdFromUri(Uri uri)
    {
        return Long.parseLong(uri.getPathSegments().get(1));
    }

    private static void notifyChanges(Context context)
    {
        context.getContentResolver().notifyChange(LogEntriesContentProvider.CONTENT_URI, null);
    }

    public static int purgeOldRecords(Context context)
    {
        int rowsDeleted = 0;
        while (getNumberOfRecords(context) > LIMIT_LOGS_TO_KEEP)
        {
            LogEntry oldestRecordToKeep = getOldestToKeep(context);
            int result = context.getContentResolver().delete(LogEntriesContentProvider.CONTENT_URI,
                    DataBaseHelper.TABLE_LOGENTRIES_COLUMN_DATE_UPDATED + " < ?",
                    new String[] { Long.toString(oldestRecordToKeep.getDateUpdated().getTime()) });
            rowsDeleted += result;
            notifyChanges(context);
        }
        return rowsDeleted;
    }
}
