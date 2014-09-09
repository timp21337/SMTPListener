package smtplistener;

import junit.framework.TestCase;

/**
 * @author timp
 * @since 2014-01-14
 */
public class EmailTest extends TestCase {

  public void testNullSubject() {
    Email empty = new Email(null, null, null, null);
    assertEquals("(no subject)", empty.getSubject());
    assertEquals("Email{sender_='null', recipient_='null', subject_='(no subject)', message_='null'}", empty.toString());
  }

  public void testSubjectMayNotContainLineBreak() {
    try {
      new Email("to@example.com", "from@example.com", "Subject\nwith a line break", "Body");
      fail("Should have bombed");
    } catch (IllegalArgumentException e) {
      e = null; // expected
    }

  }
  public void testMessageHasInitialDoubleDotsConverted() {
    Email it = new Email("to@example.com", "from@example.com", "Subject", ".. dot on first line");
    assertEquals(". dot on first line", it.getMessage());
    it = new Email("to@example.com", "from@example.com", "Subject", ".. dot on first line\n"
        + ".. and on second");
    assertEquals(". dot on first line\n"
        + ". and on second", it.getMessage());
  }
}
