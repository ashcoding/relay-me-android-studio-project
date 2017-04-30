package com.tinywebgears.relayme.service;

import java.util.HashMap;
import java.util.Map;

import android.content.Intent;
import android.util.Log;

import com.codolutions.android.common.exception.OperationFailedException;
import com.codolutions.android.common.serverside.data.ApplicationPropertiesResponse;
import com.codolutions.android.common.serverside.data.KeysResponse;
import com.codolutions.android.common.serverside.data.RegistrationResponse;
import com.codolutions.android.common.serverside.data.VerificationResponse;
import com.tinywebgears.relayme.app.CustomApplication;
import com.tinywebgears.relayme.app.UserData;

public class ServerSideIntentService extends BasicIntentService
{
    private static final String TAG = ServerSideIntentService.class.getName();

    public static final String ACTION_CODE_SYNC = "sync";
    public static final String ACTION_CODE_CHECK_UPDATES = "check-updates";
    public static final String ACTION_RESPONSE_SYNC = ServerSideIntentService.class.getName() + ":RESPONSE_SYNC";
    public static final String ACTION_RESPONSE_CHECK_UPDATES = ServerSideIntentService.class.getName()
            + ":RESPONSE_CHECK_UPDATES";
    public static final Map<String, String> ACTIONS = new HashMap<String, String>()
    {
        {
            put(ACTION_CODE_SYNC, ACTION_RESPONSE_SYNC);
            put(ACTION_CODE_CHECK_UPDATES, ACTION_RESPONSE_CHECK_UPDATES);
        }
    };

    public static final String PARAM_UPDATE_REQUIRED = "update-required";
    public static final String PARAM_UPDATE_VERSION_NAME = "update-version-name";

    public ServerSideIntentService()
    {
        super(ServerSideIntentService.class.getName());
    }

    @Override
    protected ActionResponse handleAction(String action, long actionId, Intent intent)
    {
        try
        {
            LogStoreHelper.info(this, "A new intent received by ServerSideIntentService:" + action);
            if (!((CustomApplication) getApplication()).getSystemStatus().isOnline())
                return ActionResponse.DROP;
            if (ACTION_CODE_SYNC.equalsIgnoreCase(action))
            {
                Log.d(TAG, "Syncing with server...");
                UserData userData = ((CustomApplication) getApplication()).getUserData();
                if (userData.isInSyncWithServer())
                {
                    LogStoreHelper.info(this, "Syncing with server, user data: " + userData);
                    // Try to refresh responses before they become invalid.
                    try
                    {
                        VerificationResponse verificationResponse = userData.verifyUser();
                        LogStoreHelper.info(this, "User verified: " + verificationResponse);
                    }
                    catch (OperationFailedException e)
                    {
                        LogStoreHelper.warn(this, "Couldn't verify the user in the sync process.", e);
                    }
                    try
                    {
                        KeysResponse keysResponse = userData.getKeys();
                        LogStoreHelper.info(this, "Updated keys: " + keysResponse);
                    }
                    catch (OperationFailedException e)
                    {
                        LogStoreHelper.warn(this, "Couldn't get keys in the sync process.", e);
                    }
                    return ActionResponse.SUCCESS;
                }
                reportOperationProgress(ServerSideIntentService.ACTION_CODE_SYNC, actionId, 1, 0);
                if (!userData.isRegistered())
                {
                    LogStoreHelper.info(this, "Registering the user...");
                    RegistrationResponse response = userData.registerUser();
                    if (!response.isSuccess())
                        return ActionResponse.FAILURE;
                    userData.setUsername(response.getUsername());
                }
                if (!userData.isUserVerified())
                {
                    LogStoreHelper.info(this, "Verifying the user...");
                    VerificationResponse response = userData.verifyUser();
                    if (response.isUnauthorized())
                    {
                        userData.clearUsername();
                        return ActionResponse.FAILURE;
                    }
                    if (!userData.verifyUser().isSuccess())
                        return ActionResponse.FAILURE;
                }
                if (!userData.haveValidKeys())
                {
                    LogStoreHelper.info(this, "Getting keys...");
                    KeysResponse keysResponse = userData.getKeys();
                    if (!keysResponse.isSuccess())
                        return ActionResponse.FAILURE;
                }
                return ActionResponse.SUCCESS;
            }
            else if (ACTION_CODE_CHECK_UPDATES.equalsIgnoreCase(action))
            {
                LogStoreHelper.info(this, "Checking updates...");
                UserData userData = ((CustomApplication) getApplication()).getUserData();
                ApplicationPropertiesResponse applicationPropertiesResponse = userData.getApplicationProperties();
                if (!applicationPropertiesResponse.isValid())
                    return ActionResponse.FAILURE;
                int lastSupportedVersion = applicationPropertiesResponse.getLastSupportedVersion();
                int latestKnownVersion = applicationPropertiesResponse.getLatestKnownVersion();
                String latestVersionName = applicationPropertiesResponse.getLatestVersionName();
                int currentVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
                if (currentVersion >= latestKnownVersion)
                    return ActionResponse.SUCCESS;
                boolean updateRequired = currentVersion < lastSupportedVersion;
                class UpdateCheckerExtrasAdder implements ExtrasAdder
                {
                    private final boolean updateRequired;
                    private final String versionName;

                    public UpdateCheckerExtrasAdder(boolean updateRequired, String versionName)
                    {
                        this.updateRequired = updateRequired;
                        this.versionName = versionName;
                    }

                    @Override
                    public void addExtras(Intent intent)
                    {
                        intent.putExtra(PARAM_UPDATE_REQUIRED, updateRequired);
                        intent.putExtra(PARAM_UPDATE_VERSION_NAME, versionName);
                    }
                }
                return ActionResponse.SUCCESS_WITH_EXTRAS(new UpdateCheckerExtrasAdder(updateRequired,
                        latestVersionName));
            }
            return ActionResponse.DROP;
        }
        catch (Throwable e)
        {
            Log.w(TAG, "Error processing action " + action, e);
            return ActionResponse.FAILURE;
        }
    }

    @Override
    protected String getResponseAction(String requestAction)
    {
        return ACTIONS.get(requestAction);
    }
}
