package com.tinywebgears.relayme.model;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import com.codolutions.android.common.util.StringUtil;
import com.tinywebgears.relayme.common.Constants;

public class Message
{
    private long id;
    private final String externalId;
    private final EventType eventType;
    private final MessageType messageType;
    private final String phoneNumber;
    private final String contactName;
    private final String body;
    private final Date timestamp;
    private Date dateUpdated;
    private Date dateTried;
    private AtomicInteger tries;
    private MessageStatus status;

    public Message(String externalId, EventType eventType, MessageType messageType, String phoneNumber, String body,
            Date date)
    {
        this(0, externalId, eventType, messageType, phoneNumber, "", body, date, null, null, 0, MessageStatus.NEW);
    }

    public Message(long id, String externalId, EventType eventType, MessageType messageType, String phoneNumber,
            String contactName, String body, Date timestamp, Date dateUpdated, Date dateTried, int tries,
            MessageStatus status)
    {
        this.id = id;
        this.externalId = externalId;
        this.eventType = eventType;
        this.messageType = messageType;
        this.phoneNumber = phoneNumber;
        this.contactName = contactName;
        this.body = body;
        this.timestamp = timestamp;
        this.dateUpdated = dateUpdated;
        this.dateTried = dateTried;
        this.tries = new AtomicInteger(tries);
        this.status = status;
    }

    public long getId()
    {
        return id;
    }

    public String getExternalId()
    {
        return externalId;
    }

    public EventType getEventType()
    {
        return eventType;
    }

    public MessageType getMessageType()
    {
        return messageType;
    }

    public String getPhoneNumber()
    {
        return phoneNumber;
    }

    public String getContactName()
    {
        return contactName;
    }

    public String getBody()
    {
        return body;
    }

    public Date getTimestamp()
    {
        return timestamp;
    }

    public Date getDateUpdated()
    {
        return dateUpdated;
    }

    public Date getDateTried()
    {
        return dateTried;
    }

    public int getTries()
    {
        return tries.get();
    }

    public MessageStatus getStatus()
    {
        return status;
    }

    public void updateId(long id)
    {
        this.id = id;
    }

    // This is a convenient method to update the ID after insertion.
    public void setDateUpdated(Date dateUpdated)
    {
        this.dateUpdated = dateUpdated;
    }

    public void setDateTried(Date dateTried)
    {
        this.dateTried = dateTried;
    }

    public int incrementTries()
    {
        return this.tries.incrementAndGet();
    }

    public void setStatus(MessageStatus status)
    {
        this.status = status;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((body == null) ? 0 : body.hashCode());
        result = prime * result + ((contactName == null) ? 0 : contactName.hashCode());
        result = prime * result + ((dateTried == null) ? 0 : dateTried.hashCode());
        result = prime * result + ((dateUpdated == null) ? 0 : dateUpdated.hashCode());
        result = prime * result + ((messageType == null) ? 0 : messageType.hashCode());
        result = prime * result + ((externalId == null) ? 0 : externalId.hashCode());
        result = prime * result + (int) (id ^ (id >>> 32));
        result = prime * result + ((phoneNumber == null) ? 0 : phoneNumber.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
        result = prime * result + ((tries == null) ? 0 : tries.get());
        result = prime * result + ((eventType == null) ? 0 : eventType.hashCode());
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
        Message other = (Message) obj;
        if (body == null)
        {
            if (other.body != null)
                return false;
        }
        else if (!body.equals(other.body))
            return false;
        if (contactName == null)
        {
            if (other.contactName != null)
                return false;
        }
        else if (!contactName.equals(other.contactName))
            return false;
        if (dateTried == null)
        {
            if (other.dateTried != null)
                return false;
        }
        else if (!dateTried.equals(other.dateTried))
            return false;
        if (dateUpdated == null)
        {
            if (other.dateUpdated != null)
                return false;
        }
        else if (!dateUpdated.equals(other.dateUpdated))
            return false;
        if (messageType != other.messageType)
            return false;
        if (externalId == null)
        {
            if (other.externalId != null)
                return false;
        }
        else if (!externalId.equals(other.externalId))
            return false;
        if (id != other.id)
            return false;
        if (phoneNumber == null)
        {
            if (other.phoneNumber != null)
                return false;
        }
        else if (!phoneNumber.equals(other.phoneNumber))
            return false;
        if (status != other.status)
            return false;
        if (timestamp == null)
        {
            if (other.timestamp != null)
                return false;
        }
        else if (!timestamp.equals(other.timestamp))
            return false;
        if (tries == null)
        {
            if (other.tries != null)
                return false;
        }
        else if (tries.get() != other.tries.get())
            return false;
        if (eventType != other.eventType)
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        return "ID: " + id + ", external ID: " + externalId + ", eventType: " + eventType + ", messageType: "
                + messageType + ", phone number: " + phoneNumber + ", contact name: " + contactName + ", body: "
                + StringUtil.shorten(body, Constants.MESSAGE_SHORTENING_LENGTH) + ", timestamp: " + timestamp
                + ", updated: " + dateUpdated + ", tried: " + dateTried + ", tries: " + tries + ", status: " + status;
    }
}
