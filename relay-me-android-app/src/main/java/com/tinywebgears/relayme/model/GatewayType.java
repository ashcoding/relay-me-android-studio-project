package com.tinywebgears.relayme.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tinywebgears.relayme.R;

public enum GatewayType
{
    BUILT_IN("built_in", R.string.lbl_gateway_type_builtin, R.string.lbl_gateway_type_title_builtin,
            R.string.lbl_gateway_type_desc_builtin, false), GMAIL("gmail", R.string.lbl_gateway_type_gmail,
            R.string.lbl_gateway_type_title_gmail, R.string.lbl_gateway_type_desc_gmail, true), CUSTOM("custom",
            R.string.lbl_gateway_type_custom, R.string.lbl_gateway_type_title_custom,
            R.string.lbl_gateway_type_desc_custom, true);

    private static final Map<String, GatewayType> stringToEnum = new HashMap<String, GatewayType>();

    public static List<GatewayType> getAllEnabled()
    {
        List<GatewayType> enabledGatewayTypes = new ArrayList<GatewayType>();
        for (GatewayType gatewayType : values())
            if (gatewayType.enabled)
                enabledGatewayTypes.add(gatewayType);
        return enabledGatewayTypes;
    }

    static
    {
        for (GatewayType e : values())
            stringToEnum.put(e.toString(), e);
    }

    private final String gatewayTypeId;
    private final int nameResource;
    private final int titleResource;
    private final int descriptionResource;
    private final boolean enabled;

    GatewayType(String gatewayTypeId, int nameResource, int titleResource, int descriptionResource,
            final boolean enabled)
    {
        this.gatewayTypeId = gatewayTypeId;
        this.nameResource = nameResource;
        this.titleResource = titleResource;
        this.descriptionResource = descriptionResource;
        this.enabled = enabled;
    }

    public int getNameResource()
    {
        return nameResource;
    }

    public int getTitleResource()
    {
        return titleResource;
    }

    public int getDescriptionResource()
    {
        return descriptionResource;
    }

    public String toString()
    {
        return gatewayTypeId;
    }

    public static GatewayType defaultValue()
    {
        return GMAIL;
    }

    public static GatewayType fromString(String str)
    {
        return stringToEnum.containsKey(str) ? stringToEnum.get(str) : defaultValue();
    }
}
