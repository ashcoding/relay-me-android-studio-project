package com.tinywebgears.relayme.email;

import com.codolutions.android.common.util.StringUtil;

public class ServerCredentials
{
    private final String xoauth;
    private final String username;
    private final String password;

    public ServerCredentials(String xoauth, String username, String password)
    {
        this.xoauth = xoauth;
        this.username = username;
        this.password = password;
    }

    public ServerCredentials(String xoauth)
    {
        this(xoauth, null, null);
    }

    public ServerCredentials(String username, String password)
    {
        this(null, username, password);
    }

    public boolean isValid()
    {
        return isOauth() || isUsernamePassword();
    }

    public boolean isOauth()
    {
        return !StringUtil.empty(xoauth);
    }

    public boolean isUsernamePassword()
    {
        return !StringUtil.empty(username) && !StringUtil.empty(password);
    }

    public String getXoauth()
    {
        return xoauth;
    }

    public String getUsername()
    {
        return username;
    }

    public String getPassword()
    {
        return password;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((password == null) ? 0 : password.hashCode());
        result = prime * result + ((username == null) ? 0 : username.hashCode());
        result = prime * result + ((xoauth == null) ? 0 : xoauth.hashCode());
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
        ServerCredentials other = (ServerCredentials) obj;
        if (password == null)
        {
            if (other.password != null)
                return false;
        }
        else if (!password.equals(other.password))
            return false;
        if (username == null)
        {
            if (other.username != null)
                return false;
        }
        else if (!username.equals(other.username))
            return false;
        if (xoauth == null)
        {
            if (other.xoauth != null)
                return false;
        }
        else if (!xoauth.equals(other.xoauth))
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        return "ServerCredentials [xoauth=" + xoauth + ", username=" + username + "]";
    }
}
