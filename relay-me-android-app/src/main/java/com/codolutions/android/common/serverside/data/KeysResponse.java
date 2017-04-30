package com.codolutions.android.common.serverside.data;

import com.codolutions.android.common.data.AbstractCachedData;

// This structure contains sensitive information and should never be stored or logged.
public class KeysResponse extends AbstractCachedData {
    public String googleApiKey;
    public String googleApiSecret;
    public String googleCallbackUrl;

    public KeysResponse() {
    }

    public KeysResponse(long freshnessPeriod, long validityPeriod, long retryInterval) {
        super(freshnessPeriod, validityPeriod, retryInterval);
    }

    // FIXME: TECHDEBT: Move these methods to superclass
    // http://egalluzzo.blogspot.com.au/2010/06/using-inheritance-with-fluent.html

    public KeysResponse setResult(long timestamp, Result result) {
        return setResult(timestamp, result, null);
    }

    public KeysResponse setResult(long timestamp, Result result, String errorMessage) {
        recordTry(timestamp, result, errorMessage);
        return this;
    }

    public KeysResponse setGoogleApiKey(String googleApiKey) {
        this.googleApiKey = googleApiKey;
        return this;
    }

    public KeysResponse setGoogleApiSecret(String GoogleApiSecret) {
        this.googleApiSecret = GoogleApiSecret;
        return this;
    }

    public KeysResponse setGoogleCallbackUrl(String googleCallbackUrl) {
        this.googleCallbackUrl = googleCallbackUrl;
        return this;
    }
}
