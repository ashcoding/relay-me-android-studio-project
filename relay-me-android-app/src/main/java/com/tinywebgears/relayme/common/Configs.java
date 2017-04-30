package com.tinywebgears.relayme.common;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Configs
{
    private static final String CONFIG_FILE_NAME = "config.properties";

    public static String getProperty(Context context, String key) throws IOException {
        Properties properties = new Properties();
        AssetManager assetManager = context.getAssets();
        InputStream inputStream = assetManager.open(CONFIG_FILE_NAME);
        properties.load(inputStream);
        return properties.getProperty(key);
    }
}
