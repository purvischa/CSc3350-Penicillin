/**
 * Employee Management System
 * A simple system to manage employee information
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("Welcome to Employee Management System");
        System.out.println("-----------------------------------");

        // Show login menu and get user credentials
        String[] authInfo = Menu.showLoginMenu();
        if (authInfo == null) {
            System.out.println("Login failed. Exiting...");
            return;
        }

        // Handle user based on their role
        String role = authInfo[0];
        int empId = Integer.parseInt(authInfo[1]);
        
        if (role.equals("admin")) {
            Menu.showAdminMenu();
        } else {
            Menu.showEmployeeMenu(empId);
        }

        System.out.println("Thank you for using Employee Management System");
    }
}
