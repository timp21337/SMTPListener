package com.github.timp21337.smtplistener;

public class LogTracker {

  private static Integer line = 0;

  public static String number(String in) {
    synchronized (line) {
      line++;
      if (line == 100)
        line = 1;
      return (line < 10 ? " " : "") + line  + " " + in;
    }
  }
}
