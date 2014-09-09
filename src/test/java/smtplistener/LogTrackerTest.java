package smtplistener;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Method;

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

  public void testPrivateConstructors() throws Exception {
    excercisePrivateConstuctor(LogTracker.class);
  }

  public static void excercisePrivateConstuctor(final Class<?> clazz)
      throws NoSuchMethodException, InvocationTargetException,
      InstantiationException, IllegalAccessException {
    assertTrue("There must be only one constructor", clazz.getDeclaredConstructors().length == 1);
    final Constructor<?> constructor = clazz.getDeclaredConstructor();
    assertFalse("The constructor is accessible", constructor.isAccessible());
    assertTrue("The constructor is not private", Modifier.isPrivate(constructor.getModifiers()));
    constructor.setAccessible(true);
    constructor.newInstance();
    constructor.setAccessible(false);
    for (final Method method : clazz.getMethods()) {
      if (method.getDeclaringClass().equals(clazz)) {
        assertTrue("There exists a non-static method:" + method,
            Modifier.isStatic(method.getModifiers()));
      }
    }
  }

}
