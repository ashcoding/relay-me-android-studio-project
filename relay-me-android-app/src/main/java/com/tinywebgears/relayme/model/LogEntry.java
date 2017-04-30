package com.tinywebgears.relayme.model;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import com.tinywebgears.relayme.dao.DataBaseHelper;

public class LogEntry
{
    private long id;
    private final LogLevel logLevel;
    private final boolean visibleToUser;
    private final String text;
    private Date dateUpdated;
    private AtomicInteger count;

    public LogEntry(LogLevel logLevel, boolean visibleToUser, String text)
    {
        this(0, logLevel, visibleToUser, text, null, DataBaseHelper.BOOLEAN_INT_VALUE_TRUE);
    }

    public LogEntry(long id, LogLevel logLevel, boolean visibleToUser, String text, Date dateUpdated, int count)
    {
        this.id = id;
        this.logLevel = logLevel;
        this.visibleToUser = visibleToUser;
        this.text = text;
        this.dateUpdated = dateUpdated;
        this.count = new AtomicInteger(count);
    }

    public long getId()
    {
        return id;
    }

    public LogLevel getLogLevel()
    {
        return logLevel;
    }

    public boolean isVisibleToUser()
    {
        return visibleToUser;
    }

    public String getText()
    {
        return text;
    }

    public Date getDateUpdated()
    {
        return dateUpdated;
    }

    public int getCount()
    {
        return count.get();
    }

    // This is a convenient method to update the ID after insertion.
    public void updateId(long id)
    {
        this.id = id;
    }

    public void setDateUpdated(Date dateUpdated)
    {
        this.dateUpdated = dateUpdated;
    }

    public int incrementCount()
    {
        return this.count.incrementAndGet();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((count == null) ? 0 : count.get());
        result = prime * result + ((dateUpdated == null) ? 0 : dateUpdated.hashCode());
        // Id is not considered part of object data and therefore is not used to calculate hash code.
        // result = prime * result + (int) (id ^ (id >>> 32));
        result = prime * result + ((logLevel == null) ? 0 : logLevel.hashCode());
        result = prime * result + ((text == null) ? 0 : text.hashCode());
        result = prime * result + (visibleToUser ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LogEntry other = (LogEntry) obj;
        if (count == null)
        {
            if (other.count != null)
                return false;
        }
        else if (!count.equals(other.count))
            return false;
        if (dateUpdated == null)
        {
            if (other.dateUpdated != null)
                return false;
        }
        else if (!dateUpdated.equals(other.dateUpdated))
            return false;
        // Id's value doesn't affect equality.
        // if (id != other.id)
        // return false;
        if (logLevel != other.logLevel)
            return false;
        if (text == null)
        {
            if (other.text != null)
                return false;
        }
        else if (!text.equals(other.text))
            return false;
        if (visibleToUser != other.visibleToUser)
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        return dateUpdated + " - " + logLevel + " - " + text;
    }
}
