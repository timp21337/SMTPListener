package smtplistener;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

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

  public void testConstructorIsPrivate() throws NoSuchMethodException,
      IllegalAccessException, InvocationTargetException, InstantiationException {

    Constructor<LogTracker> constructor = LogTracker.class.getDeclaredConstructor();
    assertTrue(Modifier.isPrivate(constructor.getModifiers()));
    constructor.setAccessible(true);
    constructor.newInstance();
  }
}
