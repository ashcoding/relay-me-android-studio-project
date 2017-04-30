package com.codolutions.android.common.serverside.data;

import com.codolutions.android.common.data.AbstractCachedData;

public class RegistrationResponse extends AbstractCachedData {
    private String username;
    private String secret;

    public RegistrationResponse() {
        // Not caching, only valid for one minute to consume.
        super(0, ONE_MINUTE, 0);
    }

    // FIXME: TECHDEBT: Move these methods to superclass
    // http://egalluzzo.blogspot.com.au/2010/06/using-inheritance-with-fluent.html

    public RegistrationResponse setResult(long timestamp, Result result) {
        return setResult(timestamp, result, null);
    }

    public RegistrationResponse setResult(long timestamp, Result result, String errorMessage) {
        recordTry(timestamp, result, errorMessage);
        return this;
    }

    public String getUsername() {
        return username;
    }

    public RegistrationResponse setUsername(String username) {
        this.username = username;
        return this;
    }

    public RegistrationResponse setSecret(String secret) {
        this.secret = secret;
        return this;
    }
}
