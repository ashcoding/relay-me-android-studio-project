package com.tinywebgears.relayme.serverside;

import android.content.Context;
import android.content.Intent;

import com.codolutions.android.common.data.CachedData;
import com.codolutions.android.common.serverside.AbstractServerSideClient;
import com.codolutions.android.common.serverside.ServerSideClient;
import com.codolutions.android.common.serverside.data.RegistrationResponse;
import com.codolutions.android.common.serverside.data.VerificationResponse;
import com.tinywebgears.relayme.R;
import com.tinywebgears.relayme.common.Constants;
import com.tinywebgears.relayme.service.LogStoreHelper;

public class DefaultServerSideClient extends AbstractServerSideClient
{
    private static final String TAG = DefaultServerSideClient.class.getName();

    private static final DefaultServerSideClient INSTANCE = new DefaultServerSideClient();

    public static synchronized ServerSideClient get(Context context, String deviceId,
                                                    String username, String password)
    {
        INSTANCE.context = context;
        INSTANCE.deviceId = deviceId;
        INSTANCE.username = username;
        INSTANCE.password = password;
        return INSTANCE;
    }

    private DefaultServerSideClient()
    {
    }

    @Override
    protected Environment getEnvironment()
    {
        if (Constants.SERVER_SIDE_FORCE_LOCAL_DEV)
            return new LocalDevEnvironment();
        else if (Constants.SERVER_SIDE_FORCE_STAGING)
            return new StagingEnvironmentWrapper();
        return new ProductionEnvironmentWrapper();
    }

    @Override
    protected String getApplicationName()
    {
        return context.getString(R.string.lbl_app_name);
    }

    @Override
    protected void log(String message, Exception exception, Class<? extends CachedData> clazz)
    {
        if (clazz.equals(RegistrationResponse.class) || clazz.equals(VerificationResponse.class))
            LogStoreHelper.warn(context, message, exception);
        else
            LogStoreHelper.info(context, message, exception);
    }

    protected void handleCertPathValidatorException()
    {
        context.sendBroadcast(new Intent(Constants.SHOW_ALERT_BROADCAST_ACTION).putExtra(
                Constants.SHOW_ALERT_EXTRA_REPORT_ERROR, R.string.lbl_failed_to_sync_with_server));
    }

    private static class LocalDevEnvironment implements Environment
    {
        private static final String SERVER_SIDE_URL_LOCAL_DEV = "http://codolutionstest.com:3000/relayme/server";

        @Override
        public String getServerBaseUrl()
        {
            return SERVER_SIDE_URL_LOCAL_DEV;
        }

        @Override
        public String getEncryptionSalt()
        {
            return "RiE2MV5YI2ZodG1HbEI0aw==";
        }
    }
}
