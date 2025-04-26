
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
                "Purestar64"
        );
    }

    // Hardcoded authentication (no database table needed for login)
    private static String authenticateUser(Connection conn, String username, String password) {
        if (username.equals("admin") && password.equals("admin123")) {
            return "admin";
        } else if (username.equals("employee") && password.equals("pass123")) {
            return "employee";
        }
        return null;
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
    }

    private static boolean promptYesNo(Scanner scanner, String message) {
        System.out.print("\n" + message + " (y/n): ");
        String choice = scanner.nextLine();
        return choice.equalsIgnoreCase("y");
    }

    private static void searchEmployeeData(Connection conn, Scanner scanner, boolean allowUpdate) {
        System.out.println("Search employee by:");
        System.out.println("1. Name");
        System.out.println("2. Date of Birth (YYYY-MM-DD)");
        System.out.println("3. SSN");
        System.out.println("4. Employee ID");
        System.out.print("Enter choice: ");
        int choice = scanner.nextInt();
        scanner.nextLine(); // consume newline

        String query;
        String input;

        switch (choice) {
            case 1 -> {
                System.out.print("Enter full name: ");
                input = scanner.nextLine();
                query = "SELECT * FROM employees WHERE name = ?";
            }
            case 2 -> {
                System.out.print("Enter DOB (YYYY-MM-DD): ");
                input = scanner.nextLine();
                query = "SELECT * FROM employees WHERE dob = ?";
            }
            case 3 -> {
                System.out.print("Enter SSN: ");
                input = scanner.nextLine();
                query = "SELECT * FROM employees WHERE ssn = ?";
            }
            case 4 -> {
                System.out.print("Enter Employee ID: ");
                input = scanner.nextLine();
                query = "SELECT * FROM employees WHERE empid = ?";
            }
            default -> {
                System.out.println("Invalid choice.");
                return;
            }
        }

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, input);
            ResultSet rs = pstmt.executeQuery();

            boolean found = false;
            while (rs.next()) {
                found = true;
                displayEmployeeInfo(rs);

                if (allowUpdate && promptYesNo(scanner, "Do you want to update this employee?")) {
                    updateEmployeeData(conn, scanner, rs.getInt("empid"));
                }
            }

            if (!found) {
                System.out.println("No matching employee found.");
            }

        } catch (SQLException e) {
            System.out.println("Error during search: " + e.getMessage());
        }
    }

    private static void displayEmployeeInfo(ResultSet rs) throws SQLException {
        System.out.println("\n--- Employee Info ---");
        System.out.println("Employee ID: " + rs.getInt("empid"));
        System.out.println("Name: " + rs.getString("name"));
        System.out.println("DOB: " + rs.getDate("dob"));
        System.out.println("SSN: " + rs.getString("ssn"));
        System.out.println("Email: " + rs.getString("email"));
    }

    private static void updateEmployeeData(Connection conn, Scanner scanner, int empId) {
        System.out.println("Which field do you want to update?");
        System.out.println("1. Name");
        System.out.println("2. DOB (YYYY-MM-DD)");
        System.out.println("3. SSN");
        System.out.println("4. Email");
        System.out.print("Enter your choice: ");
        int choice = scanner.nextInt();
        scanner.nextLine(); // consume newline

        String field;
        switch (choice) {
            case 1 -> field = "name";
            case 2 -> field = "dob";
            case 3 -> field = "ssn";
            case 4 -> field = "email";
            default -> {
                System.out.println("Invalid field choice.");
                return;
            }
        }

        System.out.print("Enter the new value: ");
        String newValue = scanner.nextLine();

        String updateQuery = "UPDATE employees SET " + field + " = ? WHERE empid = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
            pstmt.setString(1, newValue);
            pstmt.setInt(2, empId);

            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Employee data updated successfully.");
            } else {
                System.out.println("Update failed. No rows affected.");
            }
        } catch (SQLException e) {
            System.out.println("Update error: " + e.getMessage());
        }
    }
}

// import java.sql.*;
// import java.util.Scanner;
// import java.security.MessageDigest;

// public class main {

//     public static void main(String[] args) {
//         try (Scanner scanner = new Scanner(System.in)) {

//             System.out.print("Enter username: ");
//             String username = scanner.nextLine();

//             System.out.print("Enter password: ");
//             String password = scanner.nextLine();

//             try (Connection conn = connectToDatabase()) {
//                 String role = authenticateUser(conn, username, password);

//                 if (role != null) {
//                     System.out.println("Login successful. Role: " + role);

//                     if (role.equalsIgnoreCase("admin")) {
//                         handleAdmin(conn, scanner);
//                     } else {
//                         handleEmployee(conn, username);
//                     }
//                 } else {
//                     System.out.println("Login failed. Please check your username and password.");
//                 }

//             } catch (Exception e) {
        
//             }
//         }
//     }

//     // ✅ Connect to MySQL
//     private static Connection connectToDatabase() throws SQLException, ClassNotFoundException {
//         Class.forName("com.mysql.cj.jdbc.Driver");
//         return DriverManager.getConnection(
//                 "jdbc:mysql://localhost:3306/employeeData",
//                 "root",
//                 "Purestar64"
//         );
//     }

//     // ✅ Authenticate using hashed password
//     private static String authenticateUser(Connection conn, String username, String password) {
//         String hashedPassword = hashPassword(password);

//         // Precomputed SHA-256 hashes
//         String adminHash = "a58b46fe66f989ca7fffd0a8a6c77e0d7db5c1879dc3aa1d4a6a0e8e719b56b8";    // admin123
//         String employeeHash = "c0e97d8e9bc49a4bcdd5d7e9a238e4c221d58e3b1e7aa0f90a3d5cc21f427253"; // pass123

//         if (username.equals("admin") && hashedPassword.equals(adminHash)) {
//             return "admin";
//         } else if (username.equals("employee") && hashedPassword.equals(employeeHash)) {
//             return "employee";
//         }

//         return null;
//     }

//     // ✅ Hash the password using SHA-256
//     private static String hashPassword(String password) {
//         try {
//             MessageDigest digest = MessageDigest.getInstance("SHA-256");
//             byte[] hash = digest.digest(password.getBytes("UTF-8"));
//             StringBuilder hexString = new StringBuilder();

//             for (byte b : hash) {
//                 String hex = Integer.toHexString(0xff & b);
//                 if (hex.length() == 1) hexString.append('0');
//                 hexString.append(hex);
//             }

//             return hexString.toString();
//         } catch (Exception e) {
//             throw new RuntimeException("Error hashing password", e);
//         }
//     }

//     private static void handleAdmin(Connection conn, Scanner scanner) {
//         System.out.println("Welcome Admin. You have full access.");

//         if (promptYesNo(scanner, "Do you want to update any employee information?")) {
//             searchEmployeeData(conn, scanner, true);
//         } else {
//             System.out.println("No updates will be made.");
//         }

//         while (promptYesNo(scanner, "Do you still want to search for employee information?")) {
//             searchEmployeeData(conn, scanner, false);
//         }

//         System.out.println("Exiting the program...");
//     }

//     private static void handleEmployee(Connection conn, String username) throws SQLException {
//         System.out.println("Welcome Employee. Fetching your data...");

//         String empQuery = "SELECT * FROM employees WHERE email = ?";
//         try (PreparedStatement stmt = conn.prepareStatement(empQuery)) {
//             stmt.setString(1, username + "@example.com");
//             ResultSet empData = stmt.executeQuery();

//             if (empData.next()) {
//                 displayEmployeeInfo(empData);
//             } else {
//                 System.out.println("No employee record found.");
//             }
//         }
//     }

//     private static boolean promptYesNo(Scanner scanner, String message) {
//         System.out.print("\n" + message + " (y/n): ");
//         String choice = scanner.nextLine();
//         return choice.equalsIgnoreCase("y");
//     }

//     private static void searchEmployeeData(Connection conn, Scanner scanner, boolean allowUpdate) {
//         System.out.println("Search employee by:");
//         System.out.println("1. Name");
//         System.out.println("2. Date of Birth (YYYY-MM-DD)");
//         System.out.println("3. SSN");
//         System.out.println("4. Employee ID");
//         System.out.print("Enter choice: ");
//         int choice = scanner.nextInt();
//         scanner.nextLine(); // consume newline

//         String query;
//         String input;

//         switch (choice) {
//             case 1 -> {
//                 System.out.print("Enter full name: ");
//                 input = scanner.nextLine();
//                 query = "SELECT * FROM employees WHERE name = ?";
//             }
//             case 2 -> {
//                 System.out.print("Enter DOB (YYYY-MM-DD): ");
//                 input = scanner.nextLine();
//                 query = "SELECT * FROM employees WHERE dob = ?";
//             }
//             case 3 -> {
//                 System.out.print("Enter SSN: ");
//                 input = scanner.nextLine();
//                 query = "SELECT * FROM employees WHERE ssn = ?";
//             }
//             case 4 -> {
//                 System.out.print("Enter Employee ID: ");
//                 input = scanner.nextLine();
//                 query = "SELECT * FROM employees WHERE empid = ?";
//             }
//             default -> {
//                 System.out.println("Invalid choice.");
//                 return;
//             }
//         }

//         try (PreparedStatement pstmt = conn.prepareStatement(query)) {
//             pstmt.setString(1, input);
//             ResultSet rs = pstmt.executeQuery();

//             boolean found = false;
//             while (rs.next()) {
//                 found = true;
//                 displayEmployeeInfo(rs);

//                 if (allowUpdate && promptYesNo(scanner, "Do you want to update this employee?")) {
//                     updateEmployeeData(conn, scanner, rs.getInt("empid"));
//                 }
//             }

//             if (!found) {
//                 System.out.println("No matching employee found.");
//             }

//         } catch (SQLException e) {
//             System.out.println("Error during search: " + e.getMessage());
//         }
//     }

//     private static void displayEmployeeInfo(ResultSet rs) throws SQLException {
//         System.out.println("\n--- Employee Info ---");
//         System.out.println("Employee ID: " + rs.getInt("empid"));
//         System.out.println("Name: " + rs.getString("name"));
//         System.out.println("DOB: " + rs.getDate("dob"));
//         System.out.println("SSN: " + rs.getString("ssn"));
//         System.out.println("Email: " + rs.getString("email"));
//     }

//     private static void updateEmployeeData(Connection conn, Scanner scanner, int empId) {
//         System.out.println("Which field do you want to update?");
//         System.out.println("1. Name");
//         System.out.println("2. DOB (YYYY-MM-DD)");
//         System.out.println("3. SSN");
//         System.out.println("4. Email");
//         System.out.print("Enter your choice: ");
//         int choice = scanner.nextInt();
//         scanner.nextLine(); // consume newline

//         String field;
//         switch (choice) {
//             case 1 -> field = "name";
//             case 2 -> field = "dob";
//             case 3 -> field = "ssn";
//             case 4 -> field = "email";
//             default -> {
//                 System.out.println("Invalid field choice.");
//                 return;
//             }
//         }

//         System.out.print("Enter the new value: ");
//         String newValue = scanner.nextLine();

//         String updateQuery = "UPDATE employees SET " + field + " = ? WHERE empid = ?";
//         try (PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
//             pstmt.setString(1, newValue);
//             pstmt.setInt(2, empId);

//             int rowsUpdated = pstmt.executeUpdate();
//             if (rowsUpdated > 0) {
//                 System.out.println("Employee data updated successfully.");
//             } else {
//                 System.out.println("Update failed. No rows affected.");
//             }
//         } catch (SQLException e) {
//             System.out.println("Update error: " + e.getMessage());
//         }
//     }
// }
