import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Handles all menu operations and user interaction
 */
public class Menu {
    private static String role;
    private static int userId;
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
     * Show main menu and handle main operations
     */
    private static void showMainMenu() {
        while (true) {
            if (role.equals("admin")) {
                System.out.println("\nAdmin Menu:");
                System.out.println("1. Search Employees");
                System.out.println("2. Update Employee Data");
                System.out.println("3. Update Salaries");
                System.out.println("4. Reports");
                System.out.println("5. Insert New Employee");
                System.out.println("0. Exit");
            } else {
                System.out.println("\nEmployee Menu:");
                System.out.println("1. View Employee Data");
                System.out.println("2. Update Employee Data");
                System.out.println("3. Pay Statement History");
                System.out.println("0. Exit");
            }
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();
            
            if (role.equals("admin")) {
                switch (choice) {
                    case "1":
                        searchEmployees();
                        break;
                    case "2":
                        System.out.print("Enter employee ID to update: ");
                        try {
                            int empId = Integer.parseInt(scanner.nextLine().trim());
                            updateEmployeeInfo(empId);
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid employee ID format.");
                        }
                        break;
                    case "3":
                        updateSalariesMenu();
                        break;
                    case "4":
                        showReportsMenu(role, userId);
                        break;
                    case "5":
                        insertNewEmployee();
                        break;
                    case "0":
                        System.out.println("Exiting...");
                        return;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } else {
                switch (choice) {
                    case "1":
                        viewEmployeeData(userId);
                        break;
                    case "2":
                        updateEmployeeInfo(userId);
                        break;
                    case "3":
                        showPayStatementHistory(role, userId);
                        break;
                    case "0":
                        System.out.println("Exiting...");
                        return;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }
        }
    }

    /**
     * Show employee menu and handle employee operations
     */
    public static void showEmployeeMenu(int empId) {
        role = "employee";
        userId = empId;
        showMainMenu();
    }

    /**
     * Show admin menu and handle admin operations
     */
    public static void showAdminMenu() {
        role = "admin";
        userId = 0; // admin has no employee ID
        showMainMenu();
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
        
        System.out.println("\nCurrent Employee Information:");
        System.out.println(emp);
        
        System.out.println("\nFields available for update:");
        if (role.equals("admin")) {
            System.out.println("1. First Name");
            System.out.println("2. Last Name");
            System.out.println("3. Email");
            System.out.println("4. Phone");
            System.out.println("5. Address");
            System.out.println("6. Salary");
            System.out.println("7. DOB");
            System.out.println("8. SSN");
        } else {
            System.out.println("1. First Name");
            System.out.println("2. Last Name");
            System.out.println("3. Email");
            System.out.println("4. Address");
        }
        System.out.println("0. Cancel");
        
        System.out.print("Choose a field to update: ");
        String choice = scanner.nextLine().trim();
        
        String field = null;
        String prompt = null;
        boolean isSalary = false;
        
        if (role.equals("admin")) {
            switch (choice) {
                case "1":
                    field = "Fname";
                    prompt = "Enter new first name";
                    break;
                case "2":
                    field = "Lname";
                    prompt = "Enter new last name";
                    break;
                case "3":
                    field = "Email";
                    prompt = "Enter new email";
                    break;
                case "4":
                    field = "Phone";
                    prompt = "Enter new phone number";
                    break;
                case "5":
                    field = "Address";
                    prompt = "Enter new address";
                    break;
                case "6":
                    field = "Salary";
                    prompt = "Enter new salary";
                    isSalary = true;
                    break;
                case "7":
                    field = "DOB";
                    prompt = "Enter new date of birth (YYYY-MM-DD)";
                    break;
                case "8":
                    field = "SSN";
                    prompt = "Enter new SSN (XXX-XX-XXXX)";
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Invalid choice.");
                    return;
            }
        } else {
            switch (choice) {
                case "1":
                    field = "Fname";
                    prompt = "Enter new first name";
                    break;
                case "2":
                    field = "Lname";
                    prompt = "Enter new last name";
                    break;
                case "3":
                    field = "Email";
                    prompt = "Enter new email";
                    break;
                case "4":
                    field = "Address";
                    prompt = "Enter new address";
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Invalid choice.");
                    return;
            }
        }
            
        if (field != null) {
            System.out.print(prompt + ": ");
            String value = scanner.nextLine().trim();
            
            if (value.isEmpty()) {
                System.out.println("Value cannot be empty.");
                return;
            }
            
            if (isSalary) {
                try {
                    double salary = Double.parseDouble(value);
                    if (salary <= 0) {
                        System.out.println("Salary must be greater than 0.");
                        return;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid salary format.");
                    return;
                }
            }
            
            if (EmployeeDAO.updateEmployee(empId, field, value)) {
                System.out.println("Update successful.");
                System.out.println("\nUpdated Employee Information:");
                System.out.println(EmployeeDAO.getEmployee(empId));
            } else {
                System.out.println("Update failed.");
            }
        }
    }

    /**
     * Handle employee search
     */
    public static void searchEmployees() {
        while (true) {
            System.out.println("\nSearch by:");
            System.out.println("1. Name");
            System.out.println("2. Employee ID");
            System.out.println("3. Date of Birth");
            System.out.println("4. SSN");
            System.out.println("0. Back to Menu");
            System.out.print("Choose search criteria: ");
            
            String choice = scanner.nextLine().trim();
            if (choice.equals("0")) return;
            
            List<Employee> employees = null;
            
            switch (choice) {
                case "1":
                    System.out.print("Enter employee name: ");
                    String name = scanner.nextLine().trim();
                    employees = EmployeeDAO.searchByName(name);
                    break;
                    
                case "2":
                    System.out.print("Enter employee ID: ");
                    try {
                        int empId = Integer.parseInt(scanner.nextLine().trim());
                        Employee emp = EmployeeDAO.getEmployee(empId);
                        if (emp != null) {
                            employees = new ArrayList<>();
                            employees.add(emp);
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid employee ID format.");
                        continue;
                    }
                    break;
                    
                case "3":
                    System.out.print("Enter date of birth (YYYY-MM-DD): ");
                    String dob = scanner.nextLine().trim();
                    employees = EmployeeDAO.searchByDOB(dob);
                    break;
                    
                case "4":
                    System.out.print("Enter SSN (XXX-XX-XXXX): ");
                    String ssn = scanner.nextLine().trim();
                    employees = EmployeeDAO.searchBySSN(ssn);
                    break;
                    
                default:
                    System.out.println("Invalid choice.");
                    continue;
            }
            
            if (employees == null || employees.isEmpty()) {
                System.out.println("No employees found.");
                continue;
            }

            System.out.println("\nFound " + employees.size() + " employees:");
            for (Employee emp : employees) {
                System.out.println("\n" + emp);
            }
            
            if (promptYesNo("Would you like to update any of these employees?")) {
                if (employees.size() > 1) {
                    System.out.print("Enter the Employee ID to update: ");
                    try {
                        int empId = Integer.parseInt(scanner.nextLine().trim());
                        boolean found = false;
                        for (Employee emp : employees) {
                            if (emp.getId() == empId) {
                                updateEmployeeInfo(empId);
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            System.out.println("Employee ID not found in search results.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid employee ID format.");
                    }
                } else {
                    updateEmployeeInfo(employees.get(0).getId());
                }
            }
        }
    }

    /**
     * Utility method to get yes/no input from user
     */
    private static void showReportsMenu(String role, int userId) {
        while (true) {
            System.out.println("\nReports Menu:");
            System.out.println("1. Pay Statement History");
            if (role.equals("admin")) {
                System.out.println("2. Total Pay by Job Title");
                System.out.println("3. Total Pay by Division");
            }
            System.out.println("0. Back to Main Menu");
            System.out.print("Choose an option: ");
            
            String choice = scanner.nextLine().trim();
            if (choice.equals("0")) return;
            
            switch (choice) {
                case "1":
                    showPayStatementHistory(role, userId);
                    break;
                    
                case "2":
                    if (role.equals("admin")) {
                        showTotalPayByJobTitle();
                    } else {
                        System.out.println("Access denied. Admin only.");
                    }
                    break;
                    
                case "3":
                    if (role.equals("admin")) {
                        showTotalPayByDivision();
                    } else {
                        System.out.println("Access denied. Admin only.");
                    }
                    break;
                    
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private static void showPayStatementHistory(String role, int userId) {
        List<PayStatement> statements;
        if (role.equals("admin")) {
            statements = EmployeeDAO.getPayStatementHistory(0); // 0 means get all
        } else {
            statements = EmployeeDAO.getPayStatementHistory(userId);
        }
        
        if (statements.isEmpty()) {
            System.out.println("No pay statements found.");
            return;
        }
        
        for (PayStatement stmt : statements) {
            System.out.println("\n" + stmt);
        }
    }

    private static void showTotalPayByJobTitle() {
        int[] yearMonth = promptYearMonth();
        if (yearMonth == null) return;
        
        Map<String, Double> totals = EmployeeDAO.getTotalPayByJobTitle(yearMonth[0], yearMonth[1]);
        if (totals.isEmpty()) {
            System.out.println("No pay data found for the specified month.");
            return;
        }
        
        System.out.printf("\nTotal Pay by Job Title for %d-%02d:\n", yearMonth[0], yearMonth[1]);
        for (Map.Entry<String, Double> entry : totals.entrySet()) {
            System.out.printf("%s: $%.2f\n", entry.getKey(), entry.getValue());
        }
    }

    private static void showTotalPayByDivision() {
        int[] yearMonth = promptYearMonth();
        if (yearMonth == null) return;
        
        Map<String, Double> totals = EmployeeDAO.getTotalPayByDivision(yearMonth[0], yearMonth[1]);
        if (totals.isEmpty()) {
            System.out.println("No pay data found for the specified month.");
            return;
        }
        
        System.out.printf("\nTotal Pay by Division for %d-%02d:\n", yearMonth[0], yearMonth[1]);
        for (Map.Entry<String, Double> entry : totals.entrySet()) {
            System.out.printf("%s: $%.2f\n", entry.getKey(), entry.getValue());
        }
    }

    private static int[] promptYearMonth() {
        try {
            System.out.print("Enter year (YYYY): ");
            int year = Integer.parseInt(scanner.nextLine().trim());
            
            System.out.print("Enter month (1-12): ");
            int month = Integer.parseInt(scanner.nextLine().trim());
            
            if (month < 1 || month > 12) {
                System.out.println("Invalid month. Must be between 1 and 12.");
                return null;
            }
            
            return new int[]{year, month};
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format.");
            return null;
        }
    }

    private static void insertNewEmployee() {
        System.out.println("\nAdd New Employee");
        System.out.println("-----------------");

        // Get first name
        System.out.print("Enter first name: ");
        String fname = scanner.nextLine().trim();
        if (fname.isEmpty()) {
            System.out.println("First name cannot be empty.");
            return;
        }

        // Get last name
        System.out.print("Enter last name: ");
        String lname = scanner.nextLine().trim();
        if (lname.isEmpty()) {
            System.out.println("Last name cannot be empty.");
            return;
        }

        // Get email
        System.out.print("Enter email: ");
        String email = scanner.nextLine().trim();
        if (email.isEmpty() || !email.contains("@")) {
            System.out.println("Invalid email format.");
            return;
        }

        // Get phone
        System.out.print("Enter phone number: ");
        String phone = scanner.nextLine().trim();
        if (phone.isEmpty()) {
            System.out.println("Phone number cannot be empty.");
            return;
        }

        // Get address
        System.out.print("Enter address: ");
        String address = scanner.nextLine().trim();
        if (address.isEmpty()) {
            System.out.println("Address cannot be empty.");
            return;
        }

        // Get salary
        double salary = 0;
        while (true) {
            System.out.print("Enter annual salary: ");
            try {
                salary = Double.parseDouble(scanner.nextLine().trim());
                if (salary <= 0) {
                    System.out.println("Salary must be greater than 0.");
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid salary format. Please enter a number.");
            }
        }

        // Get job title
        Map<Integer, String> jobTitles = EmployeeDAO.getJobTitles();
        if (jobTitles.isEmpty()) {
            System.out.println("Error: No job titles available.");
            return;
        }

        System.out.println("\nAvailable Job Titles:");
        for (Map.Entry<Integer, String> entry : jobTitles.entrySet()) {
            System.out.printf("%d. %s\n", entry.getKey(), entry.getValue());
        }

        int jobTitleId = 0;
        while (true) {
            System.out.print("Enter job title ID: ");
            try {
                jobTitleId = Integer.parseInt(scanner.nextLine().trim());
                if (!jobTitles.containsKey(jobTitleId)) {
                    System.out.println("Invalid job title ID.");
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid number format.");
            }
        }

        // Get division
        Map<Integer, String> divisions = EmployeeDAO.getDivisions();
        if (divisions.isEmpty()) {
            System.out.println("Error: No divisions available.");
            return;
        }

        System.out.println("\nAvailable Divisions:");
        for (Map.Entry<Integer, String> entry : divisions.entrySet()) {
            System.out.printf("%d. %s\n", entry.getKey(), entry.getValue());
        }

        int divisionId = 0;
        while (true) {
            System.out.print("Enter division ID: ");
            try {
                divisionId = Integer.parseInt(scanner.nextLine().trim());
                if (!divisions.containsKey(divisionId)) {
                    System.out.println("Invalid division ID.");
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid number format.");
            }
        }



        // Confirm details
        System.out.println("\nNew Employee Details:");
        System.out.println("-----------------");
        System.out.printf("Name: %s %s\n", fname, lname);
        System.out.printf("Email: %s\n", email);
        System.out.printf("Phone: %s\n", phone);
        System.out.printf("Address: %s\n", address);
        System.out.printf("Salary: $%.2f\n", salary);
        System.out.printf("Job Title: %s\n", jobTitles.get(jobTitleId));
        System.out.printf("Division: %s\n", divisions.get(divisionId));

        if (!promptYesNo("\nConfirm adding this employee?")) {
            System.out.println("Operation cancelled.");
            return;
        }

        // Insert the employee
        int newEmpId = EmployeeDAO.insertEmployee(fname, lname, email, phone, address, 
                                                salary, jobTitleId, divisionId);

        if (newEmpId != -1) {
            System.out.printf("\nEmployee added successfully! Employee ID: %d\n", newEmpId);
        } else {
            System.out.println("\nError adding employee. Please try again.");
        }
    }

    private static boolean promptYesNo(String message) {
        while (true) {
            System.out.print(message + " (y/n): ");
            String response = scanner.nextLine().trim().toLowerCase();
            if (response.equals("y") || response.equals("yes")) return true;
            if (response.equals("n") || response.equals("no")) return false;
            System.out.println("Please enter 'y' or 'n'");
        }
    }

    /**
     * View employee data
     */
    private static void viewEmployeeData(int empId) {
        Employee emp = EmployeeDAO.getEmployee(empId);
        if (emp == null) {
            System.out.println("Employee not found.");
            return;
        }
        System.out.println("\nEmployee Information:");
        System.out.println(emp);
    }

    /**
     * Handle salary updates for a range of employees
     */
    private static void updateSalariesMenu() {
        System.out.println("\nUpdate Salaries:");
        
        double minSalary = 0;
        while (true) {
            System.out.print("Enter minimum salary: ");
            try {
                minSalary = Double.parseDouble(scanner.nextLine().trim());
                if (minSalary < 0) {
                    System.out.println("Minimum salary cannot be negative.");
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid number format.");
            }
        }

        double maxSalary = 0;
        while (true) {
            System.out.print("Enter maximum salary: ");
            try {
                maxSalary = Double.parseDouble(scanner.nextLine().trim());
                if (maxSalary <= minSalary) {
                    System.out.println("Maximum salary must be greater than minimum salary.");
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid number format.");
            }
        }

        double percentageChange = 0;
        while (true) {
            System.out.print("Enter percentage change (positive for increase, negative for decrease): ");
            try {
                percentageChange = Double.parseDouble(scanner.nextLine().trim());
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid number format.");
            }
        }

        if (!promptYesNo(String.format("\nConfirm updating salaries between $%.2f and $%.2f by %.1f%%?", 
                minSalary, maxSalary, percentageChange))) {
            System.out.println("Operation cancelled.");
            return;
        }

        int updatedCount = EmployeeDAO.updateSalariesInRange(minSalary, maxSalary, percentageChange);
        System.out.printf("%d employee salaries updated.\n", updatedCount);
    }
}
