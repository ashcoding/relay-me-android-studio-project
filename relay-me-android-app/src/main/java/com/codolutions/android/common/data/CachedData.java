package com.codolutions.android.common.data;

public interface CachedData {
    long ONE_SECOND = 1000;
    long ONE_MINUTE = 60 * ONE_SECOND;
    long ONE_HOUR = 60 * ONE_MINUTE;
    long ONE_DAY = 24 * ONE_HOUR;
    long ONE_WEEK = 7 * ONE_DAY;
    long DEFAULT_REFRESHING_TRY_INTERVAL_IN_MS = 15 * ONE_MINUTE;
    long DEFAULT_VALIDITY_PERIOD_IN_MS = 6 * ONE_HOUR;
    long DEFAULT_FRESHNESS_PERIOD_IN_MS = ONE_HOUR;

    enum Result {
        SUCCESS, UNAUTHORIZED, FAILURE, USERNAME_TAKEN, EMAIL_TAKEN, NONE;

        public boolean didComplete() {
            return this == SUCCESS || this == UNAUTHORIZED || this == USERNAME_TAKEN || this == EMAIL_TAKEN;
        }
    }

    Result getResult();

    boolean timeToUpdate();

    boolean isValid();

    long validFor();

    boolean isFresh();

    boolean isUnauthorized();

    boolean isSuccess();
}
