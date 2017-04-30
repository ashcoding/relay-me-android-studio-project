package com.tinywebgears.relayme.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Helper class which is responsible for managing SQLite database.
 */
// http://stackoverflow.com/questions/11686645/android-sqlite-insert-update-table-columns-to-keep-the-identifier
public class DataBaseHelper extends SQLiteOpenHelper
{
    private static final String TAG = DataBaseHelper.class.getName();

    public static final int BOOLEAN_INT_VALUE_TRUE = 1;
    public static final int BOOLEAN_INT_VALUE_FALSE = 0;

    private static final int DATABASE_VERSION = 16;
    private static final String DATABASE_NAME = "relayme.db";

    public static final String TABLE_MESSAGES = "messages";
    // Primary key
    public static final String TABLE_MESSAGES_COLUMN_ID = "_id";
    // External ID of the message
    public static final String TABLE_MESSAGES_COLUMN_EXTERNAL_ID = "externalid";
    // Event type, SMS, MMS, or missed call
    public static final String TABLE_MESSAGES_COLUMN_EVENT_TYPE = "event_type";
    // Message type, incoming, outgoing, or status
    public static final String TABLE_MESSAGES_COLUMN_MESSAGE_TYPE = "message_type";
    // Phone number associated with the message
    public static final String TABLE_MESSAGES_COLUMN_PHONE_NUMBER = "phonenumber";
    // Contact name associated with the message
    public static final String TABLE_MESSAGES_COLUMN_CONTACT_NAME = "contactname";
    // Message body
    public static final String TABLE_MESSAGES_COLUMN_BODY = "body";
    // Timestamp on the message
    public static final String TABLE_MESSAGES_COLUMN_TIMESTAMP = "timestamp";
    // Date updated
    public static final String TABLE_MESSAGES_COLUMN_DATE_UPDATED = "dateupdated";
    // Date tried
    public static final String TABLE_MESSAGES_COLUMN_DATE_TRIED = "datetried";
    // Number of tries
    public static final String TABLE_MESSAGES_COLUMN_NUMBER_OF_TRIES = "tries";
    // Status of the message
    public static final String TABLE_MESSAGES_COLUMN_STATUS = "status";
    // A handy list of all columns
    public static final String[] TABLE_MESSAGES_ALL_COLUMNS = { TABLE_MESSAGES_COLUMN_ID,
            TABLE_MESSAGES_COLUMN_EXTERNAL_ID, TABLE_MESSAGES_COLUMN_EVENT_TYPE, TABLE_MESSAGES_COLUMN_MESSAGE_TYPE,
            TABLE_MESSAGES_COLUMN_PHONE_NUMBER, TABLE_MESSAGES_COLUMN_CONTACT_NAME, TABLE_MESSAGES_COLUMN_BODY,
            TABLE_MESSAGES_COLUMN_TIMESTAMP, TABLE_MESSAGES_COLUMN_DATE_UPDATED, TABLE_MESSAGES_COLUMN_DATE_TRIED,
            TABLE_MESSAGES_COLUMN_NUMBER_OF_TRIES, TABLE_MESSAGES_COLUMN_STATUS };

    private static final String TABLE_CREATE_SCRIPT_MESSAGES = "create table " + TABLE_MESSAGES + "("
            + TABLE_MESSAGES_COLUMN_ID + " integer primary key autoincrement, " + TABLE_MESSAGES_COLUMN_EXTERNAL_ID
            + " text, " + TABLE_MESSAGES_COLUMN_EVENT_TYPE + " text, " + TABLE_MESSAGES_COLUMN_MESSAGE_TYPE + " text, "
            + TABLE_MESSAGES_COLUMN_PHONE_NUMBER + " text, " + TABLE_MESSAGES_COLUMN_CONTACT_NAME + " text, "
            + TABLE_MESSAGES_COLUMN_BODY + " text, " + TABLE_MESSAGES_COLUMN_TIMESTAMP + " long, "
            + TABLE_MESSAGES_COLUMN_DATE_UPDATED + " long, " + TABLE_MESSAGES_COLUMN_DATE_TRIED + " long, "
            + TABLE_MESSAGES_COLUMN_NUMBER_OF_TRIES + " text, " + TABLE_MESSAGES_COLUMN_STATUS + " text, UNIQUE("
            + TABLE_MESSAGES_COLUMN_EXTERNAL_ID + ", " + TABLE_MESSAGES_COLUMN_TIMESTAMP + "));";

    public static final String TABLE_LOGENTRIES = "logentries";
    // Primary key
    public static final String TABLE_LOGENTRIES_COLUMN_ID = "_id";
    // Log level
    public static final String TABLE_LOGENTRIES_COLUMN_LOGLEVEL = "loglevel";
    // Visible to user or not
    public static final String TABLE_LOGENTRIES_COLUMN_VISIBLE_TO_USER = "visibletouser";
    // Log message text
    public static final String TABLE_LOGENTRIES_COLUMN_BODY = "body";
    // Date updated
    public static final String TABLE_LOGENTRIES_COLUMN_DATE_UPDATED = "dateupdated";
    // Count of occurrences
    public static final String TABLE_LOGENTRIES_COLUMN_NUMBER_OF_OCCURRENCES = "occurrences";
    // A handy list of all columns
    public static final String[] TABLE_LOGENTRIES_ALL_COLUMNS = { TABLE_LOGENTRIES_COLUMN_ID,
            TABLE_LOGENTRIES_COLUMN_LOGLEVEL, TABLE_LOGENTRIES_COLUMN_VISIBLE_TO_USER, TABLE_LOGENTRIES_COLUMN_BODY,
            TABLE_LOGENTRIES_COLUMN_DATE_UPDATED, TABLE_LOGENTRIES_COLUMN_NUMBER_OF_OCCURRENCES };

    private static final String TABLE_CREATE_SCRIPT_LOGENTRIES = "create table " + TABLE_LOGENTRIES + "("
            + TABLE_LOGENTRIES_COLUMN_ID + " integer primary key autoincrement, " + TABLE_LOGENTRIES_COLUMN_LOGLEVEL
            + " text, " + TABLE_LOGENTRIES_COLUMN_VISIBLE_TO_USER + " integer, " + TABLE_LOGENTRIES_COLUMN_BODY
            + " varchar(2048), " + TABLE_LOGENTRIES_COLUMN_DATE_UPDATED + " long, "
            + TABLE_LOGENTRIES_COLUMN_NUMBER_OF_OCCURRENCES + " integer);";

    public DataBaseHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onOpen(SQLiteDatabase db)
    {
        super.onOpen(db);
        if (!db.isReadOnly())
        {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    private void dropDb(SQLiteDatabase db)
    {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOGENTRIES);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(TABLE_CREATE_SCRIPT_MESSAGES);
        db.execSQL(TABLE_CREATE_SCRIPT_LOGENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        if (oldVersion < 14)
        {
            Log.w(TAG, "There is no incremental upgrade before version 14. The database is recreated.");
            dropDb(db);
            onCreate(db);
        }
        else
        {
            for (int i = oldVersion + 1; i <= newVersion; i++)
                upgradeFromPreviousVersion(db, i);
        }
    }

    private void upgradeFromPreviousVersion(SQLiteDatabase db, int newVersion)
    {
        if (newVersion == 15)
        {
            Log.w(TAG, "Recreating logentries table for version 15...");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOGENTRIES);
            db.execSQL(TABLE_CREATE_SCRIPT_LOGENTRIES);
        }
        else
        {
            Log.w(TAG, "No incremental upgrade is prepared for version " + newVersion + ". The database is recreated.");
            dropDb(db);
            onCreate(db);
        }
    }
}
