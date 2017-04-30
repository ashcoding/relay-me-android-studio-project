package com.tinywebgears.relayme.model;

import java.util.Set;

import com.codolutions.android.common.exception.OperationFailedException;
import com.tinywebgears.relayme.app.UserData;

public class AutoSavingServiceConfiguration implements ServiceConfiguration
{
    private final ServiceConfiguration delegate;
    private final UserData userData;

    public AutoSavingServiceConfiguration(final ServiceConfiguration delegate, final UserData userData)
    {
        this.delegate = delegate;
        this.userData = userData;
    }

    private ServiceConfiguration save(ServiceConfiguration serviceConfiguration)
    {
        userData.setServiceConfiguration(serviceConfiguration);
        return this;
    }

    @Override
    public ServiceStatus getServiceStatus()
    {
        return delegate.getServiceStatus();
    }

    @Override
    public ServiceConfiguration setServiceStatus(ServiceStatus serviceStatus)
    {
        return save(delegate.setServiceStatus(serviceStatus));
    }

    @Override
    public String getActivationCode()
    {
        return delegate.getActivationCode();
    }

    @Override
    public ServiceConfiguration setActivationCode(String activationCode)
    {
        return save(delegate.setActivationCode(activationCode));
    }

    @Override
    public int getScheduleBeginTime()
    {
        return delegate.getScheduleBeginTime();
    }

    @Override
    public String getScheduleBeginTimeString()
    {
        return delegate.getScheduleBeginTimeString();
    }

    @Override
    public ServiceConfiguration setScheduleBeginTime(int scheduleBeginTime)
    {
        return save(delegate.setScheduleBeginTime(scheduleBeginTime));
    }

    @Override
    public ServiceConfiguration setScheduleBeginTime(String scheduleBeginTime) throws OperationFailedException
    {
        return save(delegate.setScheduleBeginTime(scheduleBeginTime));
    }

    @Override
    public int getScheduleEndTime()
    {
        return delegate.getScheduleEndTime();
    }

    @Override
    public String getScheduleEndTimeString()
    {
        return delegate.getScheduleEndTimeString();
    }

    @Override
    public ServiceConfiguration setScheduleEndTime(int scheduleEndTime)
    {
        return save(delegate.setScheduleEndTime(scheduleEndTime));
    }

    @Override
    public ServiceConfiguration setScheduleEndTime(String scheduleEndTime) throws OperationFailedException
    {
        return save(delegate.setScheduleEndTime(scheduleEndTime));
    }

    @Override
    public boolean isMissedCallNotificationEnabled()
    {
        return delegate.isMissedCallNotificationEnabled();
    }

    @Override
    public ServiceConfiguration setMissedCallNotificationEnabled(boolean missedCallNotificationEnabled)
    {
        return save(delegate.setMissedCallNotificationEnabled(missedCallNotificationEnabled));
    }

    @Override
    public boolean isReplyingEnabled()
    {
        return delegate.isReplyingEnabled();
    }

    @Override
    public ServiceConfiguration setReplyingEnabled(boolean replyingEnabled)
    {
        return save(delegate.setReplyingEnabled(replyingEnabled));
    }

    @Override
    public boolean isReplyingStatusReportEnabled()
    {
        return delegate.isReplyingStatusReportEnabled();
    }

    @Override
    public ServiceConfiguration setReplyingStatusReportEnabled(boolean replyingStatusReportEnabled)
    {
        return save(delegate.setReplyingStatusReportEnabled(replyingStatusReportEnabled));
    }

    @Override
    public boolean isReplyingFromDifferentEmail()
    {
        return delegate.isReplyingFromDifferentEmail();
    }

    @Override
    public ServiceConfiguration setReplyingFromDifferentEmail(boolean replyingFromDifferentEmail)
    {
        return save(delegate.setReplyingFromDifferentEmail(replyingFromDifferentEmail));
    }

    @Override
    public String getReplySourceEmailAddress()
    {
        return delegate.getReplySourceEmailAddress();
    }

    @Override
    public ServiceConfiguration setReplySourceEmailAddress(String replySourceEmailAddress)
    {
        return save(delegate.setReplySourceEmailAddress(replySourceEmailAddress));
    }

    @Override
    public Set<Integer> getScheduleDaysOfTheWeek()
    {
        return delegate.getScheduleDaysOfTheWeek();
    }

    @Override
    public ServiceConfiguration setScheduleDaysOfTheWeek(Set<Integer> scheduleDaysOfTheWeek)
    {
        return save(delegate.setScheduleDaysOfTheWeek(scheduleDaysOfTheWeek));
    }

    @Override
    public boolean isActive()
    {
        return delegate.isActive();
    }

    @Override
    public String toString()
    {
        return delegate.toString();
    }
}
