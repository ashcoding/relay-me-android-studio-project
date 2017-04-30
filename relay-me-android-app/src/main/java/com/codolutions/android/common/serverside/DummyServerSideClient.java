package com.codolutions.android.common.serverside;

import android.net.Uri;

import com.codolutions.android.common.data.CachedData;
import com.codolutions.android.common.serverside.data.ApplicationPropertiesResponse;
import com.codolutions.android.common.serverside.data.KeysResponse;
import com.codolutions.android.common.serverside.data.RegistrationResponse;
import com.codolutions.android.common.serverside.data.VerificationResponse;
import com.codolutions.android.common.util.Pair;

public class DummyServerSideClient implements ServerSideClient {
    private static final DummyServerSideClient INSTANCE = new DummyServerSideClient();
    private static final String GOOGLE_CALLBACK_URL_FORMAT =
            "codolutions.catchup://localhost/settings?" + "provider=google&accessToken=%s&refreshToken=%s";

    public static String getGoogleCallbackUrl(String token, String secret) {
        return String.format(GOOGLE_CALLBACK_URL_FORMAT, token, secret);
    }

    public static DummyServerSideClient get() {
        return INSTANCE;
    }

    @Override
    public RegistrationResponse registerUser() {
        return new RegistrationResponse().setResult(System.currentTimeMillis(), CachedData.Result.SUCCESS);
    }

    @Override
    public VerificationResponse verifyUser(VerificationResponse cachedVerificationResponse) {
        return new VerificationResponse().setResult(System.currentTimeMillis(), CachedData.Result.SUCCESS);
    }

    @Override
    public KeysResponse getKeys(KeysResponse cachedKeysResponse) {
        return new KeysResponse().setResult(System.currentTimeMillis(), CachedData.Result.SUCCESS);
    }

    @Override
    public ApplicationPropertiesResponse getApplicationProperties(
            ApplicationPropertiesResponse cachedApplicationPropertiesResponse) {
        return new ApplicationPropertiesResponse().setResult(System.currentTimeMillis(), CachedData.Result.SUCCESS);
    }

    @Override
    public String getGoogleOauthUrl(boolean debug) {
        return getGoogleCallbackUrl("test", "test");
    }

    @Override
    public String decrypt(String str) {
        return str;
    }

    @Override
    public Pair<String, String> getCredentials(Uri uri, String waitingFor) {
        return new Pair<String, String>("test", "test");
    }
}
