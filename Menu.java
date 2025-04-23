import java.util.Scanner;
import java.util.List;

/**
 * Handles all menu operations and user interaction
 */
public class Menu {
    private static final Scanner scanner = new Scanner(System.in);

    /**
     * Show login menu and handle authentication
     * @return String array with [role, empId] if successful, null if not
     */
    public static String[] showLoginMenu() {
        int maxAttempts = 3;
        int attempts = 0;

        while (attempts < maxAttempts) {
            System.out.println("\nLogin Instructions:");
            System.out.println("------------------");
            System.out.println("For Admin:");
            System.out.println("  Use admin login");
            System.out.println("\nFor Employees:");
            System.out.println("  Username: firstname_lastname (e.g., john_doe)");
            System.out.println("  Password: your employee ID number");
            System.out.println("------------------\n");

            System.out.print("Enter username: ");
            String username = scanner.nextLine().trim();

            System.out.print("Enter password: ");
            String password = scanner.nextLine().trim();

            String authResult = EmployeeDAO.authenticateUser(username, password);
            if (authResult != null) {
                String[] parts = authResult.split("\\|");
                System.out.println("Login successful! Role: " + parts[0]);
                return parts;
            }

            attempts++;
            int remaining = maxAttempts - attempts;
            System.out.println("Login failed. " + remaining + " attempts remaining.");
            
            if (remaining > 0 && !promptYesNo("Try again?")) {
                break;
            }
        }
        return null;
    }

    /**
     * Show employee menu and handle employee operations
     */
    public static void showEmployeeMenu(int empId) {
        while (true) {
            System.out.println("\nEmployee Menu:");
            System.out.println("1. View My Information");
            System.out.println("2. Update My Information");
            System.out.println("3. Logout");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    Employee emp = EmployeeDAO.getEmployee(empId);
                    if (emp != null) {
                        System.out.println(emp);
                    }
                    break;
                    
                case "2":
                    updateEmployeeInfo(empId);
                    break;
                    
                case "3":
                    System.out.println("Logging out...");
                    return;
                    
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    /**
     * Show admin menu and handle admin operations
     */
    public static void showAdminMenu() {
        while (true) {
            System.out.println("\nAdmin Menu:");
            System.out.println("1. Search Employees");
            System.out.println("2. Update Employee");
            System.out.println("3. Logout");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    searchEmployees();
                    break;
                    
                case "2":
                    System.out.print("Enter employee ID: ");
                    try {
                        int empId = Integer.parseInt(scanner.nextLine().trim());
                        updateEmployeeInfo(empId);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid employee ID format.");
                    }
                    break;
                    
                case "3":
                    System.out.println("Logging out...");
                    return;
                    
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    /**
     * Handle employee information update
     */
    private static void updateEmployeeInfo(int empId) {
        Employee emp = EmployeeDAO.getEmployee(empId);
        if (emp == null) {
            System.out.println("Employee not found.");
            return;
        }

        System.out.println("\nCurrent Information:");
        System.out.println(emp);

        System.out.print("Enter new first name (or press Enter to keep current): ");
        String firstName = scanner.nextLine().trim();
        if (!firstName.isEmpty()) emp.setFirstName(firstName);

        System.out.print("Enter new last name (or press Enter to keep current): ");
        String lastName = scanner.nextLine().trim();
        if (!lastName.isEmpty()) emp.setLastName(lastName);

        System.out.print("Enter new email (or press Enter to keep current): ");
        String email = scanner.nextLine().trim();
        if (!email.isEmpty()) emp.setEmail(email);

        System.out.print("Enter new phone number (or press Enter to keep current): ");
        String phone = scanner.nextLine().trim();
        if (!phone.isEmpty()) emp.setPhoneNumber(phone);

        if (EmployeeDAO.updateEmployee(emp)) {
            System.out.println("Employee information updated successfully!");
        } else {
            System.out.println("Failed to update employee information.");
        }
    }

    /**
     * Handle employee search
     */
    private static void searchEmployees() {
        System.out.print("Enter employee name to search: ");
        String name = scanner.nextLine().trim();
        
        List<Employee> employees = EmployeeDAO.searchEmployees(name);
        if (employees.isEmpty()) {
            System.out.println("No employees found.");
            return;
        }

        System.out.println("\nFound " + employees.size() + " employees:");
        for (Employee emp : employees) {
            System.out.println("\n" + emp);
        }
    }

    /**
     * Utility method to get yes/no input from user
     */
    private static boolean promptYesNo(String message) {
        while (true) {
            System.out.print(message + " (y/n): ");
            String response = scanner.nextLine().trim().toLowerCase();
            if (response.equals("y") || response.equals("yes")) return true;
            if (response.equals("n") || response.equals("no")) return false;
            System.out.println("Please enter 'y' or 'n'");
        }
    }
}
