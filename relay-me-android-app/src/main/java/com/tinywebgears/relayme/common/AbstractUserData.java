package com.tinywebgears.relayme.common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.scribe.model.Token;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

import com.codolutions.android.common.exception.OperationFailedException;
import com.codolutions.android.common.util.DataSerializationException;
import com.codolutions.android.common.util.ObjectSerializer;

public class AbstractUserData
{
    private static final String TAG = AbstractUserData.class.getName();

    protected final Context context;
    protected final SharedPreferences prefs;

    public AbstractUserData(Context context)
    {
        this.context = context;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    protected synchronized String getString(String key)
    {
        return prefs.getString(key, null);
    }

    protected synchronized void setString(String key, String value)
    {
        final Editor edit = prefs.edit();
        edit.putString(key, value);
        edit.commit();
    }

    protected synchronized boolean getBoolean(String key)
    {
        return prefs.getBoolean(key, false);
    }

    protected synchronized void setBoolean(String key, boolean value)
    {
        final Editor edit = prefs.edit();
        edit.putBoolean(key, value);
        edit.commit();
    }

    protected synchronized long getLong(String key)
    {
        return prefs.getLong(key, 0l);
    }

    protected synchronized void setLong(String key, long value)
    {
        final Editor edit = prefs.edit();
        edit.putLong(key, value);
        edit.commit();
    }

    protected synchronized int getInt(String key)
    {
        return prefs.getInt(key, 0);
    }

    protected synchronized void setInt(String key, int value)
    {
        final Editor edit = prefs.edit();
        edit.putInt(key, value);
        edit.commit();
    }

    protected synchronized Date getDate(String key, Date defaultDate)
    {
        long dateLong = prefs.getLong(key, 0);
        if (dateLong <= 0)
            return defaultDate;
        return new Date(dateLong);
    }

    protected synchronized void setDate(String key, Date value)
    {
        final Editor edit = prefs.edit();
        if (value == null)
            edit.putLong(key, 0);
        else
            edit.putLong(key, value.getTime());
        edit.commit();
    }

    @SuppressWarnings("unchecked")
    protected synchronized <T> ArrayList<T> getArrayList(String key)
    {
        String strValue = prefs.getString(key, null);
        if (strValue == null)
            return new ArrayList<T>();
        try
        {
            return (ArrayList<T>) ObjectSerializer.deserialize(strValue);
        }
        catch (IOException e)
        {
            Log.w(TAG, "Deserialization failed for: " + strValue, e);
            throw new DataSerializationException("Deserialization failed for list representation.", e);
        }
        catch (ClassCastException e)
        {
            Log.w(TAG, "Deserialization failed for: " + strValue, e);
            throw new DataSerializationException("Deserialization failed for list representation.", e);
        }
    }

    protected synchronized <T> void setArrayList(String key, ArrayList<T> arrayList)
    {
        try
        {
            final Editor edit = prefs.edit();
            edit.putString(key, ObjectSerializer.serialize(arrayList));
            edit.commit();
        }
        catch (IOException e)
        {
            Log.w(TAG, "Srialization failed for: " + arrayList, e);
            throw new DataSerializationException("Serialization failed for list.", e);
        }
    }

    protected synchronized Token getToken(String key) throws OperationFailedException
    {
        String strValue = prefs.getString(key, null);
        if (strValue == null)
            return null;
        try
        {
            return (Token) ObjectSerializer.deserialize(strValue);
        }
        catch (IOException e)
        {
            Log.w(TAG, "Deserialization failed for: " + strValue, e);
            throw new OperationFailedException("Deserialization failed for list representation.", e);
        }
        catch (ClassCastException e)
        {
            Log.w(TAG, "Deserialization failed for: " + strValue, e);
            throw new OperationFailedException("Deserialization failed for list representation.", e);
        }
    }

    protected synchronized void setToken(String key, Token token) throws OperationFailedException
    {
        try
        {
            final Editor edit = prefs.edit();
            edit.putString(key, ObjectSerializer.serialize(token));
            edit.commit();
        }
        catch (IOException e)
        {
            Log.w(TAG, "Srialization failed for: " + token, e);
            throw new OperationFailedException("Serialization failed for list.", e);
        }
    }

    @SuppressWarnings("unchecked")
    protected synchronized <K, V> HashMap<K, V> getHashMap(String key)
    {
        String strValue = prefs.getString(key, null);
        if (strValue == null)
            return new HashMap<K, V>();
        try
        {
            return (HashMap<K, V>) ObjectSerializer.deserialize(strValue);
        }
        catch (IOException e)
        {
            Log.w(TAG, "Deserialization failed for: " + strValue, e);
            throw new DataSerializationException("Deserialization failed for list representation.", e);
        }
        catch (ClassCastException e)
        {
            Log.w(TAG, "Deserialization failed for: " + strValue, e);
            throw new DataSerializationException("Deserialization failed for list representation.", e);
        }
    }

    protected synchronized <K, V> void setHashMap(String key, HashMap<K, V> hashMap)
    {
        try
        {
            final Editor edit = prefs.edit();
            edit.putString(key, ObjectSerializer.serialize(hashMap));
            edit.commit();
        }
        catch (IOException e)
        {
            Log.w(TAG, "Srialization failed for: " + hashMap, e);
            throw new DataSerializationException("Serialization failed for map.", e);
        }
    }

    protected void clearValue(String... keys)
    {
        final Editor edit = prefs.edit();
        for (String key : keys)
            edit.remove(key);
        edit.commit();
    }
}
