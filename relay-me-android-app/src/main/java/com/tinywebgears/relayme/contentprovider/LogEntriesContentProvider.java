package com.tinywebgears.relayme.contentprovider;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.tinywebgears.relayme.dao.DataBaseHelper;
import com.tinywebgears.relayme.model.LogEntry;
import com.tinywebgears.relayme.model.LogLevel;

/**
 * Main content provider class which is a wrapper on the SQLite database.
 */
public class LogEntriesContentProvider extends ContentProvider
{
    private static final String TAG = LogEntriesContentProvider.class.getName();

    public static final int UNIQUE_ID = 102;

    // Used for the UriMacher
    private static final int LOGENTRIES = 10;
    private static final int LOGENTRY_ID = 20;

    private static final String AUTHORITY = "com.tinywebgears.relayme.contentprovider.logentries";

    private static final String BASE_PATH = "logentries";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

    public static final String CONTENT_TYPE_LOGENTRIES = ContentResolver.CURSOR_DIR_BASE_TYPE + "/logentries";
    public static final String CONTENT_TYPE_LOGENTRY = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/logentry";

    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static
    {
        URI_MATCHER.addURI(AUTHORITY, BASE_PATH, LOGENTRIES);
        URI_MATCHER.addURI(AUTHORITY, BASE_PATH + "/#", LOGENTRY_ID);
    }

    // database
    private DataBaseHelper mDatabase;

    @Override
    public boolean onCreate()
    {
        mDatabase = new DataBaseHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        // Uisng SQLiteQueryBuilder instead of query() method
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        // Check if the caller has requested a column which does not exists
        checkColumns(projection);

        // Set the table
        queryBuilder.setTables(DataBaseHelper.TABLE_LOGENTRIES);

        int uriType = URI_MATCHER.match(uri);
        switch (uriType)
        {
        case LOGENTRIES:
            break;
        case LOGENTRY_ID:
            // Adding the ID to the original query
            queryBuilder.appendWhere(DataBaseHelper.TABLE_LOGENTRIES_COLUMN_ID + "=" + uri.getLastPathSegment());
            break;
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = mDatabase.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        // Make sure that potential listeners are getting notified
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri)
    {
        return null;
    }

    /**
     * This method inserts a new record, or updates an existing one.
     */
    @Override
    public Uri insert(Uri uri, ContentValues values)
    {
        int uriType = URI_MATCHER.match(uri);
        long id = 0;
        switch (uriType)
        {
        case LOGENTRIES:
            boolean insertRecord = true;
            LogEntry newRecord = readFromValues(values);
            LogEntry lastRecord = getLastUserVisibleEntry(getContext());
            SQLiteDatabase sqlDb = mDatabase.getWritableDatabase();
            if (isSameEvent(lastRecord, newRecord))
            {
                // Log.d(TAG, "This is reoccurrance of the last record: " + lastRecord);
                insertRecord = false;
                id = lastRecord.getId();
                lastRecord.incrementCount();
                lastRecord.setDateUpdated(newRecord.getDateUpdated());
                ContentValues newValues = getContentValues(lastRecord, true);
                sqlDb.update(DataBaseHelper.TABLE_LOGENTRIES, newValues, DataBaseHelper.TABLE_LOGENTRIES_COLUMN_ID
                        + " = ?", new String[] { Long.toString(id) });
            }
            if (insertRecord)
                id = sqlDb.insert(DataBaseHelper.TABLE_LOGENTRIES, null, values);
            break;
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(BASE_PATH + "/" + id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs)
    {
        int uriType = URI_MATCHER.match(uri);
        SQLiteDatabase sqlDb = mDatabase.getWritableDatabase();
        int rowsDeleted = 0;
        switch (uriType)
        {
        case LOGENTRIES:
            rowsDeleted = sqlDb.delete(DataBaseHelper.TABLE_LOGENTRIES, selection, selectionArgs);
            break;
        case LOGENTRY_ID:
            String id = uri.getLastPathSegment();
            if (TextUtils.isEmpty(selection))
                rowsDeleted = sqlDb.delete(DataBaseHelper.TABLE_LOGENTRIES, DataBaseHelper.TABLE_LOGENTRIES_COLUMN_ID
                        + "=" + id, null);
            else
                rowsDeleted = sqlDb.delete(DataBaseHelper.TABLE_LOGENTRIES, DataBaseHelper.TABLE_LOGENTRIES_COLUMN_ID
                        + "=" + id + " and " + selection, selectionArgs);
            break;
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
    {
        // TODO: Don't allow updating unique fields and what we have stored in other tables.
        int uriType = URI_MATCHER.match(uri);
        SQLiteDatabase sqlDb = mDatabase.getWritableDatabase();
        int rowsUpdated = 0;
        switch (uriType)
        {
        case LOGENTRIES:
            rowsUpdated = sqlDb.update(DataBaseHelper.TABLE_LOGENTRIES, values, selection, selectionArgs);
            break;
        case LOGENTRY_ID:
            String id = uri.getLastPathSegment();
            if (TextUtils.isEmpty(selection))
                rowsUpdated = sqlDb.update(DataBaseHelper.TABLE_LOGENTRIES, values,
                        DataBaseHelper.TABLE_LOGENTRIES_COLUMN_ID + "=" + id, null);
            else
                rowsUpdated = sqlDb.update(DataBaseHelper.TABLE_LOGENTRIES, values,
                        DataBaseHelper.TABLE_LOGENTRIES_COLUMN_ID + "=" + id + " and " + selection, selectionArgs);
            break;
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    private void checkColumns(String[] projection)
    {
        String[] available = DataBaseHelper.TABLE_LOGENTRIES_ALL_COLUMNS;
        if (projection != null)
        {
            Set<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
            Set<String> availableColumns = new HashSet<String>(Arrays.asList(available));
            // Check if all columns which are requested are available
            if (!availableColumns.containsAll(requestedColumns))
                throw new IllegalArgumentException("Unknown columns in projection");
        }
    }

    public LogEntry getLastUserVisibleEntry(Context context)
    {
        SQLiteDatabase sqlDb = mDatabase.getReadableDatabase();
        Cursor cursor = null;
        try
        {
            cursor = sqlDb.query(DataBaseHelper.TABLE_LOGENTRIES, DataBaseHelper.TABLE_LOGENTRIES_ALL_COLUMNS,
                    DataBaseHelper.TABLE_LOGENTRIES_COLUMN_VISIBLE_TO_USER + " = ?",
                    new String[] { Integer.toString(DataBaseHelper.BOOLEAN_INT_VALUE_TRUE) }, null, null,
                    DataBaseHelper.TABLE_LOGENTRIES_COLUMN_DATE_UPDATED + " DESC");
            if (cursor.getCount() > 0)
            {
                cursor.moveToFirst();
                return readFromCursor(cursor);
            }
            return null;
        }
        finally
        {
            if (cursor != null)
                cursor.close();
        }
    }

    public static LogEntry readFromCursor(Cursor cursor)
    {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(DataBaseHelper.TABLE_LOGENTRIES_COLUMN_ID));
        LogLevel logLevel = LogLevel.fromString(cursor.getString(cursor
                .getColumnIndexOrThrow(DataBaseHelper.TABLE_LOGENTRIES_COLUMN_LOGLEVEL)));
        boolean visibleToUser = cursor.getInt(cursor
                .getColumnIndexOrThrow(DataBaseHelper.TABLE_LOGENTRIES_COLUMN_VISIBLE_TO_USER)) == 1;
        String text = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseHelper.TABLE_LOGENTRIES_COLUMN_BODY));
        Date dateUpdated = readDate(cursor, DataBaseHelper.TABLE_LOGENTRIES_COLUMN_DATE_UPDATED);
        int count = cursor.getInt(cursor
                .getColumnIndexOrThrow(DataBaseHelper.TABLE_LOGENTRIES_COLUMN_NUMBER_OF_OCCURRENCES));
        LogEntry logEntry = new LogEntry(id, logLevel, visibleToUser, text, dateUpdated, count);
        return logEntry;
    }

    public static LogEntry readFromValues(ContentValues values)
    {
        long id = 0;
        if (values.containsKey(DataBaseHelper.TABLE_LOGENTRIES_COLUMN_ID))
            id = values.getAsLong(DataBaseHelper.TABLE_LOGENTRIES_COLUMN_ID);
        LogLevel logLevel = LogLevel.fromString(values.getAsString(DataBaseHelper.TABLE_LOGENTRIES_COLUMN_LOGLEVEL));
        boolean visibleToUser = values.getAsBoolean(DataBaseHelper.TABLE_LOGENTRIES_COLUMN_VISIBLE_TO_USER);
        String text = values.getAsString(DataBaseHelper.TABLE_LOGENTRIES_COLUMN_BODY);
        Date dateUpdated = new Date(values.getAsLong(DataBaseHelper.TABLE_LOGENTRIES_COLUMN_DATE_UPDATED));
        int count = values.getAsInteger(DataBaseHelper.TABLE_LOGENTRIES_COLUMN_NUMBER_OF_OCCURRENCES);
        return new LogEntry(id, logLevel, visibleToUser, text, dateUpdated, count);
    }

    public static ContentValues getContentValues(LogEntry logEntry, boolean includeId)
    {
        // Log.d(LogEntriesContentProvider.class.getName(), "Writing log entry into ContentValues: " + logEntry);
        ContentValues values = new ContentValues();
        values.clear();
        if (includeId)
            values.put(DataBaseHelper.TABLE_LOGENTRIES_COLUMN_ID, logEntry.getId());
        values.put(DataBaseHelper.TABLE_LOGENTRIES_COLUMN_LOGLEVEL, logEntry.getLogLevel().toString());
        values.put(DataBaseHelper.TABLE_LOGENTRIES_COLUMN_VISIBLE_TO_USER, logEntry.isVisibleToUser());
        values.put(DataBaseHelper.TABLE_LOGENTRIES_COLUMN_BODY, logEntry.getText());
        values.put(DataBaseHelper.TABLE_LOGENTRIES_COLUMN_DATE_UPDATED, logEntry.getDateUpdated().getTime());
        values.put(DataBaseHelper.TABLE_LOGENTRIES_COLUMN_NUMBER_OF_OCCURRENCES, logEntry.getCount());
        return values;
    }

    private static Date readDate(Cursor cursor, String columnName)
    {
        long dateLong = cursor.getLong(cursor.getColumnIndexOrThrow(columnName));
        return dateLong > 0 ? new Date(dateLong) : null;
    }

    public static boolean isSameEvent(LogEntry logEntry, LogEntry newRecord)
    {
        if (logEntry == null && newRecord == null)
            return true;
        if (logEntry == null || newRecord == null)
            return false;
        if (logEntry.getLogLevel() != newRecord.getLogLevel())
            return false;
        if (!logEntry.getText().equals(newRecord.getText()))
            return false;
        // The rest doesn't matter.
        return true;
    }
}
