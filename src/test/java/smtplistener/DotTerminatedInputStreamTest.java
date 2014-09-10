package smtplistener;

import java.io.ByteArrayInputStream;
import java.io.PushbackInputStream;

import static org.mockito.Mockito.mock;

import junit.framework.TestCase;


/**
 * @author timp
 * @since 2014/09/10
 */
public class DotTerminatedInputStreamTest extends TestCase {

  public void testSkip() throws Exception {
    PushbackInputStream backing = mock(PushbackInputStream.class);
    DotTerminatedInputStream it = new DotTerminatedInputStream((backing));
    try {
      it.skip(2);
      fail("Should have bombed");
    } catch (UnsupportedOperationException e) {
      e = null;
    }
  }

  public void testReadEdgeCases() throws Exception {
    PushbackInputStream backing = mock(PushbackInputStream.class);
    DotTerminatedInputStream it = new DotTerminatedInputStream((backing));
    byte[] bytes = new byte[100];
    try {
      it.read(bytes, 0, -2);
      fail("Should have bombed");
    } catch (IllegalArgumentException e) {
      e = null;
    }
    assertEquals(0, it.read(bytes, 0, 0));
  }

  public void testRead() throws Exception {
    // Initial state is FIRSTLF
    assertEquals(-1, get("").read());
    assertEquals(10, get("\n").read());
  }

  public void testReadToEnd() throws Exception {
    byte[] read = new byte[100];
    assertEquals(-1, get("").read(read,0,3));
    assertEquals(1, get("\n").read(read, 0, 3));
    assertEquals(1, get("\r").read(read, 0, 3));
    assertEquals(1, get("a").read(read, 0, 3));

    assertEquals(2, get("\r\n").read(read, 0, 3));
    assertEquals(2, get("\r\r").read(read, 0, 3));
    assertEquals(2, get("\ra").read(read, 0, 3));

    assertEquals(1, get(".").read(read, 0, 3));
    assertEquals(-1, get(".\r\n").read(read, 0, 3));
    assertEquals(-1, get(".\n").read(read, 0, 3));
    assertEquals(2, get(".n").read(read, 0, 3));

    // Not convinced this is correct
    assertEquals(-1, get(".\ra").read(read, 0, 3));



  }

  private DotTerminatedInputStream get(String in) throws Exception {
    ByteArrayInputStream backing = new ByteArrayInputStream(in.getBytes("UTF-8"));
    DotTerminatedInputStream it = new DotTerminatedInputStream(new PushbackInputStream(backing));
    return it;
  }
}
