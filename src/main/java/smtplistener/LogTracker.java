package smtplistener;

public class LogTracker {

  private static Integer line = 0;

  private LogTracker() { }

  public static String number(String in) {
    String out = null;
    synchronized (line) {
      line++;
      if (line == 100) {
        line = 1;
      }
      out = (line < 10 ? " " : "") + line  + " " + in;
    }
    return out;
  }
}
