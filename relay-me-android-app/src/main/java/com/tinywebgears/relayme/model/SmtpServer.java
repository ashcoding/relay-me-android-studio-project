package com.tinywebgears.relayme.model;

import javax.annotation.Nonnull;

import android.os.Parcel;
import android.os.Parcelable;

import com.codolutions.android.common.util.StringUtil;
import com.tinywebgears.relayme.email.ServerCredentials;

public class SmtpServer implements Parcelable
{
    public static final int DEFAULT_PORT = 25;

    private final String email;
    private final String address;
    private final int port;
    private final ServerSecurity security;
    private final boolean tested;
    private final ServerCredentials credentials;

    public static final Parcelable.Creator<SmtpServer> CREATOR = new Creator<SmtpServer>()
    {
        @Override
        public SmtpServer createFromParcel(Parcel source)
        {
            return SmtpServer.createFromParcel(source);
        }

        @Override
        public SmtpServer[] newArray(int size)
        {
            return new SmtpServer[size];
        }
    };

    public SmtpServer(@Nonnull String email, @Nonnull String address, int port, @Nonnull ServerSecurity security,
            boolean tested, ServerCredentials credentials)
    {
        this.email = email;
        this.address = address;
        this.port = port > 0 ? port : DEFAULT_PORT;
        this.security = security;
        this.tested = tested;
        this.credentials = credentials;
    }

    public String getEmail()
    {
        return email;
    }

    public String getAddress()
    {
        return address;
    }

    public int getPort()
    {
        return port;
    }

    public ServerSecurity getSecurity()
    {
        return security;
    }

    public boolean isTested()
    {
        return tested;
    }

    public ServerCredentials getCredentials()
    {
        return credentials;
    }

    public boolean isValid()
    {
        // TODO: Validate email
        if (StringUtil.empty(email))
            return false;
        if (StringUtil.empty(address))
            return false;
        if (credentials == null || !credentials.isValid())
            return false;
        return true;
    }

    public String getSummary()
    {
        return address + ":" + port;
    }

    public static SmtpServer createFromParcel(Parcel parcel)
    {
        String email = parcel.readString();
        String address = parcel.readString();
        int port = parcel.readInt();
        boolean tested = parcel.readInt() == 1;
        String xoauth = parcel.readString();
        String username = parcel.readString();
        String password = parcel.readString();
        ServerSecurity security = ServerSecurity.fromString(parcel.readString());
        return new SmtpServer(email, address, port, security, tested, new ServerCredentials(xoauth, username, password));
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags)
    {
        parcel.writeString(email);
        parcel.writeString(address);
        parcel.writeInt(port);
        parcel.writeInt(tested ? 1 : 0);
        parcel.writeString(credentials.getXoauth());
        parcel.writeString(credentials.getUsername());
        parcel.writeString(credentials.getPassword());
        parcel.writeString((security == null ? ServerSecurity.defaultValue() : security).toString());
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((email == null) ? 0 : email.hashCode());
        result = prime * result + ((address == null) ? 0 : address.hashCode());
        result = prime * result + (tested ? 1231 : 1237);
        result = prime * result + ((credentials == null) ? 0 : credentials.hashCode());
        result = prime * result + port;
        result = prime * result + ((security == null) ? 0 : security.hashCode());
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
        SmtpServer other = (SmtpServer) obj;
        if (email == null)
        {
            if (other.email != null)
                return false;
        }
        else if (!email.equals(other.email))
            return false;
        if (address == null)
        {
            if (other.address != null)
                return false;
        }
        else if (!address.equals(other.address))
            return false;
        if (tested != other.tested)
            return false;
        if (credentials == null)
        {
            if (other.credentials != null)
                return false;
        }
        else if (!credentials.equals(other.credentials))
            return false;
        if (port != other.port)
            return false;
        if (security != other.security)
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        return "SmtpServer [email=" + email + ", address=" + address + ", port=" + port + ", security=" + security
                + ", tested=" + tested + ", credentials=" + credentials + "]";
    }
}
