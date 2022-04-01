import java.io.Serializable;
import java.util.Date;

public class Message implements Serializable {
    private User sender;
    private User receiver;
    private String title;
    private String content;
    private Date dateSent;
    private boolean read;

    public Message() {}

    public Message(User sender, User receiver, String title, String content, Date dateSent, boolean read) {
        this.sender = sender;
        this.receiver = receiver;
        this.title = title;
        this.content = content;
        this.dateSent = dateSent;
        this.read = read;
    }

    public User getSender() {
        return sender;
    }

    public User getReceiver() {
        return receiver;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public Date getDateSent() {
        return dateSent;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    /**
     * This method returns a string containing the details of a message, each on a new line
     * The details are the sender name, the receiver name, the title, the date sent, and the content.
     **/
    @Override
    public String toString() {
        return      "Sender: " + getSender().getUsername() + "\n"
                +   "Receiver: " + getReceiver().getUsername() + "\n"
                +   "Title: " + getTitle() + "\n"
                +   "Date: " + getDateSent() + "\n"
                +   "Content: " + getContent();
    }

}
