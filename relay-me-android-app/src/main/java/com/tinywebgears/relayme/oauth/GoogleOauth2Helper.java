package com.tinywebgears.relayme.oauth;

import org.json.JSONException;
import org.json.JSONObject;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.Google2Api;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import android.util.Log;

import com.codolutions.android.common.exception.OperationFailedException;
import com.codolutions.android.common.util.Pair;
import com.tinywebgears.relayme.app.UserData;

public class GoogleOauth2Helper implements OauthHelper
{
    private static final String TAG = GoogleOauth2Helper.class.getName();

    public static final String OAUTH_SCOPE_PROFILE = "https://www.googleapis.com/auth/userinfo.profile";
    public static final String OAUTH_SCOPE_EMAIL = "https://www.googleapis.com/auth/userinfo.email";
    public static final String OAUTH_SCOPE_GMAIL = "https://mail.google.com/";
    public static final String URL_OAUTH_AUTHORIZE = "https://www.google.com/accounts/OAuthAuthorizeToken?oauth_token=";
    public static final String OAUTH_PARAM_CODE = "code";

    private static final int TOKEN_EXPIRY_SAFETY_GAP_IN_MS = 120 * 1000;
    // The string format for XOAUTH token has special characters in it, careful with copy-pasting.
    // See https://developers.google.com/gmail/xoauth2_protocol for more info.
    private static final String XOAUTH_FORMAT = "user=%sauth=Bearer %s";
    private static final String URI_GET_EMAIL = "https://www.googleapis.com/oauth2/v1/userinfo?alt=json";

    private OAuthService service;

    public static GoogleOauth2Helper get(UserData userData) throws OperationFailedException
    {
        if (!userData.haveValidKeys())
            throw new OperationFailedException("Don't have valid Google API keys. UserData: " + userData);
        Log.i(TAG, "API Key: " + userData.getGoogleApiKey() + " Secret: " + userData.getGoogleApiSecret());
        try
        {
            return new GoogleOauth2Helper(userData.getGoogleApiKey(), userData.getGoogleApiSecret(),
                    OAUTH_SCOPE_PROFILE + " " + OAUTH_SCOPE_EMAIL + " " + OAUTH_SCOPE_GMAIL,
                    userData.getGoogleCallbackUrl());
        }
        catch (RuntimeException e)
        {
            throw new OperationFailedException("Error creating OAuth helper.", e);
        }
    }

    private GoogleOauth2Helper(String appKey, String appSecret, String scope, String callbackUrl)
    {
        service = new ServiceBuilder().provider(Google2Api.class).apiKey(appKey).apiSecret(appSecret).scope(scope)
                .callback(callbackUrl).offline(true).build();
    }

    private Pair<String, Token> makeSecuredRequest(Token accessToken, String uri) throws OperationFailedException
    {
        return makeSecuredRequest(accessToken, uri, false);
    }

    public Pair<String, Token> makeSecuredRequest(Token accessToken, String url, boolean forceRefresh)
            throws OperationFailedException
    {
        // TODO: Later: Catch the exception that indicate token expiration, if possible.
        final Token refreshedToken = refreshToken(accessToken, forceRefresh);
        final OAuthRequest request = new OAuthRequest(Verb.GET, url);
        signRequest(refreshedToken, request);
        Response response = request.send();
        Log.d(TAG, "Secure request executed: " + url + ", Response: " + response.getCode());
        return Pair.create(response.getBody(), refreshedToken);
    }

    private Token refreshToken(final Token accessToken, boolean forceRefresh) throws OperationFailedException
    {
        if (forceRefresh
                || (accessToken.getExpiry() != null && (accessToken.getExpiry().getTime() - System.currentTimeMillis() < TOKEN_EXPIRY_SAFETY_GAP_IN_MS)))
        {
            // Refreshing access token first
            Log.d(TAG, "Google access token needs to be refreshed, expired/expiring at: " + accessToken.getExpiry());
            try
            {
                Token result = service.getAccessToken(accessToken, null);
                Log.d(TAG, "Google access token refreshed.");
                return new Token(result.getToken(), accessToken.getSecret(), result.getExpiry(),
                        result.getRawResponse());
            }
            catch (Exception e)
            {
                throw new OperationFailedException("Error getting access token", e);
            }
        }
        return accessToken;
    }

    private void signRequest(Token accessToken, OAuthRequest request) throws OperationFailedException
    {
        try
        {
            service.signRequest(accessToken, request);
        }
        catch (Exception e)
        {
            throw new OperationFailedException("Error signing request.", e);
        }
    }

    @Override
    public Pair<String, Token> findEmailAddress(Token accessToken) throws OperationFailedException
    {
        try
        {
            Pair<String, Token> result = makeSecuredRequest(accessToken, URI_GET_EMAIL);
            String jsonOutput = result.first;
            JSONObject jsonResponse = new JSONObject(jsonOutput);
            String email = jsonResponse.getString("email");
            Log.d(TAG, "Email address found: " + email);
            Token refreshedToken = result.second;
            return Pair.create(email, refreshedToken);
        }
        catch (JSONException e)
        {
            throw new OperationFailedException("Invalid response to user details API: " + e.getMessage(), e);
        }
    }

    @Override
    public Pair<String, Token> buildXOAuthImap(String emailAddress, Token accessToken) throws OperationFailedException
    {
        return buildXOAuth(emailAddress, accessToken);
    }

    @Override
    public Pair<String, Token> buildXOAuthSmtp(String emailAddress, Token accessToken) throws OperationFailedException
    {
        return buildXOAuth(emailAddress, accessToken);
    }

    private Pair<String, Token> buildXOAuth(String emailAddress, Token accessToken) throws OperationFailedException
    {
        final Token refreshedToken = refreshToken(accessToken, false);
        String xoauthString = String.format(XOAUTH_FORMAT, emailAddress, refreshedToken.getToken());
        // Log.v(TAG, "xoauth string " + xoauthString);
        return Pair.create(xoauthString, refreshedToken);
    }
}
