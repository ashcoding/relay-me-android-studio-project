package com.tinywebgears.relayme;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;
import android.util.Pair;

import com.codolutions.android.common.data.CachedData;
import com.codolutions.android.common.serverside.data.KeysResponse;
import com.tinywebgears.relayme.app.ImapCache;
import com.tinywebgears.relayme.app.UserData;
import com.tinywebgears.relayme.email.EmailReceiver;
import com.tinywebgears.relayme.email.ImapEmailReceiver;
import com.tinywebgears.relayme.email.ServerCredentials;
import com.tinywebgears.relayme.model.ImapServer;
import com.tinywebgears.relayme.model.IncomingEmail;
import com.tinywebgears.relayme.model.ServerSecurity;
import com.tinywebgears.relayme.oauth.GoogleOauth2Helper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.scribe.model.Token;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class GMailImapTest {
    // Details of 'Relay ME Test' API application
    private static final String GOOGLE_API_CLIENT_ID = "740050674724-pktogo9rs0o1p69j32hnij0vogj0bnf0.apps.googleusercontent.com";
    private static final String GOOGLE_API_CLIENT_SECRET = "vtqDQvJv7ztpHcXpfg2lC6_r";
    private static final String GOOGLE_API_CALLBACK_URL = "http://localhost";
    // Token generated usign Playground for the test account 'houman001@gmail.com'
    private static final Token TOKEN = new Token(
            "ya29.GlsCBLf22Go4N-RYYCYmpLIqMdIJl0mREAdzqwvlxz_6fjULR7XHI96DdrQJHTPziLztRrGw1ua8B_VuvzhGHUt0Y46WoCVxaSElfoePz5LEzVVB2o4SA2S5_i1J",
            "1/yZ_BZIU2rhJ04FCHj75oEa0gkJTSqn_2HvLDNsStVX9gth8IPj0ED7aAdStG-Etm", new Date(), null);
    private static final String EMAIL = "houman001blog@gmail.com";
    private static final String TARGET_EMAIL = "houman001@gmail.com";

    private UserData userData;
    private Context context;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getTargetContext();
        userData = new UserData(context);
        KeysResponse keysResponse = new KeysResponse(CachedData.ONE_HOUR, CachedData.ONE_DAY, CachedData.ONE_HOUR);
        keysResponse.setResult(System.currentTimeMillis(), CachedData.Result.SUCCESS);
        keysResponse.setGoogleApiKey(GOOGLE_API_CLIENT_ID);
        keysResponse.setGoogleApiSecret(GOOGLE_API_CLIENT_SECRET);
        keysResponse.setGoogleCallbackUrl(GOOGLE_API_CALLBACK_URL);
        userData.setCachedKeysResponse(keysResponse);
    }

    @Test
    public void testReceiveEmails() throws Exception {
        Date lastCheckedDate = new Date(System.currentTimeMillis() - 1 * 60 * 60 * 1000);
        EmailReceiver.SubjectChecker subjectChecker = new EmailReceiver.SubjectChecker() {
            @Override
            public boolean validSubject(String subject) {
                return true;
            }
        };
        String xoauthToken = GoogleOauth2Helper.get(userData).buildXOAuthImap(EMAIL, TOKEN).first;
        ImapCache imapCache = new ImapCache();
        ImapEmailReceiver imapEmailReceiver = new ImapEmailReceiver(context, imapCache);
        ImapServer imapServer = new ImapServer("imap.gmail.com", 993, ServerSecurity.SSL, true,
                new ServerCredentials(xoauthToken));
        Pair<Date, Set<IncomingEmail>> receiveResult = imapEmailReceiver.receiveMail(imapServer,
                subjectChecker, "INBOX", EMAIL, Collections.singletonList(TARGET_EMAIL),
                lastCheckedDate, "INBOX");
        Set<IncomingEmail> incomingEmails = receiveResult.second;
        assertNotNull(incomingEmails);
    }

    @Test
    public void testReadMailboxNames() throws Exception {
        UserData userData = new UserData(context);
        String xoauthToken = GoogleOauth2Helper.get(userData).buildXOAuthImap(EMAIL, TOKEN).first;
        ImapCache imapCache = new ImapCache();
        ImapEmailReceiver imapEmailReceiver = new ImapEmailReceiver(context, imapCache);
        ImapServer imapServer = new ImapServer("imap.gmail.com", 993, ServerSecurity.SSL, true,
                new ServerCredentials(xoauthToken));
        List<String> mailboxNames = imapEmailReceiver.readMailboxes(imapServer);
        assertNotNull(mailboxNames);
        assertTrue(mailboxNames.contains("INBOX"));
        assertTrue(mailboxNames.contains("[Gmail]/All Mail"));
        assertTrue(mailboxNames.contains("[Gmail]/Trash"));
        assertTrue(mailboxNames.contains("[Gmail]/Spam"));
        assertTrue(mailboxNames.contains("[Gmail]/Drafts"));
        assertTrue(mailboxNames.contains("[Gmail]/Starred"));
        assertTrue(mailboxNames.contains("[Gmail]/Sent Mail"));
    }
}
