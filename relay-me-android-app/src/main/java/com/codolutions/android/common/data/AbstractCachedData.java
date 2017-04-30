package com.codolutions.android.common.data;

import android.util.Log;

import com.codolutions.android.common.util.DateUtil;

import java.util.Date;

public class AbstractCachedData implements CachedData {
    private static final String TAG = AbstractCachedData.class.getName();

    protected Result result = Result.NONE;
    protected String errorMessage;
    private long lastUpdated;
    private long lastTried;
    private long freshnessPeriod;
    private long validityPeriod;
    private long refreshingTryInterval;
    private boolean stale = false;
    private long madeStale;

    public AbstractCachedData() {
        this(DEFAULT_FRESHNESS_PERIOD_IN_MS, DEFAULT_VALIDITY_PERIOD_IN_MS, DEFAULT_REFRESHING_TRY_INTERVAL_IN_MS);
    }

    public AbstractCachedData(long freshnessPeriod, long validityPeriod, long refreshingTryInterval) {
        this.freshnessPeriod = freshnessPeriod;
        this.validityPeriod = validityPeriod;
        this.refreshingTryInterval = refreshingTryInterval;
    }

    public void invalidate() {
        this.result = Result.NONE;
        this.lastUpdated = 0;
        this.lastTried = 0;
    }

    protected void recordTry(long timestamp, Result result, String errorMessage) {
        lastTried = timestamp;
        if (isValid() && !result.didComplete()) {
            Log.i(TAG, "Didn't get a success, but still valid for another " + (validFor() / 1000) + " seconds.");
            return;
        }
        this.result = result;
        this.errorMessage = errorMessage;
        if (result.didComplete()) {
            lastUpdated = timestamp;
            if (madeStale < lastTried) {
                madeStale = 0;
                stale = false;
            }
        }
    }

    @Override
    public Result getResult() {
        return result;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public boolean timeToUpdate() {
        if (!isValid() || stale)
            return true;
        if (isFresh())
            return false;
        return System.currentTimeMillis() > (lastTried + refreshingTryInterval);
    }

    @Override
    public boolean isValid() {
        return (result == Result.UNAUTHORIZED || result == Result.SUCCESS)
                && (System.currentTimeMillis() < lastUpdated + validityPeriod);
    }

    @Override
    public long validFor() {
        return lastUpdated + validityPeriod - System.currentTimeMillis();
    }

    @Override
    public boolean isFresh() {
        return !stale && isValid() && System.currentTimeMillis() < lastUpdated + freshnessPeriod;
    }

    @Override
    public boolean isUnauthorized() {
        return result == Result.UNAUTHORIZED && isValid();
    }

    @Override
    public boolean isSuccess() {
        return result == Result.SUCCESS && isValid();
    }

    @Override
    public String toString() {
        return "AbstractCachedData [result=" + result + ", errorMessage=" + errorMessage + ", stale=" + stale
                + ", madeStale=" + DateUtil.secondsPassedSince(new Date(madeStale)) + " seconds ago, lastUpdated="
                + DateUtil.secondsPassedSince(new Date(lastUpdated)) + " seconds ago, lastTried="
                + DateUtil.secondsPassedSince(new Date(lastTried)) + " seconds ago, valid=" + isValid() + ", fresh="
                + isFresh() + "]";
    }
}
