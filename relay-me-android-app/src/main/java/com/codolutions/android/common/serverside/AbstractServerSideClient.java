package com.codolutions.android.common.serverside;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.util.Base64;
import android.util.Log;

import com.codolutions.android.common.data.CachedData;
import com.codolutions.android.common.exception.OperationFailedException;
import com.codolutions.android.common.serverside.data.ApplicationPropertiesResponse;
import com.codolutions.android.common.serverside.data.KeysResponse;
import com.codolutions.android.common.serverside.data.RegistrationResponse;
import com.codolutions.android.common.serverside.data.VerificationResponse;
import com.codolutions.android.common.util.ExceptionUtil;
import com.codolutions.android.common.util.Pair;
import com.codolutions.android.common.util.StringUtil;
import com.tinywebgears.relayme.BuildConfig;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertPathValidatorException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public abstract class AbstractServerSideClient implements ServerSideClient
{
    private static final String TAG = AbstractServerSideClient.class.getName();

    public static final String URI_PARAM_VALUE_PROVIDER_GOOGLE = "google";
    public static final String URI_PARAM_NAME_PROVIDER = "provider";
    public static final String URI_PARAM_NAME_GOOGLE_ACCESS_TOKEN = "accessToken";
    public static final String URI_PARAM_NAME_GOOGLE_REFRESH_TOKEN = "refreshToken";
    private static final String URI_PARAM_VALUE_DEVICE_TYPE = "android";
    private static final String SERVER_TOKEN_FORMAT = "%s:%s";
    private static final int LEAST_VERSION_TO_SUPPORT_SNI = 11;

    protected static String createAuthorizationHeader(String username, String password)
    {
        String credentials = username + ":" + password;
        String base64Credentials = new String(Base64.encode(credentials.getBytes(), Base64.NO_WRAP));
        return "Basic " + base64Credentials;
    }

    protected Context context;
    protected String deviceId;
    protected String username;
    protected String password;

    protected AbstractServerSideClient()
    {
        if (Build.VERSION.SDK_INT < LEAST_VERSION_TO_SUPPORT_SNI)
        {
            Log.w(TAG, "Going to ignore SSL certificate errors, as this platform doesn't support SNI.");
            final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager()
            {
                public java.security.cert.X509Certificate[] getAcceptedIssuers()
                {
                    return null;
                }

                public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType)
                {
                }

                public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType)
                {
                }
            }};
            // Install the all-trusting trust manager
            try
            {
                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            }
            catch (Exception e)
            {
                Log.w(TAG, "Couldn't install custom trust manager. We might not be able to connect to server.");
            }
        }
    }

    protected abstract Environment getEnvironment();

    private String getEndpointRegister()
    {
        return getEnvironment().getServerBaseUrl() + "/api/register";
    }

    private String getEndpointVerify()
    {
        return getEnvironment().getServerBaseUrl() + "/api/user";
    }

    private String getEndpointKeys()
    {
        return getEnvironment().getServerBaseUrl() + "/api/keys";
    }

    private String getEndpointApplicationProperties()
    {
        return getEnvironment().getServerBaseUrl() + "/api/applicationproperties";
    }

    private String getGoogleOauthUrlFormat()
    {
        return getEnvironment().getServerBaseUrl() + "/auth/google/login?appname=%s&debug=%s&authorization=Basic%%20%s";
    }

    @Override
    public Pair<String, String> getCredentials(Uri uri, String waitingFor)
    {
        if (uri == null)
        {
            Log.w(TAG, "Cannot process null URI.");
            return null;
        }
        String provider = uri.getQueryParameter(URI_PARAM_NAME_PROVIDER);
        if (StringUtil.empty(provider))
        {
            Log.w(TAG, "No provider found in URI: " + uri);
            return null;
        }
        if (!provider.equals(waitingFor))
        {
            Log.w(TAG, "We weren't waiting for a " + provider + " credentials.");
            return null;
        }
        else if (provider.equals(URI_PARAM_VALUE_PROVIDER_GOOGLE))
        {
            String accessToken = uri.getQueryParameter(URI_PARAM_NAME_GOOGLE_ACCESS_TOKEN);
            String refreshToken = uri.getQueryParameter(URI_PARAM_NAME_GOOGLE_REFRESH_TOKEN);
            if (StringUtil.empty(accessToken) || StringUtil.empty(refreshToken))
            {
                Log.w(TAG, "Google access token or refresh token not found in URI: " + uri);
                return null;
            }
            return new Pair<String, String>(accessToken, refreshToken);
        }
        else
        {
            Log.w(TAG, "Invalid provider in URI: " + uri);
            return null;
        }
    }

    @Override
    public RegistrationResponse registerUser()
    {
        long now = System.currentTimeMillis();
        String deviceType = URI_PARAM_VALUE_DEVICE_TYPE;
        String deviceSubType = Build.VERSION.CODENAME;
        String deviceVersion = Build.VERSION.RELEASE;
        String appVersion;
        try
        {
            appVersion = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        }
        catch (NameNotFoundException e1)
        {
            appVersion = "UNKNOWN";
        }
        String jsonData = "{\"deviceType\": \"" + deviceType + "\", \"deviceSubType\": \"" + deviceSubType +
                "\", \"deviceVersion\": \"" + deviceVersion + "\", \"plainPassword\": \"" + password +
                "\", \"deviceId\": \"" + deviceId + "\", \"devMode\": \"" + BuildConfig.DEBUG +
                "\", \"appVersion\": \"" + appVersion + "\"}";
        try
        {
            Pair<Integer, String> result = makeHttpPostRequest(getEndpointRegister(), null, jsonData);
            int code = result.first;
            Log.d(TAG, "registerUser, Status code: " + code);
            if (code == HttpStatus.SC_OK)
            {
                JSONObject jsonObject = new JSONObject(result.second);
                String username = jsonObject.getString("username");
                return new RegistrationResponse().setResult(now, CachedData.Result.SUCCESS).setUsername(username)
                        .setSecret(password);
            }
            else
            {
                log("Couldn't register user: " + result.second, null, RegistrationResponse.class);
                return new RegistrationResponse().setResult(now, CachedData.Result.FAILURE, result.second);
            }
        }
        catch (Exception e)
        {
            log("Error registering user", e, RegistrationResponse.class);
            return new RegistrationResponse().setResult(now, CachedData.Result.FAILURE, e.toString());
        }
    }

    @Override
    public VerificationResponse verifyUser(VerificationResponse cachedVerificationResponse)
    {
        long now = System.currentTimeMillis();
        try
        {
            if (cachedVerificationResponse.timeToUpdate())
            {
                Pair<Integer, String> result = makeHttpGetRequest(getEndpointVerify(), getAuthorizationHeader());
                int code = result.first;
                Log.d(TAG, "verifyUser, Status code: " + code);
                if (code == HttpStatus.SC_OK)
                {
                    cachedVerificationResponse.setResult(now, CachedData.Result.SUCCESS);
                }
                else
                {
                    log("Couldn't verify user, response: " + result.second, null, VerificationResponse.class);
                    if (code == HttpStatus.SC_UNAUTHORIZED)
                        cachedVerificationResponse.setResult(now, CachedData.Result.UNAUTHORIZED);
                    else
                        cachedVerificationResponse.setResult(now, CachedData.Result.FAILURE, result.second);
                }
            }
        }
        catch (Exception e)
        {
            log("Couldn't verify user.", e, VerificationResponse.class);
            cachedVerificationResponse.setResult(now, CachedData.Result.FAILURE, e.toString());
        }
        return cachedVerificationResponse;
    }

    @Override
    public KeysResponse getKeys(KeysResponse cachedKeysResponse)
    {
        long now = System.currentTimeMillis();
        try
        {
            if (cachedKeysResponse.timeToUpdate())
            {
                Pair<Integer, String> result = makeHttpGetRequest(getEndpointKeys(), getAuthorizationHeader());
                int code = result.first;
                Log.d(TAG, "getKeys, Status code: " + code);
                if (code == HttpStatus.SC_OK)
                {
                    JSONObject jsonObject = new JSONObject(result.second);
                    if (jsonObject.has("google"))
                    {
                        JSONObject google = jsonObject.getJSONObject("google");
                        cachedKeysResponse.googleApiKey = decrypt(google.getString("key"));
                        cachedKeysResponse.googleApiSecret = decrypt(google.getString("secret"));
                        cachedKeysResponse.googleCallbackUrl = google.getString("callback");
                    }
                    cachedKeysResponse.setResult(now, CachedData.Result.SUCCESS);
                }
                else
                {
                    log("Couldn't get keys, response: " + result.second, null, KeysResponse.class);
                    if (code == HttpStatus.SC_UNAUTHORIZED)
                        cachedKeysResponse.setResult(now, CachedData.Result.UNAUTHORIZED);
                    else
                        cachedKeysResponse.setResult(now, CachedData.Result.FAILURE, result.second);
                }
            }
        }
        catch (Exception e)
        {
            log("Couldn't get keys.", e, KeysResponse.class);
            cachedKeysResponse.setResult(now, CachedData.Result.FAILURE, e.toString());
        }
        return cachedKeysResponse;
    }

    @Override
    public ApplicationPropertiesResponse getApplicationProperties(
            ApplicationPropertiesResponse cachedApplicationPropertiesResponse)
    {
        long now = System.currentTimeMillis();
        try
        {
            if (cachedApplicationPropertiesResponse.timeToUpdate())
            {
                Pair<Integer, String> result =
                        makeHttpGetRequest(getEndpointApplicationProperties(), getAuthorizationHeader());
                int code = result.first;
                Log.d(TAG, "getApplicationProperties, Status code: " + code);
                if (code == HttpStatus.SC_OK)
                {
                    JSONArray jsonArray = new JSONArray(result.second);
                    for (int i = 0; i < jsonArray.length(); i++)
                    {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        if (jsonObject.getString("key").equals("android.last.supported.version"))
                            cachedApplicationPropertiesResponse.setLastSupportedVersion(jsonObject.getInt("value"));
                        else if (jsonObject.getString("key").equals("android.latest.known.version"))
                            cachedApplicationPropertiesResponse.setLatestKnownVersion(jsonObject.getInt("value"));
                        else if (jsonObject.getString("key").equals("android.latest.version.name"))
                            cachedApplicationPropertiesResponse.setLatestVersionName(jsonObject.getString("value"));
                    }
                    cachedApplicationPropertiesResponse.setResult(now, CachedData.Result.SUCCESS);
                }
                else
                {
                    log("Couldn't get application properties, response: " + result.second, null,
                            ApplicationPropertiesResponse.class);
                    if (code == HttpStatus.SC_UNAUTHORIZED)
                        cachedApplicationPropertiesResponse.setResult(now, CachedData.Result.UNAUTHORIZED);
                    else
                        cachedApplicationPropertiesResponse.setResult(now, CachedData.Result.FAILURE, result.second);
                }
            }
        }
        catch (Exception e)
        {
            log("Couldn't get application properties.", e, ApplicationPropertiesResponse.class);
            cachedApplicationPropertiesResponse.setResult(now, CachedData.Result.FAILURE, e.toString());
        }
        return cachedApplicationPropertiesResponse;
    }

    @Override
    public String getGoogleOauthUrl(boolean debug)
    {
        return String.format(getGoogleOauthUrlFormat(), getApplicationName(), debug, getServerSideToken());
    }

    protected String getAuthorizationHeader()
    {
        return createAuthorizationHeader(username, password);
    }

    protected Pair<Integer, String> makeHttpGetRequest(String url, String authorization) throws Exception
    {
        try
        {
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Authorization", authorization);
            int responseCode = con.getResponseCode();
            String responseText = getResponseText(url, con);
            String errorText = getErrorText(url, con);
            return Pair.create(responseCode, StringUtil.nonEmpty(responseText) ? responseText : errorText);
        }
        catch (SSLHandshakeException e)
        {
            if (ExceptionUtil.getCause(e, CertPathValidatorException.class) != null)
            {
                Log.w(TAG, "Couldn't make SSL handshake with server.", e);
                handleCertPathValidatorException();
            }
            throw e;
        }
    }

    protected Pair<Integer, String> makeHttpPostRequest(String url, String authorization, String data) throws Exception
    {
        try
        {
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Authorization", authorization);
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(data);
            wr.flush();
            wr.close();
            int responseCode = con.getResponseCode();
            String responseText = getResponseText(url, con);
            String errorText = getErrorText(url, con);
            return Pair.create(responseCode, StringUtil.nonEmpty(responseText) ? responseText : errorText);
        }
        catch (SSLHandshakeException e)
        {
            Throwable ee = ExceptionUtil.getCause(e, CertPathValidatorException.class);
            if (ee != null)
            {
                Log.w(TAG, "Couldn't make SSL handshake with server.", ee);
                handleCertPathValidatorException();
            }
            throw e;
        }
    }

    abstract protected void handleCertPathValidatorException();

    private String getResponseText(String url, HttpURLConnection con)
    {
        try
        {
            return readStream(url, con.getInputStream());
        }
        catch (IOException e)
        {
            Log.i(TAG, "Couldn't read response for " + url);
            return null;
        }
    }

    private String getErrorText(String url, HttpURLConnection con)
    {
        return readStream(url, con.getErrorStream());
    }

    private String readStream(String url, InputStream inputStream)
    {
        if (inputStream == null)
            return null;
        try
        {
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            String inputLine;
            StringBuffer responseBuffer = new StringBuffer();
            while ((inputLine = in.readLine()) != null)
                responseBuffer.append(inputLine);
            in.close();
            return responseBuffer.toString();
        }
        catch (FileNotFoundException e)
        {
            Log.i(TAG, "No response found for " + url);
            return null;
        }
        catch (IOException e)
        {
            Log.i(TAG, "Couldn't read stream " + url);
            return null;
        }
    }

    private String getServerSideToken()
    {
        String rawToken = String.format(SERVER_TOKEN_FORMAT, username, password);
        String base64Token = Base64.encodeToString(rawToken.getBytes(), Base64.NO_WRAP);
        return base64Token;
    }

    @Override
    public String decrypt(String str) throws OperationFailedException
    {
        try
        {
            return ServerSideSecurityUtil.decryptString(str, username, getEnvironment().getEncryptionSalt());
        }
        catch (Exception e)
        {
            throw new OperationFailedException("Unable to decrypt", e);
        }
    }

    protected abstract String getApplicationName();

    protected abstract void log(String message, Exception exception, Class<? extends CachedData> clazz);

    public interface Environment
    {
        String getServerBaseUrl();

        String getEncryptionSalt();
    }
}
