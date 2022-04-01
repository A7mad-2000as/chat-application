import java.io.Serializable;

public class User implements Serializable {
    private String username;
    private String password;
    private MessageContainer inbox; // A list of messages that are directed towards this user.
    private MessageContainer sent;  // A list of messages that are sent from this user.

    public User() {}
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        inbox = new MessageContainer();
        sent = new MessageContainer();
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public MessageContainer getInbox() {
        return inbox;
    }

    public MessageContainer getSent() {
        return sent;
    }
}
