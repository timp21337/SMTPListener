package smtplistener;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.PushbackInputStream;

/**
 * A stream comprising everything in an underlying stream up to the
 * first line containing only a fullstop.
 *
 * That is a 5 character sequence of CRLF.CRLF
 *
 *
 * Once the <TT>DotTerminatedInputStream</TT> has reported
 * end-of-file, the underlying stream can be used again to read the
 * data from the dot-line up to its own end-of-file.
 *
 *
 *
 * According to the SMTP standard RFC 5321, section 4.5.2:
 *
 *  http://tools.ietf.org/html/rfc5321#section-4.5.2
 *
 *  To allow all user composed text to be transmitted transparently,
 *  the following procedures are used:
 *
 *   Before sending a line of mail text, the SMTP client checks the first character of the line.
 *   If it is a period, one additional period is inserted at the beginning of the line.

 *   When a line of mail text is received by the SMTP server, it checks the line.
 *   If the line is composed of a single period, it is treated as the end of mail indicator.
 *   If the first character is a period and there are other characters on the line,
 *   the first character is deleted.
 *
 * @author timp
 * @since 2013-12-18
 */
public class DotTerminatedInputStream extends FilterInputStream {


  private static final byte CR = 13;
  private static final byte LF = 10;
  private static final byte DOT = 46;

  // special state, also returned from Super
  private static final int END_OF_STREAM = -1;
  // the states
  private static final int
      TEXT = 0, FIRSTCR = 1, FIRSTLF = 2, THEDOT = 3, SECONDCR = 4,
      TERMINATED = 5, POP = 6;

  private int state_ = FIRSTLF;
  private int expectedState_ = END_OF_STREAM;
  private int stashedByte_ = END_OF_STREAM;

  private static final int PUSHBACK = 4;
  private byte[] toPushBack_ = new byte[PUSHBACK];
  private int toPushBackIndex_ = 0;
  private int toPushBackDone_ = 0;

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
    if (stashedByte_ == END_OF_STREAM) {
      return super.read();
    }
    else {
      int it = stashedByte_;
      stashedByte_ = END_OF_STREAM;
      return it;
    }
  }


  /**
   * Read a character.
   *
   * @return a character or END_OF_STREAM if terminated.
   */
  public synchronized int read() throws IOException {
    int b;
    int result = END_OF_STREAM;
    boolean done = false;
    while (!done) {
      switch (state_) {
        // initial state
        case FIRSTLF:
          b = superRead();
          switch (b) {
            case '.':
              state_ = THEDOT;
              toPushBack_[toPushBackIndex_++] = DOT;
              break;
            case '\n':
              state_ = POP;
              expectedState_ = TEXT;
              stashedByte_ = '\n';
              break;
            case '\r':
              state_ = POP;
              expectedState_ = TEXT;
              stashedByte_ = '\r';
              break;
            case END_OF_STREAM:
              state_ = POP;
              expectedState_ = TERMINATED;
              break;
            default:
              state_ = POP;
              expectedState_ = TEXT;
              stashedByte_ = b;
              break;
          }
          break;

        case TERMINATED:
          toPushBack_ = null;
          result = END_OF_STREAM;
          done = true;
          break;

        case POP:
          if (toPushBackDone_ < toPushBackIndex_) {
            result = toPushBack_[toPushBackDone_++];
            done = true;
            break;
          }
          toPushBackDone_ = 0;
          toPushBackIndex_ = 0;
          state_ = expectedState_;
          break;

        case TEXT:
          b = superRead();
          switch (b) {
            case '\r':
              state_ = FIRSTCR;
              toPushBack_[toPushBackIndex_++] = CR;
              break;
            case '\n':
              state_ = FIRSTLF;
              toPushBack_[toPushBackIndex_++] = LF;
              break;
            case END_OF_STREAM:
              state_ = POP;
              expectedState_ = TERMINATED;
              break;
            default:
              done = true;
              result = b;
              break;
          }
          break;

        case FIRSTCR:
          b = superRead();
          switch (b) {
            case '\n':
              state_ = FIRSTLF;
              toPushBack_[toPushBackIndex_++] = LF;
              break;
            case '\r':
              state_ = POP;
              expectedState_ = TEXT;
              stashedByte_ = '\r';
              break;
            case END_OF_STREAM:
              state_ = POP;
              expectedState_ = TERMINATED;
              break;
            default:
              state_ = POP;
              expectedState_ = TEXT;
              stashedByte_ = b;
              break;
          }
          break;

        case THEDOT:
          b = superRead();
          switch (b) {
            case '\n':
              state_ = TERMINATED;
              break;
            case '\r':
              state_ = SECONDCR;
              break;
            case END_OF_STREAM:
              state_ = POP;
              expectedState_ = TERMINATED;
              break;
            default:
              state_ = POP;
              expectedState_ = TEXT;
              stashedByte_ = b;
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
          throw new RuntimeException("Unexpected state:" + state_);
      }
    }
    return result;
  }

  /**
   * Read some characters.
   */
  @Override
  public synchronized int read(byte[] bytes, int off, int len) throws IOException {
    if (len < 0) {
      throw new IllegalArgumentException("Length:" + len);
    }
    if (len == 0) {
      return 0;
    }

    int c = read();
    if (c == END_OF_STREAM) {
      return END_OF_STREAM;
    }
    bytes[off] = (byte) c;

    int i;
    for (i = 1; i < len; i++) {
      c = read();
      if (c == END_OF_STREAM) {
        break;
      }
      bytes[off + i] = (byte) c;
    }
    return i;
  }

  public synchronized long skip(long n) throws IOException {
    throw new UnsupportedOperationException();
  }

}
