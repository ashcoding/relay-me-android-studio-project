package com.tinywebgears.relayme.common;

import com.tinywebgears.relayme.BuildConfig;

public class Constants {
    // FIXME: 111: Review these before releasing. Don't ship test mode!
    private static final boolean EMAIL_TEST_MODE_FLAG = false;
    // FIXME: 111: Review these before releasing. Don't talk to the wrong server.
    private static final boolean SERVER_SIDE_FORCE_LOCAL_DEV_FLAG = false;
    private static final boolean SERVER_SIDE_FORCE_STAGING_FLAG = false;

    public static final boolean EMAIL_TEST_MODE = BuildConfig.DEBUG && EMAIL_TEST_MODE_FLAG;
    public static final boolean SERVER_SIDE_FORCE_LOCAL_DEV = BuildConfig.DEBUG && SERVER_SIDE_FORCE_LOCAL_DEV_FLAG;
    public static final boolean SERVER_SIDE_FORCE_STAGING = BuildConfig.DEBUG && SERVER_SIDE_FORCE_STAGING_FLAG;

    public static final String FLURRY_API_KEY = "T7GRW3MQSRFZYB6N8XWZ";
    public static final String FLURRY_EVENT_LOGS_DELETED = "Delete_All_Logs";
    public static final String FLURRY_EVENT_MESSAGES_DELETED = "Delete_All_Messages";
    public static final String FLURRY_EVENT_HELP_DIALOG = "Help_Dialog";
    public static final String FLURRY_EVENT_CONTACT_DIALOG = "Contact_Dialog";
    public static final String FLURRY_EVENT_SMS_HELP_DIALOG = "SMS_Help";

    public static final String STATUS_UPDATE_BROADCAST_ACTION = "com.tinywebgears.relayme.STATUS_UPDATE";
    public static final String STATUS_UPDATE_EXTRA_WHAT = "what";
    public static final int EVENT_NONE = 0;
    public static final int EVENT_CONNECTION_STATUS_CHANGED = 1;
    public static final int EVENT_STATUS_OF_MESSAGES_CHANGED = 2;
    public static final int EVENT_SERVICE_STATUS_CHANGED = 3;
    public static final int EVENT_CONFIGURATION_CHANGED = 4;
    public static final int EVENT_LIST_OF_MAILBOXES_NEEDS_REFRESHING = 5;
    public static final int EVENT_ACCESS_TOKEN_RECEIVED = 6;

    public static final int MESSAGE_SHORTENING_LENGTH = 16;

    public static final String APPLICATION_STARTED_BROADCAST_ACTION = "com.tinywebgears.relayme.APPLICATION_STARTED";
    public static final String ALARM_BROADCAST_ACTION = "com.tinywebgears.relayme.ALARM";

    public static final String SHOW_SMS_HELP_DIALOG_BROADCAST_ACTION = "com.tinywebgears.relayme.SHOW_HELP_SMS_DIALOG";

    public static final String SHOW_ALERT_BROADCAST_ACTION = "com.tinywebgears.relayme.SHOW_ALERT";
    public static final String SHOW_ALERT_EXTRA_REPORT_WARNING = "report_warning";
    public static final String SHOW_ALERT_EXTRA_REPORT_ERROR = "report_error";
    public static final String SHOW_ALERT_EXTRA_DIALOG_TITLE = "dialog_title";
    public static final String SHOW_ALERT_EXTRA_DIALOG_CUSTOM_STRING = "custom-string";

    public static final String SMS_RECEIVED_BROADCAST_ACTION = "android.provider.Telephony.SMS_RECEIVED";
    public static final String WAP_PUSH_RECEIVED_BROADCAST_ACTION = "android.provider.Telephony.WAP_PUSH_RECEIVED";

    public static final String BOOT_COMPLETED_BROADCAST_ACTION = "android.intent.action.BOOT_COMPLETED";
    public static final String CONNECTIVITY_CHANGED_BROADCAST_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";

    public static final String RELAY_ME_PACKAGE_NAME = "com.tinywebgears.relayme";
}
