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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static Connection connectToDatabase() throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/employeedata",
                "root",
                "Aga2025tha?!"
        );
    }

    // ✅ Hardcoded authentication (no database table needed for login)
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
        while (true) {
            System.out.println("\nAdmin Menu:");
            System.out.println("1. Search and Update Employee Data");
            System.out.println("2. Update Salary Range by Percentage");
            System.out.println("3. Insert New Employee");
            System.out.println("4. Exit");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    searchEmployeeData(conn, scanner, true);
                    break;
                case "2":
                    updateSalaryRange(conn, scanner);
                    break;
                case "3":
                    insertNewEmployee(conn, scanner);
                    break;
                case "4":
                    System.out.println("Exiting the program...");
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    // Admin-only: Update salaries by a percentage within a salary range
    private static void updateSalaryRange(Connection conn, Scanner scanner) {
        try {
            System.out.print("Enter minimum salary for range (e.g., 58000): ");
            double minSalary = Double.parseDouble(scanner.nextLine());
            System.out.print("Enter maximum salary for range (e.g., 105000): ");
            double maxSalary = Double.parseDouble(scanner.nextLine());
            System.out.print("Enter percentage increase (e.g., 3.2): ");
            double percentage = Double.parseDouble(scanner.nextLine());

            // Show how many employees will be affected
            String countQuery = "SELECT COUNT(*) as count FROM employees WHERE salary >= ? AND salary < ?";
            try (PreparedStatement pstmt = conn.prepareStatement(countQuery)) {
                pstmt.setDouble(1, minSalary);
                pstmt.setDouble(2, maxSalary);
                ResultSet rs = pstmt.executeQuery();
                rs.next();
                int affectedCount = rs.getInt("count");
                System.out.println("\nThis will affect " + affectedCount + " employee(s).");
                if (!promptYesNo(scanner, "Do you want to proceed with the update?")) {
                    System.out.println("Update cancelled.");
                    return;
                }
                // Perform the update
                String updateQuery = "UPDATE employees SET salary = salary * (1 + ?/100) WHERE salary >= ? AND salary < ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                    updateStmt.setDouble(1, percentage);
                    updateStmt.setDouble(2, minSalary);
                    updateStmt.setDouble(3, maxSalary);
                    int updatedRows = updateStmt.executeUpdate();
                    System.out.println("Successfully updated salaries for " + updatedRows + " employee(s).\n");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error updating salary range: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format. Please enter valid numbers.");
        }

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

    private static void displayEmployeeInfo(ResultSet rs) throws SQLException {
        System.out.println("\n--- Employee Info ---");
        System.out.println("Employee ID: " + rs.getInt("empid"));
        System.out.println("Name: " + rs.getString("Fname") + " " + rs.getString("Lname"));
        System.out.println("Email: " + rs.getString("email"));
        System.out.println("Hire Date: " + rs.getDate("HireDate"));
        System.out.println("Salary: $" + String.format("%,.2f", rs.getDouble("Salary")));
        System.out.println("SSN: " + rs.getString("SSN"));
    }

    private static void searchEmployeeData(Connection conn, Scanner scanner, boolean allowUpdate) {
        System.out.println("Search employee by:");
        System.out.println("1. Name");
        System.out.println("2. Hire Date (YYYY-MM-DD)");
        System.out.println("3. SSN");
        System.out.println("4. Employee ID");
        System.out.print("Enter choice: ");
        int choice = scanner.nextInt();
        scanner.nextLine(); // consume newline

        String query;
        String input;

        switch (choice) {
            case 1 -> {
                System.out.print("Enter first name: ");
                String fname = scanner.nextLine();
                System.out.print("Enter last name: ");
                String lname = scanner.nextLine();
                query = "SELECT * FROM employees WHERE Fname = ? AND Lname = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                    pstmt.setString(1, fname);
                    pstmt.setString(2, lname);
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
                    return;
                } catch (SQLException e) {
                    System.out.println("Error during search: " + e.getMessage());
                    return;
                }
            }
            case 2 -> {
                System.out.print("Enter hire date (YYYY-MM-DD): ");
                input = scanner.nextLine();
                query = "SELECT * FROM employees WHERE HireDate = ?";
            }
            case 3 -> {
                System.out.print("Enter SSN: ");
                input = scanner.nextLine();
                query = "SELECT * FROM employees WHERE SSN = ?";
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

    private static void updateEmployeeData(Connection conn, Scanner scanner, int empid) {
        System.out.println("Which field do you want to update?");
        System.out.println("1. First Name");
        System.out.println("2. Last Name");
        System.out.println("3. Email");
        System.out.println("4. Hire Date (YYYY-MM-DD)");
        System.out.println("5. Salary");
        System.out.println("6. SSN");
        System.out.print("Enter your choice: ");
        int updateChoice = scanner.nextInt();
        scanner.nextLine(); // consume newline

        String field;
        boolean isNumber = false;
        switch (updateChoice) {
            case 1 -> field = "Fname";
            case 2 -> field = "Lname";
            case 3 -> field = "email";
            case 4 -> field = "HireDate";
            case 5 -> {
                field = "Salary";
                isNumber = true;
            }
            case 6 -> field = "SSN";
            default -> {
                System.out.println("Invalid field choice.");
                return;
            }
        }

        System.out.print("Enter the new value: ");
        String newValue = scanner.nextLine();

        String updateQuery = "UPDATE employees SET " + field + " = ? WHERE empid = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
            if (isNumber) {
                pstmt.setDouble(1, Double.parseDouble(newValue.replace("$", "").replace(",", "")));
            } else if (field.equals("HireDate")) {
                pstmt.setDate(1, java.sql.Date.valueOf(newValue));
            } else {
                pstmt.setString(1, newValue);
            }
            pstmt.setInt(2, empid);

            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Employee data updated successfully.");
                // Show updated employee info
                String fetchQuery = "SELECT * FROM employees WHERE empid = ?";
                try (PreparedStatement fetchStmt = conn.prepareStatement(fetchQuery)) {
                    fetchStmt.setInt(1, empid);
                    ResultSet rs = fetchStmt.executeQuery();
                    if (rs.next()) {
                        displayEmployeeInfo(rs);
                    }
                }
            } else {
                System.out.println("Update failed. No rows affected.");
            }
        } catch (SQLException e) {
            System.out.println("Update error: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format for salary.");
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid date format. Use YYYY-MM-DD.");
        }
    }

    private static int getNextAvailableempid(Connection conn) throws SQLException {
        String query = "SELECT MAX(empid) as max_id FROM employees";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt("max_id") + 1;
            }
            return 1; // If no employees exist yet
        }
    }

    private static void insertNewEmployee(Connection conn, Scanner scanner) {
        while (true) {
            try {
                System.out.println("\n--- Insert New Employee ---");
                
                // Ask about employee ID
                System.out.print("Do you have an employee ID? (y/n): ");
                String hasId = scanner.nextLine().trim().toLowerCase();
                
                int empid;
                if (hasId.equals("y")) {
                    while (true) {
                        System.out.print("Enter employee ID or 'exit' to cancel: ");
                        String empidStr = scanner.nextLine().trim();
                        if (empidStr.equalsIgnoreCase("exit")) return;
                        try {
                            empid = Integer.parseInt(empidStr);
                            // Check if ID exists
                            String checkQuery = "SELECT empid FROM employees WHERE empid = ?";
                            try (PreparedStatement pstmt = conn.prepareStatement(checkQuery)) {
                                pstmt.setInt(1, empid);
                                ResultSet rs = pstmt.executeQuery();
                                if (rs.next()) {
                                    System.out.println("Error: Employee ID already exists.");
                                    continue;
                                }
                                break;
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Error: Please enter a valid integer ID.");
                        }
                    }
                } else if (hasId.equals("n")) {
                    empid = getNextAvailableempid(conn);
                    System.out.println("Using next available ID: " + empid);
                } else {
                    System.out.println("Invalid input. Please enter 'y' or 'n'.");
                    continue;
                }
                
                // Validate first name
                String Fname;
                while (true) {
                    System.out.print("Enter employee first name or 'exit' to cancel: ");
                    Fname = scanner.nextLine().trim();
                    if (Fname.equalsIgnoreCase("exit")) return;
                    if (Fname.matches("^[A-Z][a-z]+(?:[-'][A-Z][a-z]+)?$")) break;
                    System.out.println("Formatting error: First name must start with capital letter, can include hyphen or apostrophe.");
                }

                // Validate last name
                String Lname;
                while (true) {
                    System.out.print("Enter employee last name or 'exit' to cancel: ");
                    Lname = scanner.nextLine().trim();
                    if (Lname.equalsIgnoreCase("exit")) return;
                    if (Lname.matches("^[A-Z][a-z]+(?:[-'][A-Z][a-z]+)?$")) break;
                    System.out.println("Formatting error: Last name must start with capital letter, can include hyphen or apostrophe.");
                }
                
                // Validate email
                String email;
                while (true) {
                    System.out.print("Enter email John.Doe@example.com or 'exit' to cancel: ");
                    email = scanner.nextLine().trim();
                    if (email.equalsIgnoreCase("exit")) return;
                    if (email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) break;
                    System.out.println("Formatting error: Invalid email format.");
                }

                // Validate hire date
                java.sql.Date HireDate;
                while (true) {
                    System.out.print("Enter hire date (YYYY-MM-DD) or 'exit' to cancel: ");
                    String HireDateStr = scanner.nextLine().trim();
                    if (HireDateStr.equalsIgnoreCase("exit")) return;
                    if (HireDateStr.matches("^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])$")) {
                        HireDate = java.sql.Date.valueOf(HireDateStr);
                        break;
                    }
                    System.out.println("Formatting error: Date must be in YYYY-MM-DD format with valid month and day.");
                }

                // Validate salary
                double Salary;
                while (true) {
                    System.out.print("Enter salary (e.g., 50000 or 50,000.00) or 'exit' to cancel: ");
                    String SalaryStr = scanner.nextLine().trim();
                    if (SalaryStr.equalsIgnoreCase("exit")) return;
                    if (SalaryStr.matches("^\\$?\\d{1,3}(?:,\\d{3})*(?:\\.\\d{2})?$")) {
                        // Remove $ and , from the string before parsing
                        Salary = Double.parseDouble(SalaryStr.replace("$", "").replace(",", ""));
                        break;
                    }
                    System.out.println("Formatting error: Invalid salary format.");
                }

                // Validate SSN
                String SSN;
                while (true) {
                    System.out.print("Enter SSN (XXX-XX-XXXX or XXXXXXXXX) or 'exit' to cancel: ");
                    SSN = scanner.nextLine().trim();
                    if (SSN.equalsIgnoreCase("exit")) return;
                    if (SSN.matches("^\\d{3}-?\\d{2}-?\\d{4}$")) {
                        // Ensure SSN is in XXX-XX-XXXX format
                        if (!SSN.contains("-")) {
                            SSN = SSN.substring(0,3) + "-" + SSN.substring(3,5) + "-" + SSN.substring(5);
                        }
                        break;
                    }
                    System.out.println("Formatting error: SSN must be 9 digits with optional hyphens.");
                }
                
                // Check for existing employee with same email or SSN
                String checkQuery = "SELECT * FROM employees WHERE email = ? OR SSN = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                    checkStmt.setString(1, email);
                    checkStmt.setString(2, SSN);
                    ResultSet rs = checkStmt.executeQuery();
                    
                    if (rs.next()) {
                        System.out.println("\nEmployee data overlap: an employee already exists");
                        displayEmployeeInfo(rs);
                        System.out.print("Would you like to overwrite? (y/n): ");
                        String overwrite = scanner.nextLine().trim().toLowerCase();
                        if (overwrite.equals("y")) {
                            // Update existing employee
                            String updateQuery = "UPDATE employees SET empid = ?, Fname = ?, Lname = ?, email = ?, HireDate = ?, Salary = ?, SSN = ? WHERE empid = ?";
                            try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                                updateStmt.setInt(1, empid);
                                updateStmt.setString(2, Fname);
                                updateStmt.setString(3, Lname);
                                updateStmt.setString(4, email);
                                updateStmt.setDate(5, HireDate);
                                updateStmt.setDouble(6, Salary);
                                updateStmt.setString(7, SSN);
                                updateStmt.setInt(8, rs.getInt("empid"));
                                
                                int updateRows = updateStmt.executeUpdate();
                                if (updateRows > 0) {
                                    System.out.println("\nEmployee data successfully updated!");
                                    if (promptYesNo(scanner, "Would you like to view the updated employee info?")) {
                                        // Fetch and display updated employee info
                                        String fetchQuery = "SELECT * FROM employees WHERE empid = ?";
                                        try (PreparedStatement fetchStmt = conn.prepareStatement(fetchQuery)) {
                                            fetchStmt.setInt(1, empid);
                                            ResultSet updatedRs = fetchStmt.executeQuery();
                                            if (updatedRs.next()) {
                                                displayEmployeeInfo(updatedRs);
                                            }
                                        }
                                    }
                                    return;
                                }
                            }
                        } else if (overwrite.equals("n")) {
                            System.out.println("Insertion canceled, returning to menu...");
                            return;
                        } else {
                            System.out.println("Invalid input. Insertion canceled, returning to menu...");
                            return;
                        }
                    }
                }

                // Insert new employee if no existing record found or after user declined overwrite
                String insertQuery = "INSERT INTO employees (empid, Fname, Lname, email, HireDate, Salary, SSN) VALUES (?, ?, ?, ?, ?, ?, ?)";            
                try (PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
                    pstmt.setInt(1, empid);
                    pstmt.setString(2, Fname);
                    pstmt.setString(3, Lname);
                    pstmt.setString(4, email);
                    pstmt.setDate(5, HireDate);
                    pstmt.setDouble(6, Salary);
                    pstmt.setString(7, SSN);
                    
                    int rowsAffected = pstmt.executeUpdate();
                    if (rowsAffected > 0) {
                        System.out.println("\nSuccess! Employee added successfully.");
                        if (promptYesNo(scanner, "Would you like to view the employee info?")) {
                            String fetchQuery = "SELECT * FROM employees WHERE empid = ?";
                            try (PreparedStatement fetchStmt = conn.prepareStatement(fetchQuery)) {
                                fetchStmt.setInt(1, empid);
                                ResultSet newRs = fetchStmt.executeQuery();
                                if (newRs.next()) {
                                    displayEmployeeInfo(newRs);
                                }
                            }
                        }
                        return;
                    } else {
                        System.out.println("\nInsertion failed.");
                        if (promptYesNo(scanner, "Would you like to retry?")) {
                            continue;
                        }
                        return;
                    }
                }
            } catch (SQLException e) {
                System.out.println("Error: " + e.getMessage());
                if (promptYesNo(scanner, "Would you like to retry?")) {
                    continue;
                }
                return;
            }
        }
    }
}