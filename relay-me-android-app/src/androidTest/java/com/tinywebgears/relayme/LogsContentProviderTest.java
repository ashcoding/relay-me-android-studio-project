package com.tinywebgears.relayme;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.tinywebgears.relayme.model.LogEntry;
import com.tinywebgears.relayme.model.LogLevel;
import com.tinywebgears.relayme.service.LogStoreHelper;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class LogsContentProviderTest
{
    private static final String TAG = "LogsContentProviderTest";

    private Context context;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getTargetContext();
    }

    @Test
    public void testOldLogsGetPurged() throws Exception
    {
        LogStoreHelper.LIMIT_LOGS_TO_KEEP = 200;
        LogStoreHelper.DELETE_BATCH_SIZE = 10;

        int numberOfRecords = LogStoreHelper.getNumberOfRecords(context);
        for (int i = 0; i < LogStoreHelper.LIMIT_LOGS_TO_KEEP + 1 - numberOfRecords; i++)
            LogStoreHelper.insertLogEntry(context, new LogEntry(LogLevel.INFO, false, "Log entry no. " + i));
        numberOfRecords = LogStoreHelper.getNumberOfRecords(context);
        assertTrue("Couldn't insert enough log entries to test.",
                numberOfRecords > LogStoreHelper.LIMIT_LOGS_TO_KEEP);
        int recordsDeleted = LogStoreHelper.purgeOldRecords(context);
        assertTrue("Couldn't delete old log entries.", recordsDeleted > 0);
        numberOfRecords = LogStoreHelper.getNumberOfRecords(context);
        assertTrue("Couldn't delete enough old log entries to reduce the size of table.",
                numberOfRecords < LogStoreHelper.LIMIT_LOGS_TO_KEEP);
    }
}