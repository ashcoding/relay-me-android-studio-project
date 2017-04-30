package com.tinywebgears.relayme.email;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.security.AccessController;
import java.security.Provider;
import java.security.Security;
import java.util.Date;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.smtp.AuthenticatingSMTPClient;
import org.apache.commons.net.smtp.SMTPReply;
import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.dom.Header;
import org.apache.james.mime4j.dom.MessageWriter;
import org.apache.james.mime4j.dom.Multipart;
import org.apache.james.mime4j.dom.TextBody;
import org.apache.james.mime4j.field.UnstructuredFieldImpl;
import org.apache.james.mime4j.field.address.AddressBuilder;
import org.apache.james.mime4j.message.BasicBodyFactory;
import org.apache.james.mime4j.message.BodyPart;
import org.apache.james.mime4j.message.DefaultMessageWriter;
import org.apache.james.mime4j.message.MessageImpl;
import org.apache.james.mime4j.message.MultipartImpl;
import org.apache.james.mime4j.stream.RawField;

import android.content.Context;
import android.util.Log;

import com.codolutions.android.common.exception.OperationFailedException;
import com.tinywebgears.relayme.BuildConfig;
import com.tinywebgears.relayme.exception.SmtpServerException;
import com.tinywebgears.relayme.model.SmtpServer;
import com.tinywebgears.relayme.service.LogStoreHelper;

public class SmtpEmailSender implements EmailSender
{
    private static final String TAG = SmtpEmailSender.class.getName();

    private static final boolean DEBUG_SMTP_PROTOCOL = BuildConfig.DEBUG;
    private static final Integer CONNECT_TIMEOUT_IN_MILLI_SECONDS = 24000;
    private static final Integer COMMAND_TIMEOUT_IN_MILLI_SECONDS = 12000;
    private static final String ENCODING = "UTF-8";
    private static final String OUR_HOSTNAME_FOR_SMTP = "codolutions.com";

    private final Context context;

    static
    {
        Security.addProvider(new JSSEProvider());
    }

    public SmtpEmailSender(Context context)
    {
        this.context = context;
    }

    @Override
    public void test(SmtpServer smtpServer) throws OperationFailedException
    {
        LogStoreHelper.info(this, context, "Testing SMTP server...");
        ServerCredentials serverCredentials = smtpServer.getCredentials();
        if (!serverCredentials.isValid())
            throw new OperationFailedException("Unsupported credentials provided to SMTP sender.");

        AuthenticatingSMTPClient client = null;
        try
        {
            client = login(smtpServer);
            client.logout();
        }
        catch (Exception e)
        {
            LogStoreHelper.warn(context, "Error connecting to SMTP server: " + e, e);
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
                LogStoreHelper.info(context, "Error disconnecting from SMTP server: " + e);
            }
        }
    }

    @Override
    public void sendMail(SmtpServer smtpServer, String senderId, String subject, String textHeading, String textToSend,
            String recipient, String cc) throws OperationFailedException
    {
        LogStoreHelper.info(this, context, "Sending email to " + recipient);

        AuthenticatingSMTPClient client = null;

        try
        {
            MessageImpl message = new MessageImpl();
            int subjectHash = subject.hashCode();
            Header header = message.getHeader();
            String fieldValue = "<\"" + senderId + "_" + subjectHash + "\"@relayme.tinywebgears.com>";
            header.setField(UnstructuredFieldImpl.PARSER.parse(new RawField("References", fieldValue),
                    DecodeMonitor.SILENT));
            header.setField(UnstructuredFieldImpl.PARSER.parse(new RawField("In-Reply-To", fieldValue),
                    DecodeMonitor.SILENT));
            message.setHeader(header);
            // Date and From are required fields
            message.setDate(new Date());
            message.setFrom(AddressBuilder.DEFAULT.parseMailbox(smtpServer.getEmail()));
            // String domainName = sender.substring(sender.lastIndexOf("@") + "@".length());
            // message.createMessageId(domainName);
            message.setTo(AddressBuilder.DEFAULT.parseMailbox(recipient));
            // https://bitbucket.org/codolutions/relay-me-android-app/issues/40/cc-has-stopped-working-for-gmail
            // if (!StringUtil.empty(cc))
            // message.setCc(AddressBuilder.DEFAULT.parseMailbox(cc));
            message.setSubject(subject);
            Multipart multipart = new MultipartImpl("alternative");
            BasicBodyFactory bodyFactory = new BasicBodyFactory();

            // Use UTF-8 to encode the specified text
            TextBody textBody = bodyFactory.textBody(textToSend, "UTF-8");
            BodyPart bodyPart = new BodyPart();
            bodyPart.setText(textBody);
            bodyPart.setContentTransferEncoding("quoted-printable");

            multipart.addBodyPart(bodyPart);
            message.setMultipart(multipart);
            MessageWriter messageWriter = new DefaultMessageWriter();
            OutputStream os = new ByteArrayOutputStream();
            messageWriter.writeMessage(message, os);
            String mimeMessageString = os.toString();

            client = login(smtpServer);
            client.setSoTimeout(COMMAND_TIMEOUT_IN_MILLI_SECONDS);

            client.setSender(smtpServer.getEmail());
            if (!SMTPReply.isPositiveCompletion(client.getReplyCode()))
            {
                LogStoreHelper.warn(this, context,
                        "Invalid sender: " + client.getReplyCode() + ":" + client.getReplyString(), null);
                throw new SmtpServerException("Invalid sender.");
            }
            client.addRecipient(recipient);
            if (!SMTPReply.isPositiveCompletion(client.getReplyCode()))
            {
                LogStoreHelper.warn(this, context,
                        "Invalid recepient: " + client.getReplyCode() + ":" + client.getReplyString(), null);
                throw new SmtpServerException("Invalid recepient.");
            }

            Writer writer = client.sendMessageData();
            if (writer != null)
            {
                writer.write(mimeMessageString);
                writer.close();
                client.completePendingCommand();
            }

            client.logout();
            LogStoreHelper.info(this, context, "Email is successfully sent, length: " + mimeMessageString.length());
        }
        catch (Exception e)
        {
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
                LogStoreHelper.info(context, "Error disconnecting from SMTP server: " + e);
            }
        }
    }

    private AuthenticatingSMTPClient login(SmtpServer smtpServer) throws Exception
    {
        LogStoreHelper.info(this, context, "Connecting to SMTP server...");

        ServerCredentials serverCredentials = smtpServer.getCredentials();
        if (!serverCredentials.isValid())
            throw new OperationFailedException("Unsupported credentials provided to SMTP sender.");

        AuthenticatingSMTPClient client = new AuthenticatingSMTPClient(smtpServer.getSecurity().isSecure() ? "SSL"
                : "TLS", smtpServer.getSecurity().isSecure());
        if (DEBUG_SMTP_PROTOCOL)
            client.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out), true));
        client.setDefaultTimeout(CONNECT_TIMEOUT_IN_MILLI_SECONDS);
        client.connect(smtpServer.getAddress(), smtpServer.getPort());
        boolean result = true;
        result = SMTPReply.isPositiveCompletion(client.ehlo(OUR_HOSTNAME_FOR_SMTP));
        if (smtpServer.getSecurity().isStartTls())
        {
            // Some SMTP server require HELO before STARTTLS, some after.
            result = client.execTLS();
            // Some servers need HELO before AUTH, WTF?
            if (result)
                result = SMTPReply.isPositiveCompletion(client.ehlo(OUR_HOSTNAME_FOR_SMTP));
        }
        if (!result)
            throw new OperationFailedException("Couldn't login to the SMTP server: " + client.getReplyString());
        LogStoreHelper.info(this, context, "Connected to SMTP server.");
        client.setCharset(Charset.forName(ENCODING));
        boolean authenticationResult;
        if (serverCredentials.isOauth())
        {
            Log.d(TAG, "Xoauth token: " + serverCredentials.getXoauth());
            client.auth(AuthenticatingSMTPClient.AUTH_METHOD.XOAUTH2, serverCredentials.getXoauth(), null);
            authenticationResult = SMTPReply.isPositiveCompletion(client.getReplyCode());
        }
        else if (serverCredentials.isUsernamePassword())
        {
            Log.d(TAG, "Username: " + serverCredentials.getUsername());
            client.auth(AuthenticatingSMTPClient.AUTH_METHOD.LOGIN, serverCredentials.getUsername(),
                    serverCredentials.getPassword());
            authenticationResult = SMTPReply.isPositiveCompletion(client.getReplyCode());
            if (!authenticationResult)
            {
                client.auth(AuthenticatingSMTPClient.AUTH_METHOD.PLAIN, serverCredentials.getUsername(),
                        serverCredentials.getPassword());
                authenticationResult = SMTPReply.isPositiveCompletion(client.getReplyCode());
            }
        }
        else
            throw new OperationFailedException("Unsupported authentication method: " + serverCredentials);
        if (!authenticationResult)
        {
            LogStoreHelper.warn(this, context,
                    "SMTP Authentication failed: " + client.getReplyCode() + ":" + client.getReplyString(), null);
            throw new SmtpServerException("SMTP Authentication failed.");
        }
        LogStoreHelper.info(this, context, "Authenticated successfully to SMTP server.");
        return client;
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
