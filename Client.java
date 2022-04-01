import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketException;

public class Client {
    public  static final int SERVICE_PORT = 4000;
    private static Socket socket;  // This connection socket from the client side.
    private static PrintStream outputStream; // An output stream to write to the server.
    private static BufferedReader serverInputReader; // An input reader to read from the server.
    private static BufferedReader userInputReader; // An input reader to read user input from the console.

    public static void main(String[] args) {
        try {
            // Connect to the server on SERVICE_PORT = 4000.
            socket = new Socket("localhost", SERVICE_PORT);

            outputStream = new PrintStream(socket.getOutputStream());
            serverInputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            userInputReader = new BufferedReader(new InputStreamReader(System.in));
            /* Continuously read the initial choice number to either log-in or sign-up,
            if log-in is successful enter serveUser which presents the main-menu, then when done terminate the client.
             */
            OuterLoop: while (true) {
                System.out.println("Choose operation number:\n1. Log-in\n2. Sign-up");
                String choice = userInputReader.readLine();
                outputStream.println(choice);

                switch (choice) {
                    case "1":
                        String serverResponse = logIn();
                        // If log-in is not successful(serverResponse != 0) re-prompt for a choice.
                        if (!(serverResponse.equals("0")))
                            continue;
                        else
                            serveUser();
                        break OuterLoop;

                    case "2":
                        // Always re-prompt for choice after sign-up (successful or not successful).
                        signUp();
                    break;

                    default:
                        System.out.println("\nInvalid input.\n");

                }
            }
        }

        catch (SocketException ex) {
            try {
                // If the connection ends unexpectedly from the server side, close the socket and terminate the client.
                socket.close();
            }

            catch (IOException iox) {
                System.out.println(ex);
            }
        }

        catch (IOException ex) {
            System.out.println(ex);
        }
    }

    /**
     * This method reads the username and password from the user and sends it to the server,
     *  then receive the server response and notify the user of the result of his log-in based on the server response.
     *  It also returns the server response.
     **/
    private static String logIn() throws SocketException {
        String serverResponse = null;
        try {
            // Read username and password from user console and write to the server.
            System.out.println("Enter your username:");
            outputStream.println(userInputReader.readLine());
            System.out.println("Enter your password:");
            outputStream.println(userInputReader.readLine());

            // Read the server response.
            serverResponse = serverInputReader.readLine();

            // Based on server response, notify the user.
            if (serverResponse.equals("-2"))
                System.out.println("\nUsername does not exist.\n");
            else if (serverResponse.equals("-1"))
                System.out.println("\nPassword is incorrect.\n");
            else
                System.out.println("\nLog-in successful.\n");
        }

        catch (SocketException ex) {
            // Propagate the abnormal socket termination to the above main() method.
            throw ex;
        }

        catch (IOException ex) {
            System.out.println(ex);
        }

        return serverResponse;
    }

    /**
     * This method reads the username and password from the user and sends it to the server
     * then receive the server response and notify the user of the result of his sign-up based on the server response.
     */
    private static void signUp() throws SocketException {
        try {
            // Read username and password from user console and write to the server.
            System.out.println("Enter your username: ");
            outputStream.println(userInputReader.readLine());
            System.out.println("Enter your password: ");
            outputStream.println(userInputReader.readLine());

            // Read the server response.
            String serverResponse = serverInputReader.readLine();

            // Based on server response, notify the user.
            if (serverResponse.equals("-1"))
                System.out.println("\nUsername already exists.\n");
            else
                System.out.println("\nSign-up successful.\n");
        }

        catch (SocketException ex) {
            // Propagate the abnormal socket termination to the above main() method.
            throw ex;
        }

        catch (IOException ex) {
            System.out.println(ex);
        }
    }

    /**
     * This method represents the main-menu of the application, where the user will choose an operation to perform.
     * The choice is also sent to the server to do the back-end work of selected operation.
     */
    private static void serveUser() throws SocketException {
        try {
            OuterLoop: while (true) {
                System.out.println("Enter operation number:\n1. Send a message\n2. View messages\n3. Log-off");
                String choice = userInputReader.readLine();
                outputStream.println(choice);
                switch (choice) {
                    case "1":
                        sendMessage();
                        break;

                    case "2":
                        viewMessages();
                        break;

                    case "3":
                        logOff();
                        break OuterLoop;

                    default:
                        System.out.println("\nInvalid input.\n");
                }
            }
        }

        catch (SocketException ex) {
            // Propagate the abnormal socket termination to the above main() method.
            throw ex;
        }

        catch (IOException ex) {
            System.out.println(ex);
        }
    }

    /**
     * This method represents the user-interface during the operation of sending a message,
     * it reads server response codes and notifies the user of the state of the operation.
     */
    private static void sendMessage() throws SocketException {
        try {
            // Keep prompting the user for names of users until a valid name is given.
            while (true) {
                // Read the number of other users available from the server.
                int numberOfUsers = Integer.parseInt(serverInputReader.readLine());

                // If there are no other users, notify the current user then exit the operation.
                if (numberOfUsers == 0)  {
                    System.out.println("\n<< No other registered users >>\n");
                    return;
                }
                // Read the list of other user names then notify the user to choose a name.
                System.out.println("Enter the name of the receiver: (enter \"cancel\" to return to main-menu)");
                for (int i = 0; i < numberOfUsers; i++)
                    System.out.println(serverInputReader.readLine());

                // Read the name from the user and send it to the server, if 'cancel' in any case is received exit the operation.
                String receiverName = userInputReader.readLine();
                outputStream.println(receiverName);
                if (receiverName.equalsIgnoreCase("cancel"))
                    return;

                // Read the server response code.
                String serverResponse = serverInputReader.readLine();

                // If a success code returns, proceed to getting message info
                if (serverResponse.equals("0"))
                    break;

                // If an error code returns, notify the user of the nature of the error and re-prompt for a valid name.
                else if (serverResponse.equals("-1"))
                    System.out.println("\nReceiver name does not exist. Enter a valid name.\n");
                else
                    System.out.println("\nYou cannot send a message to yourself!\n");
            }

            /* Read the title and content of the message and write them to the server,
             exiting the operation if 'cancel' is read in any case.
             */
            System.out.println("Enter the title of the message: (enter \"cancel\" to return to main-menu)");
            String title = userInputReader.readLine();
            outputStream.println(title);
            if (title.equalsIgnoreCase("cancel"))
                return;
            System.out.println("Enter the content of the message: (enter \"cancel\" to return to main-menu)");
            String content = userInputReader.readLine();
            outputStream.println(content);
        }

        catch (SocketException ex) {
            // Propagate the abnormal socket termination to the above main() method.
            throw ex;
        }

        catch (IOException ex) {
            System.out.println(ex);
        }
    }

    /**
     * This method prompts the client for the required message box to view, or otherwise return to main-menu.
     * It then gets the info of the selected message box, and prompts the user for a message to view its contents.
     **/
    private static void viewMessages() throws SocketException {
        try {
            System.out.println("Enter choice number: \n1. View inbox messages\n2. View sent messages\n3. Return to main-menu");
            String choice = userInputReader.readLine();
            outputStream.println(choice);
            switch (choice) {
                case "1":
                case "2":
                    // Read the number of messages from the server.
                    int numOfMessages = Integer.parseInt(serverInputReader.readLine());

                    if (numOfMessages != 0) {
                        while (true) {
                            // Read the headline info of the messages in the selected box.
                            System.out.println("Choose the message number to display its contents. (enter \"cancel\" to return to main-menu)");
                            for (int i = 0; i < numOfMessages; i++)
                                System.out.println(serverInputReader.readLine());

                            /* Read from the user the message number to view its contents, then write to server.
                               if 'cancel' is read in any case, the operation is exited.
                            */

                            String messageNumber = userInputReader.readLine();
                            outputStream.println(messageNumber);
                            if (messageNumber.equalsIgnoreCase("cancel"))
                                return;

                            // Read the server response code.
                            String serverResponse = serverInputReader.readLine();

                            // If a success code returns, read the message and display to the user.
                            if (serverResponse.equals("0")) {
                                for (int i = 0; i < 5; i++)
                                    System.out.println(serverInputReader.readLine());
                                break;
                            }

                            // If an error code returns, re-prompt the user for a valid message number.
                            else
                                System.out.println("\nInvalid input. Please enter a number of one of the messages shown.\n");
                        }
                    }
                    else
                        // If there are no messages available in this box, notify the user and exit.
                        System.out.println("\n<< No messages available >>\n");

                case "3":
                    return;

                default:
                    System.out.println("\nInvalid input.\n");
            }
        }

        catch (SocketException ex) {
            throw ex;
        }

        catch (IOException ex) {
            System.out.println(ex);
        }
    }

    /**
     * This method terminates the connection with the server, then causes the client to terminate.
     */
    private static void logOff() throws SocketException {
        try {
            socket.close();
        }

        catch (SocketException ex) {
            // Propagate the abnormal socket termination to the above main() method.
            throw ex;
        }

        catch (IOException ex) {
            System.out.println(ex);
        }
    }


}
