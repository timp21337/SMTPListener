package smtplistener;

/**
 * @author timp
 * @since 2013-12-19
 */
public class Email {
  private String sender_;
  private String recipient_;
  private String subject_;
  private String message_;

  public Email(String sender, String recipient, String subject, String message) {
    this.sender_ = sender;
    this.recipient_ = recipient;
    this.subject_ = subject;
    this.message_ = message;
  }

  public String getSender() {
    return sender_;
  }

  public String getRecipient() {
    return recipient_;
  }

  public String getSubject() {
    return subject_;
  }

  public String getMessage() {
    return message_;
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
    if (subject_ != null ? !subject_.equals(email.subject_) : email.subject_ != null) {
      return false;
    }
    if (message_ != null ? !message_.equals(email.message_) : email.message_ != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = sender_ != null ? sender_.hashCode() : 0;
    result = 31 * result + (recipient_ != null ? recipient_.hashCode() : 0);
    result = 31 * result + (subject_ != null ? subject_.hashCode() : 0);
    result = 31 * result + (message_ != null ? message_.hashCode() : 0);
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
}
