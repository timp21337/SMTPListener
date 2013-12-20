package com.github.timp21337.smtplistener;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author timp
 * @since 2013-12-18
 */
public class SmtpListener implements Runnable {

  private static final Logger log_ = LoggerFactory.getLogger(SmtpListener.class);
  private static int arbetraryPortNumberOver1024 = 1616;
  public boolean listening;
  private int port;
  private boolean listen = true;
  private SMTPSession currentSession;
  private ServerSocket serverSocket = null;
  private Email lastEmailReceived;

  public SmtpListener() {
    this(arbetraryPortNumberOver1024);
  }

  public SmtpListener(int port) {
    this.port = port;
  }

  @Override
  public void run() {
    try {
      serverSocket = new ServerSocket(port);
      String smtpListenerName = "smtplistener."
          + InetAddress.getLocalHost().getHostName();
      while (listen) {
        listening = true;
        log_.debug(LogTracker.number(
            "Starting new session:" + smtpListenerName + " on " + serverSocket));
        if (currentSession != null) {
          if (currentSession.initialised) {
            lastEmailReceived = currentSession.getEmail();
          }
        }
        currentSession = new SMTPSession(smtpListenerName, serverSocket);
        log_.debug(LogTracker.number(currentSession.toString()));
        new Thread(currentSession).run();
        log_.debug(LogTracker.number("Finished session"));
      }
      log_.debug(LogTracker.number("Finished serving"));
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void stopListening() {
    listen = false;
    if (serverSocket != null) {
      log_.debug(LogTracker.number("Closing socket"));
      try {
        serverSocket.close();
      }
      catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    else {
      log_.debug(LogTracker.number("Socket null"));
    }
  }

  public Email getLastEmailReceived() {
    return lastEmailReceived;
  }
}
