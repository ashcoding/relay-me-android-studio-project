package com.tinywebgears.relayme.contentprovider;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.tinywebgears.relayme.dao.DataBaseHelper;
import com.tinywebgears.relayme.model.EventType;
import com.tinywebgears.relayme.model.Message;
import com.tinywebgears.relayme.model.MessageStatus;
import com.tinywebgears.relayme.model.MessageType;
import com.tinywebgears.relayme.service.LogStoreHelper;

/**
 * Main content provider class which is a wrapper on the SQLite database.
 */
public class MessagesContentProvider extends ContentProvider
{
    private static final String TAG = MessagesContentProvider.class.getName();

    public static final int UNIQUE_ID = 102;

    // Used for the UriMacher
    private static final int MESSAGES = 10;
    private static final int MESSAGE_ID = 20;

    private static final String AUTHORITY = "com.tinywebgears.relayme.contentprovider.messages";

    private static final String BASE_PATH = "messages";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

    public static final String CONTENT_TYPE_MESSAGES = ContentResolver.CURSOR_DIR_BASE_TYPE + "/messages";
    public static final String CONTENT_TYPE_MESSAGE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/message";

    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
    private static final boolean IGNORE_INSERT_WHEN_RECORD_EXISTS = true;

    static
    {
        URI_MATCHER.addURI(AUTHORITY, BASE_PATH, MESSAGES);
        URI_MATCHER.addURI(AUTHORITY, BASE_PATH + "/#", MESSAGE_ID);
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
        queryBuilder.setTables(DataBaseHelper.TABLE_MESSAGES);

        int uriType = URI_MATCHER.match(uri);
        switch (uriType)
        {
        case MESSAGES:
            break;
        case MESSAGE_ID:
            // Adding the ID to the original query
            queryBuilder.appendWhere(DataBaseHelper.TABLE_MESSAGES_COLUMN_ID + "=" + uri.getLastPathSegment());
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
     * This method inserts a new record and ignore possible duplicates.
     */
    @Override
    public Uri insert(Uri uri, ContentValues values)
    {
        int uriType = URI_MATCHER.match(uri);
        SQLiteDatabase sqlDB = mDatabase.getWritableDatabase();
        long id = 0;
        switch (uriType)
        {
        case MESSAGES:
            String externalId = values.getAsString(DataBaseHelper.TABLE_MESSAGES_COLUMN_EXTERNAL_ID);
            Long timestamp = values.getAsLong(DataBaseHelper.TABLE_MESSAGES_COLUMN_TIMESTAMP);
            if (externalId == null || timestamp == null)
                id = sqlDB.insert(DataBaseHelper.TABLE_MESSAGES, null, values);
            else
            {
                String selection = DataBaseHelper.TABLE_MESSAGES_COLUMN_EXTERNAL_ID + " = ? AND "
                        + DataBaseHelper.TABLE_MESSAGES_COLUMN_TIMESTAMP + " = ?";
                String[] selectionArgs = new String[] { externalId, timestamp.toString() };
                Cursor cursor = null;
                try
                {
                    cursor = sqlDB.query(DataBaseHelper.TABLE_MESSAGES, DataBaseHelper.TABLE_MESSAGES_ALL_COLUMNS,
                            selection, selectionArgs, null, null, null);
                    if (cursor.getCount() > 1)
                        throw new RuntimeException("More than one record exist in database, exactly "
                                + cursor.getCount() + ", matched keys for: " + values);
                    else if (cursor.getCount() == 1)
                    {
                        LogStoreHelper.info(this, getContext(), "An existing record exists, ID: " + externalId
                                + " timestamp: " + timestamp);
                        cursor.moveToFirst();
                        if (IGNORE_INSERT_WHEN_RECORD_EXISTS)
                        {
                            LogStoreHelper.info(this, getContext(), "This is a duplicate, ignoring...");
                            return null;
                        }
                        else
                        {
                            LogStoreHelper
                                    .info(this, getContext(),
                                            "Overriding the existing entry with new data. This might result in double-processing.");
                            id = cursor.getLong(cursor.getColumnIndexOrThrow(DataBaseHelper.TABLE_MESSAGES_COLUMN_ID));
                            sqlDB.update(DataBaseHelper.TABLE_MESSAGES, values, DataBaseHelper.TABLE_MESSAGES_COLUMN_ID
                                    + " = ?", new String[] { Long.toString(id) });
                        }
                    }
                    else
                        id = sqlDB.insert(DataBaseHelper.TABLE_MESSAGES, null, values);
                }
                finally
                {
                    if (cursor != null)
                        cursor.close();
                }
            }
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
        SQLiteDatabase sqlDB = mDatabase.getWritableDatabase();
        int rowsDeleted = 0;
        switch (uriType)
        {
        case MESSAGES:
            rowsDeleted = sqlDB.delete(DataBaseHelper.TABLE_MESSAGES, selection, selectionArgs);
            break;
        case MESSAGE_ID:
            String id = uri.getLastPathSegment();
            if (TextUtils.isEmpty(selection))
                rowsDeleted = sqlDB.delete(DataBaseHelper.TABLE_MESSAGES, DataBaseHelper.TABLE_MESSAGES_COLUMN_ID + "="
                        + id, null);
            else
                rowsDeleted = sqlDB.delete(DataBaseHelper.TABLE_MESSAGES, DataBaseHelper.TABLE_MESSAGES_COLUMN_ID + "="
                        + id + " and " + selection, selectionArgs);
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
        SQLiteDatabase sqlDB = mDatabase.getWritableDatabase();
        int rowsUpdated = 0;
        switch (uriType)
        {
        case MESSAGES:
            rowsUpdated = sqlDB.update(DataBaseHelper.TABLE_MESSAGES, values, selection, selectionArgs);
            break;
        case MESSAGE_ID:
            String id = uri.getLastPathSegment();
            if (TextUtils.isEmpty(selection))
                rowsUpdated = sqlDB.update(DataBaseHelper.TABLE_MESSAGES, values,
                        DataBaseHelper.TABLE_MESSAGES_COLUMN_ID + "=" + id, null);
            else
                rowsUpdated = sqlDB.update(DataBaseHelper.TABLE_MESSAGES, values,
                        DataBaseHelper.TABLE_MESSAGES_COLUMN_ID + "=" + id + " and " + selection, selectionArgs);
            break;
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    private void checkColumns(String[] projection)
    {
        String[] available = DataBaseHelper.TABLE_MESSAGES_ALL_COLUMNS;
        if (projection != null)
        {
            Set<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
            Set<String> availableColumns = new HashSet<String>(Arrays.asList(available));
            // Check if all columns which are requested are available
            if (!availableColumns.containsAll(requestedColumns))
                throw new IllegalArgumentException("Unknown columns in projection");
        }
    }

    public static Message readFromCursor(Cursor cursor)
    {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(DataBaseHelper.TABLE_MESSAGES_COLUMN_ID));
        String externalId = cursor.getString(cursor
                .getColumnIndexOrThrow(DataBaseHelper.TABLE_MESSAGES_COLUMN_EXTERNAL_ID));
        EventType eventType = EventType.fromString(cursor.getString(cursor
                .getColumnIndexOrThrow(DataBaseHelper.TABLE_MESSAGES_COLUMN_EVENT_TYPE)));
        MessageType messageType = MessageType.fromString(cursor.getString(cursor
                .getColumnIndexOrThrow(DataBaseHelper.TABLE_MESSAGES_COLUMN_MESSAGE_TYPE)));
        String phoneNumber = cursor.getString(cursor
                .getColumnIndexOrThrow(DataBaseHelper.TABLE_MESSAGES_COLUMN_PHONE_NUMBER));
        String contactName = cursor.getString(cursor
                .getColumnIndexOrThrow(DataBaseHelper.TABLE_MESSAGES_COLUMN_CONTACT_NAME));
        String body = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseHelper.TABLE_MESSAGES_COLUMN_BODY));
        Date messageTimestamp = readDate(cursor, DataBaseHelper.TABLE_MESSAGES_COLUMN_TIMESTAMP);
        Date dateUpdated = readDate(cursor, DataBaseHelper.TABLE_MESSAGES_COLUMN_DATE_UPDATED);
        Date dateTried = readDate(cursor, DataBaseHelper.TABLE_MESSAGES_COLUMN_DATE_TRIED);
        int tries = cursor.getInt(cursor.getColumnIndexOrThrow(DataBaseHelper.TABLE_MESSAGES_COLUMN_NUMBER_OF_TRIES));
        MessageStatus status = MessageStatus.fromString(cursor.getString(cursor
                .getColumnIndexOrThrow(DataBaseHelper.TABLE_MESSAGES_COLUMN_STATUS)));
        Message message = new Message(id, externalId, eventType, messageType, phoneNumber, contactName, body,
                messageTimestamp, dateUpdated, dateTried, tries, status);
        Log.d(TAG, "Message read from cursor: " + message);
        return message;
    }

    public static ContentValues getContentValues(Message message, boolean includeId)
    {
        Log.d(TAG, "Writing message into ContentValues: " + message);
        ContentValues values = new ContentValues();
        values.clear();
        if (includeId)
            values.put(DataBaseHelper.TABLE_MESSAGES_COLUMN_ID, message.getId());
        values.put(DataBaseHelper.TABLE_MESSAGES_COLUMN_EXTERNAL_ID, message.getExternalId());
        values.put(DataBaseHelper.TABLE_MESSAGES_COLUMN_EVENT_TYPE, message.getEventType().toString());
        values.put(DataBaseHelper.TABLE_MESSAGES_COLUMN_MESSAGE_TYPE, message.getMessageType().toString());
        values.put(DataBaseHelper.TABLE_MESSAGES_COLUMN_PHONE_NUMBER, message.getPhoneNumber());
        values.put(DataBaseHelper.TABLE_MESSAGES_COLUMN_CONTACT_NAME, message.getContactName());
        values.put(DataBaseHelper.TABLE_MESSAGES_COLUMN_BODY, message.getBody());
        values.put(DataBaseHelper.TABLE_MESSAGES_COLUMN_TIMESTAMP, message.getTimestamp().getTime());
        if (message.getDateUpdated() != null)
            values.put(DataBaseHelper.TABLE_MESSAGES_COLUMN_DATE_UPDATED, message.getDateUpdated().getTime());
        if (message.getDateTried() != null)
            values.put(DataBaseHelper.TABLE_MESSAGES_COLUMN_DATE_TRIED, message.getDateTried().getTime());
        values.put(DataBaseHelper.TABLE_MESSAGES_COLUMN_NUMBER_OF_TRIES, message.getTries());
        values.put(DataBaseHelper.TABLE_MESSAGES_COLUMN_STATUS, message.getStatus().toString());
        return values;
    }

    private static Date readDate(Cursor cursor, String columnName)
    {
        long dateLong = cursor.getLong(cursor.getColumnIndexOrThrow(columnName));
        return dateLong > 0 ? new Date(dateLong) : null;
    }
}
