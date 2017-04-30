package com.tinywebgears.relayme.service;

import java.util.Date;
import java.util.HashMap;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

import com.codolutions.android.common.util.StringUtil;
import com.tinywebgears.relayme.app.CustomApplication;
import com.tinywebgears.relayme.app.UserData;
import com.tinywebgears.relayme.contentprovider.MessagesContentProvider;

// FIXME: TECHDEBT: Extend from BasicIntentService
public class ContactsIntentService extends IntentService
{
    private static final String TAG = ContactsIntentService.class.getName();
    public static final String ACTION_CODE_LOAD_CONTACTS = "load-contacts";
    public static final String ACTION_RESPONSE_LOAD_CONTACTS = ContactsIntentService.class.getCanonicalName()
            + ".RESPONSE_LOAD_CONTACTS";

    public static final String PARAM_OUT_RESULT = "out-result";
    public static final int VALUE_OUT_RESULT_SUCCESS = 0;
    public static final int VALUE_OUT_RESULT_ERROR = 1;

    public ContactsIntentService()
    {
        super("ContactsIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        String action = intent.getAction();
        if (ACTION_CODE_LOAD_CONTACTS.equalsIgnoreCase(action))
        {
            ContactsLoader contactsLoader = new ContactsLoader(getApplicationContext(),
                    ((CustomApplication) getApplication()).getUserData());
            contactsLoader.loadContacts();
        }
    }

    public static String findContactName(HashMap<String, String> contacts, String phoneNumber)
    {
        if (StringUtil.empty(phoneNumber))
            return null;
        String strippedPhoneNumber = stripPhoneNumber(phoneNumber);
        for (String number : contacts.keySet())
            if (Math.abs(number.length() - strippedPhoneNumber.length()) <= 3
                    && (strippedPhoneNumber.endsWith(number) || number.endsWith(strippedPhoneNumber)))
                return contacts.get(number);
        return null;
    }

    private static String stripPhoneNumber(String phoneNumber)
    {
        if (phoneNumber == null)
            return phoneNumber;
        String strippedPhoneNumber = phoneNumber.replaceAll("-", "").replaceAll("\\+", "").replaceAll(" ", "")
                .replace("(", "").replace(")", "");
        while (strippedPhoneNumber.startsWith("0"))
            strippedPhoneNumber = strippedPhoneNumber.substring(1);
        return strippedPhoneNumber;
    }

    public static class ContactsLoader
    {
        private final static int TIME_BEFORE_UPDATE_IN_MS = 3 * 60 * 1000;

        private final Context context;
        private final UserData userData;

        public ContactsLoader(final Context context, final UserData userData)
        {
            this.context = context;
            this.userData = userData;
        }

        public void loadContacts()
        {
            Date lastContactsRead = userData.getLastTimeContactsRead();
            Date now = new Date();
            if (lastContactsRead != null && (now.getTime() - lastContactsRead.getTime() < TIME_BEFORE_UPDATE_IN_MS))
            {
                LogStoreHelper.info(ContactsLoader.class, context, "No need to load contacts yet.");
                return;
            }
            LogStoreHelper.info(ContactsLoader.class, context, "Loading contacts...");
            try
            {
                HashMap<String, String> contacts = getContacts();
                userData.setContacts(contacts);
                userData.setLastTimeContactsRead(new Date());
                context.getContentResolver().notifyChange(MessagesContentProvider.CONTENT_URI, null);
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(ACTION_RESPONSE_LOAD_CONTACTS);
                broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
                broadcastIntent.putExtra(PARAM_OUT_RESULT, VALUE_OUT_RESULT_SUCCESS);
                context.sendBroadcast(broadcastIntent);
            }
            catch (Exception e)
            {
                LogStoreHelper.warn(ContactsLoader.class, context, "Unable to load contacts: " + e, e);
            }
        }

        private HashMap<String, String> getContacts()
        {
            Log.d(TAG, "Loading contacts now...");
            ContentResolver cr = context.getContentResolver();
            Cursor cur = null;
            try
            {
                cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
                HashMap<String, String> contactPhoneNumbers = new HashMap<String, String>();
                if (cur.getCount() > 0)
                {
                    while (cur.moveToNext())
                    {
                        String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                        String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                        if (Integer.parseInt(cur.getString(cur
                                .getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0)
                        {
                            Cursor pCur = null;
                            try
                            {
                                pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                        new String[] { id }, null);
                                while (pCur.moveToNext())
                                {
                                    String phone = pCur.getString(pCur
                                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                    if (!StringUtil.empty(phone))
                                    {
                                        // Log.v(TAG, "ID : " + id + ", name : " + name + ", phone: " + phone);
                                        contactPhoneNumbers.put(stripPhoneNumber(phone), name);
                                    }
                                }
                            }
                            finally
                            {
                                if (pCur != null)
                                    pCur.close();
                            }
                        }
                    }
                }
                return contactPhoneNumbers;
            }
            finally
            {
                if (cur != null)
                    cur.close();
            }
        }
    }
}
