package smtplistener;

/** Number log lines to help in debugging multi-threading. */
public class LogTracker {

  private static Integer line = 0;
  private static final int FIRST_DOUBLE_DIGIT_DECIMAL = 10;
  private static final int FIRST_THREE_DIGIT_DECIMAL = 100;

  private LogTracker() {
  }

  /** Prepend a number to a line. */
  public static String number(String in) {
    String out;
    synchronized (line) {
      line++;
      if (line == FIRST_THREE_DIGIT_DECIMAL) {
        line = 1;
      }
      out = (line < FIRST_DOUBLE_DIGIT_DECIMAL ? " " : "") + line  + " " + in;
    }
    return out;
  }
}
