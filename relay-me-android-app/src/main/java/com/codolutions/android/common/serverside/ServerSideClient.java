package com.codolutions.android.common.serverside;

import android.net.Uri;

import com.codolutions.android.common.exception.OperationFailedException;
import com.codolutions.android.common.serverside.data.ApplicationPropertiesResponse;
import com.codolutions.android.common.serverside.data.KeysResponse;
import com.codolutions.android.common.serverside.data.RegistrationResponse;
import com.codolutions.android.common.serverside.data.VerificationResponse;
import com.codolutions.android.common.util.Pair;

public interface ServerSideClient {
    Pair<String, String> getCredentials(Uri uri, String waitingFor);

    RegistrationResponse registerUser();

    VerificationResponse verifyUser(VerificationResponse cachedVerificationResponse);

    KeysResponse getKeys(KeysResponse keysResponse);

    ApplicationPropertiesResponse getApplicationProperties(
            ApplicationPropertiesResponse cachedApplicationPropertiesResponse);

    String getGoogleOauthUrl(boolean debug);

    String decrypt(String str) throws OperationFailedException;
}
