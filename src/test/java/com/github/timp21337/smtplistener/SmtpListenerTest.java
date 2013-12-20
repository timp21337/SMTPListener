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

  public void testUsesPort() throws Exception {
    assertTrue(available(1616));
    SmtpListener listener = new SmtpListener(1616);
    Thread it = new Thread(listener);
    it.start();
    Thread.sleep(100);
    assertFalse(available(1616));
    listener.stopListening();
    Thread.sleep(100);
    assertTrue(available(1616));
  }

  public void testEmailCatch() throws Exception {
    assertTrue(available(1616));
    SmtpListener listener = new SmtpListener();
    Thread it = new Thread(listener);
    it.start();
    Thread.sleep(100);
    assertFalse(available(1616));
    Email toSend = new Email("sender@localhost", "root@localhost", "Subject", "Message body");

    // Coverage is all
    assertEquals(-79621412, toSend.hashCode());
    Emailer.send(toSend);

    Thread.sleep(200);
    assertFalse(available(1616));
    Email receivedEmail = listener.getEmail();
    while(receivedEmail == null) {
      Thread.sleep(50);
      System.err.println("Sleeping");
      receivedEmail = listener.getEmail();
    }
    assertTrue(receivedEmail.toString(), receivedEmail.equals(toSend));
    listener.stopListening();

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
