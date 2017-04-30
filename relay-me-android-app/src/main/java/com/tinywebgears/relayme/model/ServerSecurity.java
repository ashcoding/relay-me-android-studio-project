package com.tinywebgears.relayme.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tinywebgears.relayme.R;

public enum ServerSecurity
{
    SSL("ssl", R.string.lbl_server_security_ssl), STARTTLS("startls", R.string.lbl_server_security_starttls), SSL_STARTTLS(
            "ssl_startls", R.string.lbl_server_security_ssl_starttls), ;

    private static final Map<String, ServerSecurity> stringToEnum = new HashMap<String, ServerSecurity>();

    public static List<ServerSecurity> getAll()
    {
        return Arrays.asList(values());
    }

    static
    {
        for (ServerSecurity e : values())
            stringToEnum.put(e.toString(), e);
    }

    private final String serverSecurityId;
    private final int displayNameResourceId;

    ServerSecurity(final String serverSecurityId, final int displayNameResourceId)
    {
        this.serverSecurityId = serverSecurityId;
        this.displayNameResourceId = displayNameResourceId;
    }

    public int getDisplayNameResourceId()
    {
        return displayNameResourceId;
    }

    public boolean isSecure()
    {
        return this == SSL || this == SSL_STARTTLS;
    }

    public boolean isStartTls()
    {
        return this == STARTTLS || this == SSL_STARTTLS;
    }

    public String toString()
    {
        return serverSecurityId;
    }

    public static ServerSecurity defaultValue()
    {
        return ServerSecurity.SSL;
    }

    public static ServerSecurity fromString(String str)
    {
        return stringToEnum.containsKey(str) ? stringToEnum.get(str) : defaultValue();
    }
}
