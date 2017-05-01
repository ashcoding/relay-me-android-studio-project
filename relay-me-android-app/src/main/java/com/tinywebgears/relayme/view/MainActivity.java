package com.tinywebgears.relayme.view;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.codolutions.android.common.util.StringUtil;
import com.flurry.android.FlurryAgent;
import com.tinywebgears.relayme.R;
import com.tinywebgears.relayme.common.Constants;
import com.tinywebgears.relayme.service.BasicIntentService;
import com.tinywebgears.relayme.service.BasicIntentService.ActionStatus;
import com.tinywebgears.relayme.service.ContactsIntentService;
import com.tinywebgears.relayme.service.EmailIntentService;
import com.tinywebgears.relayme.service.GoogleIntentService;
import com.tinywebgears.relayme.service.LogStoreHelper;
import com.tinywebgears.relayme.service.LogsIntentService;
import com.tinywebgears.relayme.service.ServerSideIntentService;
import com.tinywebgears.relayme.service.TaskSchedulingService;
import com.tinywebgears.relayme.view.AboutDialogFragment.AboutDialogAction;
import com.tinywebgears.relayme.view.AboutDialogFragment.AboutDialogListener;
import com.tinywebgears.relayme.view.ContactSupportDialogFragment.ContactSupportDialogAction;
import com.tinywebgears.relayme.view.ContactSupportDialogFragment.ContactSupportDialogListener;
import com.tinywebgears.relayme.view.HelpDialogFragment.HelpDialogAction;
import com.tinywebgears.relayme.view.HelpDialogFragment.HelpDialogListener;
import com.tinywebgears.relayme.view.SmsHelpDialogFragment.SmsHelpDialogAction;
import com.tinywebgears.relayme.view.SmsHelpDialogFragment.SmsHelpDialogListener;
import com.tinywebgears.relayme.view.OpenSourceNoticeDialogFragment.OpenSourceDialogListener;

import org.androidannotations.annotations.EActivity;

@EActivity(R.layout.main)
public class MainActivity extends BaseTabActivity implements HelpDialogListener,
        SmsHelpDialogListener, ContactSupportDialogListener, AboutDialogListener,
        OpenSourceDialogListener {
    private static final String TAG = MainActivity.class.getName();
    private static final String FRAGMENT_TAG_CONFIGURATION = "Configuration";
    private static final String FRAGMENT_TAG_MESSAGES = "Messages";
    private static final String FRAGMENT_TAG_ERRORS = "Errors";

    public static final String STATE_ACTIVE_TAB = "activeTab";
    public static final String STATE_URI = "uri";

    // Transient state
    private BroadcastReceiver messageReceiver;
    private ResponseReceiver mReceiver;
    // Persistent state
    private String uri;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "MainActivity.onCreate");
        super.onCreate(savedInstanceState);

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.removeAllTabs();
        Tab tab1 = actionBar
                .newTab()
                .setText(R.string.lbl_tab_configuration)
                .setTabListener(
                        new TabListener<ConfigurationTabFragment_>(this, FRAGMENT_TAG_CONFIGURATION,
                                ConfigurationTabFragment_.class));
        actionBar.addTab(tab1);
        Tab tab2 = actionBar
                .newTab()
                .setText(R.string.lbl_tab_messages)
                .setTabListener(
                        new TabListener<MessagesTabFragment_>(this, FRAGMENT_TAG_MESSAGES, MessagesTabFragment_.class));
        actionBar.addTab(tab2);
        Tab tab3 = actionBar
                .newTab()
                .setText(R.string.lbl_tab_errors)
                .setTabListener(
                        new TabListener<ErrorsTabFragment_>(this, FRAGMENT_TAG_ERRORS, ErrorsTabFragment_.class));
        actionBar.addTab(tab3);

        startService(new Intent(this, TaskSchedulingService.class));

        IntentFilter filter = new IntentFilter();
        filter.addAction(GoogleIntentService.ACTION_RESPONSE_GET_ACCESS_TOKEN);
        filter.addAction(EmailIntentService.ACTION_RESPONSE_LOAD_MAILBOX_NAMES);
        filter.addAction(ContactsIntentService.ACTION_RESPONSE_LOAD_CONTACTS);
        filter.addAction(LogsIntentService.ACTION_RESPONSE_DETECT_CRASH);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        mReceiver = new ResponseReceiver();
        registerReceiver(mReceiver, filter);

        processNewIntent(getIntent());
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Restore the previously serialized current tab position.
        if (savedInstanceState.containsKey(STATE_ACTIVE_TAB))
            getSupportActionBar().setSelectedNavigationItem(savedInstanceState.getInt(STATE_ACTIVE_TAB));
        uri = savedInstanceState.getString(STATE_URI);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Serialize the current tab position.
        outState.putInt(STATE_ACTIVE_TAB, getSupportActionBar().getSelectedNavigationIndex());
        outState.putString(STATE_URI, uri);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "MainActivity.onDestroy");

        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "MainActivity.onStart");
        super.onStart();
        FlurryAgent.onStartSession(this, Constants.FLURRY_API_KEY);
        syncWithServer();
    }

    public void syncWithServer() {
        Intent intent = new Intent(this, ServerSideIntentService.class);
        startIntentService(ServerSideIntentService.ACTION_CODE_SYNC, intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "MainActivity.onNewIntent");
        super.onNewIntent(intent);
        processNewIntent(intent);
    }

    private void processNewIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        Log.d(TAG, "processing new intent, extras: " + extras);
        uri = (intent.getData() == null) ? null : intent.getData().toString();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "MainActivity.onStop");
        super.onStop();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "MainActivity.onResume");
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.STATUS_UPDATE_BROADCAST_ACTION);
        filter.addAction(Constants.SHOW_SMS_HELP_DIALOG_BROADCAST_ACTION);
        filter.addAction(Constants.SHOW_ALERT_BROADCAST_ACTION);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        messageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Constants.SHOW_SMS_HELP_DIALOG_BROADCAST_ACTION.equals(intent.getAction())) {
                    showSmsHelpDialog();
                } else if (Constants.SHOW_ALERT_BROADCAST_ACTION.equals(intent.getAction())) {
                    boolean isErrorDialog = intent.hasExtra(Constants.SHOW_ALERT_EXTRA_REPORT_ERROR);
                    boolean isWarningDialog = intent.hasExtra(Constants.SHOW_ALERT_EXTRA_REPORT_WARNING);
                    int textRes = Constants.EVENT_NONE;
                    int titleRes = Constants.EVENT_NONE;
                    if (isErrorDialog) {
                        textRes = intent.getIntExtra(Constants.SHOW_ALERT_EXTRA_REPORT_ERROR, Constants.EVENT_NONE);
                        titleRes = R.string.lbl_error_title;
                    } else if (isWarningDialog) {
                        textRes = intent.getIntExtra(Constants.SHOW_ALERT_EXTRA_REPORT_WARNING, Constants.EVENT_NONE);
                        titleRes = R.string.lbl_warning_title;
                    }
                    if (intent.hasExtra(Constants.SHOW_ALERT_EXTRA_DIALOG_TITLE))
                        titleRes = intent.getIntExtra(Constants.SHOW_ALERT_EXTRA_DIALOG_TITLE, Constants.EVENT_NONE);
                    String text = getString(textRes);
                    if (intent.hasExtra(Constants.SHOW_ALERT_EXTRA_DIALOG_CUSTOM_STRING))
                        text = getString(textRes,
                                intent.getStringExtra(Constants.SHOW_ALERT_EXTRA_DIALOG_CUSTOM_STRING));
                    showAlertDialog(getString(titleRes), text);
                }
            }
        };
        registerReceiver(messageReceiver, filter);

        startIntentService(LogsIntentService.ACTION_CODE_DETECT_CRASH, LogsIntentService.class);
    }

    // http://www.androiddesignpatterns.com/2013/08/fragment-transaction-commit-state-loss.html
    @Override
    public void onPostResume() {
        super.onPostResume();

        // TODO: Protect from being called twice.
        if (!StringUtil.empty(uri)) {
            try {
                LogStoreHelper.info(this, "Oauth callback received: " + uri);
                Toast.makeText(getApplicationContext(), getString(R.string.lbl_please_wait), Toast.LENGTH_LONG).show();
                LogStoreHelper.info(this, "Retrieving access token...");
                Intent gmailIntent = new Intent(this, GoogleIntentService.class);
                gmailIntent.putExtra(GoogleIntentService.PARAM_IN_VERIFIER_URI, uri);
                startIntentService(GoogleIntentService.ACTION_CODE_GET_ACCESS_TOKEN, gmailIntent);
            } catch (Exception e) {
                LogStoreHelper.warn(this, "Unable to extract token: " + e.getMessage(), e);
                showErrorDialog(R.string.lbl_oauth_error);
            } finally {
                uri = null;
            }
        } else if (!getUserData().isOpenSourceNoticeShown()) {
            showOpenSourceNotice();
        }
    }

    @Override
    public void onPause() {
        Log.d(TAG, "MainActivity.onPause");
        super.onPause();

        if (messageReceiver != null)
            unregisterReceiver(messageReceiver);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_rateus:
                openAppInMarket();
                return true;
            case R.id.action_help:
                showHelpDialog();
                return true;
            case R.id.action_about:
                showAboutDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showErrorDialog(int textRes) {
        showAlertDialog(R.string.lbl_error_title, textRes);
    }

    private void showWarningDialog(int textRes) {
        showAlertDialog(R.string.lbl_warning_title, textRes);
    }

    private void showAlertDialog(int titleRes, int textRes) {
        showAlertDialog(getString(titleRes), getString(textRes));
    }

    private void showAlertDialog(String title, String text) {
        FragmentManager fm = getSupportFragmentManager();
        AlertDialogFragment warningDialog = new AlertDialogFragment_().setTitle(title).setMessage(text);
        warningDialog.show(fm, "fragment_alert_dialog");
    }

    private void showOpenSourceNotice() {
        FragmentManager fm = getSupportFragmentManager();
        DialogFragment openSourceNoticeDialog = new OpenSourceNoticeDialogFragment_();
        openSourceNoticeDialog.show(fm, "fragment_open_source_dialog");
    }

    private void showSmsHelpDialog() {
        FragmentManager fm = getSupportFragmentManager();
        SmsHelpDialogFragment smsHelpDialog = new SmsHelpDialogFragment_();
        smsHelpDialog.setListener(this);
        smsHelpDialog.show(fm, "fragment_sms_helpdialog");
        FlurryAgent.logEvent(Constants.FLURRY_EVENT_SMS_HELP_DIALOG);
    }

    private void showAboutDialog() {
        FragmentManager fm = getSupportFragmentManager();
        AboutDialogFragment aboutDialog = new AboutDialogFragment_();
        aboutDialog.show(fm, "fragment_about_dialog");
    }

    @Override
    public void onFinishAboutDialog(AboutDialogAction action) {
        if (action == AboutDialogAction.EMAIL)
            sendEmail();
        else if (action == AboutDialogAction.WEB)
            openWebsite();
        else if (action == AboutDialogAction.HELP)
            showHelpDialog();
    }

    private void showHelpDialog() {
        FragmentManager fm = getSupportFragmentManager();
        HelpDialogFragment helpDialog = new HelpDialogFragment_();
        helpDialog.show(fm, "fragment_help_dialog");
        FlurryAgent.logEvent(Constants.FLURRY_EVENT_HELP_DIALOG);
    }

    @Override
    public void onFinishHelpDialog(HelpDialogAction action) {
        LogStoreHelper.info(this, "Help dialog finished: " + action);
        if (action == HelpDialogAction.WIKI)
            openWiki();
        else if (action == HelpDialogAction.CONTACT)
            showContactSupportDialog(false);
    }

    @Override
    public void onFinishSmsHelpDialog(SmsHelpDialogAction action) {
        LogStoreHelper.info(this, "Help dialog finished: " + action);
        if (action == SmsHelpDialogAction.WIKI)
            openWiki();
        else if (action == SmsHelpDialogAction.CONTACT)
            showContactSupportDialog(false);
    }

    private void showContactSupportDialog(boolean crashed) {
        FragmentManager fm = getSupportFragmentManager();
        ContactSupportDialogFragment contactSupportDialog = new ContactSupportDialogFragment_().setCrashed(crashed);
        contactSupportDialog.show(fm, "fragment_help_contact_support");
        FlurryAgent.logEvent(Constants.FLURRY_EVENT_CONTACT_DIALOG);
    }

    @Override
    public void onFinishContactSupportDialog(ContactSupportDialogAction action, boolean includeSettings,
                                             String category, String comments) {
        LogStoreHelper.info(this, "Contact support dialog finished: " + action + " , include settings: "
                + includeSettings + ", category: " + category + ", comment: " + comments);
        if (action == ContactSupportDialogAction.WIKI)
            openWiki();
        else if (action == ContactSupportDialogAction.SEND_LOGS)
            sendLogs(includeSettings, category, comments);
    }

    private void sendEmail() {
        Toast.makeText(getApplicationContext(), getString(R.string.lbl_please_wait), Toast.LENGTH_LONG).show();
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(getSystemStatus().getEmailUri()));
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.str_about_dialog_email_subject));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void openWebsite() {
        Toast.makeText(getApplicationContext(), getString(R.string.lbl_please_wait), Toast.LENGTH_LONG).show();
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getSystemStatus().getWebsiteUri())));
    }

    private void openWiki() {
        Toast.makeText(getApplicationContext(), getString(R.string.lbl_please_wait), Toast.LENGTH_LONG).show();
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getSystemStatus().getWikiUri())));
    }

    private void openAppInMarket() {
        Toast.makeText(getApplicationContext(), getString(R.string.lbl_please_wait), Toast.LENGTH_LONG).show();
        try {
            LogStoreHelper.info(this, "Opening market app...");
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getSystemStatus().getMarketUri())));
        } catch (ActivityNotFoundException e) {
            LogStoreHelper.info(this, "Market app not found, opening browser...");
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getSystemStatus().getMarketWebUri())));
        }
    }

    private void sendLogs(boolean includeSettings, String category, String comments) {
        Toast.makeText(getApplicationContext(), getString(R.string.lbl_please_wait), Toast.LENGTH_LONG).show();
        Intent contactsIntent = new Intent(this, LogsIntentService.class);
        contactsIntent.putExtra(LogsIntentService.PARAM_IN_INCLUDE_SETTINGS, includeSettings);
        contactsIntent.putExtra(LogsIntentService.PARAM_IN_ISSUE_CATEGORY, category);
        contactsIntent.putExtra(LogsIntentService.PARAM_IN_USER_COMMENTS, comments);
        startIntentService(LogsIntentService.ACTION_CODE_SEND_LOGS, contactsIntent);
    }

    @Override
    public void onFinishWelcomeDialog() {
        getUserData().setOpenSourceNoticeShown(true);
    }

    /**
     * Broadcast receiver to receive response of intent services.
     */
    public class ResponseReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(GoogleIntentService.ACTION_RESPONSE_GET_ACCESS_TOKEN)) {
                ActionStatus result = ActionStatus.fromString(intent
                        .getStringExtra(BasicIntentService.PARAM_OUT_RESULT));
                if (result == ActionStatus.SUCCESS)
                    sendBroadcast(new Intent(Constants.STATUS_UPDATE_BROADCAST_ACTION).putExtra(
                            Constants.STATUS_UPDATE_EXTRA_WHAT, Constants.EVENT_ACCESS_TOKEN_RECEIVED));
                else {
                    LogStoreHelper.info(this, context, "Failed to get authorization uri.");
                    sendBroadcast(new Intent(Constants.SHOW_ALERT_BROADCAST_ACTION).putExtra(
                            Constants.SHOW_ALERT_EXTRA_REPORT_ERROR, R.string.lbl_oauth_error).putExtra(
                            Constants.SHOW_ALERT_EXTRA_DIALOG_TITLE, R.string.lbl_error_title));
                }
            } else if (intent.getAction().equals(EmailIntentService.ACTION_RESPONSE_LOAD_MAILBOX_NAMES)) {
                ActionStatus result = ActionStatus.fromString(intent
                        .getStringExtra(BasicIntentService.PARAM_OUT_RESULT));
                if (result == ActionStatus.SUCCESS)
                    sendBroadcast(new Intent(Constants.STATUS_UPDATE_BROADCAST_ACTION).putExtra(
                            Constants.STATUS_UPDATE_EXTRA_WHAT, Constants.EVENT_LIST_OF_MAILBOXES_NEEDS_REFRESHING));
                else
                    LogStoreHelper.info(this, context, "Failed to load list of mailboxes.");
            } else if (intent.getAction().equals(ContactsIntentService.ACTION_RESPONSE_LOAD_CONTACTS)) {
                String result = intent.getStringExtra(BasicIntentService.PARAM_OUT_RESULT);
                if (!ActionStatus.SUCCESS.toString().equals(result))
                    LogStoreHelper.info(this, context, "Failed to load list of mailboxes.");
            } else if (intent.getAction().equals(LogsIntentService.ACTION_RESPONSE_DETECT_CRASH)) {
                showContactSupportDialog(true);
            }
        }
    }
}
