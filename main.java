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
