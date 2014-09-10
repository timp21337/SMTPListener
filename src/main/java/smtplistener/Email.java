package smtplistener;


/**
 * @author timp
 * @since 2013-12-19
 */
public class Email {

  private String sender_;
  private String recipient_;
  private Subject subject_;
  private Message message_;

  public Email(String sender, String recipient, String subject, String message) {
    this.sender_ = clean(sender);
    this.recipient_ = clean(recipient);
    this.subject_ = new Subject(subject);
    this.message_ = new Message(message);
  }

  public String getSender() {
    return sender_;
  }

  public String getRecipient() {
    return recipient_;
  }

  public String getSubject() {
    return subject_.toString();
  }

  public String getMessage() {
    return message_.toString();
  }



  @Override
  public String toString() {
    return "Email{"
        + "sender_='" + sender_ + '\''
        + ", recipient_='" + recipient_ + '\''
        + ", subject_='" + subject_ + '\''
        + ", message_='" + message_ + '\''
        + '}';
  }

  class Subject {
    private String it_;

    Subject(String it) {
      if (it != null) {
        if (it.contains("\n")) {
          throw new IllegalArgumentException("Subjects may not contain line breaks");
        }
        this.it_ = it;
      }
    }

    public String toString() {
      if (it_ == null) {
        return "(no subject)";
      }
      else {
        return it_;
      }
    }

  }

  class Message {
    private String it_;

    Message(String it) {
      if (it != null) {
        this.it_ = it.replaceFirst("^\\.\\.", ".").replaceAll("\n\\.\\.", "\n.").replace("\r", "");
      }
    }

    public String toString() {
      return it_;
    }

  }

  private String clean(final String address) {
    if (address == null) {
      return null;
    }
    if (address.charAt(0) == '<') {
      return address.substring(1, address.length() - 1);
    }
    return address;
  }

}
