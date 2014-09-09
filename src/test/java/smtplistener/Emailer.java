package smtplistener;

import java.io.File;
import java.io.IOException;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import smtplistener.Email;


/**
 * Send an email to one or more recipients with or without attachments.
 *
 * @author timp
 * @since 19/12/13
 */
public class Emailer {


  private Emailer() {
  }

  /**
   * Send the email from this machine.
   */
  public static void send(Email email)
      throws IOException {
    send("localhost", email.getSender(), email.getRecipient(),
         email.getSubject(), email.getMessage());
  }

  /**
   * Send the email.
   *
   * @param smtpServer name of SMTP server to use
   * @param from       email address and optionally name of sender
   * @param to         email address and optionally name of recipient
   * @param subject    subject of message
   * @param text       text body of email
   */
  public static void send(String smtpServer, String from, String to,
                          String subject, String text)
      throws IOException {
    send(smtpServer, from, to, from, subject, text);
  }

  /**
   * Send the email.
   *
   * @param smtpServer name of SMTP server to use
   * @param from       email address and optionally name of sender
   * @param to         email address and optionally name of recipient
   * @param replyto    email address and optionally name to reply to
   * @param subject    subject of message
   * @param text       text body of email
   */
  public static void send(String smtpServer, String from, String to,
                          String replyto, String subject, String text)
    {
    File[] empty = {};
    sendWithAttachments(smtpServer, from, to, replyto, subject, text, empty);
  }

  /**
   * Send message with attachments.
   *
   * @param smtpServer  name of SMTP server to use
   * @param from        email address and optionally name of sender
   * @param to          email address and optionally name of recipient
   * @param replyTo     email address and optionally name to reply to
   * @param subject     subject of message
   * @param text        text body of email
   * @param attachments Array of files to attach
   */
  public static void sendWithAttachments(String smtpServer,
                                         String from,
                                         String to,
                                         String replyTo,
                                         String subject,
                                         String text,
                                         File[] attachments)
    {

    // Construct the message
    Message message = initialiseMessage(smtpServer, from, to, replyTo, subject);
    try {
      // create and fill the first, text message part
      MimeBodyPart mbp1 = new MimeBodyPart();
      mbp1.setText(text);
      Multipart mp = new MimeMultipart();
      mp.addBodyPart(mbp1);
      for (File f : attachments) {
        if (f != null) {
          // create the second message part
          MimeBodyPart mbp2 = new MimeBodyPart();
          // attach the file to the message
          FileDataSource fds = new FileDataSource(f);
          mbp2.setDataHandler(new DataHandler(fds));
          mbp2.setFileName(fds.getName());
          mp.addBodyPart(mbp2);
        }
      }
      // add the Multipart to the message
      message.setContent(mp);
    }
    catch (Exception e) {
      throw new RuntimeException("Problem creating email message", e);
    }
    post(message);
  }

  /**
   * Send HTML message with attachments.
   *
   * @param smtpServer  name of SMTP server to use
   * @param from        email address and optionally name of sender
   * @param to          email address and optionally name of recipient
   * @param replyto     email address and optionally name to reply to
   * @param subject     subject of message
   * @param plainText   text body of email
   * @param htmlText    HTML body of email
   * @param referenced  Array of Files referenced withing the HTML body
   * @param attachments Array of files to attach
   */
  public static void sendAsHtmlWithAttachments(String smtpServer,
                                               String from,
                                               String to,
                                               String replyto,
                                               String subject,
                                               String plainText,
                                               String htmlText,
                                               File[] referenced,
                                               File[] attachments)
      {

    // Construct the message
    Message message = initialiseMessage(smtpServer, from, to, replyto, subject);
    try {
      Multipart mp = new MimeMultipart("related");
      MimeBodyPart mbp1 = new MimeBodyPart();
      //mbp1.setText(plainText);
      mbp1.setContent(plainText, "text/plain");
      mp.addBodyPart(mbp1);
      MimeBodyPart mbp2 = new MimeBodyPart();
      mbp2.setContent(htmlText, "text/html");
      mp.addBodyPart(mbp2);

      if (referenced != null) {
        for (File f : referenced) {
          if (f != null) {
            MimeBodyPart mbp3 = new MimeBodyPart();
            FileDataSource fds = new FileDataSource(f);
            mbp3.setDataHandler(new DataHandler(fds));
            mbp3.setFileName(fds.getName());
            mp.addBodyPart(mbp3);
          }
        }
      }
      if (attachments != null) {
        for (File f : attachments) {
          if (f != null) {
            MimeBodyPart mbp4 = new MimeBodyPart();
            FileDataSource fds = new FileDataSource(f);
            mbp4.setDataHandler(new DataHandler(fds));
            mbp4.setFileName(fds.getName());
            mp.addBodyPart(mbp4);
          }
        }
      }
      // add the Multipart to the message
      message.setContent(mp);
    }
    catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Problem creating message", e);
    }
    // send the message
    post(message);
  }

  private static Message initialiseMessage(String smtpServer, String from,
                                           String to, String replyto, String subject) {
    // Create the JavaMail session
    // The properties for the whole system, sufficient to send a mail
    // and much more besides.
    java.util.Properties properties = System.getProperties();
    properties.put("mail.smtp.host", smtpServer);
    // this is required if InetAddress.getLocalHost().getHostName()  returns null
    // which it does nor me in Eclipse
    properties.put("mail.smtp.localhost", smtpServer);
    Session session = Session.getInstance(properties, null);
    MimeMessage message = new MimeMessage(session);
    // Set the from address
    Address fromAddress;
    try {
      fromAddress = new InternetAddress(from);
      message.setFrom(fromAddress);
      // Parse and set the recipient addresses
      Address[] toAddresses = InternetAddress.parse(to);
      message.setRecipients(Message.RecipientType.TO, toAddresses);
      /*
       * Address[] ccAddresses = InternetAddress.parse(cc);
       * message.setRecipients(Message.RecipientType.CC,ccAddresses);
       *
       * Address[] bccAddresses = InternetAddress.parse(bcc);
       * message.setRecipients(Message.RecipientType.BCC,bccAddresses);
       */
      if (replyto != null) {
        Address[] replyTos = InternetAddress.parse(replyto);
        message.setReplyTo(replyTos);
      }
      message.setSubject(subject);
    }
    catch (Exception e) {
      throw new RuntimeException("Problem sending message", e);
    }
    return message;
  }

  private static void post(Message message) {
    try {
      Transport.send(message);
    }
    catch (MessagingException e) {
      throw new RuntimeException("Problem sending message", e);
    }
  }


}
