package com.tinywebgears.relayme.app;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import com.tinywebgears.relayme.common.Constants;
import com.tinywebgears.relayme.service.LogStoreHelper;
import com.tinywebgears.relayme.service.MessageStoreHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SystemStatus
{
    private static final String TAG = SystemStatus.class.getName();

    private final Context context;
    private final UserData userData;
    private final ImapCache gmailImapCache;

    SystemStatus(Context context)
    {
        this.context = context;
        LogStoreHelper.info(context, "SystemStatus constructor");
        userData = new UserData(context);
        gmailImapCache = new ImapCache();
    }

    public static boolean isLowMemory(Context context)
    {
        MemoryInfo memoryInfo = new MemoryInfo();
        ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryInfo(memoryInfo);
        if (memoryInfo.lowMemory)
            return true;
        return ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass() <= 16;
    }

    public static List<String> getAppsWithPermission(Context context, String permission, boolean includePackageName)
    {
        List<String> results = new ArrayList<String>();
        PackageManager packageManager = context.getPackageManager();
        for (PackageInfo pk : packageManager.getInstalledPackages(0))
        {
            boolean systemApp = (pk.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
            if (systemApp)
                continue;
            if (Constants.RELAY_ME_PACKAGE_NAME.equals(pk.packageName))
                continue;
            if (PackageManager.PERMISSION_GRANTED == packageManager.checkPermission(permission, pk.packageName))
                results.add(pk.applicationInfo.loadLabel(packageManager)
                        + (includePackageName ? " (" + pk.packageName + ")" : ""));
        }
        return results;
    }

    public String getUniqueId()
    {
        return getUserData().getUsername();
    }

    public boolean isNetworkAvailable(boolean forceCheck)
    {
        // TODO: Check network status.
        return true;
    }

    public ImapCache getGmailImapCache()
    {
        return gmailImapCache;
    }

    public boolean isOnline()
    {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public boolean isInternetBrowserFound(String uri)
    {
        PackageManager manager = context.getPackageManager();
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setData(Uri.parse(uri));
        List<ResolveInfo> infos = manager.queryIntentActivities(intent, PackageManager.GET_RESOLVED_FILTER);
        for (ResolveInfo info : infos)
        {
            IntentFilter filter = info.filter;
            if (filter != null && filter.hasAction(Intent.ACTION_VIEW) && filter.hasCategory(Intent.CATEGORY_BROWSABLE))
                return true;
        }
        return false;
    }

    public boolean isThereAnyPendingMessage()
    {
        // Check pre-conditions.
        if (!userData.getServiceConfiguration().isActive())
        {
            LogStoreHelper.info(this, context, "The application is not active.");
            return false;
        }
        return MessageStoreHelper.getPendingMessages(context).size() > 0;
    }

    public UserData getUserData()
    {
        return userData;
    }

    public String getEmailUri()
    {
        return "mailto:relayme@codolutions.com";
    }

    public String getWebsiteUri()
    {
        return "http://codolutions.com/";
    }

    public String getWikiUri()
    {
        return "http://codolutions.com/relayme/wiki.html";
    }

    public String getMarketUri()
    {
        return "market://details?id=" + context.getPackageName();
    }

    public String getMarketWebUri()
    {
        return "http://play.google.com/store/apps/details?id=" + context.getPackageName();
    }

    public String getGoogleOauthTokensUri()
    {
        return "https://security.google.com/settings/security/permissions";
    }

    public Map<String, Object> getAllForLogging()
    {
        Map<String, Object> all = new HashMap<String, Object>();
        all.put("Unique ID", getUniqueId());
        all.put("Online", isOnline());
        all.put("Any SMS received", getUserData().isAnySmsReceived());
        all.put("Any pending messages", isThereAnyPendingMessage());
        all.put("Is sending messages", getUserData().isSendingMessagesNow());
        all.put("Last contacts read", getUserData().getLastTimeContactsRead());
        all.put("Messaging configuration", getUserData().getMessagingConfiguration());
        all.put("Service configuration", getUserData().getServiceConfiguration());
        all.put("Is Open-source notice shown", getUserData().isOpenSourceNoticeShown());
        return all;
    }
}
