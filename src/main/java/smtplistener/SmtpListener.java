package smtplistener;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author timp
 * @since 2013-12-18
 */
public class SmtpListener implements Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(SmtpListener.class);
  private static final int PORT_NO_OVER_1024 = 1616;

  private int port_;
  private boolean listening_;
  private boolean listen_ = true;
  private SMTPSession currentSession_;
  private ServerSocket serverSocket_ = null;
  private Email lastEmailReceived_;

  public SmtpListener() {
    this(PORT_NO_OVER_1024);
  }

  public SmtpListener(int port) {
    this.port_ = port;
  }

  @Override
  public void run() {
    try {
      serverSocket_ = new ServerSocket(port_);
      String smtpListenerName = "smtplistener."
          + InetAddress.getLocalHost().getHostName();
      while (listen_) {
        setListening(true);
        LOG.debug(LogTracker.number(
            "Starting new session:" + smtpListenerName + " on " + serverSocket_));
        if (currentSession_ != null) {
          lastEmailReceived_ = currentSession_.getEmail();
        }
        currentSession_ = new SMTPSession(smtpListenerName, serverSocket_);
        LOG.debug(LogTracker.number(currentSession_.toString()));
        new Thread(currentSession_).run();
        LOG.debug(LogTracker.number("Finished session"));
      }
      LOG.debug(LogTracker.number("Finished serving"));
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void startListening() {
    listen_ = true;
    new Thread(this).start();
    while (!isListening()) {
      try {
        Thread.sleep(1);
      }
      catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
    LOG.debug(LogTracker.number("Started listening"));
  }

  public void stopListening() {
    if (listening_) {
      listen_ = false;
      try {
        listening_ = false;
        serverSocket_.close();
        Thread.sleep(1);
      }
      catch (IOException e) {
        throw new RuntimeException(e);
      }
      catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
      LOG.debug(LogTracker.number("Stopped listening"));
    }
    else {
      throw new RuntimeException("Called when not listening");
    }
  }

  public Email getLastEmailReceived() {
    return lastEmailReceived_;
  }
  public Map getLastEmailReceivedAsMap() {
    Email it = getLastEmailReceived();
    if (it == null) {
      return null;
    }
    Map<String, String> copy = new HashMap<String, String>();
    copy.put("to", getLastEmailReceived().getRecipient());
    copy.put("from", getLastEmailReceived().getSender());
    copy.put("subject", getLastEmailReceived().getSubject());
    copy.put("message", getLastEmailReceived().getMessage());
    return copy;
  }

  public boolean isListening() {
    return listening_;
  }

  private void setListening(final boolean listening) {
    this.listening_ = listening;
  }

  /**
   * Checks to see if a specific port is isFree.
   *
   * @param port the port to check for availability
   */
  public static boolean isFree(int port) {
    ServerSocket ss = null;
    try {
      ss = new ServerSocket(port);
      ss.close();
      return true;
    }
    catch (IOException e) {
      return false;
    }
  }


}
