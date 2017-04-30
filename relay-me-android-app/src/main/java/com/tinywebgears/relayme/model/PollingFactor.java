package com.tinywebgears.relayme.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tinywebgears.relayme.R;

public enum PollingFactor
{
    FAST("fast", 8, 60, R.string.lbl_polling_factor_fast), SLOW("slow", 32, 120, R.string.lbl_polling_factor_slow);

    private static final Map<String, PollingFactor> stringToEnum = new HashMap<String, PollingFactor>();

    public static List<PollingFactor> getAll()
    {
        return Arrays.asList(values());
    }

    static
    {
        for (PollingFactor e : values())
            stringToEnum.put(e.toString(), e);
    }

    private final String gatewayTypeId;
    private final int durationCoefficientInSeconds;
    private final int schedulerIntervalInSeconds;
    private final int nameResource;

    PollingFactor(String gatewayTypeId, int durationCoefficient, int schedulerIntervalInSeconds, int nameResource)
    {
        this.gatewayTypeId = gatewayTypeId;
        this.durationCoefficientInSeconds = durationCoefficient;
        this.schedulerIntervalInSeconds = schedulerIntervalInSeconds;
        this.nameResource = nameResource;
    }

    public int getDurationCoefficientInSeconds()
    {
        return durationCoefficientInSeconds;
    }

    public int getSchedulerIntervalInSeconds()
    {
        return schedulerIntervalInSeconds;
    }

    public int getNameResource()
    {
        return nameResource;
    }

    public String toString()
    {
        return gatewayTypeId;
    }

    public static PollingFactor defaultValue()
    {
        return FAST;
    }

    public static PollingFactor fromString(String str)
    {
        return stringToEnum.containsKey(str) ? stringToEnum.get(str) : defaultValue();
    }
}
