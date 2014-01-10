package smtplistener;

import java.io.File;
import java.util.Map;

import junit.framework.TestCase;

/**
 * @author timp
 * @since 2013-12-18
 */
public class SmtpListenerTest
    extends TestCase {

  final static int PORT = 1616;

  public void testStopListeningBeforeListening() {
    SmtpListener listener = new SmtpListener(1616);
    try {
      listener.stopListening();
      fail("Should have bombed");
    } catch (RuntimeException e) {
      e = null;
    }
  }

  public void testUsesPort() throws Exception {
    assertTrue(SmtpListener.isFree(PORT));
    SmtpListener listener = new SmtpListener(1616);
    listener.startListening();
    assertFalse(SmtpListener.isFree(PORT));
    listener.stopListening();
    Thread.sleep(1);
    assertTrue(SmtpListener.isFree(PORT));
  }

  private Email fetchEmail(SmtpListener listener) throws Exception {
    Email it = listener.getLastEmailReceived();
    int goes = 0;
    while(it == null) {
      if (goes++ > 10)
        fail("Maybe you have not configured your MTA to route "
             + "smtptlistener mails to " + PORT);
      Thread.sleep(50);
      it = listener.getLastEmailReceived();
    }
    return it;
  }


  public void testEmailCatch() throws Exception {
    assertTrue(SmtpListener.isFree(PORT));
    SmtpListener listener = new SmtpListener();
    listener.startListening();

    assertFalse(SmtpListener.isFree(PORT));
    Email toSend = new Email(
        "sender@smtplistener",
        "root@smtplistener",
        "Subject",
        "Message body\nLine 2\nLine 3\n\n"
        + ". not alone\n"
        + "\n.\n"
        + ". a line starting with a dot but not the end of message\n");

    Emailer.send(toSend);

    assertFalse(SmtpListener.isFree(PORT));
    Email receivedEmail = fetchEmail(listener);
    assertTrue(receivedEmail.toString() + "\n" + toSend.toString(), receivedEmail.equals(toSend));

    Map emailAsMap = listener.getLastEmailReceivedAsMap();
    assertEquals(receivedEmail.getRecipient(), emailAsMap.get("to"));
    assertEquals(receivedEmail.getSender(), emailAsMap.get("from"));
    assertEquals(receivedEmail.getSubject(), emailAsMap.get("subject"));
    assertEquals(receivedEmail.getMessage(), emailAsMap.get("message"));

    listener.stopListening();
    assertTrue(SmtpListener.isFree(PORT));
  }

  public void testEmailWithAttachmentsDropped() throws Exception {
    assertTrue(SmtpListener.isFree(PORT));
    SmtpListener listener = new SmtpListener();
    listener.startListening();

    assertFalse(SmtpListener.isFree(PORT));
    Email toSend = new Email("sender@smtplistener",
        "root@smtplistener", "Subject", "Message body");
    File [] attachments = new File[] {new File("README.md")};
    Emailer.sendWithAttachments(
        "localhost",
        "sender@smtplistener",
        "root@smtplistener",
        "sender@smtplistener",
        "Subject",
        "Message body",
        attachments);

    assertFalse(SmtpListener.isFree(PORT));
    Email receivedEmail = fetchEmail(listener);
    assertTrue(receivedEmail.toString() + "\n" + toSend.toString(), receivedEmail.equals(toSend));
    listener.stopListening();

    assertTrue(SmtpListener.isFree(PORT));
  }


  /** Shows that this cannot be used, as is, for repeated tests, 
   *  but it does inch the coverage up.
   *
   *  Exim needs to have retrys configured off for this to work repeatedly,
   *  see README.md.
   */
  public void testNotRepeatable() throws Exception {
    assertTrue(SmtpListener.isFree(PORT));
    SmtpListener listener = new SmtpListener();
    listener.startListening();
    assertFalse(SmtpListener.isFree(PORT));
    for (int i = 1; i < 3; i++) {
      Email toSend = new Email("sender" + i + "@smtplistener", 
          "root@smtplistener", "Subject " + i, "Message body" + i);

      Emailer.send(toSend);

      assertFalse(SmtpListener.isFree(PORT));
      Email receivedEmail = fetchEmail(listener);
      // Neither assertion holds
//      assertTrue(receivedEmail.toString().equals(toSend.toString()));
//      assertFalse(receivedEmail.toString().equals(toSend.toString()));
    }
    listener.stopListening();
    Thread.sleep(30);
  }

  public void testMapIsInitiallyNull() {
    assertTrue(SmtpListener.isFree(PORT));
    SmtpListener listener = new SmtpListener();
    listener.startListening();
    assertFalse(SmtpListener.isFree(PORT));
    assertNull(listener.getLastEmailReceived());
    assertNull(listener.getLastEmailReceivedAsMap());
  }

  public void testIsFree() {
    try {
      SmtpListener.isFree(-1);
      fail("Should have bombed");
    }
    catch (IllegalArgumentException e) {
      e = null;
    }
    try {
      SmtpListener.isFree(65536);
      fail("Should have bombed");
    }
    catch (IllegalArgumentException e) {
      e = null;
    }
  }
}
