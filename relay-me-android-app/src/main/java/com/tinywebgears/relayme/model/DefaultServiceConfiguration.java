package com.tinywebgears.relayme.model;

import java.util.Date;
import java.util.Set;
import java.util.StringTokenizer;

import com.codolutions.android.common.exception.OperationFailedException;
import com.tinywebgears.relayme.service.LogStoreHelper;

public class DefaultServiceConfiguration implements ServiceConfiguration
{
    private ServiceStatus serviceStatus;
    private String activationCode;
    private int scheduleBeginTime;
    private int scheduleEndTime;
    private boolean missedCallNotificationEnabled = false;
    private boolean replyingEnabled = true;
    private boolean replyingStatusReportEnabled = false;
    private boolean replyingFromDifferentEmail = false;
    private String replySourceEmailAddress;
    private Set<Integer> scheduleDaysOfTheWeek;

    @Override
    public ServiceStatus getServiceStatus()
    {
        return serviceStatus == null ? ServiceStatus.defaultValue() : serviceStatus;
    }

    @Override
    public DefaultServiceConfiguration setServiceStatus(ServiceStatus serviceStatus)
    {
        this.serviceStatus = serviceStatus;
        return this;
    }

    @Override
    public String getActivationCode()
    {
        return activationCode;
    }

    @Override
    public DefaultServiceConfiguration setActivationCode(String activationCode)
    {
        this.activationCode = activationCode;
        return this;
    }

    @Override
    public int getScheduleBeginTime()
    {
        return scheduleBeginTime;
    }

    @Override
    public String getScheduleBeginTimeString()
    {
        return convertTime(scheduleBeginTime);
    }

    @Override
    public DefaultServiceConfiguration setScheduleBeginTime(int scheduleStartTime)
    {
        this.scheduleBeginTime = scheduleStartTime;
        return this;
    }

    @Override
    public DefaultServiceConfiguration setScheduleBeginTime(String scheduleBeginTime) throws OperationFailedException
    {
        try
        {
            return setScheduleBeginTime(convertTime(scheduleBeginTime));
        }
        catch (NumberFormatException e)
        {
            throw new OperationFailedException("Invalid time: " + scheduleBeginTime, e);
        }
    }

    @Override
    public int getScheduleEndTime()
    {
        return scheduleEndTime;
    }

    @Override
    public String getScheduleEndTimeString()
    {
        return convertTime(scheduleEndTime);
    }

    @Override
    public DefaultServiceConfiguration setScheduleEndTime(int scheduleEndTime)
    {
        this.scheduleEndTime = scheduleEndTime;
        return this;
    }

    @Override
    public DefaultServiceConfiguration setScheduleEndTime(String scheduleEndTime) throws OperationFailedException
    {
        try
        {
            return setScheduleEndTime(convertTime(scheduleEndTime));
        }
        catch (NumberFormatException e)
        {
            throw new OperationFailedException("Invalid time: " + scheduleBeginTime, e);
        }
    }

    @Override
    public boolean isMissedCallNotificationEnabled()
    {
        return missedCallNotificationEnabled;
    }

    @Override
    public ServiceConfiguration setMissedCallNotificationEnabled(boolean missedCallNotificationEnabled)
    {
        this.missedCallNotificationEnabled = missedCallNotificationEnabled;
        return this;
    }

    @Override
    public boolean isReplyingEnabled()
    {
        return replyingEnabled;
    }

    @Override
    public DefaultServiceConfiguration setReplyingEnabled(boolean replyingEnabled)
    {
        this.replyingEnabled = replyingEnabled;
        return this;
    }

    @Override
    public boolean isReplyingStatusReportEnabled()
    {
        return replyingStatusReportEnabled;
    }

    @Override
    public DefaultServiceConfiguration setReplyingStatusReportEnabled(boolean replyingStatusReportEnabled)
    {
        this.replyingStatusReportEnabled = replyingStatusReportEnabled;
        return this;
    }

    @Override
    public boolean isReplyingFromDifferentEmail()
    {
        return replyingFromDifferentEmail;
    }

    @Override
    public ServiceConfiguration setReplyingFromDifferentEmail(boolean replyingFromDifferentEmail)
    {
        this.replyingFromDifferentEmail = replyingFromDifferentEmail;
        return this;
    }

    @Override
    public String getReplySourceEmailAddress()
    {
        return replySourceEmailAddress;
    }

    @Override
    public ServiceConfiguration setReplySourceEmailAddress(String replySourceEmailAddress)
    {
        this.replySourceEmailAddress = replySourceEmailAddress;
        return this;
    }

    @Override
    public Set<Integer> getScheduleDaysOfTheWeek()
    {
        return scheduleDaysOfTheWeek;
    }

    @Override
    public DefaultServiceConfiguration setScheduleDaysOfTheWeek(Set<Integer> scheduleDaysOfTheWeek)
    {
        this.scheduleDaysOfTheWeek = scheduleDaysOfTheWeek;
        return this;
    }

    public boolean isActive()
    {
        if (getServiceStatus() == ServiceStatus.ENABLED)
            return true;
        if (getServiceStatus() == ServiceStatus.SCHEDULED)
        {
            Date now = new Date();
            int startTime = getScheduleBeginTime();
            int endTime = getScheduleEndTime();
            if (endTime <= startTime)
                endTime += 2400;
            int time = now.getHours() * 100 + now.getMinutes();
            int timeTomorrow = time + 2400;
            return (startTime <= time && time <= endTime) || (startTime <= timeTomorrow && timeTomorrow <= endTime);
        }
        return false;
    }

    @Override
    public String toString()
    {
        return "ServiceConfiguration [serviceStatus=" + serviceStatus + ", activationCode=" + activationCode
                + ", scheduleBeginTime=" + scheduleBeginTime + ", scheduleEndTime=" + scheduleEndTime
                + ", missedCallNotificationEnabled=" + missedCallNotificationEnabled + ", replyingEnabled="
                + replyingEnabled + ", replyingStatusReportEnabled=" + replyingStatusReportEnabled
                + ", scheduleDaysOfTheWeek=" + scheduleDaysOfTheWeek + "]";
    }

    private int convertTime(String time)
    {
        StringTokenizer tokenizer = new StringTokenizer(time, ":");
        int hours = 0;
        if (tokenizer.hasMoreTokens())
            hours = Integer.parseInt(tokenizer.nextToken()) % 24;
        int minutes = 0;
        if (tokenizer.hasMoreTokens())
            minutes = Integer.parseInt(tokenizer.nextToken()) % 60;
        return hours * 100 + minutes;
    }

    private String convertTime(int time)
    {
        int hours = (time / 100) % 24;
        int minutes = (time % 100) % 60;
        return "" + hours + ":" + minutes;
    }
}
