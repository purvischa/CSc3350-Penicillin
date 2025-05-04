/**
 * Entry point for the Employee Management console application.
 * 
 * This class handles initial user authentication and then delegates
 * to the Menu class for the main application loop.
 */
public class Main {

    /**
     * Main method invoked by the JVM.
     * @param args Command-line arguments (not used)
     */
    public static void main(String[] args) {
        // Show the login menu and capture the result:
        // auth[0] will be "admin" or "employee", auth[1] will be the empId string.
        String[] auth = Menu.showLoginMenu();

        // If the user failed to authenticate (or exceeded attempts), exit.
        if (auth == null) {
            System.out.println("Goodbye.");
            return;
        }

        // Initialize the Menu with the authenticated role and employee ID.
        // This sets up access controls (admin vs. employee).
        Menu.initialize(auth[0], Integer.parseInt(auth[1]));

        // Enter the main application loop (console menu).
        Menu.showMainMenu();

        // After the user exits the menu loop, print a farewell message.
        System.out.println("Exited.");
    }
}