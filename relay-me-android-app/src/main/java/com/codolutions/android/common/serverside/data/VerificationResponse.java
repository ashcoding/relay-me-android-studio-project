package com.codolutions.android.common.serverside.data;

import com.codolutions.android.common.data.AbstractCachedData;

public class VerificationResponse extends AbstractCachedData {
    private String userDetailsJson;

    // FIXME: TECHDEBT: Move these methods to superclass
    // http://egalluzzo.blogspot.com.au/2010/06/using-inheritance-with-fluent.html

    public VerificationResponse() {
    }

    public VerificationResponse(long freshnessPeriod, long validityPeriod, long retryInterval) {
        super(freshnessPeriod, validityPeriod, retryInterval);
    }

    public VerificationResponse setResult(long timestamp, Result result) {
        return setResult(timestamp, result, null);
    }

    public VerificationResponse setResult(long timestamp, Result result, String errorMessage) {
        recordTry(timestamp, result, errorMessage);
        return this;
    }
}
