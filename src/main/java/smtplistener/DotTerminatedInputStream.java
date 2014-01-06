package smtplistener;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.PushbackInputStream;

/**
 * A stream comprising everything in an underlying stream up to the
 * first line containing only a fullstop.
 * <p/>
 * Once the <TT>DotTerminatedInputStream</TT> has reported
 * end-of-file, the underlying stream can be used again to read the
 * data from the dot-line up to its own end-of-file.
 *
 * @author timp
 * @since 2013-12-18
 */
public class DotTerminatedInputStream extends FilterInputStream {
  private static final int
      TEXT = 0, FIRSTCR = 1, FIRSTNL = 2, DOT = 3, SECONDCR = 4, TERMINATED = 5,
      POP = 6;
  private int state_ = FIRSTNL;
  private int state1_ = -1;
  private int byte1_ = -1;
  private byte[] pushedBack_ = new byte[4];
  private int pushedBackIn_ = 0;
  private int pushedBackOut_ = 0;

  /**
   * @param in the underlying stream: must be a
   *           <TT>PushbackInputStream</TT> because
   *           it's theoretically possible to end up
   *           with a character that has to be pushed
   *           back
   */
  public DotTerminatedInputStream(PushbackInputStream in) {
    super(in);
  }

  private int superRead() throws IOException {
    if (byte1_ == -1) {
      return super.read();
    }
    int it = byte1_;
    byte1_ = -1;
    return it;
  }

  private void pushBack(byte b) {
    if (pushedBackIn_ == pushedBack_.length) {
      // in fact this doesn't happen
      byte[] pushedBack = new byte[this.pushedBack_.length * 2];
      System.arraycopy(this.pushedBack_, 0, pushedBack, 0, this.pushedBack_.length);
      this.pushedBack_ = pushedBack;
    }

    pushedBack_[pushedBackIn_++] = b;
  }

  /**
   * Read a character.
   *
   * @return a character or -1 if terminated.
   */
  public synchronized int read() throws IOException {
    int b;
    while (true) {
      switch (state_) {
        case TERMINATED:
          pushedBack_ = null;
          return -1;

        case POP:
          if (pushedBackOut_ < pushedBackIn_) {
            return pushedBack_[pushedBackOut_++];
          }
          pushedBackOut_ = 0;
          pushedBackIn_ = 0;
          state_ = state1_;
          break;

        case TEXT:
          b = superRead();
          switch (b) {
            case '\r':
              state_ = FIRSTCR;
              pushBack((byte) 13);
              break;
            case '\n':
              state_ = FIRSTNL;
              pushBack((byte) 10);
              break;
            case -1:
              state_ = POP;
              state1_ = TERMINATED;
              break;
            default:
              return b;
          }
          break;

        case FIRSTCR:
          b = superRead();
          switch (b) {
            case '\n':
              state_ = FIRSTNL;
              pushBack((byte) 10);
              break;
            case '\r':
              state_ = POP;
              state1_ = TEXT;
              byte1_ = '\r';
              break;
            case -1:
              state_ = POP;
              state1_ = TERMINATED;
              break;
            default:
              state_ = POP;
              state1_ = TEXT;
              byte1_ = b;
              break;
          }
          break;

        case FIRSTNL:
          b = superRead();
          switch (b) {
            case '.':
              state_ = DOT;
              pushBack((byte) 46);
              break;
            case '\n':
              state_ = POP;
              state1_ = TEXT;
              byte1_ = '\n';
              break;
            case '\r':
              state_ = POP;
              state1_ = TEXT;
              byte1_ = '\r';
              break;
            case -1:
              state_ = POP;
              state1_ = TERMINATED;
              break;
            default:
              state_ = POP;
              state1_ = TEXT;
              byte1_ = b;
              break;
          }
          break;

        case DOT:
          b = superRead();
          switch (b) {
            case '\n':
              state_ = TERMINATED;
              break;
            case '\r':
              state_ = SECONDCR;
              break;
            case -1:
              state_ = POP;
              state1_ = TERMINATED;
              break;
            default:
              state_ = POP;
              state1_ = TEXT;
              byte1_ = b;
              break;
          }
          break;

        case SECONDCR:
          b = superRead();
          if (b != '\n') {
            ((PushbackInputStream) in).unread(b);
          }
          state_ = TERMINATED;
          break;
        default:
          break;
      }
    }
  }

  /**
   * Read some characters.
   */
  public synchronized int read(byte[] b, int off, int len) throws IOException {
    if (len <= 0) {
      return 0;
    }

    int c = read();
    if (c == -1) {
      return -1;
    }
    b[off] = (byte) c;

    int i = 1;
    for (; i < len; i++) {
      c = read();
      if (c == -1) {
        break;
      }
      if (b != null) {
        b[off + i] = (byte) c;
      }
    }
    return i;
  }

  /**
   * Skip forward a number of characters.
   *
   * @return the actual number of characters skipped
   * @see java.io.InputStream#skip(long)
   */
  public synchronized long skip(long n) throws IOException {
    while (n > 0L && read() != -1) {
      --n;
    }
    return n;
  }
}
