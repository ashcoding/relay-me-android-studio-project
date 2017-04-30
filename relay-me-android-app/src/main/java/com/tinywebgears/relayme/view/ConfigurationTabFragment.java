package com.tinywebgears.relayme.view;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.codolutions.android.common.exception.OperationFailedException;
import com.codolutions.android.common.util.StringUtil;
import com.tinywebgears.relayme.R;
import com.tinywebgears.relayme.common.Constants;
import com.tinywebgears.relayme.common.DialogParams;
import com.tinywebgears.relayme.common.DialogParams.StringDialogExtra;
import com.tinywebgears.relayme.email.ImapEmailReceiver;
import com.tinywebgears.relayme.email.ServerCredentials;
import com.tinywebgears.relayme.model.GatewayType;
import com.tinywebgears.relayme.model.ImapServer;
import com.tinywebgears.relayme.model.PollingFactor;
import com.tinywebgears.relayme.model.ServerSecurity;
import com.tinywebgears.relayme.model.ServiceStatus;
import com.tinywebgears.relayme.model.SmtpServer;
import com.tinywebgears.relayme.service.BasicIntentService;
import com.tinywebgears.relayme.service.BasicIntentService.ActionStatus;
import com.tinywebgears.relayme.service.EmailIntentService;
import com.tinywebgears.relayme.service.LogStoreHelper;
import com.tinywebgears.relayme.service.TaskSchedulingService;
import com.tinywebgears.relayme.view.ConfirmClearOauthDialogFragment.ConfirmClearOauthDialogListener;
import com.tinywebgears.relayme.view.ConfirmationDialogFragment.ConfirmationDialogListener;
import com.tinywebgears.relayme.view.OauthDialogFragment.OauthDialogAction;
import com.tinywebgears.relayme.view.OauthDialogFragment.OauthDialogListener;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// FIXME: Add a button to test replies and check for SMS messages

@EFragment
public class ConfigurationTabFragment extends TabFragment implements ConfirmClearOauthDialogListener,
        OauthDialogListener, ConfirmationDialogListener
{
    private static final String TAG = ConfigurationTabFragment.class.getName();
    private static final String STATE_EDITING_SMTP_SERVER = "state_editing_smtp_server";
    private static final String STATE_SMTP_SERVER_STATUS_MESSAGE = "state_smtp_server_status_message";
    private static final String STATE_SMTP_SERVER_SAVE_ERROR = "state_smtp_server_save_error";
    private static final String STATE_SMTP_SERVER_EMAIL = "state_smtp_server_email";
    private static final String STATE_SMTP_SERVER_IP = "state_smtp_server_ip";
    private static final String STATE_SMTP_SERVER_PORT = "state_smtp_server_port";
    private static final String STATE_SMTP_SERVER_USERNAME = "state_smtp_server_username";
    private static final String STATE_SMTP_SERVER_PASSWORD = "state_smtp_server_password";
    private static final String STATE_SMTP_SERVER_SECURITY = "state_smtp_server_security";
    private static final String STATE_EDITING_IMAP_SERVER = "state_editing_imap_server";
    private static final String STATE_IMAP_SERVER_STATUS_MESSAGE = "state_imap_server_status_message";
    private static final String STATE_IMAP_SERVER_SAVE_ERROR = "state_imap_server_save_error";
    private static final String STATE_IMAP_SERVER_IP = "state_imap_server_ip";
    private static final String STATE_IMAP_SERVER_PORT = "state_imap_server_port";
    private static final String STATE_IMAP_SERVER_USERNAME = "state_imap_server_username";
    private static final String STATE_IMAP_SERVER_PASSWORD = "state_imap_server_password";
    private static final String STATE_IMAP_SERVER_SECURITY = "state_imap_server_security";
    private static final String STATE_EDITING_TARGET_EMAIL = "state_editing_target_email";
    private static final String STATE_TARGET_EMAIL = "state_target_email";
    private static final String STATE_EDITING_TARGET_CC_EMAIL = "state_editing_target_cc_email";
    private static final String STATE_TARGET_CC_EMAIL = "state_target_cc_email";
    private static final String STATE_REPLY_SOURCE = "state_reply_source";
    private static final String STATE_EDITING_REPLY_SOURCE = "state_editing_reply_source";
    private static final String STATE_EDITING_EMAIL_SUBJECT_PREFIX = "state_editing_email_subject_prefix";
    private static final String STATE_EMAIL_SUBJECT_PREFIX = "state_email_subject_prefix";
    private static final String STATE_EDITING_SMS_CODE = "state_editing_sms_code";
    private static final String STATE_SMS_CODE = "state_sms_code";
    private static final String STATE_EDITING_SCHEDULE = "state_editing_schedule";
    private static final String STATE_SCHEDULE_START = "state_schedule_start";
    private static final String STATE_SCHEDULE_END = "state_schedule_end";
    public static final String DIALOG_ID_CONFIRM_DELETE_TARGET_EMAIL = "confirm-delete-target-email";
    public static final String DIALOG_ID_CONFIRM_DELETE_CC_EMAIL = "confirm-delete-cc-email";
    public static final String DIALOG_ID_CONFIRM_DELETE_SMTP_SERVER = "confirm-delete-smtp-server";
    public static final String DIALOG_ID_CONFIRM_DELETE_IMAP_SERVER = "confirm-delete-imap-server";

    // Transient state

    private BroadcastReceiver messageReceiver;

    private List<String> spinnerItemsMailboxNames = new ArrayList<String>();

    private List<GatewayType> spinnerItemsGatewayTypes = new ArrayList<GatewayType>();

    private List<ServerSecurity> spinnerItemsServerSecurities = new ArrayList<ServerSecurity>();

    private List<PollingFactor> spinnerItemPollingFactors = new ArrayList<PollingFactor>();

    private List<ServiceStatus> spinnerItemServiceStatuses = new ArrayList<ServiceStatus>();

    // Targets

    @ViewById(R.id.targetslayout)
    View targetsLayout;

    @ViewById(R.id.targetsoptionslayout)
    View targetsOptionsLayout;

    @ViewById(R.id.targetemaillayout)
    View targetEmailLayout;

    @ViewById(R.id.edittargetemaillayout)
    View editTargetEmailLayout;

    @ViewById(R.id.ccemaillayout)
    View ccEmailLayout;

    @ViewById(R.id.editccemaillayout)
    View editCcEmailLayout;

    @ViewById(R.id.addccemailinstructions)
    View addCcEmailInstructionsLayout;

    @ViewById(R.id.addccemail)
    View addCcEmailButton;

    @ViewById(R.id.setuptargetinstructionslayout)
    View targetsInstructionsLayout;

    @ViewById(R.id.targetemailaddress)
    TextView targetEmailAddress;

    @ViewById(R.id.edittargetemailtextbox)
    EditText targetEmailEditText;

    @ViewById(R.id.ccemailaddress)
    TextView ccEmailAddress;

    @ViewById(R.id.editccemailtextbox)
    EditText ccEmailEditText;

    // Email Gateways

    @ViewById(R.id.emailgatewaydescriptiontext)
    TextView emailGatewayDescription;

    @ViewById(R.id.gatewaytypespinner)
    Spinner gatewayTypeSpinner;

    @ViewById(R.id.gatewaysettingslayout)
    View gatewaySettingsLayout;

    @ViewById(R.id.gatewayoptionslayout)
    View gatewayOptionsLayout;

    @ViewById(R.id.configurationwarninglayout)
    View configurationWarningLayout;

    @ViewById(R.id.configurationwarningtext)
    TextView configurationWarningText;

    // GMail

    @ViewById(R.id.gmailaccountlayout)
    View gmailAccountLayout;

    @ViewById(R.id.gmailaccountaddress)
    TextView gmailAccountAddress;

    @ViewById(R.id.setupgmailbutton)
    Button setUpGmailButton;

    @ViewById(R.id.cleargmailoauthbutton)
    View clearGmailOauthButton;

    // SMTP Server

    @ViewById(R.id.smtplayout)
    View smtpAccountLayout;

    @ViewById(R.id.smtpserverviewlayout)
    View smtpServerViewLayout;

    @ViewById(R.id.smtpserverstatuslayout)
    View smtpServerStatusLayout;

    @ViewById(R.id.smtpserverstatusmessage)
    TextView smtpServerStatusMessage;

    @ViewById(R.id.smtpserverstatusactions)
    View smtpServerStatusActions;

    @ViewById(R.id.smtpserversummary)
    TextView smtpServerSummary;

    @ViewById(R.id.smtpservereditlayout)
    View smtpServerEditLayout;

    @ViewById(R.id.smtpserveremail)
    EditText smtpServerEmailEditText;

    @ViewById(R.id.smtpserverip)
    EditText smtpServerIpEditText;

    @ViewById(R.id.smtpserverport)
    EditText smtpServerPortEditText;

    @ViewById(R.id.smtpserverusernamelayout)
    View smtpServerUsernameLayout;

    @ViewById(R.id.smtpserverusername)
    EditText smtpServerUsernameEditText;

    @ViewById(R.id.smtpserverpasswordlayout)
    View smtpServerPasswordLayout;

    @ViewById(R.id.smtpserverpassword)
    EditText smtpServerPasswordEditText;

    @ViewById(R.id.smtpserversecurity)
    Spinner smtpServerSecuritySpinner;

    @ViewById(R.id.smtpserverviewbuttons)
    View smtpServerViewButtons;

    @ViewById(R.id.smtpservereditbuttons)
    View smtpServerEditButtons;

    @ViewById(R.id.editsmtpserverbutton)
    View editSmtpServerButton;

    @ViewById(R.id.deletesmtpserverbutton)
    View deleteSmtpServerButton;

    @ViewById(R.id.canceleditsmtpserverbutton)
    View cancelEditSmtpServerButton;

    @ViewById(R.id.savesmtpserverbutton)
    View saveSmtpServerButton;

    @ViewById(R.id.smtpserversaveanyway)
    View forceSaveSmtpServerButton;

    @ViewById(R.id.setupsmtpserver)
    View setupSmtpServerButton;

    // IMAP Server

    @ViewById(R.id.imaplayout)
    View imapAccountLayout;

    @ViewById(R.id.imapserverviewlayout)
    View imapServerViewLayout;

    @ViewById(R.id.imapserverstatuslayout)
    View imapServerStatusLayout;

    @ViewById(R.id.imapserverstatusmessage)
    TextView imapServerStatusMessage;

    @ViewById(R.id.imapserverstatusactions)
    View imapServerStatusActions;

    @ViewById(R.id.imapserversummary)
    TextView imapServerSummary;

    @ViewById(R.id.imapservereditlayout)
    View imapServerEditLayout;

    @ViewById(R.id.imapserverip)
    EditText imapServerIpEditText;

    @ViewById(R.id.imapserverport)
    EditText imapServerPortEditText;

    @ViewById(R.id.imapserverusernamelayout)
    View imapServerUsernameLayout;

    @ViewById(R.id.imapserverusername)
    EditText imapServerUsernameEditText;

    @ViewById(R.id.imapserverpasswordlayout)
    View imapServerPasswordLayout;

    @ViewById(R.id.imapserverpassword)
    EditText imapServerPasswordEditText;

    @ViewById(R.id.imapserversecurity)
    Spinner imapServerSecuritySpinner;

    @ViewById(R.id.imapserverviewbuttons)
    View imapServerViewButtons;

    @ViewById(R.id.imapservereditbuttons)
    View imapServerEditButtons;

    @ViewById(R.id.editimapserverbutton)
    View editImapServerButton;

    @ViewById(R.id.deleteimapserverbutton)
    View deleteImapServerButton;

    @ViewById(R.id.canceleditimapserverbutton)
    View cancelEditImapServerButton;

    @ViewById(R.id.saveimapserverbutton)
    View saveImapServerButton;

    @ViewById(R.id.imapserversaveanyway)
    View forceSaveImapServerButton;

    @ViewById(R.id.setupimapserver)
    View setupImapServerButton;

    // Secondary configuration

    @ViewById(R.id.secondaryconfigurationlayout)
    View secondaryConfigurationLayout;

    // Forwarding configuration

    @ViewById(R.id.forwardinglayout)
    View forwardingLayout;

    @ViewById(R.id.missedcallnotificationcheckbox)
    CheckBox missedCallNotificationCheckBox;

    @ViewById(R.id.emailsubjectprefixvaluelayout)
    View emailSubjectPrefixValueLayout;

    @ViewById(R.id.emailsubjectprefixvalue)
    TextView emailSubjectPrefixValue;

    @ViewById(R.id.editemailsubjectprefixlayout)
    View editEmailSubjectPrefixLayout;

    @ViewById(R.id.editemailsubjectprefixtextbox)
    EditText emailSubjectPrefixEditText;

    // Replying configuration

    @ViewById(R.id.replyinglayout)
    View replyingLayout;

    @ViewById(R.id.replyingswitchcheckbox)
    CheckBox replyingSwitch;

    @ViewById(R.id.replyinginstructions)
    View replyingInstructions;

    @ViewById(R.id.replyingoptionslayout)
    View replyingOptionsLayout;

    @ViewById(R.id.mailboxspinner)
    Spinner mailboxesSpinner;

    @ViewById(R.id.pollingfactorspinner)
    Spinner pollingFactorSpinner;

    @ViewById(R.id.replyingstatusswitchcheckbox)
    CheckBox replyingStatusSwitch;

    @ViewById(R.id.replyingfromdifferentemailswitchcheckbox)
    CheckBox replyingFromDifferentEmailSwitch;

    @ViewById(R.id.replysourcelayout)
    View replySourceLayout;

    @ViewById(R.id.editreplysourcelayout)
    View editReplySourceLayout;

    @ViewById(R.id.editreplysourcetextbox)
    EditText replySourceEditText;

    @ViewById(R.id.replysourcevalue)
    TextView replyingSourceEmailAddress;

    // Service configuration

    @ViewById(R.id.servicecontrollayout)
    View serviceControlLayout;

    @ViewById(R.id.servicecontrolspinner)
    Spinner serviceControlSpinner;

    @ViewById(R.id.smstriggerlayout)
    View smsTriggerLayout;

    @ViewById(R.id.smstriggerheadinglayout)
    View smsTriggerHeadingLayout;

    @ViewById(R.id.editsmstriggercodelayout)
    View editSmsTriggerCodeLayout;

    @ViewById(R.id.setsmstriggercodehelplabel)
    TextView smsTriggerCodeHelpLabel;

    @ViewById(R.id.editsmstriggercodetextbox)
    EditText smsTriggerCodeEditText;

    @ViewById(R.id.setschedulehelplabel)
    TextView setScheduleHelpLabel;

    @ViewById(R.id.currentschedulelabel)
    TextView currentScheduleLabel;

    @ViewById(R.id.serviceschedulelayout)
    View serviceScheduleLayout;

    @ViewById(R.id.servicescheduleheadinglayout)
    View serviceScheduleHeadingLayout;

    @ViewById(R.id.editschedulelayout)
    View editScheduleLayout;

    @ViewById(R.id.editschedulestarttimetextbox)
    EditText scheduleStartTimeEditText;

    @ViewById(R.id.editscheduleendtimetextbox)
    EditText scheduleEndTimeEditText;

    // Application status

    @ViewById(R.id.applicationstatuslayout)
    ViewGroup applicationStatusLayout;

    @ViewById(R.id.connectionstatuslayout)
    ViewGroup connectionStatusLayout;

    @ViewById(R.id.statusseparatorline)
    ImageView statusSeparatorImageView;

    @ViewById(R.id.messagesstatuslayout)
    ViewGroup messagesStatusLayout;

    @ViewById(R.id.serviceoffwarningtext)
    View serviceOffWarningText;

    @ViewById(R.id.replyingdisabledwarningtext)
    View replyingDisabledWarningText;

    @ViewById(R.id.pendingmessageswarningtext)
    View pendingMessagesWarningText;

    @ViewById(R.id.smschecklayout)
    View smsCheckLayout;

    @ViewById(R.id.smscheckbutton)
    Button smsCheckButton;

    // Persistent state

    private boolean editingTargetEmail = false;

    private String targetEmailAddressDraft = null;

    private boolean editingCcEmail = false;

    private String ccEmailAddressDraft = null;

    private boolean editingSmtpServer = false;

    private String smtpServerStatusText = null;

    private boolean smtpServerSaveError = false;

    private String smtpServerEmailDraft = null;

    private String smtpServerIpDraft = null;

    private String smtpServerPortDraft = null;

    private String smtpServerUsernameDraft = null;

    private String smtpServerPasswordDraft = null;

    private ServerSecurity smtpServerSecurityDraft = null;

    private boolean editingImapServer = false;

    private String imapServerStatusText = null;

    private boolean imapServerSaveError = false;

    private String imapServerIpDraft = null;

    private String imapServerPortDraft = null;

    private String imapServerUsernameDraft = null;

    private String imapServerPasswordDraft = null;

    private ServerSecurity imapServerSecurityDraft = null;

    private boolean editingReplySource = false;

    private String replySourceDraft = null;

    private boolean editingEmailSubjectPrefix = false;

    private String emailSubjectPrefixDraft = null;

    private boolean editingSmsTriggerCode = false;

    private String smsTriggerCodeDraft = null;

    private boolean editingSchedule = false;

    private String scheduleStartDraft = null;

    private String scheduleEndDraft = null;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        LogStoreHelper.info(this, null, "ConfigurationTabFragment.onCreate");
        super.onCreate(savedInstanceState);

        // We need to store the state, then assign them to fields when they are available after injection.
        if (savedInstanceState != null)
        {
            editingSmtpServer = savedInstanceState.getBoolean(STATE_EDITING_SMTP_SERVER);
            smtpServerStatusText = savedInstanceState.getString(STATE_SMTP_SERVER_STATUS_MESSAGE);
            smtpServerSaveError = savedInstanceState.getBoolean(STATE_SMTP_SERVER_SAVE_ERROR);
            smtpServerEmailDraft = savedInstanceState.getString(STATE_SMTP_SERVER_EMAIL);
            smtpServerIpDraft = savedInstanceState.getString(STATE_SMTP_SERVER_IP);
            smtpServerPortDraft = savedInstanceState.getString(STATE_SMTP_SERVER_PORT);
            smtpServerUsernameDraft = savedInstanceState.getString(STATE_SMTP_SERVER_USERNAME);
            smtpServerPasswordDraft = savedInstanceState.getString(STATE_SMTP_SERVER_PASSWORD);
            smtpServerSecurityDraft = ServerSecurity.fromString(savedInstanceState
                    .getString(STATE_SMTP_SERVER_SECURITY));
            editingImapServer = savedInstanceState.getBoolean(STATE_EDITING_IMAP_SERVER);
            imapServerStatusText = savedInstanceState.getString(STATE_IMAP_SERVER_STATUS_MESSAGE);
            imapServerSaveError = savedInstanceState.getBoolean(STATE_IMAP_SERVER_SAVE_ERROR);
            imapServerIpDraft = savedInstanceState.getString(STATE_IMAP_SERVER_IP);
            imapServerPortDraft = savedInstanceState.getString(STATE_IMAP_SERVER_PORT);
            imapServerUsernameDraft = savedInstanceState.getString(STATE_IMAP_SERVER_USERNAME);
            imapServerPasswordDraft = savedInstanceState.getString(STATE_IMAP_SERVER_PASSWORD);
            imapServerSecurityDraft = ServerSecurity.fromString(savedInstanceState
                    .getString(STATE_IMAP_SERVER_SECURITY));
            editingTargetEmail = savedInstanceState.getBoolean(STATE_EDITING_TARGET_EMAIL);
            targetEmailAddressDraft = savedInstanceState.getString(STATE_TARGET_EMAIL);
            editingCcEmail = savedInstanceState.getBoolean(STATE_EDITING_TARGET_CC_EMAIL);
            ccEmailAddressDraft = savedInstanceState.getString(STATE_TARGET_CC_EMAIL);
            editingReplySource = savedInstanceState.getBoolean(STATE_EDITING_REPLY_SOURCE);
            replySourceDraft = savedInstanceState.getString(STATE_REPLY_SOURCE);
            editingEmailSubjectPrefix = savedInstanceState.getBoolean(STATE_EDITING_EMAIL_SUBJECT_PREFIX);
            emailSubjectPrefixDraft = savedInstanceState.getString(STATE_EMAIL_SUBJECT_PREFIX);
            editingSmsTriggerCode = savedInstanceState.getBoolean(STATE_EDITING_SMS_CODE);
            smsTriggerCodeDraft = savedInstanceState.getString(STATE_SMS_CODE);
            editingSchedule = savedInstanceState.getBoolean(STATE_EDITING_SCHEDULE);
            scheduleStartDraft = savedInstanceState.getString(STATE_SCHEDULE_START);
            scheduleEndDraft = savedInstanceState.getString(STATE_SCHEDULE_END);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.configuration, container, false);
        return view;
    }

    @Override
    public void onAttach(Activity a)
    {
        super.onAttach(a);
        LogStoreHelper.info(this, myActivity, "ConfigurationTabFragment.onAttach");
    }

    @Override
    public void onDetach()
    {
        Log.d(TAG, "ConfigurationTabFragment.onDetach");
        super.onDetach();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putBoolean(STATE_EDITING_SMTP_SERVER, editingSmtpServer);
        if (editingSmtpServer)
        {
            savedInstanceState.putString(STATE_SMTP_SERVER_STATUS_MESSAGE, smtpServerStatusText);
            savedInstanceState.putBoolean(STATE_SMTP_SERVER_SAVE_ERROR, smtpServerSaveError);
            savedInstanceState.putString(STATE_SMTP_SERVER_EMAIL, smtpServerEmailEditText.getText().toString().trim());
            savedInstanceState.putString(STATE_SMTP_SERVER_IP, smtpServerIpEditText.getText().toString().trim());
            savedInstanceState.putString(STATE_SMTP_SERVER_PORT, smtpServerPortEditText.getText().toString().trim());
            savedInstanceState.putString(STATE_SMTP_SERVER_USERNAME, smtpServerUsernameEditText.getText().toString()
                    .trim());
            savedInstanceState.putString(STATE_SMTP_SERVER_PASSWORD, smtpServerPasswordEditText.getText().toString()
                    .trim());
            if (smtpServerSecuritySpinner.getSelectedItem() != null)
                savedInstanceState.putString(STATE_SMTP_SERVER_SECURITY,
                        spinnerItemsServerSecurities.get(smtpServerSecuritySpinner.getSelectedItemPosition())
                                .toString());
        }
        savedInstanceState.putBoolean(STATE_EDITING_IMAP_SERVER, editingImapServer);
        if (editingImapServer)
        {
            savedInstanceState.putString(STATE_IMAP_SERVER_STATUS_MESSAGE, imapServerStatusText);
            savedInstanceState.putBoolean(STATE_IMAP_SERVER_SAVE_ERROR, imapServerSaveError);
            savedInstanceState.putString(STATE_IMAP_SERVER_IP, imapServerIpEditText.getText().toString().trim());
            savedInstanceState.putString(STATE_IMAP_SERVER_PORT, imapServerPortEditText.getText().toString().trim());
            savedInstanceState.putString(STATE_IMAP_SERVER_USERNAME, imapServerUsernameEditText.getText().toString()
                    .trim());
            savedInstanceState.putString(STATE_IMAP_SERVER_PASSWORD, imapServerPasswordEditText.getText().toString()
                    .trim());
            if (imapServerSecuritySpinner.getSelectedItem() != null)
                savedInstanceState.putString(STATE_IMAP_SERVER_SECURITY,
                        spinnerItemsServerSecurities.get(imapServerSecuritySpinner.getSelectedItemPosition())
                                .toString());
        }
        savedInstanceState.putBoolean(STATE_EDITING_TARGET_EMAIL, editingTargetEmail);
        if (editingTargetEmail)
            savedInstanceState.putString(STATE_TARGET_EMAIL, targetEmailEditText.getText().toString().trim());
        savedInstanceState.putBoolean(STATE_EDITING_TARGET_CC_EMAIL, editingCcEmail);
        if (editingCcEmail)
            savedInstanceState.putString(STATE_TARGET_CC_EMAIL, ccEmailEditText.getText().toString().trim());
        savedInstanceState.putBoolean(STATE_EDITING_REPLY_SOURCE, editingReplySource);
        if (editingReplySource)
            savedInstanceState.putString(STATE_REPLY_SOURCE, replySourceEditText.getText().toString().trim());
        savedInstanceState.putBoolean(STATE_EDITING_EMAIL_SUBJECT_PREFIX, editingEmailSubjectPrefix);
        if (editingEmailSubjectPrefix)
            savedInstanceState.putString(STATE_EMAIL_SUBJECT_PREFIX, emailSubjectPrefixEditText.getText().toString()
                    .trim());
        savedInstanceState.putBoolean(STATE_EDITING_SMS_CODE, editingSmsTriggerCode);
        if (editingSmsTriggerCode)
            savedInstanceState.putString(STATE_SMS_CODE, smsTriggerCodeEditText.getText().toString().trim());
        savedInstanceState.putBoolean(STATE_EDITING_SCHEDULE, editingSchedule);
        if (editingSchedule)
        {
            savedInstanceState.putString(STATE_SCHEDULE_START, scheduleStartTimeEditText.getText().toString());
            savedInstanceState.putString(STATE_SCHEDULE_END, scheduleEndTimeEditText.getText().toString());
        }
    }

    @Override
    public void onStart()
    {
        Log.d(TAG, "ConfigurationTabFragment.onStart");
        super.onStart();
    }

    @Override
    public void onResume()
    {
        Log.d(TAG, "ConfigurationTabFragment.onResume");
        super.onResume();

        setUpView();
        refreshGatewayTypes();
        refreshSmtpServerSecurity();
        refreshImapServerSecurity();
        refreshMailboxNames();
        refreshPollingFactor();
        refreshServiceStatus();
        refreshUI();
        restoreFieldValues();

        if (getUserData().getMessagingConfiguration().isGmailLinkSetUp())
            reloadMailboxNames();

        messageReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                if (Constants.STATUS_UPDATE_BROADCAST_ACTION.equals(intent.getAction()))
                {
                    int what = intent.getIntExtra(Constants.STATUS_UPDATE_EXTRA_WHAT, Constants.EVENT_NONE);
                    if (what == Constants.EVENT_CONNECTION_STATUS_CHANGED
                            || what == Constants.EVENT_STATUS_OF_MESSAGES_CHANGED)
                    {
                        try
                        {
                            refreshStatus();
                        }
                        catch (Exception e)
                        {
                            LogStoreHelper.warn(this, myActivity, "Error refreshing system status: " + e, e);
                        }
                    }
                    else if (what == Constants.EVENT_SERVICE_STATUS_CHANGED)
                    {
                        refreshServiceStatus();
                    }
                    else if (what == Constants.EVENT_CONFIGURATION_CHANGED)
                    {
                        refreshUI();
                    }
                    else if (what == Constants.EVENT_LIST_OF_MAILBOXES_NEEDS_REFRESHING)
                    {
                        refreshMailboxNames();
                    }
                    else if (what == Constants.EVENT_ACCESS_TOKEN_RECEIVED)
                    {
                        processAccessToken();
                    }
                }
                else if (EmailIntentService.ACTION_RESPONSE_CHECK_SMTP_SERVER.equals(intent.getAction()))
                {
                    ActionStatus status = ActionStatus.fromString(intent
                            .getStringExtra(BasicIntentService.PARAM_OUT_RESULT));
                    if (status == ActionStatus.SUCCESS)
                        finishSavingSmtpServer(true);
                    else
                    {
                        String errorMessage = intent.getStringExtra(BasicIntentService.PARAM_OUT_ERROR_MESSAGE);
                        onSmtpServerVerificationFailed(errorMessage);
                    }
                }
                else if (EmailIntentService.ACTION_RESPONSE_CHECK_IMAP_SERVER.equals(intent.getAction()))
                {
                    ActionStatus status = ActionStatus.fromString(intent
                            .getStringExtra(BasicIntentService.PARAM_OUT_RESULT));
                    if (status == ActionStatus.SUCCESS)
                        finishSavingImapServer(true);
                    else
                    {
                        String errorMessage = intent.getStringExtra(BasicIntentService.PARAM_OUT_ERROR_MESSAGE);
                        onImapServerVerificationFailed(errorMessage);
                    }
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.STATUS_UPDATE_BROADCAST_ACTION);
        intentFilter.addAction(EmailIntentService.ACTION_RESPONSE_CHECK_SMTP_SERVER);
        intentFilter.addAction(EmailIntentService.ACTION_RESPONSE_CHECK_IMAP_SERVER);
        applicationContext.registerReceiver(messageReceiver, intentFilter);
    }

    @Override
    public void onPause()
    {
        if (messageReceiver != null)
            applicationContext.unregisterReceiver(messageReceiver);

        super.onPause();
    }

    private void setUpView()
    {
        spinnerItemsMailboxNames = new ArrayList<String>(getUserData().getMessagingConfiguration().getAllMailboxes());

        spinnerItemsGatewayTypes = GatewayType.getAllEnabled();

        spinnerItemsServerSecurities = ServerSecurity.getAll();

        spinnerItemPollingFactors = PollingFactor.getAll();

        spinnerItemServiceStatuses = ServiceStatus.getAll();

        targetEmailEditText.setOnKeyListener(new View.OnKeyListener()
        {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if (keyCode == KeyEvent.KEYCODE_ENTER)
                {
                    onSaveTargetEmailButtonClicked();
                    return true;
                }
                else if (keyCode == KeyEvent.KEYCODE_ESCAPE)
                {
                    onCancelEditTargetEmailButtonClicked();
                    return true;
                }
                return false;
            }
        });

        ccEmailEditText.setOnKeyListener(new View.OnKeyListener()
        {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if (keyCode == KeyEvent.KEYCODE_ENTER)
                {
                    onSaveCcEmailButtonClicked();
                    return true;
                }
                else if (keyCode == KeyEvent.KEYCODE_ESCAPE)
                {
                    onCancelEditCcEmailButtonClicked();
                    return true;
                }
                return false;
            }
        });

        replySourceEditText.setOnKeyListener(new View.OnKeyListener()
        {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if (keyCode == KeyEvent.KEYCODE_ENTER)
                {
                    onSaveReplySourceButtonClicked();
                    return true;
                }
                else if (keyCode == KeyEvent.KEYCODE_ESCAPE)
                {
                    onCancelEditReplySourceButtonClicked();
                    return true;
                }
                return false;
            }
        });

        emailSubjectPrefixEditText.setOnKeyListener(new View.OnKeyListener()
        {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if (keyCode == KeyEvent.KEYCODE_ENTER)
                {
                    onSaveEmailSubjectPrefixButtonClicked();
                    return true;
                }
                else if (keyCode == KeyEvent.KEYCODE_ESCAPE)
                {
                    onCancelEditEmailSubjectPrefixButtonClicked();
                    return true;
                }
                return false;
            }
        });

        smsTriggerCodeEditText.setOnKeyListener(new View.OnKeyListener()
        {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if (keyCode == KeyEvent.KEYCODE_ENTER)
                {
                    onSaveSmsCodeButtonClicked();
                    return true;
                }
                else if (keyCode == KeyEvent.KEYCODE_ESCAPE)
                {
                    onCancelEditSmsCodeButtonClicked();
                    return true;
                }
                return false;
            }
        });

        gatewayTypeSpinner.setOnItemSelectedListener(new OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id)
            {
                Log.d(TAG, "Forwarding method selector: Item selected: " + position + " view: " + v);
                GatewayType gatewayType = spinnerItemsGatewayTypes.get(position);
                Log.d(TAG, "Forwarding method is now: " + gatewayType);
                getUserData().getMessagingConfiguration().setGatewayType(gatewayType);
                refreshUI();
                reloadMailboxNames();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
                Log.d(TAG, "Forwarding method: Nothing selected.");
            }
        });

        mailboxesSpinner.setOnItemSelectedListener(new OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id)
            {
                Log.d(TAG, "Mailbox selector: Item selected: " + position + " view: " + v);
                String oldMailboxName = getUserData().getMessagingConfiguration().getReplyMailboxName();
                String mailboxName = spinnerItemsMailboxNames.get(position);
                getUserData().getMessagingConfiguration().setReplyMailboxName(mailboxName);
                if (!StringUtil.stringsEqual(oldMailboxName, mailboxName))
                    resetLastEmailCheckDate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
                Log.d(TAG, "Mailbox selector: Nothing selected.");
            }
        });

        pollingFactorSpinner.setOnItemSelectedListener(new OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id)
            {
                Log.d(TAG, "Polling factor selector: Item selected: " + position + " view: " + v);
                PollingFactor pollingFactor = spinnerItemPollingFactors.get(position);
                Log.d(TAG, "Polling factor is now: " + pollingFactor);
                PollingFactor oldPollingFactor = getUserData().getMessagingConfiguration().getPollingFactor();
                getUserData().getMessagingConfiguration().setPollingFactor(pollingFactor);
                refreshUI();
                if (oldPollingFactor != pollingFactor)
                    restartScheduledTasks();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
                Log.d(TAG, "Polling factor: Nothing selected.");
            }
        });

        serviceControlSpinner.setOnItemSelectedListener(new OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id)
            {
                Log.d(TAG, "Service status selector: Item selected: " + position + " view: " + v);
                ServiceStatus serviceStatus = spinnerItemServiceStatuses.get(position);
                Log.d(TAG, "Service status is now: " + serviceStatus);
                getUserData().getServiceConfiguration().setServiceStatus(serviceStatus);
                refreshUI();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
                Log.d(TAG, "Service status: Nothing selected.");
            }
        });

        smtpServerSecuritySpinner.setOnItemSelectedListener(new OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id)
            {
                Log.d(TAG, "Service status selector: Item selected: " + position + " view: " + v);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
                Log.d(TAG, "Service status: Nothing selected.");
            }
        });

        imapServerSecuritySpinner.setOnItemSelectedListener(new OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id)
            {
                Log.d(TAG, "Service status selector: Item selected: " + position + " view: " + v);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
                Log.d(TAG, "Service status: Nothing selected.");
            }
        });
    }

    protected void refreshUI()
    {
        Log.d(TAG, "ConfigurationTabFragment.refreshUI");

        refreshFields();
        refreshStatus();
    }

    private void refreshFields()
    {
        final boolean missedCallNotificationEnabled = getUserData().getServiceConfiguration()
                .isMissedCallNotificationEnabled();
        final boolean replyingSwitchOn = getUserData().getServiceConfiguration().isReplyingEnabled();
        final boolean replyingStatusSwitchOn = getUserData().getServiceConfiguration().isReplyingStatusReportEnabled();
        final boolean replyingFromDifferentEmailOn = getUserData().getServiceConfiguration()
                .isReplyingFromDifferentEmail();
        final String replySourceEmailAddress = getUserData().getServiceConfiguration().getReplySourceEmailAddress();
        final String targetEmail = getUserData().getMessagingConfiguration().getTargetEmailAddress();
        final String oauthEmail = getUserData().getMessagingConfiguration().getOauthEmailAddress();
        final boolean gmailLinkSetUp = getUserData().getMessagingConfiguration().isGmailLinkSetUp();
        final boolean smtpSetUp = getUserData().getMessagingConfiguration().isSmtpServerSetUp();
        final boolean imapSetUp = getUserData().getMessagingConfiguration().isImapServerSetUp();
        final boolean targetSetup = StringUtil.nonEmpty(getUserData().getMessagingConfiguration()
                .getTargetEmailAddress());
        final boolean ccSetup = StringUtil.nonEmpty(getUserData().getMessagingConfiguration().getCcEmailAddress());
        final boolean anySmsReceived = getUserData().isAnySmsReceived();
        final boolean targetSetUp = getUserData().getMessagingConfiguration().isDestinationSetUp();
        final boolean emailGatewaySetUp = getUserData().getMessagingConfiguration().isEmailGatewaySetUp();
        final boolean emailReplyGatewaySetUp = getUserData().getMessagingConfiguration().isEmailReplyGatewaySetUp();
        final boolean messagingConfigurationSetUp = getUserData().getMessagingConfiguration().isSetUp();
        final GatewayType gatewayType = getUserData().getMessagingConfiguration().getGatewayType();
        final boolean isUsingDeprecatedOauth = getUserData().getMessagingConfiguration().isUsingDeprecatedOauthMethod();
        final ServiceStatus serviceStatus = getUserData().getServiceConfiguration().getServiceStatus();

        // targets
        targetsLayout.setVisibility(View.VISIBLE);
        targetsOptionsLayout.setVisibility(View.VISIBLE);

        addCcEmailInstructionsLayout.setVisibility(View.GONE);
        // https://bitbucket.org/codolutions/relay-me-android-app/issues/40/cc-has-stopped-working-for-gmail
        // if (!editingCcEmail && !StringUtil.empty(getUserData().getMessagingConfiguration().getTargetEmailAddress())
        // && StringUtil.empty(getUserData().getMessagingConfiguration().getCcEmailAddress()))
        // addCcEmailInstructionsLayout.setVisibility(View.VISIBLE);

        targetEmailLayout.setVisibility(View.GONE);
        editTargetEmailLayout.setVisibility(View.GONE);
        targetsInstructionsLayout.setVisibility(View.GONE);
        ccEmailLayout.setVisibility(View.GONE);
        editCcEmailLayout.setVisibility(View.GONE);
        if (editingTargetEmail)
            editTargetEmailLayout.setVisibility(View.VISIBLE);
        else if (targetSetup)
        {
            targetEmailLayout.setVisibility(View.VISIBLE);
            targetEmailAddress.setText(getUserData().getMessagingConfiguration().getTargetEmailAddress());
        }
        else
            targetsInstructionsLayout.setVisibility(View.VISIBLE);

        // email gateway
        gatewaySettingsLayout.setVisibility(View.VISIBLE);
        gatewayOptionsLayout.setVisibility(View.VISIBLE);

        emailGatewayDescription.setVisibility(View.GONE);
        if (!emailGatewaySetUp || (replyingSwitchOn && !emailReplyGatewaySetUp))
        {
            emailGatewayDescription.setText(gatewayType.getDescriptionResource());
            emailGatewayDescription.setVisibility(View.VISIBLE);
        }

        smtpAccountLayout.setVisibility(View.GONE);
        imapAccountLayout.setVisibility(View.GONE);
        gmailAccountLayout.setVisibility(View.GONE);
        if (gatewayType == GatewayType.GMAIL)
        {
            gmailAccountLayout.setVisibility(View.VISIBLE);
            clearGmailOauthButton.setVisibility(View.GONE);
            setUpGmailButton.setVisibility(View.GONE);
            gmailAccountAddress.setVisibility(View.GONE);
            if (gmailLinkSetUp)
            {
                gmailAccountAddress.setVisibility(View.VISIBLE);
                gmailAccountAddress.setText(oauthEmail);
                clearGmailOauthButton.setVisibility(View.VISIBLE);
            }
            else
                setUpGmailButton.setVisibility(View.VISIBLE);
        }
        else if (gatewayType == GatewayType.CUSTOM)
        {
            smtpAccountLayout.setVisibility(View.VISIBLE);
            setupSmtpServerButton.setVisibility(View.GONE);
            smtpServerViewLayout.setVisibility(View.GONE);
            smtpServerViewButtons.setVisibility(View.GONE);
            smtpServerEditLayout.setVisibility(View.GONE);
            smtpServerEditButtons.setVisibility(View.GONE);
            smtpServerStatusLayout.setVisibility(View.GONE);
            if (editingSmtpServer)
            {
                if (!StringUtil.empty(smtpServerStatusText))
                {
                    smtpServerStatusLayout.setVisibility(View.VISIBLE);
                    smtpServerStatusMessage.setText(smtpServerStatusText);
                    smtpServerStatusActions.setVisibility(View.GONE);
                    if (smtpServerSaveError)
                        smtpServerStatusActions.setVisibility(View.VISIBLE);
                }
                smtpServerEditLayout.setVisibility(View.VISIBLE);
                smtpServerEditButtons.setVisibility(View.VISIBLE);
            }
            else if (smtpSetUp)
            {
                smtpServerSummary.setText(getUserData().getMessagingConfiguration().getSmtpServer().getSummary());
                smtpServerViewLayout.setVisibility(View.VISIBLE);
                smtpServerViewButtons.setVisibility(View.VISIBLE);
            }
            else
                setupSmtpServerButton.setVisibility(View.VISIBLE);

            if (smtpSetUp && replyingSwitchOn)
            {
                imapAccountLayout.setVisibility(View.VISIBLE);
                setupImapServerButton.setVisibility(View.GONE);
                imapServerViewLayout.setVisibility(View.GONE);
                imapServerViewButtons.setVisibility(View.GONE);
                imapServerEditLayout.setVisibility(View.GONE);
                imapServerEditButtons.setVisibility(View.GONE);
                imapServerStatusLayout.setVisibility(View.GONE);
                if (editingImapServer)
                {
                    if (!StringUtil.empty(imapServerStatusText))
                    {
                        imapServerStatusLayout.setVisibility(View.VISIBLE);
                        imapServerStatusMessage.setText(imapServerStatusText);
                        imapServerStatusActions.setVisibility(View.GONE);
                        if (imapServerSaveError)
                            imapServerStatusActions.setVisibility(View.VISIBLE);
                    }
                    imapServerEditLayout.setVisibility(View.VISIBLE);
                    imapServerEditButtons.setVisibility(View.VISIBLE);
                }
                else if (imapSetUp)
                {
                    imapServerSummary.setText(getUserData().getMessagingConfiguration().getImapServer().getSummary());
                    imapServerViewLayout.setVisibility(View.VISIBLE);
                    imapServerViewButtons.setVisibility(View.VISIBLE);
                }
                else
                    setupImapServerButton.setVisibility(View.VISIBLE);
            }
        }

        // configuration warning
        configurationWarningLayout.setVisibility(View.GONE);
        if (!targetSetUp)
        {
            configurationWarningLayout.setVisibility(View.VISIBLE);
            configurationWarningText.setText(R.string.lbl_configuration_not_done_target);
        }
        else if (!emailGatewaySetUp || (replyingSwitchOn && !emailReplyGatewaySetUp))
        {
            configurationWarningLayout.setVisibility(View.VISIBLE);
            configurationWarningText.setText(R.string.lbl_configuration_not_done_gateway);
        }
        else if (gatewayType == GatewayType.GMAIL && isUsingDeprecatedOauth)
        {
            configurationWarningLayout.setVisibility(View.VISIBLE);
            configurationWarningText.setText(R.string.lbl_using_deprecated_oauth);
        }

        // secondary configuration
        secondaryConfigurationLayout.setVisibility(View.GONE);
        if (anySmsReceived)
        {
            secondaryConfigurationLayout.setVisibility(View.VISIBLE);

            // forwarding
            forwardingLayout.setVisibility(View.GONE);
            if (messagingConfigurationSetUp)
                forwardingLayout.setVisibility(View.VISIBLE);
            emailSubjectPrefixValueLayout.setVisibility(editingEmailSubjectPrefix ? View.GONE : View.VISIBLE);
            emailSubjectPrefixValue.setText(getUserData().getMessagingConfiguration().getEmailSubjectPrefix());
            editEmailSubjectPrefixLayout.setVisibility(editingEmailSubjectPrefix ? View.VISIBLE : View.GONE);

            // replying
            replyingLayout.setVisibility(View.GONE);
            missedCallNotificationCheckBox.setChecked(missedCallNotificationEnabled);
            if (messagingConfigurationSetUp)
            {
                replyingLayout.setVisibility(View.VISIBLE);
                replyingSwitch.setChecked(replyingSwitchOn);
                replyingInstructions.setVisibility(View.GONE);
                replyingOptionsLayout.setVisibility(View.GONE);
                if (replyingSwitchOn)
                {
                    if (emailReplyGatewaySetUp)
                    {
                        replyingOptionsLayout.setVisibility(View.VISIBLE);
                        replyingStatusSwitch.setChecked(replyingStatusSwitchOn);
                        replyingFromDifferentEmailSwitch.setChecked(replyingFromDifferentEmailOn);
                        replySourceLayout.setVisibility(View.GONE);
                        editReplySourceLayout.setVisibility(View.GONE);
                        if (replyingFromDifferentEmailOn)
                        {
                            if (editingReplySource)
                                editReplySourceLayout.setVisibility(View.VISIBLE);
                            else
                            {
                                replySourceLayout.setVisibility(View.VISIBLE);
                                replyingSourceEmailAddress.setText(replySourceEmailAddress);
                            }
                        }
                    }
                    else
                        replyingInstructions.setVisibility(View.VISIBLE);
                }
            }

            // service control
            serviceControlLayout.setVisibility(View.GONE);
            if (messagingConfigurationSetUp)
            {
                serviceControlLayout.setVisibility(View.VISIBLE);
                boolean smsTriggerVisibility = serviceStatus != ServiceStatus.ENABLED;
                smsTriggerLayout.setVisibility(smsTriggerVisibility ? View.VISIBLE : View.GONE);
                smsTriggerCodeHelpLabel.setText(getString(R.string.lbl_service_trigger_help, getUserData()
                        .getServiceConfiguration().getActivationCode()));
                smsTriggerHeadingLayout.setVisibility(editingSmsTriggerCode ? View.GONE : View.VISIBLE);
                editSmsTriggerCodeLayout.setVisibility(editingSmsTriggerCode ? View.VISIBLE : View.GONE);
                boolean serviceScheduleVisibility = serviceStatus == ServiceStatus.SCHEDULED;
                serviceScheduleLayout.setVisibility(serviceScheduleVisibility ? View.VISIBLE : View.GONE);
                serviceScheduleHeadingLayout.setVisibility(editingSchedule ? View.GONE : View.VISIBLE);
                editScheduleLayout.setVisibility(editingSchedule ? View.VISIBLE : View.GONE);
                setScheduleHelpLabel.setVisibility(editingSchedule ? View.VISIBLE : View.GONE);
                currentScheduleLabel.setVisibility(editingSchedule ? View.GONE : View.VISIBLE);
                String scheduleStartTime = getUserData().getServiceConfiguration().getScheduleBeginTimeString();
                String scheduleEndTime = getUserData().getServiceConfiguration().getScheduleEndTimeString();
                boolean nextDay = getUserData().getServiceConfiguration().getScheduleBeginTime() >= getUserData()
                        .getServiceConfiguration().getScheduleEndTime();
                currentScheduleLabel.setText(getString(nextDay ? R.string.lbl_service_schedule_current_next_day
                        : R.string.lbl_service_schedule_current, scheduleStartTime, scheduleEndTime));
            }
        }

        smsCheckLayout.setVisibility(View.GONE);
        applicationStatusLayout.setVisibility(View.VISIBLE);
        if (messagingConfigurationSetUp)
        {
            if (!anySmsReceived)
            {
                // Check SMS section
                smsCheckLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    private void restoreFieldValues()
    {
        // Update the state of edit fields
        if (editingTargetEmail)
        {
            targetEmailLayout.requestFocus();
            targetEmailEditText.setText(targetEmailAddressDraft);
        }
        else if (editingCcEmail)
        {
            ccEmailEditText.requestFocus();
            ccEmailEditText.setText(ccEmailAddressDraft);
        }
        else if (editingReplySource)
        {
            replySourceLayout.requestFocus();
            replySourceEditText.setText(replySourceDraft);
        }
        else if (editingEmailSubjectPrefix)
        {
            editEmailSubjectPrefixLayout.requestFocus();
            emailSubjectPrefixEditText.setText(emailSubjectPrefixDraft);
        }
        else if (editingSmsTriggerCode)
        {
            editSmsTriggerCodeLayout.requestFocus();
            smsTriggerCodeEditText.setText(smsTriggerCodeDraft);
        }
        else if (editingSchedule)
        {
            editScheduleLayout.requestFocus();
            scheduleStartTimeEditText.setText(scheduleStartDraft);
            scheduleEndTimeEditText.setText(scheduleEndDraft);
        }
        else if (editingSmtpServer)
        {
            smtpServerEmailEditText.requestFocus();
            smtpServerEmailEditText.setText(smtpServerEmailDraft);
            smtpServerIpEditText.setText(smtpServerIpDraft);
            smtpServerPortEditText.setText(smtpServerPortDraft);
            smtpServerUsernameEditText.setText(smtpServerUsernameDraft);
            smtpServerPasswordEditText.setText(smtpServerPasswordDraft);
            if (spinnerItemsServerSecurities.contains(smtpServerSecurityDraft))
                smtpServerSecuritySpinner.setSelection(spinnerItemsServerSecurities.indexOf(smtpServerSecurityDraft));
        }
        else if (editingImapServer)
        {
            imapServerIpEditText.requestFocus();
            imapServerIpEditText.setText(imapServerIpDraft);
            imapServerPortEditText.setText(imapServerPortDraft);
            imapServerUsernameEditText.setText(imapServerUsernameDraft);
            imapServerPasswordEditText.setText(imapServerPasswordDraft);
            if (spinnerItemsServerSecurities.contains(imapServerSecurityDraft))
                imapServerSecuritySpinner.setSelection(spinnerItemsServerSecurities.indexOf(imapServerSecurityDraft));
        }
    }

    private void refreshStatus()
    {
        boolean messagingConfigurationSetUp = getUserData().getMessagingConfiguration().isSetUp();

        connectionStatusLayout.setVisibility(View.GONE);
        messagesStatusLayout.setVisibility(View.GONE);
        if (!systemStatus.isOnline())
            connectionStatusLayout.setVisibility(View.VISIBLE);
        serviceOffWarningText.setVisibility(View.GONE);
        if (!getUserData().getServiceConfiguration().isActive())
        {
            serviceOffWarningText.setVisibility(View.VISIBLE);
            messagesStatusLayout.setVisibility(View.VISIBLE);
        }
        replyingDisabledWarningText.setVisibility(View.GONE);
        if (messagingConfigurationSetUp
                && !getUserData().getServiceConfiguration().isReplyingEnabled())
        {
            replyingDisabledWarningText.setVisibility(View.VISIBLE);
            messagesStatusLayout.setVisibility(View.VISIBLE);
        }
        pendingMessagesWarningText.setVisibility(View.GONE);
        if (messagingConfigurationSetUp && systemStatus.isThereAnyPendingMessage()
                && !getUserData().isSendingMessagesNow())
        {
            pendingMessagesWarningText.setVisibility(View.VISIBLE);
            messagesStatusLayout.setVisibility(View.VISIBLE);
        }
        statusSeparatorImageView.setVisibility(View.GONE);
        boolean separatorRequired = false;
        if (connectionStatusLayout.getVisibility() == View.VISIBLE)
            separatorRequired = true;
        if (messagesStatusLayout.getVisibility() == View.VISIBLE)
        {
            if (separatorRequired)
                statusSeparatorImageView.setVisibility(View.VISIBLE);
            separatorRequired = true;
        }
    }

    private void refreshGatewayTypes()
    {
        GatewayType gatewayType = getUserData().getMessagingConfiguration().getGatewayType();

        final List<String> displayGatewayTypes = new ArrayList<String>();
        for (GatewayType gwType : spinnerItemsGatewayTypes)
            displayGatewayTypes.add(applicationContext.getString(gwType.getNameResource()));

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(myActivity, android.R.layout.simple_spinner_item,
                displayGatewayTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gatewayTypeSpinner.setAdapter(adapter);
        if (spinnerItemsGatewayTypes.contains(gatewayType))
            gatewayTypeSpinner.setSelection(spinnerItemsGatewayTypes.indexOf(gatewayType));
    }

    private void refreshSmtpServerSecurity()
    {
        final List<String> displayServerSecurities = new ArrayList<String>();
        for (ServerSecurity serverSecurity : spinnerItemsServerSecurities)
            displayServerSecurities.add(applicationContext.getString(serverSecurity.getDisplayNameResourceId()));

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(myActivity, android.R.layout.simple_spinner_item,
                displayServerSecurities);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        smtpServerSecuritySpinner.setAdapter(adapter);
    }

    private void refreshImapServerSecurity()
    {
        final List<String> displayServerSecurities = new ArrayList<String>();
        for (ServerSecurity serverSecurity : spinnerItemsServerSecurities)
            displayServerSecurities.add(applicationContext.getString(serverSecurity.getDisplayNameResourceId()));

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(myActivity, android.R.layout.simple_spinner_item,
                displayServerSecurities);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        imapServerSecuritySpinner.setAdapter(adapter);
    }

    private void refreshMailboxNames()
    {
        spinnerItemsMailboxNames = new ArrayList<String>(getUserData().getMessagingConfiguration().getAllMailboxes());
        final List<String> displayMailboxNames = ImapEmailReceiver.getDisplayMailboxNames(applicationContext,
                spinnerItemsMailboxNames);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(myActivity, android.R.layout.simple_spinner_item,
                displayMailboxNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mailboxesSpinner.setAdapter(adapter);
        String userSelectedMailbox = getUserData().getMessagingConfiguration().getReplyMailboxName();
        if (userSelectedMailbox != null && spinnerItemsMailboxNames.contains(userSelectedMailbox))
            mailboxesSpinner.setSelection(spinnerItemsMailboxNames.indexOf(userSelectedMailbox));
        else if (spinnerItemsMailboxNames.contains(ImapEmailReceiver.INBOX_FOLDER))
            mailboxesSpinner.setSelection(spinnerItemsMailboxNames.indexOf(ImapEmailReceiver.INBOX_FOLDER));
    }

    private void refreshPollingFactor()
    {
        final List<String> displayPollingFactors = new ArrayList<String>();
        for (PollingFactor pollingFactor : spinnerItemPollingFactors)
            displayPollingFactors.add(applicationContext.getString(pollingFactor.getNameResource()));

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(myActivity, android.R.layout.simple_spinner_item,
                displayPollingFactors);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        pollingFactorSpinner.setAdapter(adapter);
        PollingFactor userSelectedPollingFactor = getUserData().getMessagingConfiguration().getPollingFactor();
        if (userSelectedPollingFactor != null && spinnerItemPollingFactors.contains(userSelectedPollingFactor))
            pollingFactorSpinner.setSelection(spinnerItemPollingFactors.indexOf(userSelectedPollingFactor));
    }

    private void refreshServiceStatus()
    {
        ServiceStatus userSelectedServiceStatus = getUserData().getServiceConfiguration().getServiceStatus();

        final List<String> displayServiceStatuses = new ArrayList<String>();
        for (ServiceStatus serviceStatus : spinnerItemServiceStatuses)
            displayServiceStatuses.add(applicationContext.getString(serviceStatus.getDisplayNameResourceId()));

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(myActivity, android.R.layout.simple_spinner_item,
                displayServiceStatuses);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        serviceControlSpinner.setAdapter(adapter);
        if (userSelectedServiceStatus != null && spinnerItemServiceStatuses.contains(userSelectedServiceStatus))
            serviceControlSpinner.setSelection(spinnerItemServiceStatuses.indexOf(userSelectedServiceStatus));
    }

    private void restartScheduledTasks()
    {
        ((BaseTabActivity) getActivity()).stopService(new Intent(applicationContext, TaskSchedulingService.class));
        ((BaseTabActivity) getActivity()).startService(new Intent(applicationContext, TaskSchedulingService.class));
    }

    @Override
    public void onFinishConfirmationDialog(DialogParams<?> parcelable)
    {
        if (parcelable.getId() == DIALOG_ID_CONFIRM_DELETE_TARGET_EMAIL)
        {
            LogStoreHelper.info(this, applicationContext,
                    "Deleting email address " + ((StringDialogExtra) parcelable).getValue());
            getUserData().getMessagingConfiguration().clearTargetEmailAddress();
            LogStoreHelper.info(this, myActivity, "Email address cleared");
            refreshUI();
        }
        else if (parcelable.getId() == DIALOG_ID_CONFIRM_DELETE_CC_EMAIL)
        {
            LogStoreHelper.info(this, applicationContext, "Deleting CC email address "
                    + ((StringDialogExtra) parcelable).getValue());
            getUserData().getMessagingConfiguration().clearCcEmailAddress();
            LogStoreHelper.info(this, myActivity, "Email address cleared");
            refreshUI();
        }
        else if (parcelable.getId() == DIALOG_ID_CONFIRM_DELETE_SMTP_SERVER)
        {
            LogStoreHelper.info(this, applicationContext,
                    "Deleting SMTP server " + ((StringDialogExtra) parcelable).getValue());
            getUserData().getMessagingConfiguration().clearSmtpServer();
            LogStoreHelper.info(this, myActivity, "SMTP server cleared");
            refreshUI();
        }
        else if (parcelable.getId() == DIALOG_ID_CONFIRM_DELETE_IMAP_SERVER)
        {
            LogStoreHelper.info(this, applicationContext,
                    "Deleting IMAP server " + ((StringDialogExtra) parcelable).getValue());
            getUserData().getMessagingConfiguration().clearImapServer();
            LogStoreHelper.info(this, myActivity, "IMAP server cleared");
            refreshUI();
        }
    }

    @Override
    public void onCancelConfirmationDialog(DialogParams<?> parcelable)
    {
        if (DIALOG_ID_CONFIRM_DELETE_TARGET_EMAIL.equals(parcelable.getId()))
        {
            LogStoreHelper.info(this, applicationContext, "Not deleting target email address "
                    + ((StringDialogExtra) parcelable).getValue());
        }
        else if (DIALOG_ID_CONFIRM_DELETE_CC_EMAIL.equals(parcelable.getId()))
        {
            LogStoreHelper.info(this, applicationContext, "Not deleting CC email address "
                    + ((StringDialogExtra) parcelable).getValue());
        }
    }

    @Override
    public void onFinishConfirmClearOauthDialog(boolean confirmed, boolean goToGoogle)
    {
        getUserData().getMessagingConfiguration().clearAllMailboxes();
        getUserData().getMessagingConfiguration().clearReplyMailboxName();
        getUserData().getMessagingConfiguration().clearLastCheck();
        getUserData().getMessagingConfiguration().clearOauthEmailAddress();
        getUserData().getMessagingConfiguration().clearOauthAccessToken();
        refreshUI();
        if (goToGoogle)
        {
            LogStoreHelper.info(this, applicationContext, "Taking user to Google account dashboard");
            Toast.makeText(applicationContext, getString(R.string.lbl_please_wait), Toast.LENGTH_LONG).show();
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(systemStatus.getGoogleOauthTokensUri())));
        }
    }

    @Override
    public void onFinishOauthDialog(OauthDialogAction action)
    {
        String url = getUserData().getServerSideClient().getGoogleOauthUrl(false);
        if (!systemStatus.isInternetBrowserFound(url))
        {
            LogStoreHelper.warn(this, myActivity, "No Internet browser found.", null);
            applicationContext.sendBroadcast(new Intent(Constants.SHOW_ALERT_BROADCAST_ACTION).putExtra(
                    Constants.SHOW_ALERT_EXTRA_REPORT_ERROR, R.string.lbl_no_internet_browser).putExtra(
                    Constants.SHOW_ALERT_EXTRA_DIALOG_TITLE, R.string.lbl_error_title));
        }
        else
        {
            Toast.makeText(applicationContext, getString(R.string.lbl_please_wait), Toast.LENGTH_LONG).show();
            LogStoreHelper.info(this, applicationContext, "Opening OAuth URL");
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url)).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
                    | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_FROM_BACKGROUND);
            startActivity(intent);
        }
    }

    @Click(R.id.smscheckbutton)
    protected void onSmsCheckButtonClicked()
    {
        getActivity().sendBroadcast(new Intent(Constants.SHOW_SMS_HELP_DIALOG_BROADCAST_ACTION));
    }

    @Click(R.id.missedcallnotificationcheckbox)
    protected void onMissedCallNotificationCheckBoxClicked()
    {
        getUserData().getServiceConfiguration().setMissedCallNotificationEnabled(
                missedCallNotificationCheckBox.isChecked());
        LogStoreHelper.info(this, myActivity, "Missed call notifications switch saved: "
                + missedCallNotificationCheckBox.isChecked());
        refreshUI();
    }

    @Click(R.id.replyingswitchcheckbox)
    protected void onReplyingSwitchCheckBoxClicked()
    {
        boolean oldReplyingSwitch = getUserData().getServiceConfiguration().isReplyingEnabled();
        getUserData().getServiceConfiguration().setReplyingEnabled(replyingSwitch.isChecked());
        LogStoreHelper.info(this, myActivity, "Reply switch saved: " + replyingSwitch.isChecked());
        if (!oldReplyingSwitch && getUserData().getServiceConfiguration().isReplyingEnabled())
        {
            resetLastEmailCheckDate();
            reloadMailboxNames();
        }
        refreshUI();
    }

    @Click(R.id.replyingstatusswitchcheckbox)
    protected void onReplyingStatusSwitchCheckBoxClicked()
    {
        getUserData().getServiceConfiguration().setReplyingStatusReportEnabled(replyingStatusSwitch.isChecked());
        LogStoreHelper.info(this, myActivity, "Reply status switch saved: " + replyingStatusSwitch.isChecked());
        refreshUI();
    }

    @Click(R.id.replyingfromdifferentemailswitchcheckbox)
    protected void onReplyingFromDifferentEmailSwitchCheckBoxClicked()
    {
        getUserData().getServiceConfiguration().setReplyingFromDifferentEmail(
                replyingFromDifferentEmailSwitch.isChecked());
        LogStoreHelper.info(this, myActivity, "Reply from different email switch saved: "
                + replyingFromDifferentEmailSwitch.isChecked());
        refreshUI();
    }

    @Click(R.id.setuptargetbutton)
    protected void onSetUpTargetEmailButtonClicked()
    {
        setEditingTargetEmail(true);
    }

    @Click(R.id.addccemail)
    protected void onAddCcEmailButtonClicked()
    {
        setEditingCcEmail(true);
    }

    @Click(R.id.saveemailbutton)
    protected void onSaveTargetEmailButtonClicked()
    {
        if (editingTargetEmail)
        {
            String oldEmailAddress = getUserData().getMessagingConfiguration().getTargetEmailAddress();
            String emailAddress = targetEmailEditText.getText().toString().trim();
            if (!emailAddress.equalsIgnoreCase(oldEmailAddress))
            {
                getUserData().getMessagingConfiguration().setTargetEmailAddress(emailAddress);
                resetLastEmailCheckDate();
                LogStoreHelper.info(this, myActivity, "Target email address saved: " + emailAddress);
            }
        }
        setEditingTargetEmail(false);
    }

    @Click(R.id.edittargetemailbutton)
    protected void onEditTargetEmailButtonClicked()
    {
        setEditingTargetEmail(true);
    }

    @Click(R.id.canceleditemailbutton)
    protected void onCancelEditTargetEmailButtonClicked()
    {
        String savedEmailAddress = getUserData().getMessagingConfiguration().getTargetEmailAddress();
        targetEmailEditText.setText(savedEmailAddress);
        LogStoreHelper.info(this, myActivity, "Target email address unchanged");
        setEditingTargetEmail(false);
    }

    @Click(R.id.deletetargetemailbutton)
    protected void onDeleteTargetEmailButtonClicked()
    {
        String emailAddress = getUserData().getMessagingConfiguration().getTargetEmailAddress();
        ConfirmationDialogFragment frag = new ConfirmationDialogFragment_();
        Bundle args = new Bundle();
        args.putString(ConfirmationDialogFragment.ARGUMENT_TITLE, getString(R.string.lbl_delete_email_dialog_title));
        args.putString(ConfirmationDialogFragment.ARGUMENT_MESSAGE,
                getString(R.string.lbl_delete_email_dialog_confirmation, emailAddress));
        args.putParcelable(ConfirmationDialogFragment.ARGUMENT_DIALOG_DATA, new DialogParams.StringDialogExtra(
                DIALOG_ID_CONFIRM_DELETE_TARGET_EMAIL, emailAddress));
        frag.setArguments(args);
        frag.setTargetFragment(this, 0);
        frag.setListener(this);
        frag.show(getFragmentManager(), "confirm_delete_target_email_dialog");
    }

    @Click(R.id.saveccemailbutton)
    protected void onSaveCcEmailButtonClicked()
    {
        if (editingCcEmail)
        {
            String oldEmailAddress = getUserData().getMessagingConfiguration().getCcEmailAddress();
            String emailAddress = ccEmailEditText.getText().toString().trim();
            if (!emailAddress.equalsIgnoreCase(oldEmailAddress))
            {
                getUserData().getMessagingConfiguration().setCcEmailAddress(emailAddress);
                LogStoreHelper.info(this, myActivity, "CC email address saved: " + emailAddress);
            }
        }
        setEditingCcEmail(false);
    }

    @Click(R.id.editccemailbutton)
    protected void onEditCcEmailButtonClicked()
    {
        setEditingCcEmail(true);
    }

    @Click(R.id.canceleditccemailbutton)
    protected void onCancelEditCcEmailButtonClicked()
    {
        String savedEmailAddress = getUserData().getMessagingConfiguration().getCcEmailAddress();
        ccEmailEditText.setText(savedEmailAddress);
        LogStoreHelper.info(this, myActivity, "CC email address unchanged");
        setEditingCcEmail(false);
    }

    @Click(R.id.deleteccemailbutton)
    protected void onDeleteCcEmailButtonClicked()
    {
        String emailAddress = getUserData().getMessagingConfiguration().getCcEmailAddress();
        ConfirmationDialogFragment frag = new ConfirmationDialogFragment_();
        Bundle args = new Bundle();
        args.putString(ConfirmationDialogFragment.ARGUMENT_TITLE, getString(R.string.lbl_delete_email_dialog_title));
        args.putString(ConfirmationDialogFragment.ARGUMENT_MESSAGE,
                getString(R.string.lbl_delete_email_dialog_confirmation, emailAddress));
        args.putParcelable(ConfirmationDialogFragment.ARGUMENT_DIALOG_DATA, new DialogParams.StringDialogExtra(
                DIALOG_ID_CONFIRM_DELETE_CC_EMAIL, emailAddress));
        frag.setArguments(args);
        frag.setTargetFragment(this, 0);
        frag.setListener(this);
        frag.show(getFragmentManager(), "confirm_delete_cc_email_dialog");
    }

    @Click(R.id.setupsmtpserver)
    protected void onSetupSmtpServerButtonClicked()
    {
        setEditingSmtpServer(true);
    }

    @Click(R.id.editsmtpserverbutton)
    protected void onEditSmtpServerButtonClicked()
    {
        smtpServerStatusText = null;
        smtpServerSaveError = false;
        setEditingSmtpServer(true);
    }

    @Click(R.id.deletesmtpserverbutton)
    protected void onDeleteSmtpServerButtonClicked()
    {
        SmtpServer smtpServer = getUserData().getMessagingConfiguration().getSmtpServer();
        ConfirmationDialogFragment frag = new ConfirmationDialogFragment_();
        Bundle args = new Bundle();
        args.putString(ConfirmationDialogFragment.ARGUMENT_TITLE,
                getString(R.string.lbl_delete_smtp_server_dialog_title));
        args.putString(ConfirmationDialogFragment.ARGUMENT_MESSAGE,
                getString(R.string.lbl_delete_smtp_server_dialog_confirmation, smtpServer.getSummary()));
        args.putParcelable(ConfirmationDialogFragment.ARGUMENT_DIALOG_DATA, new DialogParams.StringDialogExtra(
                DIALOG_ID_CONFIRM_DELETE_SMTP_SERVER, smtpServer.getSummary()));
        frag.setArguments(args);
        frag.setTargetFragment(this, 0);
        frag.setListener(this);
        frag.show(getFragmentManager(), "confirm_delete_smtp_server_dialog");
    }

    @Click(R.id.canceleditsmtpserverbutton)
    protected void onCancelEditSmtpServerButtonClicked()
    {
        setEditingSmtpServer(false);
    }

    @Click(R.id.savesmtpserverbutton)
    protected void onSaveSmtpServerButtonClicked()
    {
        if (editingSmtpServer)
            saveSmtpServer(false);
    }

    @Click(R.id.smtpserversaveanyway)
    protected void onForceSaveSmtpServerButtonClicked()
    {
        if (editingSmtpServer)
            saveSmtpServer(true);
    }

    private SmtpServer getInputSmtpServer(boolean tested)
    {
        String email = smtpServerEmailEditText.getText().toString();
        String address = smtpServerIpEditText.getText().toString();
        int port = SmtpServer.DEFAULT_PORT;
        try
        {
            port = Integer.parseInt(smtpServerPortEditText.getText().toString());
        }
        catch (NumberFormatException e)
        {
            LogStoreHelper.info(applicationContext, "Invalid port number: "
                    + smtpServerPortEditText.getText().toString());
        }
        ServerSecurity serverSecurity = spinnerItemsServerSecurities.get(smtpServerSecuritySpinner
                .getSelectedItemPosition());
        String username = smtpServerUsernameEditText.getText().toString();
        String password = smtpServerPasswordEditText.getText().toString();
        return new SmtpServer(email, address, port, serverSecurity, tested, new ServerCredentials(username, password));
    }

    private void saveSmtpServer(boolean forceSave)
    {
        if (forceSave)
            finishSavingSmtpServer(false);
        else
        {
            SmtpServer smtpServer = getInputSmtpServer(false);
            if (smtpServer.isValid())
            {
                SmtpServer oldSmtpServer = getUserData().getMessagingConfiguration().getSmtpServer();
                if (smtpServer.equals(oldSmtpServer) && oldSmtpServer.isTested())
                    setEditingSmtpServer(false);
                else
                {
                    smtpServerStatusText = getString(R.string.lbl_checking_smtp_server);
                    smtpServerSaveError = false;
                    refreshUI();
                    Intent messagingIntent = new Intent(applicationContext, EmailIntentService.class);
                    messagingIntent.putExtra(EmailIntentService.PARAM_IN_SMTP_SERVER, smtpServer);
                    ((BaseTabActivity) getActivity()).startIntentService(
                            EmailIntentService.ACTION_CODE_CHECK_SMTP_SERVER, messagingIntent);
                }
            }
            else
                onSmtpServerVerificationFailed(getString(R.string.lbl_invalid_smtp_server_configuration));
        }
    }

    private void finishSavingSmtpServer(boolean tested)
    {
        SmtpServer smtpServer = getInputSmtpServer(tested);
        smtpServerStatusText = null;
        smtpServerSaveError = false;
        getUserData().getMessagingConfiguration().setSmtpServer(smtpServer);
        LogStoreHelper.info(this, myActivity, "SMTP server saved: " + smtpServer);
        setEditingSmtpServer(false);
    }

    private void onSmtpServerVerificationFailed(String errorMessage)
    {
        smtpServerStatusText = errorMessage;
        smtpServerSaveError = true;
        refreshUI();
    }

    @Click(R.id.setupimapserver)
    protected void onSetupImapServerButtonClicked()
    {
        setEditingImapServer(true);
    }

    @Click(R.id.editimapserverbutton)
    protected void onEditImapServerButtonClicked()
    {
        imapServerStatusText = null;
        imapServerSaveError = false;
        setEditingImapServer(true);
    }

    @Click(R.id.deleteimapserverbutton)
    protected void onDeleteImapServerButtonClicked()
    {
        ImapServer imapServer = getUserData().getMessagingConfiguration().getImapServer();
        ConfirmationDialogFragment frag = new ConfirmationDialogFragment_();
        Bundle args = new Bundle();
        args.putString(ConfirmationDialogFragment.ARGUMENT_TITLE,
                getString(R.string.lbl_delete_imap_server_dialog_title));
        args.putString(ConfirmationDialogFragment.ARGUMENT_MESSAGE,
                getString(R.string.lbl_delete_imap_server_dialog_confirmation, imapServer.getSummary()));
        args.putParcelable(ConfirmationDialogFragment.ARGUMENT_DIALOG_DATA, new DialogParams.StringDialogExtra(
                DIALOG_ID_CONFIRM_DELETE_IMAP_SERVER, imapServer.getSummary()));
        frag.setArguments(args);
        frag.setTargetFragment(this, 0);
        frag.setListener(this);
        frag.show(getFragmentManager(), "confirm_delete_imap_server_dialog");
    }

    @Click(R.id.canceleditimapserverbutton)
    protected void onCancelEditImapServerButtonClicked()
    {
        setEditingImapServer(false);
    }

    @Click(R.id.saveimapserverbutton)
    protected void onSaveImapServerButtonClicked()
    {
        if (editingImapServer)
            saveImapServer(false);
    }

    @Click(R.id.imapserversaveanyway)
    protected void onForceSaveImapServerButtonClicked()
    {
        if (editingImapServer)
            saveImapServer(true);
    }

    private ImapServer getInputImapServer(boolean tested)
    {
        String address = imapServerIpEditText.getText().toString();
        int port = ImapServer.DEFAULT_PORT;
        try
        {
            port = Integer.parseInt(imapServerPortEditText.getText().toString());
        }
        catch (NumberFormatException e)
        {
            LogStoreHelper.info(applicationContext, "Invalid port number: "
                    + imapServerPortEditText.getText().toString());
        }
        ServerSecurity serverSecurity = spinnerItemsServerSecurities.get(imapServerSecuritySpinner
                .getSelectedItemPosition());
        String username = imapServerUsernameEditText.getText().toString();
        String password = imapServerPasswordEditText.getText().toString();
        return new ImapServer(address, port, serverSecurity, tested, new ServerCredentials(username, password));
    }

    private void saveImapServer(boolean forceSave)
    {
        if (forceSave)
            finishSavingImapServer(false);
        else
        {
            ImapServer imapServer = getInputImapServer(false);
            if (imapServer.isValid())
            {
                ImapServer oldImapServer = getUserData().getMessagingConfiguration().getImapServer();
                if (imapServer.equals(oldImapServer) && oldImapServer.isTested())
                    setEditingImapServer(false);
                else
                {
                    imapServerStatusText = getString(R.string.lbl_checking_imap_server);
                    imapServerSaveError = false;
                    refreshUI();
                    Intent messagingIntent = new Intent(applicationContext, EmailIntentService.class);
                    messagingIntent.putExtra(EmailIntentService.PARAM_IN_IMAP_SERVER, imapServer);
                    ((BaseTabActivity) getActivity()).startIntentService(
                            EmailIntentService.ACTION_CODE_CHECK_IMAP_SERVER, messagingIntent);
                }
            }
            else
                onImapServerVerificationFailed(getString(R.string.lbl_invalid_imap_server_configuration));
        }
    }

    private void finishSavingImapServer(boolean tested)
    {
        ImapServer imapServer = getInputImapServer(tested);
        imapServerStatusText = null;
        imapServerSaveError = false;
        getUserData().getMessagingConfiguration().setImapServer(imapServer);
        resetLastEmailCheckDate();
        LogStoreHelper.info(this, myActivity, "IMAP server saved: " + imapServer);
        setEditingImapServer(false);
        reloadMailboxNames();
    }

    private void onImapServerVerificationFailed(String errorMessage)
    {
        imapServerStatusText = errorMessage;
        imapServerSaveError = true;
        refreshUI();
    }

    @Click(R.id.savereplysourcebutton)
    protected void onSaveReplySourceButtonClicked()
    {
        if (editingReplySource)
        {
            String oldReplySource = getUserData().getServiceConfiguration().getReplySourceEmailAddress();
            String replySource = replySourceEditText.getText().toString().trim();
            if (!replySource.equalsIgnoreCase(oldReplySource))
            {
                getUserData().getServiceConfiguration().setReplySourceEmailAddress(replySource);
                resetLastEmailCheckDate();
                LogStoreHelper.info(this, myActivity, "Reply source saved: " + replySource);
            }
        }
        setEditingReplySource(false);
    }

    @Click(R.id.editreplysourcebutton)
    protected void onEditReplySourceButtonClicked()
    {
        setEditingReplySource(true);
    }

    @Click(R.id.canceleditreplysourcebutton)
    protected void onCancelEditReplySourceButtonClicked()
    {
        String savedEmailAddress = getUserData().getServiceConfiguration().getReplySourceEmailAddress();
        replySourceEditText.setText(savedEmailAddress);
        LogStoreHelper.info(this, myActivity, "Email address unchanged");
        setEditingReplySource(false);
    }

    @Click(R.id.saveemailsubjectprefixbutton)
    protected void onSaveEmailSubjectPrefixButtonClicked()
    {
        if (editingEmailSubjectPrefix)
        {
            String prefix = emailSubjectPrefixEditText.getText().toString().trim();
            getUserData().getMessagingConfiguration().setEmailSubjectPrefix(prefix);
            LogStoreHelper.info(this, myActivity, "Email subject prefix saved: " + prefix);
        }
        setEditingEmailSubjectPrefix(false);
    }

    @Click(R.id.canceleditemailsubjectprefixbutton)
    protected void onCancelEditEmailSubjectPrefixButtonClicked()
    {
        String savedPrefix = getUserData().getMessagingConfiguration().getEmailSubjectPrefix();
        emailSubjectPrefixEditText.setText(savedPrefix);
        LogStoreHelper.info(this, myActivity, "Email subject prefix unchanged");
        setEditingEmailSubjectPrefix(false);
    }

    @Click(R.id.editemailsubjectprefixbutton)
    protected void onEditEmailSubjectPrefixButtonClicked()
    {
        setEditingEmailSubjectPrefix(true);
    }

    @Click(R.id.savesmstriggercodebutton)
    protected void onSaveSmsCodeButtonClicked()
    {
        if (editingSmsTriggerCode)
        {
            String smsCode = smsTriggerCodeEditText.getText().toString().trim();
            getUserData().getServiceConfiguration().setActivationCode(smsCode);
            LogStoreHelper.info(this, myActivity, "SMS trigger code saved: " + smsCode);
        }
        setEditingSmsCode(false);
    }

    @Click(R.id.canceleditsmstriggercodebutton)
    protected void onCancelEditSmsCodeButtonClicked()
    {
        String savedSmsCode = getUserData().getServiceConfiguration().getActivationCode();
        smsTriggerCodeEditText.setText(savedSmsCode);
        LogStoreHelper.info(this, myActivity, "SMS trigger code unchanged");
        setEditingSmsCode(false);
    }

    @Click(R.id.setsmstriggercodebutton)
    protected void onEditSmsCodeButtonClicked()
    {
        setEditingSmsCode(true);
    }

    @Click(R.id.saveschedulebutton)
    protected void onSaveScheduleButtonClicked()
    {
        if (editingSchedule)
        {
            String scheduleStart = scheduleStartTimeEditText.getText().toString();
            String scheduleEnd = scheduleEndTimeEditText.getText().toString();
            try
            {
                getUserData().getServiceConfiguration().setScheduleBeginTime(scheduleStart);
                getUserData().getServiceConfiguration().setScheduleEndTime(scheduleEnd);
                LogStoreHelper.info(this, myActivity, "Schedule saved: " + scheduleStart + " - " + scheduleEnd);
            }
            catch (OperationFailedException e)
            {
                LogStoreHelper.warn(applicationContext, "Failed saving dates", e);
            }
        }
        setEditingSchedule(false);
    }

    @Click(R.id.canceleditschedulebutton)
    protected void onCancelEditScheduleButtonClicked()
    {
        String scheduleStartTime = getUserData().getServiceConfiguration().getScheduleBeginTimeString();
        String scheduleEndTime = getUserData().getServiceConfiguration().getScheduleEndTimeString();
        scheduleStartTimeEditText.setText(scheduleStartTime);
        scheduleEndTimeEditText.setText(scheduleEndTime);
        LogStoreHelper.info(this, myActivity, "Schedule unchanged");
        setEditingSchedule(false);
    }

    @Click(R.id.setschedulebutton)
    protected void onEditScheduleButtonClicked()
    {
        setEditingSchedule(true);
    }

    @Click(R.id.setupgmailbutton)
    protected void onSetupGatewayButtonClicked()
    {
        if (!systemStatus.isOnline())
        {
            LogStoreHelper.warn(this, myActivity, "No Internet connection to set up OAuth.", null);
            applicationContext.sendBroadcast(new Intent(Constants.SHOW_ALERT_BROADCAST_ACTION).putExtra(
                    Constants.SHOW_ALERT_EXTRA_REPORT_ERROR, R.string.lbl_no_internet_connection).putExtra(
                    Constants.SHOW_ALERT_EXTRA_DIALOG_TITLE, R.string.lbl_error_title));
        }
        else if (!systemStatus.getUserData().isInSyncWithServer())
        {
            LogStoreHelper.warn(this, myActivity, "Not in sync with server.", null);
            applicationContext.sendBroadcast(new Intent(Constants.SHOW_ALERT_BROADCAST_ACTION).putExtra(
                    Constants.SHOW_ALERT_EXTRA_REPORT_ERROR, R.string.lbl_not_in_sync_with_server).putExtra(
                    Constants.SHOW_ALERT_EXTRA_DIALOG_TITLE, R.string.lbl_warning_title));
            ((MainActivity) getActivity()).syncWithServer();
        }
        else
        {
            DialogFragment confirmDialog = new OauthDialogFragment_();
            confirmDialog.setTargetFragment(this, 0);
            confirmDialog.show(getChildFragmentManager(), "oauth_dialog");
        }
    }

    @Click(R.id.cleargmailoauthbutton)
    protected void onClearOauthButtonClicked()
    {
        DialogFragment confirmDialog = new ConfirmClearOauthDialogFragment_();
        confirmDialog.setTargetFragment(this, 0);
        confirmDialog.show(getChildFragmentManager(), "confirm_clear_oauth_dialog");
    }

    private void setEditingTargetEmail(boolean editing)
    {
        editingTargetEmail = editing;
        // Only editing one thing at a time.
        if (editing)
        {
            editingCcEmail = false;
            editingSmtpServer = false;
            editingImapServer = false;
            editingReplySource = false;
            editingEmailSubjectPrefix = false;
            editingSmsTriggerCode = false;
            editingSchedule = false;
        }
        targetEmailEditText.setText(getUserData().getMessagingConfiguration().getTargetEmailAddress());
        refreshUI();
    }

    private void setEditingCcEmail(boolean editing)
    {
        editingCcEmail = editing;
        // Only editing one thing at a time.
        if (editing)
        {
            editingTargetEmail = false;
            editingSmtpServer = false;
            editingImapServer = false;
            editingReplySource = false;
            editingEmailSubjectPrefix = false;
            editingSmsTriggerCode = false;
            editingSchedule = false;
        }
        ccEmailEditText.setText(getUserData().getMessagingConfiguration().getCcEmailAddress());
        refreshUI();
    }

    private void setEditingSmtpServer(boolean editing)
    {
        editingSmtpServer = editing;
        // Only editing one thing at a time.
        if (editing)
        {
            editingTargetEmail = false;
            editingCcEmail = false;
            editingImapServer = false;
            editingReplySource = false;
            editingEmailSubjectPrefix = false;
            editingSmsTriggerCode = false;
            editingSchedule = false;
        }
        SmtpServer smtpServer = getUserData().getMessagingConfiguration().getSmtpServer();
        if (smtpServer != null)
        {
            smtpServerEmailEditText.setText(smtpServer.getEmail());
            smtpServerIpEditText.setText(smtpServer.getAddress());
            smtpServerPortEditText.setText(Integer.toString(smtpServer.getPort()));
            if (smtpServer.getCredentials() != null)
            {
                smtpServerUsernameEditText.setText(smtpServer.getCredentials().getUsername());
                smtpServerPasswordEditText.setText(smtpServer.getCredentials().getPassword());
            }
            smtpServerSecurityDraft = smtpServer.getSecurity();
            if (spinnerItemsServerSecurities.contains(smtpServer.getSecurity()))
                smtpServerSecuritySpinner.setSelection(spinnerItemsServerSecurities.indexOf(smtpServer.getSecurity()));
        }
        refreshUI();
    }

    private void setEditingImapServer(boolean editing)
    {
        editingImapServer = editing;
        // Only editing one thing at a time.
        if (editing)
        {
            editingTargetEmail = false;
            editingCcEmail = false;
            editingSmtpServer = false;
            editingReplySource = false;
            editingEmailSubjectPrefix = false;
            editingSmsTriggerCode = false;
            editingSchedule = false;
        }
        ImapServer imapServer = getUserData().getMessagingConfiguration().getImapServer();
        if (imapServer != null)
        {
            imapServerIpEditText.setText(imapServer.getAddress());
            imapServerPortEditText.setText(Integer.toString(imapServer.getPort()));
            if (imapServer.getCredentials() != null)
            {
                imapServerUsernameEditText.setText(imapServer.getCredentials().getUsername());
                imapServerPasswordEditText.setText(imapServer.getCredentials().getPassword());
            }
            imapServerSecurityDraft = imapServer.getSecurity();
            if (spinnerItemsServerSecurities.contains(imapServer.getSecurity()))
                imapServerSecuritySpinner.setSelection(spinnerItemsServerSecurities.indexOf(imapServer.getSecurity()));
        }
        refreshUI();
    }

    private void setEditingReplySource(boolean editing)
    {
        editingReplySource = editing;
        // Only editing one thing at a time.
        if (editing)
        {
            editingTargetEmail = false;
            editingCcEmail = false;
            editingSmtpServer = false;
            editingImapServer = false;
            editingEmailSubjectPrefix = false;
            editingSmsTriggerCode = false;
            editingSchedule = false;
        }
        String replySource = getUserData().getServiceConfiguration().getReplySourceEmailAddress();
        if (StringUtil.empty(replySource))
            replySource = getUserData().getMessagingConfiguration().getTargetEmailAddress();
        replySourceEditText.setText(replySource);
        refreshUI();
    }

    private void setEditingEmailSubjectPrefix(boolean editing)
    {
        editingEmailSubjectPrefix = editing;
        // Only editing one thing at a time.
        if (editing)
        {
            editingTargetEmail = false;
            editingCcEmail = false;
            editingSmtpServer = false;
            editingImapServer = false;
            editingReplySource = false;
            editingSmsTriggerCode = false;
            editingSchedule = false;
        }
        emailSubjectPrefixEditText.setText(getUserData().getMessagingConfiguration().getEmailSubjectPrefix());
        refreshUI();
    }

    private void setEditingSmsCode(boolean editing)
    {
        editingSmsTriggerCode = editing;
        // Only editing one thing at a time.
        if (editing)
        {
            editingTargetEmail = false;
            editingCcEmail = false;
            editingSmtpServer = false;
            editingImapServer = false;
            editingReplySource = false;
            editingEmailSubjectPrefix = false;
            editingSchedule = false;
        }
        smsTriggerCodeEditText.setText(getUserData().getServiceConfiguration().getActivationCode());
        refreshUI();
    }

    private void setEditingSchedule(boolean editing)
    {
        editingSchedule = editing;
        // Only editing one thing at a time.
        if (editing)
        {
            editingTargetEmail = false;
            editingCcEmail = false;
            editingSmtpServer = false;
            editingImapServer = false;
            editingReplySource = false;
            editingEmailSubjectPrefix = false;
            editingSmsTriggerCode = false;
        }
        scheduleStartTimeEditText.setText(getUserData().getServiceConfiguration().getScheduleBeginTimeString());
        scheduleEndTimeEditText.setText(getUserData().getServiceConfiguration().getScheduleEndTimeString());
        refreshUI();
    }

    private void processAccessToken()
    {
        LogStoreHelper.info(this, applicationContext, "Processing access token...");
        Toast.makeText(applicationContext, getString(R.string.lbl_oauth_success), Toast.LENGTH_LONG).show();
        refreshUI();
        reloadMailboxNames();
    }

    private void resetLastEmailCheckDate()
    {
        getUserData().getMessagingConfiguration().setLastCheck(new Date());
    }

    private void reloadMailboxNames()
    {
        ((BaseTabActivity) getActivity()).startIntentService(EmailIntentService.ACTION_CODE_LOAD_MAILBOX_NAMES,
                EmailIntentService.class);
    }
}
