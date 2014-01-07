package smtplistener;

import junit.framework.TestCase;

/**
 * @author timp
 * @since 2014-01-14
 */
public class EmailTest extends TestCase{

  public void testEquals() throws Exception {
    Email it = new Email("from@example.com", "to@example.com", "Subject", "Body\nLine 2");
    assertTrue(it.equals(it));
    it = new Email("from@example.com", "to@example.com", "Subject", "Body\nLine 2");
    assertTrue(it.equals(it));
    assertFalse(it.equals(null));
    assertFalse(it.equals(new Object()));
    Email it2 = new Email("from@example.com", "to@example.com", "Subject", "Body\nLine 2");
    assertTrue(it.equals(it2));

    Email empty = new Email(null, null, null, null);
    assertFalse(empty.equals(it));
    assertFalse(it.equals(empty));
    Email empty2 = new Email(null, null, null, null);
    assertTrue(empty.equals(empty2));

    Email emptyRecipient = new Email("from@example.com", null, "Subject", "Body\nLine 2");
    assertFalse(it.equals(emptyRecipient));
    assertFalse(emptyRecipient.equals(it));

    Email emptySender = new Email(null, "to@example.com", "Subject", "Body\nLine 2");
    assertFalse(it.equals(emptySender));
    assertFalse(emptySender.equals(it));

    Email emptySubject = new Email("from@example.com", "to@example.com", null, "Body\nLine 2");
    assertFalse(it.equals(emptySubject));
    assertFalse(emptySubject.equals(it));

    Email emptyMessage = new Email("from@example.com", "to@example.com", "Subject", null);
    assertFalse(it.equals(emptyMessage));
    assertFalse(emptyMessage.equals(it));


  }

  public void testHashCode() throws Exception {
    Email empty = new Email(null, null, null, null);
    assertEquals(0, empty.hashCode());
    Email it = new Email("to@example.com", "from@example.com", "Subject", "Body");
    assertEquals(-93518811, it.hashCode());
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
