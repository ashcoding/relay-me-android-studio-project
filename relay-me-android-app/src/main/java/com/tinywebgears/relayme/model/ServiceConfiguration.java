package com.tinywebgears.relayme.model;

import java.util.Set;

import com.codolutions.android.common.exception.OperationFailedException;

public interface ServiceConfiguration
{
    ServiceStatus getServiceStatus();

    ServiceConfiguration setServiceStatus(ServiceStatus serviceStatus);

    String getActivationCode();

    ServiceConfiguration setActivationCode(String activationCode);

    int getScheduleBeginTime();

    String getScheduleBeginTimeString();

    ServiceConfiguration setScheduleBeginTime(int scheduleBeginTime);

    ServiceConfiguration setScheduleBeginTime(String scheduleBeginTime) throws OperationFailedException;

    int getScheduleEndTime();

    String getScheduleEndTimeString();

    ServiceConfiguration setScheduleEndTime(int scheduleEndTime);

    ServiceConfiguration setScheduleEndTime(String scheduleEndTime) throws OperationFailedException;

    boolean isMissedCallNotificationEnabled();

    ServiceConfiguration setMissedCallNotificationEnabled(boolean missedCallNotificationEnabled);

    boolean isReplyingEnabled();

    ServiceConfiguration setReplyingEnabled(boolean replyingEnabled);

    boolean isReplyingStatusReportEnabled();

    ServiceConfiguration setReplyingStatusReportEnabled(boolean replyingStatusReportEnabled);

    boolean isReplyingFromDifferentEmail();

    ServiceConfiguration setReplyingFromDifferentEmail(boolean replyingFromDifferentEmail);

    String getReplySourceEmailAddress();

    ServiceConfiguration setReplySourceEmailAddress(String replySourceEmailAddress);

    Set<Integer> getScheduleDaysOfTheWeek();

    ServiceConfiguration setScheduleDaysOfTheWeek(Set<Integer> scheduleDaysOfTheWeek);

    boolean isActive();
}
