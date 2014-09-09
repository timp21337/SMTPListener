package smtplistener;


/**
 * @author timp
 * @since 2013-12-19
 */
public class Email {
  private static final int MAGIC_NUMBER = 31;

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
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final Email email = (Email) o;

    if (sender_ != null ? !sender_.equals(email.sender_) : email.sender_ != null) {
      return false;
    }
    if (recipient_ != null ? !recipient_.equals(email.recipient_) : email.recipient_ != null) {
      return false;
    }
    if (!getSubject().equals(email.getSubject())) {
      return false;
    }
    if (getMessage() == null ? email.getMessage() != null : !getMessage().equals(email.getMessage())) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = sender_ != null ? sender_.hashCode() : 0;
    result = MAGIC_NUMBER * result + (recipient_ != null ? recipient_.hashCode() : 0);
    result = MAGIC_NUMBER * result + subject_.toString().hashCode();
    result = MAGIC_NUMBER * result + message_.toString().hashCode();
    return result;
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
