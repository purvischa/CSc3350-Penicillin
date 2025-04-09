
// testing employee login
import java.sql.*;
import java.util.Scanner;

public class main {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {

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
                "Purestar64");
    }

    private static String authenticateUser(Connection conn, String username, String password) throws SQLException {
        String loginQuery = "SELECT role FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement stmt = conn.prepareStatement(loginQuery)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getString("role") : null;
        }
    }

    private static void handleAdmin(Connection conn, Scanner scanner) {
        System.out.println("Welcome Admin. You have full access.");

        if (promptYesNo(scanner, "Do you want to update any employee information?")) {
            searchEmployeeData(conn, scanner, true);
        } else {
            System.out.println("No updates will be made.");
        }

        while (promptYesNo(scanner, "Do you still want to search for employee information?")) {
            searchEmployeeData(conn, scanner, false);
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
        System.out.println("You have read-only access. Exiting the program...");
    }

    private static boolean promptYesNo(Scanner scanner, String message) {
        System.out.print("\n" + message + " (y/n): ");
        String choice = scanner.nextLine();
        return choice.equalsIgnoreCase("y");
    }

    // The rest of the methods (`searchEmployeeData`, `updateEmployeeData`,
    // `displayEmployeeInfo`) remain unchanged
}
