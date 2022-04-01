import java.io.Serializable;
import java.util.ArrayList;

public class UserContainer implements Serializable {
    private ArrayList<User> users;


    public UserContainer() {
        users = new ArrayList<User>();
    }

    /**
     * This method takes a username and searches the (users) list for a User that has that username,
     * if the object is found it is returned, otherwise null is returned.
     **/
    public User findUserName(String username) {
        for (User user : users) {
            if (user.getUsername().equals(username))
                return user;
        }
        return null;
    }

    /**
     * This method takes a User object and a password and checks to see if there is a user with a matching credentials.
     * if the passed User is null or if the User is not found in the list or if the user is found but the password does
     * not match the User then false is returned, otherwise the return value is true.
     **/
    public boolean authenticateUser(User potentialUser, String password) {
        if (potentialUser == null) return false;
        for (User user : users) {
            if (user.getUsername().equals(potentialUser.getUsername()))
                return user.getPassword().equals(password);
        }
        return false;
    }

    /**
     * This method takes a new username and password, creates a User object and adds it to the (users) list.
     **/
    public void addUser(String username, String password) {
        User user = new User(username, password);
        users.add(user);
    }

    /**
     * This method returns the number of users in the (users) list.
     **/
    public int getNumberOfUsers() {
        return users.size();
    }

    /**
     * This method returns the names of all users in the (users) list, each name on a separate line.
     **/
    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        for (User user : users)
            buffer.append(user.getUsername() + "\n");
        return buffer.toString().stripTrailing();
    }
}
