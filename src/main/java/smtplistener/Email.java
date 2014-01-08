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
    this.sender_ = sender;
    this.recipient_ = recipient;
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
    if (!subject_.equals(email.subject_)) {
      return false;
    }
    if (!message_.equals(email.message_)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = sender_ != null ? sender_.hashCode() : 0;
    result = 31 * result + (recipient_ != null ? recipient_.hashCode() : 0);
    result = 31 * result + subject_.hashCode();
    result = 31 * result + message_.hashCode();
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

  private class Subject {
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
      return it_;
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      final Subject subject = (Subject) o;

      if (it_ != null ? !it_.equals(subject.it_) : subject.it_ != null) {
        return false;
      }

      return true;
    }

    @Override
    public int hashCode() {
      return it_ != null ? it_.hashCode() : 0;
    }
  }

  private class Message {
    private String it_;

    Message(String it) {
      if (it != null) {
        this.it_ = it.replaceFirst("^\\.\\.", ".").replaceAll("\n\\.\\.", "\n.").replace("\r", "");
      }
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      final Message message = (Message) o;

      if (it_ != null ? !it_.equals(message.it_) : message.it_ != null) {
        return false;
      }

      return true;
    }

    @Override
    public int hashCode() {
      return it_ != null ? it_.hashCode() : 0;
    }

    public String toString() {
      return it_;
    }
  }
}
