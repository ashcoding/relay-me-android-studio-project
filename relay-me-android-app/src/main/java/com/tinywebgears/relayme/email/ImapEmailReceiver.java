package com.tinywebgears.relayme.email;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.AccessController;
import java.security.Provider;
import java.security.Security;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Pattern;

import javax.mail.internet.MimeUtility;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.imap.AuthenticatingIMAPClient;
import org.apache.commons.net.util.Base64;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.dom.Entity;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.MessageBuilder;
import org.apache.james.mime4j.dom.TextBody;
import org.apache.james.mime4j.message.BodyPart;
import org.apache.james.mime4j.message.DefaultMessageBuilder;
import org.apache.james.mime4j.message.MultipartImpl;

import android.content.Context;
import android.util.Log;

import com.codolutions.android.common.exception.OperationFailedException;
import com.codolutions.android.common.util.DateUtil;
import com.codolutions.android.common.util.Pair;
import com.codolutions.android.common.util.StringUtil;
import com.tinywebgears.relayme.BuildConfig;
import com.tinywebgears.relayme.app.ImapCache;
import com.tinywebgears.relayme.exception.ImapServerException;
import com.tinywebgears.relayme.model.ImapServer;
import com.tinywebgears.relayme.model.IncomingEmail;
import com.tinywebgears.relayme.service.LogStoreHelper;

public class ImapEmailReceiver implements EmailReceiver
{
    private static final String TAG = ImapEmailReceiver.class.getName();

    private static final boolean DEBUG_IMAP_PROTOCOL = BuildConfig.DEBUG;
    private static final String ENCODING = "UTF-8";
    private static final Integer CONNECT_TIMEOUT_IN_MILLI_SECONDS = 24000;
    private static final Integer COMMAND_TIMEOUT_IN_MILLI_SECONDS = 12000;
    public static final String INBOX_FOLDER = "INBOX";
    private static final String REPLY_HEADER_1_STARTING_REGEX = "\\s*-----Original Message-----.*";
    private static final Pattern REPLY_HEADER_1_STARTING_PATTERN = Pattern.compile(REPLY_HEADER_1_STARTING_REGEX,
            Pattern.CASE_INSENSITIVE);
    private static final String REPLY_HEADER_2_STARTING_REGEX = "\\s*On.*";
    private static final Pattern REPLY_HEADER_2_STARTING_PATTERN = Pattern.compile(REPLY_HEADER_2_STARTING_REGEX,
            Pattern.CASE_INSENSITIVE);
    private static final String REPLY_HEADER_2_REGEX_FORMAT = REPLY_HEADER_2_STARTING_PATTERN.pattern()
            + ".*<%s>.*wrote:\\s*";
    private static final String REPLY_HEADER_3_STARTING_REGEX = "^\\s*20[0-9][0-9]-[0-1]?[0-9]-[0-3]?[0-9] [0-2]?[0-9]:[0-5]?[0-9]";
    private static final Pattern REPLY_HEADER_3_STARTING_PATTERN = Pattern.compile(REPLY_HEADER_3_STARTING_REGEX,
            Pattern.CASE_INSENSITIVE);
    private static final String REPLY_HEADER_3_REGEX_FORMAT = REPLY_HEADER_3_STARTING_REGEX + ".*<%s>.*:\\s*";
    // Transient state
    private final Context context;
    // Cache for visited messages so that we don't inspect every message every time
    private final ImapCache imapCache;

    static
    {
        Security.addProvider(new JSSEProvider());
    }

    public ImapEmailReceiver(Context context, ImapCache imapCache)
    {
        this.context = context;
        this.imapCache = imapCache;
    }

    public void test(ImapServer imapServer) throws OperationFailedException
    {
        LogStoreHelper.info(this, context, "Testing IMAP server...");
        ServerCredentials serverCredentials = imapServer.getCredentials();
        if (!serverCredentials.isValid())
            throw new OperationFailedException("Unsupported credentials provided to IMAP sender.");

        AuthenticatingIMAPClient client = null;
        try
        {
            client = login(imapServer);
            client.logout();
        }
        catch (Exception e)
        {
            LogStoreHelper.warn(context, "Error connecting to IMAP server: " + e, e);
            throw new OperationFailedException(e.getMessage(), e);
        }
        finally
        {
            try
            {
                if (client != null && client.isConnected())
                    client.disconnect();
            }
            catch (Exception e)
            {
                LogStoreHelper.info(context, "Error disconnecting from IMAP server: " + e);
            }
        }
    }

    @Override
    public List<String> readMailboxes(ImapServer imapServer) throws OperationFailedException
    {
        LogStoreHelper.info(this, context, "Listing mailboxes now...");
        AuthenticatingIMAPClient client = null;
        try
        {
            client = login(imapServer);
            client.setSoTimeout(COMMAND_TIMEOUT_IN_MILLI_SECONDS);

            if (!client.capability())
                throw new ImapServerException("Capability command failed: " + client.getReplyString());
            if (!client.list("\"\"", "\"*\""))
                throw new ImapServerException("Mailbox listing failed: " + client.getReplyString());
            List<String> mailboxes = new ArrayList<String>();
            for (String line : client.getReplyStrings())
            {
                if (line.startsWith("* LIST") && line.contains("\\HasNoChildren") && !line.contains("\\Noselect"))
                {
                    String[] tokens = line.split("\"");
                    mailboxes.add(tokens[tokens.length - 1].trim());
                }
            }
            client.logout();
            return mailboxes;
        }
        catch (Exception e)
        {
            throw new OperationFailedException("Error reading mailbox names", e);
        }
        finally
        {
            try
            {
                if (client != null)
                    client.disconnect();
            }
            catch (IOException e)
            {
                LogStoreHelper.info(context, "Error disconnecting from IMAP server: " + e);
            }
        }
    }

    @Override
    public Pair<Date, Set<IncomingEmail>> receiveMail(ImapServer imapServer, SubjectChecker subjectChecker,
            String emailSubjectMagicWord, String emailAddress, Collection<String> fromAddresses, Date lastEmailDate,
            String mailboxName) throws OperationFailedException
    {
        LogStoreHelper.info(this, context, "Checking for emails, last message was seen on: " + lastEmailDate + " ("
                + DateUtil.secondsPassedSince(lastEmailDate) + " secs ago), mailbox: " + mailboxName);
        Date lastTimestamp = lastEmailDate;
        AuthenticatingIMAPClient client = null;
        try
        {
            client = login(imapServer);
            client.setSoTimeout(COMMAND_TIMEOUT_IN_MILLI_SECONDS);

            if (!client.capability())
                throw new ImapServerException("Capability command failed: " + client.getReplyString());
            String quotedMailbox = "\"" + (mailboxName == null ? INBOX_FOLDER : mailboxName) + "\"";
            if (!client.select(quotedMailbox))
                throw new ImapServerException("Mailbox selection failed: " + client.getReplyString());
            Set<IncomingEmail> incomingMessages = new HashSet<IncomingEmail>();
            Date startDate = DateUtil.addDays(lastEmailDate, -1);
            DateFormat startDateFormat = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);
            startDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            String lastEmailDateStr = startDateFormat.format(startDate);

            String searchFilter = "SUBJECT \"" + emailSubjectMagicWord + "\" SINCE \"" + lastEmailDateStr + "\"";
            LogStoreHelper.info(this, context, "Searching with filter: " + searchFilter);
            if (!client.search(searchFilter))
                throw new ImapServerException("Search failed: " + client.getReplyString());
            String[] headerResponse = client.getReplyStrings();
            Set<String> messageIds = getMessageIds(headerResponse[0]);
            LogStoreHelper.info(this, context, messageIds.size() + " messages returned by search: " + messageIds);
            Map<String, Date> oldMessages = new HashMap<String, Date>();
            Set<String> visitedMessageIds = imapCache.getVisitedMessageIds(emailAddress, mailboxName);
            LogStoreHelper.info(this, context, visitedMessageIds.size() + " messages already seen: "
                    + (visitedMessageIds.size() < 100 ? visitedMessageIds : "set is too big to show."));
            for (String messageId : messageIds)
            {
                if (visitedMessageIds.contains(messageId))
                {
                    // Log.d(TAG, "Message already visited: " + messageId);
                    continue;
                }
                LogStoreHelper.info(this, context, "Now checking out message " + messageId);
                if (!client.fetch(messageId, "BODY.PEEK[HEADER]"))
                    throw new ImapServerException("Fetch failed: " + client.getReplyString());
                String[] bodyHeaderResponse = client.getReplyStrings();
                Map<String, String> values = findValues(bodyHeaderResponse, "Subject", "MIME-Version", "Content-Type",
                        "Content-Transfer-Encoding", "Date", "From");
                String dateStr = values.get("Date");
                String subject = parseSubject(values.get("Subject"));
                String mimeVersion = values.get("MIME-Version");
                String contentType = values.get("Content-Type");
                String contentTransferEncoding = values.get("Content-Transfer-Encoding");
                String senderAddress = values.get("From");
                LogStoreHelper.info(this, context, "Email found, from: " + senderAddress + " subject: " + subject + " date: " + dateStr);
                visitedMessageIds.add(messageId);
                boolean emailMatching = false;
                for (String fromAddress : fromAddresses)
                {
                    if ("*".equals(fromAddress) || senderAddress.toLowerCase().contains(fromAddress.toLowerCase()))
                    {
                        emailMatching = true;
                        break;
                    }
                }
                if (!emailMatching)
                {
                    LogStoreHelper.info(this, context, "Not interested in emails from: " + senderAddress);
                    continue;
                }
                Date messageDate = null;
                try
                {
                    // TODO: Parse dates of other servers.
                    messageDate = parseGmailDate(dateStr);
                    if (!messageDate.after(lastEmailDate))
                    {
                        Log.d(TAG, "Ignoring old message " + messageId + ", for "
                                + ((lastEmailDate.getTime() - messageDate.getTime()) / 1000) + " seconds");
                        oldMessages.put(messageId, messageDate);
                        continue;
                    }
                    if (messageDate.after(lastTimestamp))
                        lastTimestamp = messageDate;
                }
                catch (ParseException e)
                {
                    LogStoreHelper.warn(this, context, "Invalid date: " + dateStr + ", ID: " + messageId, e);
                    continue;
                }

                if (!subjectChecker.validSubject(subject))
                {
                    LogStoreHelper.info(this, context, "Message " + messageId + " does not have a valid subject: "
                            + subject + ", date: " + messageDate);
                    continue;
                }
                LogStoreHelper.info(this, context, "Message " + messageId + " seems to match our criteria, subject: "
                        + subject + ", date: " + messageDate);
                if (!client.fetch(messageId, "BODY[TEXT]"))
                    throw new ImapServerException("Fetch failed: " + client.getReplyString());
                String[] bodyResponse = client.getReplyStrings();
                StringBuilder mimeStringBuffer = new StringBuilder();
                mimeStringBuffer.append("MIME-Version: ").append(mimeVersion).append("\n");
                mimeStringBuffer.append("Content-Type: ").append(contentType).append("\n");
                if (!StringUtil.empty(contentTransferEncoding))
                    mimeStringBuffer.append("Content-Transfer-Encoding: ").append(contentTransferEncoding).append("\n");
                mimeStringBuffer.append("\n");
                // First line and last two lines are not part of the message.
                for (int i = 1; i < bodyResponse.length - 2; i++)
                    mimeStringBuffer.append(bodyResponse[i]).append("\n");
                // LogStoreHelper.info(context, "MIME String: " + mimeStringBuffer.toString());
                String messageText = parseMimeString(context, mimeStringBuffer.toString(), emailAddress);
                if (messageText != null)
                {
                    IncomingEmail incomingEmail = new IncomingEmail(messageId, messageDate, subject, messageText);
                    incomingMessages.add(incomingEmail);
                }
                else
                {
                    LogStoreHelper.warn(this, context, "Couldn't parse MIME message, ID: " + messageId + " subject: "
                            + subject + " at: " + messageDate, null);
                    LogStoreHelper.info(this, context, "MIME string buffer: " + mimeStringBuffer.toString());
                }
            }
            imapCache.removeOldEntries(messageIds);
            if (oldMessages.size() > 0)
            {
                Date lastIgnoredMessageDate = null;
                for (Date date : oldMessages.values())
                    if (lastIgnoredMessageDate == null || lastIgnoredMessageDate.before(date))
                        lastIgnoredMessageDate = date;
                LogStoreHelper.info(this, context, "Old messages ignored: " + oldMessages.keySet() + ", last one at "
                        + lastIgnoredMessageDate);
            }

            if (incomingMessages.size() < 1)
                LogStoreHelper.info(this, context, "Couldn't find any incoming email.");
            client.logout();
            return Pair.create(lastTimestamp, incomingMessages);
        }
        catch (Exception e)
        {
            throw new OperationFailedException("Error receiving emails from IMAP server", e);
        }
        finally
        {
            try
            {
                if (client != null)
                    client.disconnect();
            }
            catch (IOException e)
            {
                LogStoreHelper.info(context, "Error disconnecting from IMAP server: " + e);
            }
        }
    }

    private AuthenticatingIMAPClient login(ImapServer imapServer) throws Exception
    {
        LogStoreHelper.info(this, context, "Connecting to IMAP server...");

        ServerCredentials serverCredentials = imapServer.getCredentials();
        if (!serverCredentials.isValid())
            throw new OperationFailedException("Unsupported credentials provided to IMAP sender.");

        AuthenticatingIMAPClient client = new AuthenticatingIMAPClient(imapServer.getSecurity().isSecure() ? "SSL"
                : "TLS", imapServer.getSecurity().isSecure());
        if (DEBUG_IMAP_PROTOCOL)
            client.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out), true));
        client.setDefaultTimeout(CONNECT_TIMEOUT_IN_MILLI_SECONDS);
        client.connect(imapServer.getAddress(), imapServer.getPort());
        if (imapServer.getSecurity().isStartTls())
            if (!client.execTLS())
                throw new OperationFailedException("Couldn't login to the SMTP server: " + client.getReplyString());
        client.setCharset(Charset.forName(ENCODING));
        boolean authenticationResult;
        if (serverCredentials.isOauth())
        {
            Log.d(TAG, "Xoauth token: " + serverCredentials.getXoauth());
            String base64XoauthToken = Base64.encodeBase64StringUnChunked(serverCredentials.getXoauth().getBytes(
                    Charset.forName(ENCODING)));
            authenticationResult = client.authenticate(AuthenticatingIMAPClient.AUTH_METHOD.XOAUTH2, base64XoauthToken,
                    null);
        }
        else if (serverCredentials.isUsernamePassword())
        {
            Log.d(TAG, "Username: " + serverCredentials.getUsername());
            authenticationResult = client.authenticate(AuthenticatingIMAPClient.AUTH_METHOD.LOGIN,
                    serverCredentials.getUsername(), serverCredentials.getPassword());
            if (!authenticationResult)
                authenticationResult = client.authenticate(AuthenticatingIMAPClient.AUTH_METHOD.PLAIN,
                        serverCredentials.getUsername(), serverCredentials.getPassword());
        }
        else
            throw new OperationFailedException("Unsupported authentication method: " + imapServer.getCredentials());
        if (!authenticationResult)
            throw new ImapServerException("IMAP Authentication failed: " + client.getReplyString());
        LogStoreHelper.info(this, context, "Authenticated successfully to IMAP server.");
        return client;
    }

    private String parseMimeString(Context context, String mimeString, String emailAddress)
    {
        try
        {
            InputStream mimeStream = new ByteArrayInputStream(mimeString.getBytes());
            MessageBuilder builder = new DefaultMessageBuilder();
            Message message = builder.parseMessage(mimeStream);
            if (message.getBody() instanceof TextBody)
            {
                return parseTextBody((TextBody) message.getBody(), message.getContentTransferEncoding(), emailAddress);
            }
            else if (message.getBody() instanceof MultipartImpl)
            {
                MultipartImpl multipart = (MultipartImpl) message.getBody();
                boolean alternativeFound = multipart.getSubType().equals("alternative");
                while (!alternativeFound && multipart != null)
                {
                    List<Entity> mparts = multipart.getBodyParts();
                    MultipartImpl multipartRelated = null;
                    for (Entity mpart : mparts)
                    {
                        BodyPart mbodyPart = (BodyPart) mpart;
                        if (mbodyPart.getMimeType().equalsIgnoreCase("multipart/alternative"))
                        {
                            multipart = (MultipartImpl) mbodyPart.getBody();
                            alternativeFound = true;
                            break;
                        }
                        else if (mbodyPart.getMimeType().equalsIgnoreCase("multipart/related"))
                            multipartRelated = (MultipartImpl) mbodyPart.getBody();
                    }
                    if (alternativeFound)
                        break;
                    multipart = multipartRelated;
                }
                if (alternativeFound)
                {
                    boolean textFound = false;
                    List<Entity> parts = multipart.getBodyParts();
                    for (Entity part : parts)
                    {
                        BodyPart bodyPart = (BodyPart) part;
                        if (!bodyPart.getMimeType().equalsIgnoreCase("text/plain"))
                            continue;
                        textFound = true;
                        return parseTextBody((TextBody) bodyPart.getBody(), bodyPart.getContentTransferEncoding(),
                                emailAddress);
                    }
                    if (!textFound)
                    {
                        LogStoreHelper.warn(this, context,
                                "Message not supported, I couldn't find a text/plain section: " + mimeString, null);
                        LogStoreHelper.info(this, context, "MIME string buffer: " + mimeString);
                    }
                }
                else
                {
                    LogStoreHelper.warn(this, context,
                            "Message not supported, I couldn't find a multipart/alternative section: " + mimeString,
                            null);
                    LogStoreHelper.info(this, context, "MIME string buffer: " + mimeString);
                }
            }
            return null;
        }
        catch (IOException e)
        {
            LogStoreHelper.warn(this, context, "IO exception caught while checking for new emails.", e);
            LogStoreHelper.info(this, context, "MIME string buffer: " + mimeString);
            return null;
        }
        catch (MimeException e)
        {
            LogStoreHelper.warn(this, context, "Mime exception caught while checking for new emails: " + mimeString, e);
            LogStoreHelper.info(this, context, "MIME string buffer: " + mimeString);
            return null;
        }
        catch (RuntimeException e)
        {
            LogStoreHelper.warn(this, context, "Runtime exception caught from the the underlying library.", e);
            LogStoreHelper.info(this, context, "MIME string buffer: " + mimeString);
            return null;
        }
    }

    public static String parseTextBody(TextBody textBody, String contentTransferEncoding, String originalSenderEmail)
            throws IOException
    {
        return extractText(
                readInputStreamAsString(textBody.getInputStream(), textBody.getMimeCharset(), contentTransferEncoding),
                originalSenderEmail);
    }

    public static String parseSubject(String subject)
    {
        try
        {
            String decodedSubject = MimeUtility.decodeText(subject);
            return decodedSubject;
        }
        catch (UnsupportedEncodingException e)
        {
            Log.d(TAG, "Unable to decode subject: " + subject);
            return subject;
        }
    }

    public static Date parseGmailDate(String dateStr) throws ParseException
    {
        try
        {
            // An example: Thu, 29 Aug 2013 18:07:23 +0430
            DateFormat gmailDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.US);
            gmailDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            return gmailDateFormat.parse(dateStr);
        }
        catch (ParseException e1)
        {
            try
            {
                // An example: 20 Jun 2014 02:42:12 -0700
                DateFormat gmailDateFormat = new SimpleDateFormat("d MMM yyyy HH:mm:ss Z", Locale.US);
                gmailDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                return gmailDateFormat.parse(dateStr);
            }
            catch (ParseException e2)
            {
                throw e2;
            }
        }
    }

    public static List<String> getDisplayMailboxNames(Context context, Collection<String> mailboxNames)
    {
        List<String> displayNames = new ArrayList<String>();
        for (String mailboxName : mailboxNames)
        {
            try
            {
                if (StringUtil.empty(mailboxName))
                {
                    displayNames.add("");
                    continue;
                }
                boolean ascii = true;
                int lastIndex = 0;
                StringBuilder sb = new StringBuilder();
                for (int index = 0; index < mailboxName.length(); index++)
                {
                    char ch = mailboxName.charAt(index);
                    if (ascii && ch == '&')
                    {
                        sb.append(mailboxName.substring(lastIndex, index));
                        lastIndex = index + 1;
                        ascii = false;
                    }
                    else if (!ascii && ch == '-')
                    {
                        String base64Str = mailboxName.substring(lastIndex, index);
                        base64Str = base64Str.replaceAll(",", "/");
                        byte[] decodedBytes = Base64.decodeBase64(base64Str);
                        String decodedStr = new String(decodedBytes, "UTF-16");
                        sb.append(decodedStr);
                        lastIndex = index + 1;
                        ascii = true;
                    }
                }
                sb.append(mailboxName.substring(lastIndex, mailboxName.length()));
                displayNames.add(sb.toString());
            }
            catch (UnsupportedEncodingException e)
            {
                LogStoreHelper.warn(context, "Unable to decode mailbox name " + mailboxName, e);
                displayNames.add(mailboxName);
            }
        }
        return displayNames;
    }

    public static String readInputStreamAsString(InputStream in, String charSet, String contentTransferEncoding)
            throws IOException
    {
        BufferedInputStream bis = new BufferedInputStream(in);
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int result = bis.read();
        while (result != -1)
        {
            byte b = (byte) result;
            buf.write(b);
            result = bis.read();
        }
        return buf.toString(charSet);
    }

    public static String extractText(String text, String originalSenderEmail)
    {
        StringBuilder justReply = new StringBuilder();
        String lineSeparator = System.getProperty("line.separator");
        String[] lines = text.split(System.getProperty("line.separator"));
        for (int i = 0; i < lines.length; i++)
        {
            String line = lines[i].replaceAll("[\\r\\n]", " ");
            String extendedLine = line;
            if (i > 0)
                extendedLine = lines[i - 1].replaceAll("[\\r\\n]", " ") + " " + line;
            if (linesMatchReplyHeader(line, extendedLine, originalSenderEmail))
                break;
            justReply.append(lines[i]).append(lineSeparator);
        }
        String strippedText = justReply.toString();
        while (strippedText.endsWith("\n"))
        {
            strippedText = strippedText.substring(0, strippedText.length() - "\n".length());
            if (strippedText.endsWith("\r"))
                strippedText = strippedText.substring(0, strippedText.length() - "\r".length());
        }
        return strippedText;
    }

    public static boolean linesMatchReplyHeader(String line, String extendedLine, String originalSenderEmail)
    {
        if (REPLY_HEADER_1_STARTING_PATTERN.matcher(line).find()
                || REPLY_HEADER_1_STARTING_PATTERN.matcher(extendedLine).find())
            return true;
        if (REPLY_HEADER_2_STARTING_PATTERN.matcher(line).find()
                || REPLY_HEADER_2_STARTING_PATTERN.matcher(extendedLine).find())
        {
            Pattern replyHeader2 = Pattern.compile(String.format(REPLY_HEADER_2_REGEX_FORMAT, originalSenderEmail),
                    Pattern.CASE_INSENSITIVE);
            if (replyHeader2.matcher(line).find() || replyHeader2.matcher(extendedLine).find())
                return true;
        }
        if (REPLY_HEADER_3_STARTING_PATTERN.matcher(line).find()
                || REPLY_HEADER_3_STARTING_PATTERN.matcher(extendedLine).find())
        {
            Pattern replyHeader3 = Pattern.compile(String.format(REPLY_HEADER_3_REGEX_FORMAT, originalSenderEmail),
                    Pattern.CASE_INSENSITIVE);
            if (replyHeader3.matcher(line).find() || replyHeader3.matcher(extendedLine).find())
                return true;
        }
        return false;
    }

    private Set<String> getMessageIds(String searchResult)
    {
        int index = searchResult.indexOf("SEARCH") + "SEARCH".length();
        String idsStr = searchResult.substring(index);
        return StringUtil.splitString(idsStr);
    }

    private Map<String, String> findValues(String[] lines, String... keys)
    {
        Map<String, String> values = new HashMap<String, String>();
        String currentLine = "";
        boolean recordComplete = true;
        for (int i = 0; i < lines.length; i++)
        {
            String line = lines[i].trim();
            if (recordComplete)
                currentLine = line;
            else
                currentLine = currentLine + line;
            recordComplete = !currentLine.trim().endsWith(";");
            if (recordComplete)
            {
                for (String key : keys)
                {
                    String prefix = key + ": ";
                    if (currentLine.startsWith(prefix))
                        values.put(key, currentLine.substring(prefix.length()));
                }
            }
        }
        return values;
    }

    @SuppressWarnings("serial")
    public static final class JSSEProvider extends Provider
    {
        public JSSEProvider()
        {
            super("HarmonyJSSE", 1.0, "Harmony JSSE Provider");
            AccessController.doPrivileged(new java.security.PrivilegedAction<Void>()
            {
                @Override
                public Void run()
                {
                    put("SSLContext.TLS", "org.apache.harmony.xnet.provider.jsse.SSLContextImpl");
                    put("Alg.Alias.SSLContext.TLSv1", "TLS");
                    put("KeyManagerFactory.X509", "org.apache.harmony.xnet.provider.jsse.KeyManagerFactoryImpl");
                    put("TrustManagerFactory.X509", "org.apache.harmony.xnet.provider.jsse.TrustManagerFactoryImpl");
                    return null;
                }
            });
        }
    }
}
