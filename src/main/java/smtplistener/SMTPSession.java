package smtplistener;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * An SMTP session for receiving one or more incoming emails from a
 * client.
 * <p/>
 * One of these threads is spawned each time a mailer
 * connects to the socket.
 */

public class SMTPSession implements Runnable {

  private static final Logger log_ = LoggerFactory.getLogger(SMTPSession.class);
  public static final int BUFFER_SIZE = 65536;
  private String smtpIdentifier_;
  private ServerSocket serverSocket_;
  private Socket clientSocket_;
  private BufferedReader fromClient_;
  private PrintWriter toClient_;
  private PushbackInputStream fromClientPushBack_;
  private String sender_ = null;
  private String recipient_ = null;
  private String subject_ = null;
  private String body_ = null;

  /**
   * A handler for an SMTP session.
   * Tested against postfix.
   *
   * @param smtpIdentifier what we should report our hostname as:
   *                       this must be different from what
   *                       <TT>mailer</TT> thinks it is called,
   *                       or it will fail with a loopback error
   * @param serverSocket   the socket on which <TT>mailer</TT>
   *                       has just connected
   */

  SMTPSession(String smtpIdentifier,
              ServerSocket serverSocket) {

    this.smtpIdentifier_ = smtpIdentifier;
    this.serverSocket_ = serverSocket;

  }

  public void run() {
    log_.debug(LogTracker.number("Running session"));

    try {
      try {
        // this blocks, waiting for input
        log_.debug(LogTracker.number("Waiting"));
        clientSocket_ = serverSocket_.accept();
      }
      catch (java.net.SocketException e) {
        log_.debug(LogTracker.number("Closed "));
        return;
      }
      fromClientPushBack_ =
          new PushbackInputStream(new BufferedInputStream(
              clientSocket_.getInputStream(),
              BUFFER_SIZE));

      fromClient_ = new BufferedReader(new InputStreamReader(fromClientPushBack_));

      toClient_ =
          new PrintWriter(new OutputStreamWriter(clientSocket_.getOutputStream(),
              "8859_1"),
              true);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
    reset();
    try {
      toClient("220 " + smtpIdentifier_ + " SMTP");
      boolean live = true;
      while (live) {
        String command = fromClient_.readLine().trim();

        if (command.regionMatches(true, 0, "HELO", 0, 4)) {
          toClient("250 " + smtpIdentifier_);
        }
        else {
          if (command.regionMatches(true, 0, "MAIL FROM:", 0, 10)) {
            mailFrom(command.substring(10).trim());
          }
          else {
            if (command.regionMatches(true, 0, "RCPT TO:", 0, 8)) {
              rcptTo(command.substring(8).trim());
            }
            else {
              if (command.regionMatches(true, 0, "DATA", 0, 4)) {
                data();
              }
              else {
                if (command.regionMatches(true, 0, "RSET", 0, 4)) {
                  reset();
                  toClient("250 Reset state");
                }
                else {
                  if (command.regionMatches(true, 0, "QUIT", 0, 4)) {
                    toClient("221 " + smtpIdentifier_ + " closing connection");
                    live = false;
                  }
                  // Not recognising EHLO is a good thing
                  // does it matter that we don't do VRFY?
                  else {
                    toClient("500 Command unrecognized: \"" + command + "\"");
                  }
                }
              }
            }
          }
        }
      }
    }
    catch (Exception e) {
      toClient("554 Sorry: something is wrong with this server---"
          + e.getMessage().replace('\n', ' ').replace('\r', ' '));
      log_.error("Post of message from `" + sender_ + "' failed:" + e.getMessage());
    }
    finally {
      try {
        clientSocket_.close();
      }
      catch (Exception e) {
        log_.error("Closing error:" + e.getMessage());
      }
    }
  }

  /**
   * Handle an SMTP <TT>RSET</TT> command, or generally otherwise tidy up.
   */

  private void reset() {
    sender_ = null;
    recipient_ = null;
    subject_ = null;
    body_ = null;
  }

  private String clean(final String address) {
    if (address.charAt(0) == '<') {
      return address.substring(1, address.length() - 1);
    }
    return address;
  }

  private void toClient(String line) {
    log_.debug(LogTracker.number(line));
    toClient_.println(line);
  }

  /**
   * Handle an SMTP <TT>MAIL FROM:</TT> command.
   *
   * @param addressFrom the address after the colon
   */
  private void mailFrom(String addressFrom) {
    sender_ = clean(addressFrom);
    log_.debug(LogTracker.number("Recipient:" + recipient_));
    toClient("250 " + addressFrom + "... Sender provisionally OK");
  }

  /**
   * Handle an SMTP <TT>RCPT TO:</TT> command.
   *
   * @param addressTo the address after the colon
   */

  private void rcptTo(String addressTo) throws Exception {
    recipient_ = clean(addressTo);
    log_.debug(LogTracker.number("Recipient:" + recipient_));
    toClient("250 Recipient OK");
  }

  /**
   * Handle an SMTP <TT>DATA</TT> command---post a message.
   */

  private void data() throws Exception {
    toClient("354 Enter mail, end with \".\" on a line by itself");
    String messageId = "123";
    messageAccept(new DotTerminatedInputStream(fromClientPushBack_));

    toClient("250 " + messageId + " Message accepted for delivery");
    log_.debug(LogTracker.number("Accepted message from " + sender_ + " to " + recipient_));
  }

  public void messageAccept(InputStream text) throws Exception {
    MimeMessage message = new MimeMessage(
        Session.getDefaultInstance(System.getProperties(), null),
        text);

    subject_ =
        message.getSubject() == null
            ? "(no subject_)"
            : message.getSubject();


    StringBuffer bodyText = new StringBuffer(100);

    Object content = message.getContent();
    if (content instanceof String) {
      bodyText.append((String) content);
    }
    else if (content instanceof Multipart) {
      Multipart parts = (Multipart) content;
      for (int p = 0; p < parts.getCount(); ++p) {
        Part part = parts.getBodyPart(p);

        // can it go in line?

        Object partContent = part.getContent();

        if (partContent instanceof String
            && part.getFileName() == null) {
          bodyText.append((String) partContent);
        }
        else {
          throw new RuntimeException("Attachment handling not implemented yet, you gave me a "
              + content.getClass()
              + ": " + content);
        }
      }
    }
    else {
      throw new RuntimeException("Unrecognised message content: " + content);
    }
    body_ = bodyText.toString();
  }

  public Email getEmail() {
    return new Email(sender_, recipient_, subject_, body_);
  }
}


