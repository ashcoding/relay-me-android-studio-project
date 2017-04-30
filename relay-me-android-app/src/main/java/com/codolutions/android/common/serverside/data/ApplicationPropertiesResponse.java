package com.codolutions.android.common.serverside.data;

import com.codolutions.android.common.data.AbstractCachedData;

public class ApplicationPropertiesResponse extends AbstractCachedData {
    private int lastSupportedVersion;
    private int latestKnownVersion;
    private String latestVersionName;

    // FIXME: TECHDEBT: Move these methods to superclass
    // http://egalluzzo.blogspot.com.au/2010/06/using-inheritance-with-fluent.html

    public ApplicationPropertiesResponse() {
    }

    public ApplicationPropertiesResponse(long freshnessPeriod, long validityPeriod, long retryInterval) {
        super(freshnessPeriod, validityPeriod, retryInterval);
    }

    public ApplicationPropertiesResponse setResult(long timestamp, Result result) {
        return setResult(timestamp, result, null);
    }

    public ApplicationPropertiesResponse setResult(long timestamp, Result result, String errorMessage) {
        recordTry(timestamp, result, errorMessage);
        return this;
    }

    public int getLastSupportedVersion() {
        return lastSupportedVersion;
    }

    public ApplicationPropertiesResponse setLastSupportedVersion(int lastSupportedVersion) {
        this.lastSupportedVersion = lastSupportedVersion;
        return this;
    }

    public int getLatestKnownVersion() {
        return latestKnownVersion;
    }

    public ApplicationPropertiesResponse setLatestKnownVersion(int latestKnownVersion) {
        this.latestKnownVersion = latestKnownVersion;
        return this;
    }

    public String getLatestVersionName() {
        return latestVersionName;
    }

    public ApplicationPropertiesResponse setLatestVersionName(String latestVersionName) {
        this.latestVersionName = latestVersionName;
        return this;
    }
}
