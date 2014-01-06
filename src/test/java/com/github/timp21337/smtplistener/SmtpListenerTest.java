package com.github.timp21337.smtplistener;

import java.io.IOException;
import java.net.ServerSocket;

import junit.framework.TestCase;

/**
 * @author timp
 * @since 2013-12-18
 */
public class SmtpListenerTest
    extends TestCase {

  final static int PORT = 1616;

  public void testUsesPort() throws Exception {
    assertTrue(available(PORT));
    SmtpListener listener = new SmtpListener(1616);
    Thread it = new Thread(listener);
    it.start();
    Thread.sleep(1);  // slightly longer than yield()
    assertFalse(available(PORT));
    listener.stopListening();
    Thread.sleep(1);
    assertTrue(available(PORT));
  }

  private Email fetchEmail(SmtpListener listener) throws Exception {
    Email it = listener.getLastEmailReceived();
    int goes = 0;
    while(it == null) {
      System.err.println("Goes:" + goes);
      if (goes++ > 10)
        fail("Maybe you have not configured your MTA to route "
             + "smtptlistener mails to " + PORT);
      Thread.sleep(50);
      System.err.println("Sleeping");
      it = listener.getLastEmailReceived();
    }
    return it;
  }

  public void testEmailCatch() throws Exception {
    assertTrue(available(PORT));
    SmtpListener listener = new SmtpListener();
    new Thread(listener).start();
    Thread.sleep(1);

    assertFalse(available(PORT));
    Email toSend = new Email("sender@smtplistener", 
        "root@smtplistener", "Subject", "Message body");

    // Coverage is all
    assertEquals(-18618622, toSend.hashCode());

    Emailer.send(toSend);

    assertFalse(available(PORT));
    Email receivedEmail = fetchEmail(listener);
    assertTrue(receivedEmail.toString(), receivedEmail.equals(toSend));
    listener.stopListening();
    Thread.sleep(1);
  }


  /** Shows that this cannot be used, as is, for repeated tests, 
   *  but it does inch the coverage up.
   *
   *  This test upsets exim, though fine with postfix.
   */
  public void badTestNotRepeatable() throws Exception {
    assertTrue(available(PORT));
    SmtpListener listener = new SmtpListener();
    new Thread(listener).start();
    Thread.sleep(1);
    assertFalse(available(PORT));
    for (int i = 1; i < 3; i++) {
      Email toSend = new Email("sender" + i + "@smtplistener", 
          "root@smtplistener", "Subject " + i, "Message body" + i);

      Emailer.send(toSend);

      assertFalse(available(PORT));
      Email receivedEmail = fetchEmail(listener);
      // Neither assertion holds
//      assertTrue(receivedEmail.toString().equals(toSend.toString()));
//      assertFalse(receivedEmail.toString().equals(toSend.toString()));
    }
    listener.stopListening();
    Thread.sleep(1);
  }

  /**
   * Checks to see if a specific port is available.
   *
   * @param port the port to check for availability
   */
  public static boolean available(int port) {
    if (port < 0 || port > 65535) {
      throw new IllegalArgumentException("Invalid start port: " + port);
    }

    ServerSocket ss = null;
    try {
      ss = new ServerSocket(port);
      return true;
    }
    catch (IOException e) {
      return false;
    }
    finally {
      if (ss != null) {
        try {
          ss.close();
        } catch (IOException e) {
          throw new RuntimeException("I promised this could never happen", e);
        }
      }
    }
  }
}
