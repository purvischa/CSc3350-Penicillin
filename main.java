import java.sql.*;
import java.util.Scanner;
import java.util.List;

public class main {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            // Display login menu
            System.out.println("\n=== Login Menu ===\n");
            System.out.println("For Admin:");
            System.out.println("  Username: -----");
            System.out.println("  Password: --------\n");
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
        // Check if it's admin credentials
        if ("admin".equals(username) && "admin123".equals(password)) {
            return "admin";
        }

        // Regular employee authentication
        String loginQuery = "SELECT login_id FROM employee_logins WHERE username = ? AND password = ?";
        try (PreparedStatement stmt = conn.prepareStatement(loginQuery)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return "employee";
            }
        }
        return null;
    }

    private static void handleAdmin(Connection conn, Scanner scanner) {
        System.out.println("\nWelcome Admin. You have full access.");
        
        boolean running = true;
        while (running) {
            System.out.println("\n=== Admin Menu ===");
            System.out.println("1. Search for an employee");
            System.out.println("2. Update salary range");
            System.out.println("3. Reports");
            System.out.println("4. Exit");
            System.out.print("\nEnter your choice (1-4): ");
            
            String choice = scanner.nextLine();
            
            switch (choice) {
                case "1":
                    handleEmployeeSearch(conn, scanner);
                    break;
                case "2":
                    handleSalaryRangeUpdate(conn, scanner);
                    break;
                case "3":
                    handleReports(conn, scanner);
                    break;
                case "4":
                    running = false;
                    System.out.println("Logging out...");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void handleEmployeeSearch(Connection conn, Scanner scanner) {
        System.out.println("\n=== Search Employee ===");
        System.out.println("1. Search by Name");
        System.out.println("2. Search by DOB");
        System.out.println("3. Search by SSN");
        System.out.println("4. Search by Employee ID");
        System.out.println("5. Back to main menu");
        System.out.print("\nEnter your choice (1-5): ");

        String choice = scanner.nextLine();
        ResultSet results = null;

        try {
            switch (choice) {
                case "1":
                    System.out.print("Enter name (Firstname Lastname): ");
                    String name = scanner.nextLine();
                    results = EmployeeDAO.searchByName(conn, name);
                    break;
                case "2":
                    System.out.print("Enter DOB (YYYY-MM-DD): ");
                    String dob = scanner.nextLine();
                    List<Employee> employees = EmployeeDAO.searchByDOB(dob);
                    if (!employees.isEmpty()) {
                        results = convertEmployeeToResultSet(employees.get(0));
                    }
                    break;
                case "3":
                    System.out.print("Enter SSN: ");
                    String ssn = scanner.nextLine();
                    List<Employee> employeesBySSN = EmployeeDAO.searchBySSN(ssn);
                    if (!employeesBySSN.isEmpty()) {
                        ResultSet employeeRs = convertEmployeeToResultSet(employeesBySSN.get(0));
                        displayEmployeeInfo(employeeRs);
                    } else {
                        System.out.println("No employee found.");
                    }
                    break;
                case "4":
                    System.out.print("Enter Employee ID: ");
                    int empId = Integer.parseInt(scanner.nextLine());
                    results = EmployeeDAO.searchById(conn, empId);
                    break;
                case "5":
                    return;
                default:
                    System.out.println("Invalid choice.");
                    return;
            }

            if (results != null && results.next()) {
                displayEmployeeInfo(results);
                if (promptYesNo(scanner, "Would you like to edit this data?")) {
                    updateEmployeeData(conn, scanner, results.getInt("empid"));
                }
            } else {
                System.out.println("No employee found.");
            }
        } catch (SQLException e) {
            System.out.println("Error searching for employee: " + e.getMessage());
        }
    }

    private static void handleSalaryRangeUpdate(Connection conn, Scanner scanner) {
        System.out.println("\n=== Update Salary Range ===");
        try {
            System.out.print("Enter minimum salary (e.g., 58000): ");
            double minSalary = Double.parseDouble(scanner.nextLine());

            System.out.print("Enter maximum salary (e.g., 105000): ");
            double maxSalary = Double.parseDouble(scanner.nextLine());

            System.out.print("Enter percentage increase (e.g., 3.2): ");
            double percentage = Double.parseDouble(scanner.nextLine());

            int updatedCount = EmployeeDAO.updateSalariesInRange(conn, minSalary, maxSalary, percentage);
            System.out.println("Updated " + updatedCount + " employee salaries.");
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format.");
        } catch (SQLException e) {
            System.out.println("Error updating salaries: " + e.getMessage());
        }
    }

    private static void handleReports(Connection conn, Scanner scanner) {
        while (true) {
            System.out.println("\n=== Reports ===");
            System.out.println("1. Pay statement history");
            System.out.println("2. Total pay by job title");
            System.out.println("3. Total pay by division");
            System.out.println("4. Back to main menu");
            System.out.print("\nEnter your choice (1-4): ");

            String choice = scanner.nextLine();

            try {
                switch (choice) {
                    case "1":
                        ResultSet payHistory = EmployeeDAO.getPayStatementHistory(conn);
                        displayPayStatementHistory(payHistory);
                        break;
                    case "2":
                        ResultSet jobTitlePay = EmployeeDAO.getTotalPayByJobTitle(conn);
                        displayTotalPayByCategory(jobTitlePay, "Job Title");
                        break;
                    case "3":
                        ResultSet divisionPay = EmployeeDAO.getTotalPayByDivision(conn);
                        displayTotalPayByCategory(divisionPay, "Division");
                        break;
                    case "4":
                        return;
                    default:
                        System.out.println("Invalid choice.");
                }
            } catch (SQLException e) {
                System.out.println("Error generating report: " + e.getMessage());
            }
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

    private static ResultSet convertEmployeeToResultSet(Employee employee) {
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT e.*, j.title as job_title, d.name as division_name " +
                 "FROM employees e " +
                 "JOIN job_titles j ON e.job_title_id = j.id " +
                 "JOIN divisions d ON e.division_id = d.id " +
                 "WHERE e.empid = ?")) {
            
            stmt.setInt(1, employee.getId());
            return stmt.executeQuery();
        } catch (SQLException e) {
            System.err.println("Error converting employee to ResultSet: " + e.getMessage());
            return null;
        }
    }

    private static void displayEmployeeInfo(ResultSet rs) throws SQLException {
        System.out.println("\nEmployee Information:");
        System.out.println("------------------------");
        System.out.println("Employee ID: " + rs.getInt("empid"));
        System.out.println("Name: " + rs.getString("fname") + " " + rs.getString("lname"));
        System.out.println("Email: " + rs.getString("email"));
        System.out.println("Phone: " + rs.getString("phone"));
        System.out.println("Address: " + rs.getString("address"));
        System.out.println("DOB: " + rs.getString("DOB"));
        System.out.println("SSN: " + rs.getString("SSN"));
        System.out.println("Salary: $" + String.format("%.2f", rs.getDouble("salary")));
        System.out.println("------------------------");
    }

    private static void updateEmployeeData(Connection conn, Scanner scanner, int empId) throws SQLException {
        System.out.println("\n=== Update Employee Data ===");
        System.out.println("What would you like to update?");
        System.out.println("1. Name");
        System.out.println("2. Email");
        System.out.println("3. Phone");
        System.out.println("4. Address");
        System.out.println("5. Salary");
        System.out.println("6. Cancel");
        System.out.print("Enter your choice (1-6): ");

        String choice = scanner.nextLine();
        try {
            switch (choice) {
                case "1":
                    System.out.print("Enter new first name: ");
                    String fname = scanner.nextLine();
                    System.out.print("Enter new last name: ");
                    String lname = scanner.nextLine();
                    EmployeeDAO.updateName(conn, empId, fname, lname);
                    break;
                case "2":
                    System.out.print("Enter new email: ");
                    String email = scanner.nextLine();
                    EmployeeDAO.updateEmail(conn, empId, email);
                    break;
                case "3":
                    System.out.print("Enter new phone: ");
                    String phone = scanner.nextLine();
                    EmployeeDAO.updatePhone(conn, empId, phone);
                    break;
                case "4":
                    System.out.print("Enter new address: ");
                    String address = scanner.nextLine();
                    EmployeeDAO.updateAddress(conn, empId, address);
                    break;
                case "5":
                    System.out.print("Enter new salary: ");
                    double salary = Double.parseDouble(scanner.nextLine());
                    EmployeeDAO.updateEmployeeField(empId, "Salary", String.valueOf(salary));
                    break;
                case "6":
                    System.out.println("Update cancelled.");
                    return;
                default:
                    System.out.println("Invalid choice.");
                    return;
            }
            System.out.println("Update successful!");
        } catch (SQLException e) {
            System.out.println("Error updating employee data: " + e.getMessage());
        }
    }

    private static void displayPayStatementHistory(ResultSet history) throws SQLException {
        System.out.println("\nPay Statement History");
        System.out.println("------------------------");
        System.out.printf("%-10s %-20s %-15s %-12s %-12s%n", 
                        "Emp ID", "Name", "Pay Date", "Gross Pay", "Net Pay");
        System.out.println("------------------------------------------------");

        while (history.next()) {
            System.out.printf("%-10d %-20s %-15s $%-11.2f $%-11.2f%n",
                history.getInt("empid"),
                history.getString("fname") + " " + history.getString("lname"),
                history.getString("pay_date"),
                history.getDouble("gross_pay"),
                history.getDouble("net_pay"));
        }
        System.out.println("------------------------");
    }

    private static void displayTotalPayByCategory(ResultSet totals, String category) throws SQLException {
        System.out.println("\nTotal Pay by " + category);
        System.out.println("------------------------");
        System.out.printf("%-30s %-15s%n", category, "Total Pay");
        System.out.println("------------------------------------------------");

        while (totals.next()) {
            System.out.printf("%-30s $%-14.2f%n",
                totals.getString("name"),
                totals.getDouble("total_pay"));
        }
        System.out.println("------------------------");
    }
}