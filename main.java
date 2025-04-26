import java.sql.*;
import java.util.Scanner;

public class main {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            // Display login menu
            System.out.println("\n=== Login Menu ===\n");
            System.out.println("For Administrators:");
            System.out.println("  Username: admin");
            System.out.println("  Password: admin123\n");
            System.out.println("For Employees:");
            System.out.println("  Username format: Firstname_Lastname (e.g., John_Doe)");
            System.out.println("  Password format: Your employee ID (e.g., 7)\n");
            System.out.println("==================\n");

            System.out.print("Enter username: ");
            String username = scanner.nextLine();

            System.out.print("Enter password: ");
            String password = scanner.nextLine();

            try (Connection conn = connectToDatabase()) {
                String role = authenticateUser(conn, username, password);

                if (role != null) {
                    System.out.println("Login successful. Role: " + role);

                    if (role.equalsIgnoreCase("admin")) {
                        handleAdmin(conn, scanner);
                    } else {
                        handleEmployee(conn, username);
                    }
                } else {
                    System.out.println("Login failed. Please check your username and password.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static Connection connectToDatabase() throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/employeeData",
                "root",
                "Aga2025tha?!"
        );
    }

    private static String authenticateUser(Connection conn, String username, String password) throws SQLException {
        // First try admin authentication
        String adminQuery = "SELECT role FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement stmt = conn.prepareStatement(adminQuery)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("role");
            }
        }

        // If admin auth fails, try employee authentication
        String empQuery = "SELECT id FROM employees WHERE CONCAT(fname, '_', lname) = ? AND id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(empQuery)) {
            stmt.setString(1, username);
            try {
                stmt.setInt(2, Integer.parseInt(password));
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return "employee";
                }
            } catch (NumberFormatException e) {
                // Password is not a valid employee ID
                return null;
            }
        }

        return null;
    }

    private static void handleAdmin(Connection conn, Scanner scanner) {
        System.out.println("Welcome Admin. You have full access.");

        if (promptYesNo(scanner, "Do you want to search for employee information?")) {
            Menu.searchEmployees();
        } else {
            System.out.println("No search will be performed.");
        }

        System.out.println("Exiting the program...");
    }

    private static void handleEmployee(Connection conn, String username) throws SQLException {
        System.out.println("Welcome Employee. Fetching your data...");

        String empQuery = "SELECT * FROM employees WHERE email = ?";
        try (PreparedStatement stmt = conn.prepareStatement(empQuery)) {
            stmt.setString(1, username + "@example.com");
            ResultSet empData = stmt.executeQuery();

            if (empData.next()) {
                displayEmployeeInfo(empData);
            } else {
                System.out.println("No employee record found.");
            }
        }
    }

    private static boolean promptYesNo(Scanner scanner, String message) {
        System.out.print("\n" + message + " (y/n): ");
        String choice = scanner.nextLine();
        return choice.equalsIgnoreCase("y");
    }

    private static void displayEmployeeInfo(ResultSet empData) throws SQLException {
        System.out.println("\nEmployee Information:");
        System.out.println("------------------------");
        System.out.println("Name: " + empData.getString("fname") + " " + empData.getString("lname"));
        System.out.println("Email: " + empData.getString("email"));
        System.out.println("Phone: " + empData.getString("phone"));
        System.out.println("Address: " + empData.getString("address"));
        System.out.println("Salary: $" + String.format("%.2f", empData.getDouble("salary")));
        System.out.println("------------------------");
    }
}