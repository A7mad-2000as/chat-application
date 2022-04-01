import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class Server {
    public static final int SERVICE_PORT = 4000;
    private static UserContainer users;  // users is the list of all registered users to the application

    public static void main(String[] args)  {
        try {
            try {
                // Create ObjectInputStream to check if old data exists.
                FileInputStream fis = new FileInputStream("data.txt");
                ObjectInputStream ois = new ObjectInputStream(fis);

                try {
                    // If data exists assign it to users.
                    users = (UserContainer) ois.readObject();
                }

                catch (ClassCastException | ClassNotFoundException ex) {
                    // If the data stored is incompatible for some reason, assign an empty user list.
                    users = new UserContainer();
                }
            }

            catch (FileNotFoundException ex) {
                // If there is no old data, assign an empty user list.
                users = new UserContainer();
            }

            // Bind to port SERVICE_PORT = 4000 to start listening for connection requests.
            ServerSocket serverSocket = new ServerSocket(SERVICE_PORT);

            // Always listen for connection requests, accept them then start a new ServiceThread for each connection passing the corresponding socket and the user list.
            while (true) {
                Socket socket = serverSocket.accept();
                Runnable serviceThread = new ServiceThread(socket, users);
                Thread newThread = new Thread(serviceThread);
                newThread.start();
            }

        }

        catch (IOException ex) {
            System.out.println(ex);
        }
    }
}

/**
 * This class represents the thread connected to a client, each client connection creates a new thread.
 */
class ServiceThread implements Runnable {
    private Socket socket;   // The connection socket from the server-side.
    private UserContainer users;  // The same global user list of the server (will be shared between all threads).
    private User loggedInUser = null;  // The current logged-in user for this connection/thread.
    private PrintStream outputStream;    // An output stream to write to the client side of the application.
    private BufferedReader inputReader;  // An input reader to read from the client side of the application.


    public ServiceThread(Socket socket, UserContainer users) {
        this.socket = socket;
        this.users = users;
        try {
            outputStream = new PrintStream(socket.getOutputStream());
            inputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }

        catch (IOException ex) {
            System.out.println(ex);
        }
    }

    public void run() {
        try {
            /* Continuously read the initial choice to either log-in or sign-up,
            if log-in is successful enter serveUser which presents the main-menu, then when done terminate the thread.
             */
 OuterLoop:  while (true) {

                String operationChoice = inputReader.readLine();
                switch (operationChoice) {
                    case "1":
                        loggedInUser = logIn();
                        // If log-in is not successful, re-prompt for a choice.
                        if (loggedInUser == null)
                            continue;
                        else
                            serveUser();
                        break OuterLoop;

                    case "2":
                        // Always re-prompt for choice after sign-up (successful or not successful).
                        signUp();
                    break;
                }
            }
        }

        catch (SocketException ex) {
            try {
                // If the connection ends unexpectedly from the client side, close the socket and terminate the thread.
                socket.close();
            }

            catch (IOException iox) {
                System.out.println(iox);
            }
        }

        catch (IOException ex) {
            System.out.println(ex);
        }
    }

    /**
     * This method reads the username and password from the client and returns a User object of the corresponding credentials
     *  if they are valid. Otherwise returns null.
     **/
    private User logIn() throws SocketException {
        User user = null;

        try {
            // Read username and password from client.
            String username = inputReader.readLine();
            String password = inputReader.readLine();

            // Gain thread-safe access to (users) then determine if the credentials are valid or not (user != null and isAuthenticated = true)
            boolean isAuthenticated;
            synchronized (users) {
                user = users.findUserName(username);
                isAuthenticated = users.authenticateUser(user, password);
            }

            /* Write error or success code to client, -2 -> username does not exist.
                                                      -1 -> password is incorrect.
                                                      0 -> successful log-in.
            */
            if (user == null)
                outputStream.println("-2");
            else if (!isAuthenticated) {
                outputStream.println("-1");
                user = null;
            }
            else
                outputStream.println("0");
        }

        catch (SocketException ex) {
            // Propagate abnormal socket termination to the run() method.
            throw ex;
        }

        catch (IOException ex) {
            System.out.println(ex);
        }

        return user;
    }

    /**
     * This method reads the new username and password from the client and adds a User object of the corresponding credentials
     * to the users list if the username does not already exist. Otherwise writes error code then returns.
    **/
    private void signUp() throws SocketException {
        User user = null;
        try {
            // Read username and password from client.
            String username = inputReader.readLine();
            String password = inputReader.readLine();

            // Gain thread-safe access to (users) then determine if the username already exists (user != null).
            synchronized (users) {
                user = users.findUserName(username);
            }

             /* Write error or success code to client, -1 -> username already exists.
                                                      0 -> successful sign-up.
            */
            if (user != null)
                outputStream.println("-1");
            else {
                outputStream.println("0");
                // Gain thread-safe access to users and add the new user, then write the users list to the data file on the local system.
                synchronized (users) {
                    users.addUser(username, password);
                    ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("data.txt"));
                    oos.writeObject(users);
                }
            }
        }

        catch (SocketException ex) {
            // Propagate abnormal socket termination to the run() method.
            throw ex;
        }

        catch (IOException ex) {
            System.out.println(ex);
        }
    }

    /**
     *  This method represents the main-menu of the application, where the client will send an operation to perform.
     **/
    private void serveUser() throws SocketException {
        try {
            /* Continuously read the user choice then perform the correct operation, exit serving the user if he chooses log-off.
             */
OuterLoop:  while (true) {
                String operationChoice = inputReader.readLine();
                switch (operationChoice) {
                    case "1" :
                        sendMessage();
                    break;

                    case "2" :
                        viewMessages();
                    break;

                    case "3" :
                        logOff();
                        break OuterLoop;
                }
            }
        }

        catch (SocketException ex) {
            // Propagate abnormal socket termination to the run() method.
            throw ex;
        }

        catch (IOException ex) {
            System.out.println(ex);
        }
    }

    /**
     * This method sends a message to another registered user,
     * it reads the name and message information from the client then sends if successful or returns error if not.
     **/
    private void sendMessage() throws SocketException {
        try {
            User receiver;
            // Keep prompting the client for names of users until a valid name is given.
            while (true) {

                /* Gain thread-safe access to (users), then get the list of names removing the username of the requesting user,
                 send the number of other users then the list of names itself to the client to choose a name.
                */
                synchronized (users) {
                    int numOfOtherUsers = users.getNumberOfUsers() - 1;
                    outputStream.println("" + numOfOtherUsers);

                    // If there are no other users exit the operation.
                    if (numOfOtherUsers == 0)
                        return;

                    // Remove the requesting username from the list of other users before sending it.
                    ArrayList<String> list = new ArrayList<>(Arrays.asList(users.toString().split("\\n")));
                    list.remove(loggedInUser.getUsername());
                    String temp = String.join("\n", list);
                    outputStream.println(temp);
                }

                // Read the name of the required user from the client, if 'cancel' in any case is received exit the operation.
                String receiverName = inputReader.readLine();
                if (receiverName.equalsIgnoreCase("cancel"))
                    return;

                // Gain thread-safe access to (users) then search for the required user.
                synchronized (users) {
                    receiver = users.findUserName(receiverName);
                }

                // If the receiver name exists and is not the same as the requesting user, write success code 0 then proceed to getting message info.
                if (receiver != null && !receiverName.equals(loggedInUser.getUsername())) {
                    outputStream.println("0");
                    break;
                }

                // If the receiver name does not exist, write error code -1.
                else if (receiver == null)
                    outputStream.println("-1");

                // If the receiver name is the same as the requesting username, write error code -2.
                else
                    outputStream.println("-2");
            }

            // Read the title and content of the message, exiting the operation if 'cancel' is read in any case.
            String title = inputReader.readLine();
            if (title.equalsIgnoreCase("cancel"))
                return;
            String content = inputReader.readLine();
            if (content.equalsIgnoreCase("cancel"))
                return;

            // Create a new message with the entered information, setting the 'read' flag to false and the data to the current date
            Message message = new Message(loggedInUser, receiver, title, content, new Date(), false);

            /* Gain thread-safe access to both the requesting user object, and the receiving user object, and add the message to
            their 'sent' and 'inbox' message boxes respectively.
            */
            synchronized (receiver) {
                receiver.getInbox().addMessage(message);
            }

            synchronized (loggedInUser) {
                loggedInUser.getSent().addMessage(message);
            }

            // Gain thread-safe access to (users), then write it to data.txt on the local system.
            synchronized (users) {
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("data.txt"));
                oos.writeObject(users);
            }

        }

        catch (SocketException ex) {
            // Propagate abnormal socket termination to the run() method.
            throw ex;
        }

        catch (IOException ex) {
            System.out.println(ex);
        }
    }

    /**
     * This method prompts the client for the required message box to view, or otherwise return to main-menu.
     * It calls another method to implement the functionality with 'i' for inbox and 's' for sent.
     **/
    private void viewMessages() throws SocketException {
        try {
            String choice = inputReader.readLine();
            switch (choice) {
                case "1" :
                    viewMessages("i");
                    break;
                case "2" :
                    viewMessages("s");
                case "3":
                    return;
            }
        }

        catch (SocketException ex) {
            // Propagate abnormal socket termination to the run() method.
            throw ex;
        }

        catch (IOException ex) {
            System.out.println(ex);
        }
    }

    /**
     * This method actually implements viewing messages, accepting a parameter that represents the name of the message box
     **/
    private void viewMessages(String messageContainer) throws SocketException {
        try {
            String info = null;
            MessageContainer messages = null;

            /* Gain thread-safe access to the current user, then get the wanted message box based on the parameter
             along with an 'info' string the represents some headline information about the messages to display to the
            user.
             */
            synchronized (loggedInUser) {
                info = (messageContainer.equals("i") ? loggedInUser.getInbox().getReceiveMessageInfo() : loggedInUser.getSent().getSentMessageInfo());
                messages = (messageContainer.equals("i") ? loggedInUser.getInbox() : loggedInUser.getSent());
            }

            // Get the number of messages then send to the client.
            int length = info.length() > 0 ? info.split("\n").length : 0;
            outputStream.println("" + length);

            // If there are messages available, keep prompting the client until getting a valid message number or a 'cancel'.
            if (length != 0) {
                while (true) {
                    // Write the info string to the client.
                    outputStream.println(info);

                    // Read the number sent by the client, exiting when receiving 'cancel' in any case.
                    String messageNumber = inputReader.readLine();
                    if (messageNumber.equalsIgnoreCase("cancel"))
                        return;

                    try {
                        // Get the message requested by the client.
                        Message message = messages.getMessageByNumber(Integer.parseInt(messageNumber));

                        // Write success code 0 if successful.
                        outputStream.println("0");

                        /* Gain thread-safe access to this message, then write it to the client, and if the message is
                        in inbox, set it as read.
                        */
                        synchronized (message) {
                            outputStream.println(message);
                            if (messageContainer.equals("i"))
                                message.setRead(true);
                        }

                        // Gain thread-safe access to (users) then write it to data.txt on the local system.
                        synchronized (users) {
                            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("data.txt"));
                            oos.writeObject(users);
                        }
                        break;
                    }

                    catch (NumberFormatException | IndexOutOfBoundsException ex) {
                        /* If input from client is not a number or a number outside the boundaries of the available
                           messages, write error code -1.
                        */
                        outputStream.println("-1");
                    }
                }

            }
        }

        catch (SocketException ex) {
            // Propagate abnormal socket termination to the run() method.
            throw ex;
        }

        catch (IOException ex) {
            System.out.println(ex);
        }
    }

    /**
     * This method terminates the connection with the client, then causes the thread to terminate.
     */
    private void logOff() throws SocketException {
        try {
            socket.close();
        }

        catch (SocketException ex) {
            // Propagate abnormal socket termination to the run() method.
            throw ex;
        }

        catch (IOException ex) {
            System.out.println(ex);
        }
    }
}
