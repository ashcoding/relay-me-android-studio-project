package com.tinywebgears.relayme.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tinywebgears.relayme.R;

public enum ServiceStatus
{
    ENABLED("enabled", R.string.lbl_activation_always, R.color.enabled), DISABLED("disabled",
            R.string.lbl_activation_disabled, R.color.disabled), SCHEDULED("scheduled",
            R.string.lbl_activation_scheduled, R.color.scheduled);

    private static final Map<String, ServiceStatus> stringToEnum = new HashMap<String, ServiceStatus>();

    public static List<ServiceStatus> getAll()
    {
        return Arrays.asList(values());
    }

    static
    {
        for (ServiceStatus e : values())
            stringToEnum.put(e.toString(), e);
    }

    private final String activationStatus;
    private final int displayNameResourceId;
    private final int background;

    ServiceStatus(String activationStatus, int displayNameResourceId, int background)
    {
        this.activationStatus = activationStatus;
        this.displayNameResourceId = displayNameResourceId;
        this.background = background;
    }

    public int getDisplayNameResourceId()
    {
        return displayNameResourceId;
    }

    public int getBackground()
    {
        return background;
    }

    public String toString()
    {
        return activationStatus;
    }

    public static ServiceStatus defaultValue()
    {
        return ENABLED;
    }

    public static ServiceStatus fromString(String str)
    {
        return stringToEnum.containsKey(str) ? stringToEnum.get(str) : defaultValue();
    }
}
