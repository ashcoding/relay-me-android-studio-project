package com.tinywebgears.relayme.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;

import com.codolutions.android.common.data.CachedData;
import com.codolutions.android.common.exception.OperationFailedException;
import com.codolutions.android.common.serverside.ServerSideClient;
import com.codolutions.android.common.serverside.data.ApplicationPropertiesResponse;
import com.codolutions.android.common.serverside.data.KeysResponse;
import com.codolutions.android.common.serverside.data.RegistrationResponse;
import com.codolutions.android.common.serverside.data.VerificationResponse;
import com.codolutions.android.common.util.NumberUtil;
import com.codolutions.android.common.util.StringUtil;
import com.tinywebgears.relayme.R;
import com.tinywebgears.relayme.common.AbstractUserData;
import com.tinywebgears.relayme.common.Constants;
import com.tinywebgears.relayme.model.AutoSavingMessagingConfiguration;
import com.tinywebgears.relayme.model.AutoSavingServiceConfiguration;
import com.tinywebgears.relayme.model.DefaultMessagingConfiguration;
import com.tinywebgears.relayme.model.DefaultServiceConfiguration;
import com.tinywebgears.relayme.model.GatewayType;
import com.tinywebgears.relayme.model.MessagingConfiguration;
import com.tinywebgears.relayme.model.ServiceConfiguration;
import com.tinywebgears.relayme.model.ServiceStatus;
import com.tinywebgears.relayme.serverside.DefaultServerSideClient;
import com.tinywebgears.relayme.service.LogStoreHelper;
import com.tinywebgears.relayme.service.MessageStoreHelper;

import org.scribe.model.Token;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.UUID;

import br.com.kots.mob.complex.preferences.ComplexPreferences;

public class UserData extends AbstractUserData {
    private static final String TAG = UserData.class.getName();

    private static final String PREF_KEY_SENDING_MESSAGES_NOW = "SENDING_MESSAGES_NOW";
    private static final String PREF_KEY_CONTACTS = "CONTACTS";
    private static final String PREF_KEY_LAST_TIME_CONTACTS_READ = "LAST_CONTACTS_READ";
    private static final String PREF_KEY_LAST_SMS_SEEN_ON = "LAST_SMS_SEEN_ON";
    private static final String PREF_KEY_MESSAGING_CONFIGURATION = "MESSAGING_CONFIGURATION";
    private static final String PREF_KEY_SERVICE_CONFIGURATION = "SERVICE_CONFIGURATION";
    private static final String PREF_KEY_CACHED_VERIFICATION_RESPONSE = "CACHED_VERIFICATION_RESPONSE";
    private static final String PREF_KEY_CACHED_KEYS_RESPONSE = "CACHED_KEYS_RESPONSE";
    private static final String PREF_KEY_CACHED_APPLICATION_PROPERTIES_RESPONSE = "CACHED_APPLICATION_PROPERTIES_RESPONSE";
    private static final String PREF_KEY_DEVICE_ID = "DEVICE_ID";
    private static final String PREF_KEY_PASSWORD = "PASSWORD";
    private static final String PREF_KEY_SERVER_SIDE_USERNAME = "SERVER_SIDE_KEY";
    private static final String PREF_KEY_ANY_SMS_RECEIVED = "ANY_SMS_RECEIVED";
    private static final String PREF_KEY_ONLINE = "ONLINE";
    private static final String PREF_KEY_OPEN_SOURCE_NOTICE_SHOWN = "OPEN_SOURCE_NOTICE_SHOWN";
    private static final long SERVER_RESPONSE_FRESHNESS_PERIOD_IN_MS = CachedData.ONE_WEEK;
    private static final long SERVER_RESPONSE_LONGER_FRESHNESS_PERIOD_IN_MS = 6 * CachedData.ONE_WEEK;
    private static final long SERVER_RESPONSE_VALIDITY_PERIOD_IN_MS = 2 * 52 * CachedData.ONE_WEEK;
    private static final long SERVER_RESPONSE_REFRESHING_TRY_INTERVAL_IN_MS = CachedData.ONE_DAY;

    private static final int DEFAULT_PASSWORD_LENGTH = 16;

    public UserData(Context context) {
        super(context);
        LogStoreHelper.info(context, "UserData constructor - hash: " + toString() + " - context: " + context);
    }

    public MessagingConfiguration getMessagingConfiguration() {
        ComplexPreferences complexPreferences = getComplexPreferences();
        MessagingConfiguration messagingConfiguration = null;
        try {
            messagingConfiguration = complexPreferences.getObject(PREF_KEY_MESSAGING_CONFIGURATION,
                    DefaultMessagingConfiguration.class);
        } catch (RuntimeException e) {
            LogStoreHelper.warn(this, context, "Couldn't retrieve messaging configuration from shared preferences.", e);
        }
        if (messagingConfiguration == null) {
            messagingConfiguration = readOldMessagingConfiguration();
            complexPreferences.putObject(PREF_KEY_MESSAGING_CONFIGURATION, messagingConfiguration);
            complexPreferences.commit();
        }
        return wrap(messagingConfiguration);
    }

    public void setMessagingConfiguration(MessagingConfiguration messagingConfiguration) {
        ComplexPreferences complexPreferences = getComplexPreferences();
        complexPreferences.putObject(PREF_KEY_MESSAGING_CONFIGURATION, messagingConfiguration);
        complexPreferences.commit();
    }

    private MessagingConfiguration wrap(final MessagingConfiguration messagingConfiguration) {
        return new AutoSavingMessagingConfiguration(messagingConfiguration, this);
    }

    public ServiceConfiguration getServiceConfiguration() {
        ComplexPreferences complexPreferences = getComplexPreferences();
        ServiceConfiguration serviceConfiguration = null;
        try {
            serviceConfiguration = complexPreferences.getObject(PREF_KEY_SERVICE_CONFIGURATION,
                    DefaultServiceConfiguration.class);
        } catch (RuntimeException e) {
            LogStoreHelper.warn(this, context, "Couldn't retrieve service configuration from shared preferences.", e);
        }
        if (serviceConfiguration == null) {
            serviceConfiguration = readOldServiceConfiguration();
            complexPreferences.putObject(PREF_KEY_SERVICE_CONFIGURATION, serviceConfiguration);
            complexPreferences.commit();
        }
        return wrap(serviceConfiguration);
    }

    public void setServiceConfiguration(ServiceConfiguration serviceConfiguration) {
        ComplexPreferences complexPreferences = getComplexPreferences();
        complexPreferences.putObject(PREF_KEY_SERVICE_CONFIGURATION, serviceConfiguration);
        complexPreferences.commit();
    }

    private synchronized ServiceConfiguration wrap(final ServiceConfiguration serviceConfiguration) {
        return new AutoSavingServiceConfiguration(serviceConfiguration, this);
    }

    public ApplicationPropertiesResponse getCachedApplicationPropertiesResponse() {
        ComplexPreferences complexPreferences = getComplexPreferences();
        ApplicationPropertiesResponse cachedApplicationPropertiesResponse = null;
        try {
            cachedApplicationPropertiesResponse = complexPreferences.getObject(
                    PREF_KEY_CACHED_APPLICATION_PROPERTIES_RESPONSE, ApplicationPropertiesResponse.class);
        } catch (RuntimeException e) {
            LogStoreHelper.warn(this, context,
                    "Couldn't retrieve application properties cached response from shared preferences.", e);
        }
        if (cachedApplicationPropertiesResponse == null) {
            cachedApplicationPropertiesResponse = new ApplicationPropertiesResponse(
                    SERVER_RESPONSE_LONGER_FRESHNESS_PERIOD_IN_MS, SERVER_RESPONSE_VALIDITY_PERIOD_IN_MS,
                    SERVER_RESPONSE_REFRESHING_TRY_INTERVAL_IN_MS);
            complexPreferences.putObject(PREF_KEY_CACHED_APPLICATION_PROPERTIES_RESPONSE,
                    cachedApplicationPropertiesResponse);
            complexPreferences.commit();
        }
        return cachedApplicationPropertiesResponse;
    }

    public void setCachedApplicationPropertiesResponse(ApplicationPropertiesResponse cachedApplicationPropertiesResponse) {
        ComplexPreferences complexPreferences = getComplexPreferences();
        complexPreferences.putObject(PREF_KEY_CACHED_APPLICATION_PROPERTIES_RESPONSE,
                cachedApplicationPropertiesResponse);
        complexPreferences.commit();
    }

    public KeysResponse getCachedKeysResponse() {
        ComplexPreferences complexPreferences = getComplexPreferences();
        KeysResponse cachedKeysResponse = null;
        try {
            cachedKeysResponse = complexPreferences.getObject(PREF_KEY_CACHED_KEYS_RESPONSE, KeysResponse.class);
        } catch (RuntimeException e) {
            LogStoreHelper.warn(this, context, "Couldn't retrieve keys cached response from shared preferences.", e);
        }
        if (cachedKeysResponse == null) {
            cachedKeysResponse = new KeysResponse(SERVER_RESPONSE_FRESHNESS_PERIOD_IN_MS,
                    SERVER_RESPONSE_VALIDITY_PERIOD_IN_MS, SERVER_RESPONSE_REFRESHING_TRY_INTERVAL_IN_MS);
            complexPreferences.putObject(PREF_KEY_CACHED_KEYS_RESPONSE, cachedKeysResponse);
            complexPreferences.commit();
        }
        return cachedKeysResponse;
    }

    public void setCachedKeysResponse(KeysResponse cachedKeysResponse) {
        ComplexPreferences complexPreferences = getComplexPreferences();
        complexPreferences.putObject(PREF_KEY_CACHED_KEYS_RESPONSE, cachedKeysResponse);
        complexPreferences.commit();
    }

    public VerificationResponse getCachedVerificationResponse() {
        ComplexPreferences complexPreferences = getComplexPreferences();
        VerificationResponse cachedVerificationResponse = null;
        try {
            cachedVerificationResponse = complexPreferences.getObject(PREF_KEY_CACHED_VERIFICATION_RESPONSE,
                    VerificationResponse.class);
        } catch (RuntimeException e) {
            LogStoreHelper.warn(this, context,
                    "Couldn't retrieve verification cached response from shared preferences.", e);
        }
        if (cachedVerificationResponse == null) {
            cachedVerificationResponse = new VerificationResponse(SERVER_RESPONSE_FRESHNESS_PERIOD_IN_MS,
                    SERVER_RESPONSE_VALIDITY_PERIOD_IN_MS, SERVER_RESPONSE_REFRESHING_TRY_INTERVAL_IN_MS);
            complexPreferences.putObject(PREF_KEY_CACHED_VERIFICATION_RESPONSE, cachedVerificationResponse);
            complexPreferences.commit();
        }
        return cachedVerificationResponse;
    }

    public void setCachedVerificationResponse(VerificationResponse cachedVerificationResponse) {
        ComplexPreferences complexPreferences = getComplexPreferences();
        complexPreferences.putObject(PREF_KEY_CACHED_VERIFICATION_RESPONSE, cachedVerificationResponse);
        complexPreferences.commit();
    }

    public boolean isSendingMessagesNow() {
        return getBoolean(PREF_KEY_SENDING_MESSAGES_NOW);
    }

    public void setSendingMessagesNow(boolean sendintMessagesNow) {
        setBoolean(PREF_KEY_SENDING_MESSAGES_NOW, sendintMessagesNow);
    }

    public boolean isOpenSourceNoticeShown() {
        return getBoolean(PREF_KEY_OPEN_SOURCE_NOTICE_SHOWN);
    }

    public void setOpenSourceNoticeShown(boolean openSourceNoticeShown) {
        setBoolean(PREF_KEY_OPEN_SOURCE_NOTICE_SHOWN, openSourceNoticeShown);
    }

    public HashMap<String, String> getContacts() {
        return getHashMap(PREF_KEY_CONTACTS);
    }

    public void setContacts(HashMap<String, String> contacts) {
        setHashMap(PREF_KEY_CONTACTS, contacts);
    }

    public Date getLastTimeContactsRead() {
        long lastContactsRead = getLong(PREF_KEY_LAST_TIME_CONTACTS_READ);
        if (lastContactsRead > 0)
            return new Date(lastContactsRead);
        return null;
    }

    public void setLastTimeContactsRead(Date date) {
        setLong(PREF_KEY_LAST_TIME_CONTACTS_READ, date.getTime());
    }

    public Date getLastSmsTimestamp() {
        long lastSmsTimestamp = getLong(PREF_KEY_LAST_SMS_SEEN_ON);
        if (lastSmsTimestamp > 0)
            return new Date(lastSmsTimestamp);
        return null;
    }

    public void setLastSmsTimestamp(Date date) {
        setLong(PREF_KEY_LAST_SMS_SEEN_ON, date.getTime());
    }

    private ComplexPreferences getComplexPreferences() {
        return ComplexPreferences.getComplexPreferences(context, context.getPackageName() + "_preferences",
                Context.MODE_PRIVATE);
    }

    public boolean haveValidKeys() {
        try {
            return getKeys(true).isSuccess();
        } catch (OperationFailedException e) {
            return false;
        }
    }

    public KeysResponse getKeys() throws OperationFailedException {
        return getKeys(false);
    }

    public KeysResponse getKeys(boolean checkOnly) throws OperationFailedException {
        KeysResponse cachedKeysResponse = getCachedKeysResponse();
        if (checkOnly)
            return cachedKeysResponse;
        cachedKeysResponse = getServerSideClient().getKeys(cachedKeysResponse);
        setCachedKeysResponse(cachedKeysResponse);
        if (cachedKeysResponse.isValid())
            return cachedKeysResponse;
        throw new OperationFailedException("Keys are not valid and couldn't be updated, result: "
                + cachedKeysResponse.getResult());
    }

    public String getGoogleApiKey() throws OperationFailedException {
        return getKeys().googleApiKey;
    }

    public String getGoogleApiSecret() throws OperationFailedException {
        return getKeys().googleApiSecret;
    }

    public String getGoogleCallbackUrl() throws OperationFailedException {
        return getKeys().googleCallbackUrl;
    }

    public ApplicationPropertiesResponse getApplicationProperties() throws OperationFailedException {
        return getApplicationProperties(false);
    }

    public ApplicationPropertiesResponse getApplicationProperties(boolean checkOnly) throws OperationFailedException {
        ApplicationPropertiesResponse cachedApplicationProperties = getCachedApplicationPropertiesResponse();
        if (checkOnly)
            return cachedApplicationProperties;
        cachedApplicationProperties = getServerSideClient().getApplicationProperties(cachedApplicationProperties);
        setCachedApplicationPropertiesResponse(cachedApplicationProperties);
        if (cachedApplicationProperties.isValid())
            return cachedApplicationProperties;
        throw new OperationFailedException("Couldn't get application properties from server.");
    }

    public String getDeviceId() {
        String devideId = prefs.getString(PREF_KEY_DEVICE_ID, null);
        if (devideId == null) {
            synchronized (UserData.class) {
                devideId = prefs.getString(PREF_KEY_DEVICE_ID, null);
                if (devideId == null) {
                    // TODO: Later: Generate a device ID for GCM.
                    devideId = Long.toString(NumberUtil.getRandomNumber());
                    setString(PREF_KEY_DEVICE_ID, devideId);
                }
            }
        }
        return devideId;
    }

    public String getPassword() {
        String password = prefs.getString(PREF_KEY_PASSWORD, null);
        if (password == null) {
            synchronized (UserData.class) {
                password = prefs.getString(PREF_KEY_PASSWORD, null);
                if (password == null) {
                    password = UUID.randomUUID().toString().substring(0, DEFAULT_PASSWORD_LENGTH);
                    setString(PREF_KEY_PASSWORD, password);
                }
            }
        }
        return password;
    }

    public String getUsername() {
        return getString(PREF_KEY_SERVER_SIDE_USERNAME);
    }

    public void setUsername(String username) {
        setString(PREF_KEY_SERVER_SIDE_USERNAME, username);
    }

    public void clearUsername() {
        clearValue(PREF_KEY_SERVER_SIDE_USERNAME);
    }

    public boolean isRegistered() {
        return !StringUtil.empty(getUsername());
    }

    public RegistrationResponse registerUser() throws OperationFailedException {
        return getServerSideClient().registerUser();
    }

    public boolean isUserVerified() {
        try {
            return verifyUser(true).isSuccess();
        } catch (OperationFailedException e) {
            return false;
        }
    }

    public VerificationResponse verifyUser() throws OperationFailedException {
        return verifyUser(false);
    }

    public VerificationResponse verifyUser(boolean checkOnly) throws OperationFailedException {
        VerificationResponse cachedVerificationResponse = getCachedVerificationResponse();
        if (checkOnly)
            return cachedVerificationResponse;
        cachedVerificationResponse = getServerSideClient().verifyUser(cachedVerificationResponse);
        setCachedVerificationResponse(cachedVerificationResponse);
        if (cachedVerificationResponse.isUnauthorized()) {
            LogStoreHelper.info(context, "The user is unauthorized, clearing the username...");
            clearUsername();
            cachedVerificationResponse.invalidate();
        }
        if (cachedVerificationResponse.isValid())
            return cachedVerificationResponse;
        throw new OperationFailedException("Couldn't verify the user: " + cachedVerificationResponse.getErrorMessage());
    }

    public boolean isInSyncWithServer() {
        return isRegistered() && isUserVerified() && haveValidKeys();
    }

    public ServerSideClient getServerSideClient() {
        return DefaultServerSideClient.get(context, getDeviceId(), getUsername(), getPassword());
    }

    public void checkMessagesAndSetFlags() {
        if (!isAnySmsReceived()) {
            if (MessageStoreHelper.getNumberOfTextsReceived(context) > 0)
                setAnySmsReceived(true);
            context.sendBroadcast(new Intent(Constants.STATUS_UPDATE_BROADCAST_ACTION).putExtra(
                    Constants.STATUS_UPDATE_EXTRA_WHAT, Constants.EVENT_CONFIGURATION_CHANGED));
        }
    }

    public boolean isAnySmsReceived() {
        return prefs.getBoolean(PREF_KEY_ANY_SMS_RECEIVED, false);
    }

    public void setAnySmsReceived(boolean value) {
        final Editor edit = prefs.edit();
        edit.putBoolean(PREF_KEY_ANY_SMS_RECEIVED, value);
        edit.commit();
    }

    public boolean isOnlineStatusAvailable() {
        return prefs.contains(PREF_KEY_ONLINE);
    }

    public boolean isOnline() {
        return prefs.getBoolean(PREF_KEY_ONLINE, false);
    }

    public void setOnline(boolean value) {
        final Editor edit = prefs.edit();
        edit.putBoolean(PREF_KEY_ONLINE, value);
        edit.commit();
    }

    public MessagingConfiguration readOldMessagingConfiguration() {
        final String PREF_KEY_OAUTH_ACCESS_TOKEN = "OAUTH_TOKEN";
        final String PREF_KEY_OAUTH_ACCESS_TOKEN_SECRET = "OAUTH_TOKEN_SECRET";
        final String PREF_KEY_OAUTH_EMAIL_ADDRESS = "OAUTH_EMAIL_ADDRESS";
        final String PREF_KEY_TARGET_EMAIL_ADDRESS = "TARGET_EMAIL_ADDRESS";
        final String PREF_KEY_MAILBOX_NAME = "MAILBOX_NAME";
        final String PREF_KEY_ALL_MAILBOXES = "ALL_MAILBOXES";
        final String PREF_KEY_LAST_EMAIL_CHECK = "LAST_EMAIL_CHECK";
        String targetEmailAddress = prefs.getString(PREF_KEY_TARGET_EMAIL_ADDRESS, null);
        DefaultMessagingConfiguration messagingConfiguration = new DefaultMessagingConfiguration();
        messagingConfiguration.setTargetEmailAddress(targetEmailAddress);
        String oauthAccessToken = prefs.getString(PREF_KEY_OAUTH_ACCESS_TOKEN, null);
        String oauthTokenSecret = prefs.getString(PREF_KEY_OAUTH_ACCESS_TOKEN_SECRET, null);
        if (oauthAccessToken != null && oauthTokenSecret != null) {
            Token oauthToken = new Token(oauthAccessToken, oauthTokenSecret, new Date(), null);
            String oauthEmailAddress = prefs.getString(PREF_KEY_OAUTH_EMAIL_ADDRESS, null);
            Date lastCheck = getDate(PREF_KEY_LAST_EMAIL_CHECK, null);
            ArrayList<String> allMailboxes = getArrayList(PREF_KEY_ALL_MAILBOXES);
            String replyMailboxName = getString(PREF_KEY_MAILBOX_NAME);
            GatewayType gatewayType = GatewayType.GMAIL;
            messagingConfiguration.setGatewayType(gatewayType).setUsingDeprecatedOauthMethod(true)
                    .setOauthAccessToken(oauthToken).setOauthEmailAddress(oauthEmailAddress).setLastCheck(lastCheck)
                    .setAllMailboxes(new HashSet<String>(allMailboxes)).setReplyMailboxName(replyMailboxName);
        }
        return messagingConfiguration;
    }

    public ServiceConfiguration readOldServiceConfiguration() {
        final String PREF_KEY_FORWARDING_SWITCH = "GLOBAL_SWITCH";
        final String PREF_KEY_REPLYING_SWITCH = "REPLY_SWITCH";
        final String PREF_KEY_REPLYING_STATUS_SWITCH = "REPLY_STATUS_SWITCH";
        final String PREF_KEY_ACTIVATION_STATUS = "ACTIVATION_STATUS";
        final String PREF_KEY_SMS_TRIGGER_CODE = "ACTIVATION_SMS_CODE";
        final String PREF_KEY_SCHEDULE_BEGIN_TIME = "SCHEDULE_START_TIME";
        final String PREF_KEY_SCHEDULE_END_TIME = "SCHEDULE_END_TIME";

        ServiceStatus serviceStatus = ServiceStatus.DISABLED;
        if (prefs.contains(PREF_KEY_ACTIVATION_STATUS)) {
            ServiceStatus savedServiceStatus = ServiceStatus.fromString(prefs.getString(PREF_KEY_ACTIVATION_STATUS,
                    ServiceStatus.ENABLED.toString()));
            if (savedServiceStatus != null)
                serviceStatus = savedServiceStatus;
        } else {
            boolean forwardingSwtich = prefs.getBoolean(PREF_KEY_FORWARDING_SWITCH, true);
            serviceStatus = forwardingSwtich ? ServiceStatus.ENABLED : ServiceStatus.DISABLED;
        }
        String activationCode = null;
        if (prefs.contains(PREF_KEY_SMS_TRIGGER_CODE)) {
            activationCode = prefs.getString(PREF_KEY_SMS_TRIGGER_CODE, null);
        } else {
            StringBuilder code = new StringBuilder();
            for (int i = 0; i < 4; i++)
                code.append(new Random().nextInt(9) + 1);
            activationCode = context.getString(R.string.str_sms_activation_code, code.toString());
        }
        boolean replyingEnabled = prefs.getBoolean(PREF_KEY_REPLYING_SWITCH, true);
        boolean replyingStatusReportEnabled = prefs.getBoolean(PREF_KEY_REPLYING_STATUS_SWITCH, false);
        int scheduleBeginTime = getInt(PREF_KEY_SCHEDULE_BEGIN_TIME);
        int scheduleEndTime = getInt(PREF_KEY_SCHEDULE_END_TIME);
        return new DefaultServiceConfiguration().setServiceStatus(serviceStatus).setReplyingEnabled(replyingEnabled)
                .setReplyingStatusReportEnabled(replyingStatusReportEnabled).setActivationCode(activationCode)
                .setScheduleBeginTime(scheduleBeginTime).setScheduleEndTime(scheduleEndTime);
    }
}
