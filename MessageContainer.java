import java.io.Serializable;
import java.util.ArrayList;

public class MessageContainer implements Serializable {
    private ArrayList<Message> messages;

    public MessageContainer() {
        messages = new ArrayList<Message>();
    }

    /**
     * This method takes a Message object and adds it to the (messages) list.
     **/
    public void addMessage(Message message) {
       messages.add(message);
    }

    /**
     * This method returns a string containing headline information about the messages in the sent list, each on a new line.
     * The returned information for each message include a count value, the receiver name, the title,
     * the date sent and if it is read or not. The different fields are separated by two spaces and a slash.
     **/
    public String getSentMessageInfo() {
        StringBuffer buffer = new StringBuffer();
        int counter = 1;
        for (Message message : messages) {
            buffer.append(counter + ". Receiver: " + message.getReceiver().getUsername() + "  / Title: " + message.getTitle() + "  / Date: " + message.getDateSent()
                           + (message.isRead() ? "  / Read" : "   / Not read") + "\n");
            counter++;
        }

        return buffer.toString().stripTrailing();
    }

    /**
     * This method returns a string containing headline information about the messages in the inbox list, each on a new line.
     * The returned information for each message include a count value, the sender name, the title,
     * the date sent and if it is read or not. The different fields are separated by two spaces and a slash.
     **/
    public String getReceiveMessageInfo() {
        StringBuffer buffer = new StringBuffer();
        int counter = 1;
        for (Message message : messages) {
            buffer.append(counter + ". Sender: " + message.getSender().getUsername() + "  / Title: " + message.getTitle() + "  / Date: " + message.getDateSent()
                    + (message.isRead() ? "/   Read" : "  / Not read") + "\n");
            counter++;
        }

        return buffer.toString().stripTrailing();
    }

    /**
     * This method takes a number that is 1-indexed, and returns the corresponding 0-indexed Message.
     **/
    public Message getMessageByNumber(int num) {
        return messages.get(num - 1);
    }




}
