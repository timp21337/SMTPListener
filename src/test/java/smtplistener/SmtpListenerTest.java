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

  String host = "smtplistener.local";

  private String email(String name) {
    return name + "@" + host;
  }

  public void testStopListeningBeforeListening() {
    SmtpListener listener = new SmtpListener(PORT);
    try {
      listener.stopListening();
      fail("Should have bombed");
    } catch (RuntimeException e) {
      e = null;
    }
  }

  private Email fetchEmail(SmtpListener listener) throws Exception {
    Email it = listener.getLastEmailReceived();
    int goes = 0;
    while(it == null) {
      if (goes++ > 10)
        fail("Maybe you have not configured your MTA to route "
             + host + " mails to " + PORT);
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
        email("sender"),
        email("root"),
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

  public void testEmailWithAttachmentsWorks() throws Exception {
    assertTrue(SmtpListener.isFree(PORT));
    SmtpListener listener = new SmtpListener();
    listener.startListening();
    assertFalse(SmtpListener.isFree(PORT));

    Email toSend = new Email(email("sender"), email("root"), "Subject", "Message body");
    File [] attachments = new File[] {new File("README.md")};
    Emailer.sendWithAttachments(
        "localhost",
        email("sender"),
        email("root"),
        email("sender"),
        "Subject",
        "Message body",
        attachments);

    assertFalse(SmtpListener.isFree(PORT));
    Email receivedEmail = fetchEmail(listener);
    assertTrue(receivedEmail.toString() + "\n" + toSend.toString(), receivedEmail.equals(toSend));
    listener.stopListening();

    assertTrue(SmtpListener.isFree(PORT));
  }


  public void testRepeatable() throws Exception {
    assertTrue(SmtpListener.isFree(PORT));
    SmtpListener listener = new SmtpListener();
    listener.startListening();
    assertFalse(SmtpListener.isFree(PORT));
    for (int i = 1; i < 7; i++) {
      Email toSend = new Email("sender" + i + "@" + host,
          "root@" + host, "Subject " + i, "Message body" + i);

      Emailer.send(toSend);

      assertFalse(SmtpListener.isFree(PORT));
      Thread.sleep(1000);   // Wait for delivery
      Email receivedEmail = fetchEmail(listener);
      // Neither assertion holds
      assertEquals(toSend.toString(), receivedEmail.toString());
    }
    listener.stopListening();
    Thread.sleep(30);
    assertTrue(SmtpListener.isFree(PORT));
  }

  public void testMapIsInitiallyNull() {
    assertTrue(SmtpListener.isFree(PORT));
    SmtpListener listener = new SmtpListener();
    listener.startListening();
    assertFalse(SmtpListener.isFree(PORT));
    assertNull(listener.getLastEmailReceived());
    assertNull(listener.getLastEmailReceivedAsMap());
    listener.stopListening();
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

  public void testUsesPort() throws Exception {
    assertTrue(SmtpListener.isFree(PORT));
    SmtpListener listener = new SmtpListener(PORT);
    listener.startListening();
    assertFalse(SmtpListener.isFree(PORT));
    listener.stopListening();
    Thread.sleep(50);
    assertTrue(SmtpListener.isFree(PORT));
  }

}
