import java.util.*;
import model.Employee;
import model.PayStatement;

/**
 * Console-based menu system for the Employee Management application.
 * Handles login, command dispatch, and input validation.
 */
public class Menu {
    /** Role of the current user: "admin" or "employee". */
    private static String role;
    /** Employee ID of the current user (0 if admin). */
    private static int    userId;
    /** Shared Scanner for reading console input. */
    private static final Scanner scanner = new Scanner(System.in);

    /**
     * Store the authenticated user's role and ID for access control.
     * @param userRole "admin" or "employee"
     * @param id the empId (0 for admin)
     */
    public static void initialize(String userRole, int id) {
        role   = userRole;
        userId = id;
    }

    /**
     * Display a login prompt. Allows up to 3 attempts.
     * @return a String[] of {role, empId} on success, or null on failure
     */
    public static String[] showLoginMenu() {
        for (int i = 0; i < 3; i++) {
            System.out.println("\nLogin as:");
            System.out.println("  Admin:    admin / admin123");
            System.out.println("  Employee: fname_lname / your_empid");
            System.out.print("Username: ");
            String user = scanner.nextLine().trim();
            System.out.print("Password: ");
            String pass = scanner.nextLine().trim();

            // Delegate authentication to DAO
            String auth = EmployeeDAO.authenticateUser(user, pass);
            if (auth != null) {
                String[] parts = auth.split("\\|");
                System.out.println("Welcome, " + parts[0]);
                return parts;
            }

            System.out.println("Invalid credentials. Attempts left: " + (2 - i));
        }
        // Exceeded attempts
        return null;
    }

    /**
     * Main menu loop. Presents different options based on role.
     */
    public static void showMainMenu() {
        while (true) {
            if ("admin".equals(role)) {
                System.out.println("\n1) Search  2) Update  3) Salaries  4) Reports  5) Insert  6) Delete  0) Exit");
            } else {
                System.out.println("\n1) View    2) Update  3) Pay History  0) Exit");
            }
            System.out.print("Choice: ");
            String input = scanner.nextLine().trim();
            int choice;
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid choice");
                continue;
            }
            if (choice == 0) break;

            // Dispatch based on role
            if ("admin".equals(role)) handleAdminChoice(choice);
            else                     handleEmployeeChoice(choice);
        }
    }

    /** Routes admin menu selections to the appropriate handler. */
    private static void handleAdminChoice(int ch) {
        switch (ch) {
            case 1 -> searchEmployees();
            case 2 -> updateEmployeeInfo();
            case 3 -> updateSalariesMenu();
            case 4 -> showReportsMenu();
            case 5 -> insertNewEmployee();
            case 6 -> deleteEmployeeMenu();
            default -> System.out.println("Invalid choice");
        }
    }

    /** Routes employee menu selections to the appropriate handler. */
    private static void handleEmployeeChoice(int ch) {
        switch (ch) {
            case 1 -> viewEmployeeData(userId);
            case 2 -> updateEmployeeInfo(userId);
            case 3 -> payHistory(userId);
            default -> System.out.println("Invalid choice");
        }
    }

    /**
     * Admin: Search employees by name, ID, DOB, or SSN.
     * Displays results and optionally prompts for update.
     */
    private static void searchEmployees() {
        while (true) {
            System.out.println("\nSearch by: 1) Name  2) ID  3) DOB  4) SSN  0) Back");
            System.out.print("Choice: ");
            String c = scanner.nextLine().trim();
            if ("0".equals(c)) return;

            List<Employee> list = new ArrayList<>();
            switch (c) {
                case "1" -> {
                    System.out.print("Enter name: ");
                    list = EmployeeDAO.searchByName(scanner.nextLine().trim());
                }
                case "2" -> {
                    System.out.print("Enter ID: ");
                    try {
                        int id = Integer.parseInt(scanner.nextLine().trim());
                        Employee e = EmployeeDAO.getEmployee(id);
                        if (e != null) list.add(e);
                    } catch (NumberFormatException ignore) { }
                }
                case "3" -> {
                    System.out.print("Enter DOB (YYYY-MM-DD): ");
                    list = EmployeeDAO.searchByDOB(scanner.nextLine().trim());
                }
                case "4" -> {
                    System.out.print("Enter SSN: ");
                    list = EmployeeDAO.searchBySSN(scanner.nextLine().trim());
                }
                default -> {
                    System.out.println("Invalid choice");
                    continue;
                }
            }

            if (list.isEmpty()) {
                System.out.println("No employees found.");
                continue;
            }

            // Display found employees
            System.out.println("\nFound " + list.size() + " employee(s):");
            list.forEach(System.out::println);

            // Prompt to update one of the results
            if (promptYesNo("Update one of these?")) {
                System.out.print("Enter empId: ");
                try {
                    int id = Integer.parseInt(scanner.nextLine().trim());
                    updateEmployeeInfo(id);
                } catch (NumberFormatException ex) {
                    System.out.println("Bad ID");
                }
            }
            return;
        }
    }

    /**
     * Admin: Prompt for an empId to update, then call the overloaded update method.
     */
    private static void updateEmployeeInfo() {
        System.out.print("Enter empId to update: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            updateEmployeeInfo(id);
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID");
        }
    }

    /**
     * Display current data for empId, allow choosing a single field to update.
     * Supports Fname, Lname, email, phone_number, Salary, DOB, SSN.
     */
    private static void updateEmployeeInfo(int empId) {
        Employee emp = EmployeeDAO.getEmployee(empId);
        if (emp == null) {
            System.out.println("Employee not found.");
            return;
        }
        System.out.println("\nCurrent Data:\n" + emp);
        System.out.println("Fields: 1) Fname  2) Lname  3) Email  4) Phone  5) Salary  6) DOB  7) SSN  0) Cancel");
        System.out.print("Choose field: ");
        String c = scanner.nextLine().trim();

        String field, prompt;
        boolean isSalary = false;
        switch (c) {
            case "1" -> { field = "Fname"; prompt = "New first name"; }
            case "2" -> { field = "Lname"; prompt = "New last name"; }
            case "3" -> { field = "email"; prompt = "New email"; }
            case "4" -> { field = "phone_number"; prompt = "New phone"; }
            case "5" -> { field = "Salary"; prompt = "New salary"; isSalary = true; }
            case "6" -> { field = "DOB"; prompt = "New DOB (YYYY-MM-DD)"; }
            case "7" -> { field = "SSN"; prompt = "New SSN"; }
            case "0" -> { return; }
            default  -> { System.out.println("Invalid"); return; }
        }

        System.out.print(prompt + ": ");
        String val = scanner.nextLine().trim();
        if (val.isEmpty()) {
            System.out.println("Cannot be empty.");
            return;
        }
        if (isSalary) {
            try { Double.parseDouble(val); }
            catch (NumberFormatException e) { System.out.println("Bad salary format."); return; }
        }

        boolean ok = EmployeeDAO.updateEmployee(empId, field, val);
        System.out.println(ok ? "Updated successfully." : "Update failed.");
    }

    /**
     * Admin: Prompt for a salary range and percentage, then apply bulk update.
     */
    private static void updateSalariesMenu() {
        System.out.println("\nUpdate salaries in range:");
        double min = promptDouble("Min salary");
        double max = promptDouble("Max salary");
        double pct = promptDouble("Pct change (e.g. 5 for +5%)");

        if (!promptYesNo(String.format("Confirm change %.2f%% for [%.2f–%.2f]?", pct, min, max))) {
            System.out.println("Cancelled.");
            return;
        }
        int updated = EmployeeDAO.updateSalariesInRange(min, max, pct);
        System.out.println(updated + " salary records updated.");
    }

    /**
     * Admin: Show sub-menu for reports: pay history, total pay by job, total pay by division.
     */
    private static void showReportsMenu() {
        while (true) {
            System.out.println("\nReports: 1) PayHistory  2) ByJobTitle  3) ByDivision  0) Back");
            System.out.print("Choice: ");
            String c = scanner.nextLine().trim();
            switch (c) {
                case "0" -> { return; }
                case "1" -> {
                    System.out.print("Enter empId (0=all): ");
                    int id = Integer.parseInt(scanner.nextLine().trim());
                    payHistory(id);
                }
                case "2" -> {
                    int y = (int)promptDouble("Year (YYYY)");
                    int m = (int)promptDouble("Month (1–12)");
                    Map<String,Double> tj = EmployeeDAO.getTotalPayByJobTitle(y, m);
                    System.out.printf("Total pay by JobTitle for %d-%02d:%n", y, m);
                    tj.forEach((job, tot) -> System.out.printf("  %s: $%.2f%n", job, tot));
                }
                case "3" -> {
                    int y = (int)promptDouble("Year (YYYY)");
                    int m = (int)promptDouble("Month (1–12)");
                    Map<String,Double> td = EmployeeDAO.getTotalPayByDivision(y, m);
                    System.out.printf("Total pay by Division for %d-%02d:%n", y, m);
                    td.forEach((div, tot) -> System.out.printf("  %s: $%.2f%n", div, tot));
                }
                default -> System.out.println("Invalid choice");
            }
        }
    }

    /**
     * Display pay-statement history for a given empId (or all with 0).
     * Sorted by pay date descending.
     */
    private static void payHistory(int empId) {
        List<PayStatement> history = EmployeeDAO.getPayStatementHistory(empId);
        if (history.isEmpty()) {
            System.out.println("No pay statements found.");
            return;
        }
        System.out.println("\nPay Statements:");
        history.forEach(ps -> {
            System.out.println(ps);
            System.out.println("-----");
        });
    }

    /** Employee: View own data or admin: view any empId data. */
    private static void viewEmployeeData(int empId) {
        Employee e = EmployeeDAO.getEmployee(empId);
        System.out.println(e != null ? e : "Employee not found.");
    }

    /**
     * Admin: Prompt for all fields to insert a new employee record,
     * including demographics, SSN, address, job title, and division.
     */
    private static void insertNewEmployee() {
        System.out.println("\nAdd New Employee:");
        System.out.print("First name: ");               String fn     = scanner.nextLine().trim();
        System.out.print("Last name:  ");               String ln     = scanner.nextLine().trim();
        System.out.print("Email:      ");               String em     = scanner.nextLine().trim();
        System.out.print("Phone:      ");               String ph     = scanner.nextLine().trim();
        System.out.print("Gender:     ");               String gender= scanner.nextLine().trim();
        System.out.print("Race:       ");               String race  = scanner.nextLine().trim();
        System.out.print("SSN:        ");               String ssn   = scanner.nextLine().trim();
        System.out.print("DOB (YYYY-MM-DD): ");        String dob   = scanner.nextLine().trim();
        System.out.print("Hire Date (YYYY-MM-DD): ");  String hire  = scanner.nextLine().trim();
        double sal = promptDouble("Salary");

        Map<Integer,String> jobs = EmployeeDAO.getJobTitles();
        System.out.println("Job Titles:");
        jobs.forEach((id, t) -> System.out.printf("  %d) %s%n", id, t));
        int jid = (int)promptDouble("Job title ID");

        Map<Integer,String> divs = EmployeeDAO.getDivisions();
        System.out.println("Divisions:");
        divs.forEach((id, d) -> System.out.printf("  %d) %s%n", id, d));
        int did = (int)promptDouble("Division ID");

        System.out.print("Street: "); String street = scanner.nextLine().trim();
        System.out.print("City ID: "); int cityId   = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("State ID: ");int stateId  = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("ZIP: ");     String zip    = scanner.nextLine().trim();

        if (!promptYesNo("Confirm add?")) {
            System.out.println("Cancelled.");
            return;
        }

        // Insert the new employee record via DAO
        int newId = EmployeeDAO.insertEmployee(
            fn, ln, em, ph,
            gender, race, ssn,
            dob, hire,
            sal, jid, did,
            street, cityId, stateId, zip
        );

        System.out.println(newId > 0
            ? "Added! New empId=" + newId
            : "Insert failed."
        );
    }

    /** Admin: Prompt for empId and delete that employee and related data. */
    private static void deleteEmployeeMenu() {
        System.out.print("Enter empId to delete: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            if (promptYesNo("Are you sure you want to delete employee " + id + "?")) {
                boolean ok = EmployeeDAO.deleteEmployee(id);
                System.out.println(ok ? "Deleted." : "Delete failed.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID");
        }
    }

    /**
     * Utility: Ask a yes/no question and return true for 'y' or 'Y'.
     * @param msg the prompt message
     */
    private static boolean promptYesNo(String msg) {
        System.out.print(msg + " (y/n): ");
        String response = scanner.nextLine().trim().toLowerCase();
        return response.startsWith("y");
    }

    /**
     * Utility: Prompt repeatedly until a valid double is entered.
     * @param msg the prompt message
     * @return the parsed double
     */
    private static double promptDouble(String msg) {
        while (true) {
            System.out.print(msg + ": ");
            try {
                return Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number.");
            }
        }
    }
}