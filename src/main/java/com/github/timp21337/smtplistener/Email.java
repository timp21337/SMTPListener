package com.github.timp21337.smtplistener;

/**
 * @author timp
 * @since 2013-12-19
 */
public class Email {
  private String sender;
  private String recipient;
  private String subject;
  private String message;

  public Email( String sender, String recipient, String subject, String message) {
    this.sender = sender;
    this.recipient = recipient;
    this.subject = subject;
    this.message = message;
  }

  public String getSender() {
    return sender;
  }

  public String getRecipient() {
    return recipient;
  }

  public String getSubject() {
    return subject;
  }

  public String getMessage() {
    return message;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final Email email = (Email) o;

    if (sender != null ? !sender.equals(email.sender) : email.sender != null) return false;
    if (recipient != null ? !recipient.equals(email.recipient) : email.recipient != null) return false;
    if (subject != null ? !subject.equals(email.subject) : email.subject != null) return false;
    if (message != null ? !message.equals(email.message) : email.message != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = sender != null ? sender.hashCode() : 0;
    result = 31 * result + (recipient != null ? recipient.hashCode() : 0);
    result = 31 * result + (subject != null ? subject.hashCode() : 0);
    result = 31 * result + (message != null ? message.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "Email{" +
        "sender='" + sender + '\'' +
        ", recipient='" + recipient + '\'' +
        ", subject='" + subject + '\'' +
        ", message='" + message + '\'' +
        '}';
  }
}
