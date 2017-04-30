package com.tinywebgears.relayme.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.scribe.model.Token;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.codolutions.android.common.exception.OperationFailedException;
import com.codolutions.android.common.util.Pair;
import com.tinywebgears.relayme.app.CustomApplication;
import com.tinywebgears.relayme.app.SystemStatus;
import com.tinywebgears.relayme.app.UserData;
import com.tinywebgears.relayme.oauth.GoogleOauth2Helper;
import com.tinywebgears.relayme.serverside.DefaultServerSideClient;

public class GoogleIntentService extends BasicIntentService
{
    private static final String TAG = GoogleIntentService.class.getName();
    public static final String ACTION_CODE_GET_ACCESS_TOKEN = "get-access-token";
    public static final String ACTION_RESPONSE_GET_ACCESS_TOKEN = GoogleIntentService.class.getCanonicalName()
            + ".RESPONSE_ACCESS_TOKEN";
    public static final Map<String, String> ACTIONS = new HashMap<String, String>()
    {
        {
            put(ACTION_CODE_GET_ACCESS_TOKEN, ACTION_RESPONSE_GET_ACCESS_TOKEN);
        }
    };

    public static final String PARAM_IN_VERIFIER_URI = "verifier-uri";
    public static final String PARAM_OUT_RESULT = "out-result";
    public static final String PARAM_OUT_AUTHORIZATION_URI = "authorization-uri";

    protected SystemStatus systemStatus;
    protected UserData userData;

    public GoogleIntentService()
    {
        super("GoogleIntentService");
    }

    @Override
    protected ActionResponse handleAction(String action, long actionId, Intent intent)
    {
        LogStoreHelper.info(this, "A new intent received by GoogleIntentService: " + action);
        systemStatus = ((CustomApplication) getApplication()).getSystemStatus();
        userData = ((CustomApplication) getApplication()).getUserData();

        if (ACTION_CODE_GET_ACCESS_TOKEN.equalsIgnoreCase(action))
        {
            LogStoreHelper.info(this, "Getting access token...");
            String uri = intent.getStringExtra(PARAM_IN_VERIFIER_URI);
            try
            {
                getAccessToken(uri);
                return ActionResponse.SUCCESS;
            }
            catch (Exception e)
            {
                try
                {
                    LogStoreHelper.info(this, "Getting access token failed, retrying...");
                    getAccessToken(uri);
                    return ActionResponse.SUCCESS;
                }
                catch (Exception ee)
                {
                    StringBuilder errorMessage = new StringBuilder();
                    errorMessage.append(ee.getMessage());
                    if (ee.getCause() != null)
                        errorMessage.append("\n").append(ee.getCause().getMessage());
                    LogStoreHelper.warn(this, "Unable to get an OAuth access token: " + errorMessage.toString(), e);
                    return ActionResponse.FAILURE;
                }
            }
        }
        return ActionResponse.DROP;
    }

    @Override
    protected String getResponseAction(String requestAction)
    {
        return ACTIONS.get(requestAction);
    }

    private void getAccessToken(String uri) throws OperationFailedException
    {
        Pair<String, String> credentials = userData.getServerSideClient().getCredentials(Uri.parse(uri),
                DefaultServerSideClient.URI_PARAM_VALUE_PROVIDER_GOOGLE);
        if (credentials == null)
        {
            LogStoreHelper.info(this, "Couldn't retrieve access token.");
            return;
        }
        Token accessToken = new Token(credentials.first, credentials.second, new Date(), null);
        LogStoreHelper.info(this, "Access token retrieved successfully.");
        userData.getMessagingConfiguration().setUsingDeprecatedOauthMethod(false);
        userData.getMessagingConfiguration().setOauthAccessToken(accessToken);
        Pair<String, Token> result = GoogleOauth2Helper.get(userData).findEmailAddress(accessToken);
        userData.getMessagingConfiguration().setOauthAccessToken(result.second);
        Log.d(TAG, "Email: " + result.first);
        userData.getMessagingConfiguration().setOauthEmailAddress(result.first);
    }
}
