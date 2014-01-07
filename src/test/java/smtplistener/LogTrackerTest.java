package smtplistener;

import junit.framework.TestCase;

/**
 * @author timp
 * @since 2014-01-07
 */
public class LogTrackerTest extends TestCase {

  public void testCounterRollover() {
    String current = LogTracker.number("Message");
    for (int i = 1; i < 99; i++) {
      LogTracker.number("Message");
    }
    assertEquals(current, LogTracker.number("Message"));
  }
}
