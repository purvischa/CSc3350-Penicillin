import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Data Access Object for Employee operations
 * This class handles all database operations related to employees
 */
public class EmployeeDAO {
    /**
     * Authenticate a user
     * @return Role and employee ID if successful, null if authentication fails
     */
    public static String authenticateUser(String username, String password) {
        // Check for admin login
        if (username.equals("admin") && password.equals("admin123")) {
            return "admin|0"; // Using 0 as admin's empId
        }

        // For employees, username should be in format: fname_lname
        // and password should be their empId
        try (Connection conn = Database.getConnection()) {
            String sql = """
                SELECT e.empid, e.fname, e.lname 
                FROM employees e 
                WHERE CONCAT(e.fname, '_', e.lname) = ? 
                AND e.empid = ?
                """;
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                try {
                    stmt.setInt(2, Integer.parseInt(password));
                } catch (NumberFormatException e) {
                    return null; // Password must be a number (empId)
                }
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int empId = rs.getInt("empid");
                        return "employee|" + empId;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Authentication error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get employee by ID
     */
    public static Employee getEmployee(int empId) {
        String sql = """
            SELECT 
                e.empid, e.Fname, e.Lname, e.email, e.Salary,
                a.street, a.city_id, a.state_id, a.zip, a.phone_number,
                d.Name as division_name,
                jt.job_title
            FROM employees e
            LEFT JOIN address a ON e.empid = a.empid
            LEFT JOIN employee_job_titles ejt ON e.empid = ejt.empid
            LEFT JOIN job_titles jt ON ejt.job_title_id = jt.job_title_id
            LEFT JOIN employee_division ed ON e.empid = ed.empid
            LEFT JOIN division d ON ed.div_ID = d.ID
            WHERE e.empid = ?
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, empId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Employee(
                        rs.getInt("empid"),
                        rs.getString("Fname"),
                        rs.getString("Lname"),
                        rs.getString("email"),
                        rs.getString("phone_number"),
                        rs.getDouble("Salary"),
                        rs.getString("job_title") != null ? rs.getString("job_title") : "Not Available",
                        rs.getString("division_name") != null ? rs.getString("division_name") : "Not Available",
                        rs.getString("street") != null ? rs.getString("street") : "Not Available",
                        rs.getString("city_id") != null ? rs.getString("city_id") : "Not Available",
                        rs.getString("state_id") != null ? rs.getString("state_id") : "Not Available",
                        rs.getString("zip") != null ? rs.getString("zip") : "Not Available"
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting employee: " + e.getMessage());
        }
        return null;
    }

    /**
     * Search for employees by name
     */
    public static List<Employee> searchByName(String name) {
        List<Employee> results = new ArrayList<>();
        String sql = BASE_EMPLOYEE_QUERY + " WHERE CONCAT(e.Fname, ' ', e.Lname) LIKE ?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "%" + name + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(createEmployeeFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error searching by name: " + e.getMessage());
        }
        
        return results;
    }

    /**
     * Search for employees by date of birth
     */
    public static List<Employee> searchByDOB(String dob) {
        List<Employee> results = new ArrayList<>();
        String sql = BASE_EMPLOYEE_QUERY + " WHERE e.DOB = ?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, dob);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(createEmployeeFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error searching by DOB: " + e.getMessage());
        }
        
        return results;
    }

    /**
     * Search for employees by SSN
     */
    public static List<Employee> searchBySSN(String ssn) {
        List<Employee> results = new ArrayList<>();
        String sql = BASE_EMPLOYEE_QUERY + " WHERE e.SSN = ?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, ssn);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(createEmployeeFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error searching by SSN: " + e.getMessage());
        }
        
        return results;
    }

    /**
     * Helper method to create an Employee object from a ResultSet
     */
    private static Employee createEmployeeFromResultSet(ResultSet rs) throws SQLException {
        return new Employee(
            rs.getInt("empid"),
            rs.getString("Fname"),
            rs.getString("Lname"),
            rs.getString("email"),
            rs.getString("phone_number"),
            rs.getDouble("Salary"),
            rs.getString("job_title") != null ? rs.getString("job_title") : "Not Available",
            rs.getString("division_name") != null ? rs.getString("division_name") : "Not Available",
            rs.getString("street") != null ? rs.getString("street") : "Not Available",
            rs.getString("city_id") != null ? rs.getString("city_id") : "Not Available",
            rs.getString("state_id") != null ? rs.getString("state_id") : "Not Available",
            rs.getString("zip") != null ? rs.getString("zip") : "Not Available"
        );
    }

    /**
     * Get pay statement history for employees
     * @param empId if not 0, get only this employee's history; if 0, get all employees (admin only)
     * @return List of PayStatement objects sorted by employee ID and pay date
     */
    public static List<PayStatement> getPayStatementHistory(int empId) {
        List<PayStatement> history = new ArrayList<>();
        
        String sql = """
            SELECT 
                p.empID,
                e.Fname,
                e.Lname,
                p.pay_date,
                p.earnings
            FROM payroll p
            JOIN employees e ON p.empID = e.empid
            WHERE (? = 0 OR p.empID = ?)
            ORDER BY p.empID, p.pay_date DESC
            """;
            
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, empId);
            stmt.setInt(2, empId);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                history.add(new PayStatement(
                    rs.getInt("empID"),
                    rs.getString("Fname") + " " + rs.getString("Lname"),
                    rs.getDate("pay_date").toLocalDate(),
                    rs.getDouble("earnings"),
                    rs.getDouble("earnings") * 0.8, // Assuming 20% tax for net pay
                    rs.getDouble("hours_worked"),
                    rs.getString("job_title"),
                    rs.getString("division_name")
                ));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting pay history: " + e.getMessage());
        }
        
        return history;
    }

    public static Map<String, Double> getTotalPayByDivision(int year, int month) {
        Map<String, Double> totals = new HashMap<>();
        
        String sql = """
            SELECT 
                d.Name as division_name,
                SUM(p.earnings) as total_pay
            FROM payroll p
            JOIN employees e ON p.empID = e.empid
            JOIN divisions d ON e.division = d.id
            WHERE YEAR(p.pay_date) = ? AND MONTH(p.pay_date) = ?
            GROUP BY d.Name
            ORDER BY total_pay DESC
            """;
            
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, year);
            stmt.setInt(2, month);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                totals.put(
                    rs.getString("division_name"),
                    rs.getDouble("total_pay")
                );
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting pay by division: " + e.getMessage());
        }
        
        return totals;
    }

    public static Map<String, Double> getTotalPayByJobTitle(int year, int month) {
        Map<String, Double> totals = new HashMap<>();
        
        String sql = """
            SELECT 
                jt.job_title,
                SUM(p.earnings) as total_pay
            FROM payroll p
            JOIN employees e ON p.empID = e.empid
            JOIN job_titles jt ON e.job_title = jt.id
            WHERE YEAR(p.pay_date) = ? AND MONTH(p.pay_date) = ?
            GROUP BY jt.job_title
            ORDER BY total_pay DESC
            """;
            
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, year);
            stmt.setInt(2, month);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                totals.put(
                    rs.getString("job_title"),
                    rs.getDouble("total_pay")
                );
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting pay by job title: " + e.getMessage());
        }
        
        return totals;
    }

    /**
     * Insert a new employee into the database
     * @param fname First name
     * @param lname Last name
     * @param email Email address
     * @param phone Phone number
     * @param address Address
     * @param salary Annual salary
     * @param jobTitle Job title ID
     * @param division Division ID
     * @return The new employee's ID if successful, -1 if failed
     */
    public static int insertEmployee(String fname, String lname, String email, 
                                    String phone, String address, double salary,
                                    int jobTitle, int division) {
        String sql = """
            INSERT INTO employees (Fname, Lname, Email, Phone, Address, Salary, job_title, division)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
            
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, fname);
            stmt.setString(2, lname);
            stmt.setString(3, email);
            stmt.setString(4, phone);
            stmt.setString(5, address);
            stmt.setDouble(6, salary);
            stmt.setInt(7, jobTitle);
            stmt.setInt(8, division);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                return -1;
            }
            
            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("Error inserting new employee: " + e.getMessage());
        }
        
        return -1;
    }

    /**
     * Get all available job titles
     * @return Map of job title IDs to titles
     */
    public static Map<Integer, String> getJobTitles() {
        Map<Integer, String> titles = new HashMap<>();
        String sql = "SELECT id, job_title FROM job_titles ORDER BY job_title";
        
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                titles.put(rs.getInt("id"), rs.getString("job_title"));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting job titles: " + e.getMessage());
        }
        
        return titles;
    }

    /**
     * Get all available divisions
     * @return Map of division IDs to names
     */
    public static Map<Integer, String> getDivisions() {
        Map<Integer, String> divisions = new HashMap<>();
        String sql = "SELECT id, Name FROM divisions ORDER BY Name";
        
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                divisions.put(rs.getInt("id"), rs.getString("Name"));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting divisions: " + e.getMessage());
        }
        
        return divisions;
    }

    public static int updateSalariesInRange(double minSalary, double maxSalary, double percentageChange) {
        String sql = "UPDATE employees SET Salary = Salary * (1 + ?) WHERE Salary BETWEEN ? AND ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDouble(1, percentageChange / 100.0);
            stmt.setDouble(2, minSalary);
            stmt.setDouble(3, maxSalary);
            
            return stmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error updating salaries: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Update a specific field for an employee
     * @param empId Employee ID
     * @param field Field to update
     * @param value New value
     * @return true if update successful, false otherwise
     */
    public static boolean updateEmployee(int empId, String field, String value) {
        String sql = "UPDATE employees SET " + field + " = ? WHERE EmpID = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            if (field.equals("Salary")) {
                stmt.setDouble(1, Double.parseDouble(value));
            } else {
                stmt.setString(1, value);
            }
            stmt.setInt(2, empId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating employee: " + e.getMessage());
            return false;
        }
    }

    /**
     * Update employee's basic information
     */
    public static boolean updateEmployeeField(int empId, String fieldName, String value) {
        String sql = String.format("UPDATE employees SET %s = ? WHERE empid = ?", fieldName);

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, value);
            stmt.setInt(2, empId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating employee field: " + e.getMessage());
            return false;
        }
    }

    public static boolean updateAddressField(int empId, String fieldName, String value) {
        String sql = String.format("UPDATE address SET %s = ? WHERE empid = ?", fieldName);

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, value);
            stmt.setInt(2, empId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating address field: " + e.getMessage());
            return false;
        }
    }

    public static boolean updateDivision(int empId, String divisionName) {
        try (Connection conn = Database.getConnection()) {
            // First, get the division ID
            String getDivSql = "SELECT ID FROM division WHERE Name = ?";
            int divId;
            
            try (PreparedStatement stmt = conn.prepareStatement(getDivSql)) {
                stmt.setString(1, divisionName);
                ResultSet rs = stmt.executeQuery();
                if (!rs.next()) {
                    System.err.println("Division not found: " + divisionName);
                    return false;
                }
                divId = rs.getInt("ID");
            }
            
            // Update or insert into employee_division
            String upsertSql = """
                INSERT INTO employee_division (empid, div_ID) 
                VALUES (?, ?)
                ON DUPLICATE KEY UPDATE div_ID = ?
            """;
            
            try (PreparedStatement stmt = conn.prepareStatement(upsertSql)) {
                stmt.setInt(1, empId);
                stmt.setInt(2, divId);
                stmt.setInt(3, divId);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error updating division: " + e.getMessage());
            return false;
        }
    }

    public static boolean updateJobTitle(int empId, String jobTitle) {
        try (Connection conn = Database.getConnection()) {
            // First, get the job title ID
            String getJobSql = "SELECT job_title_id FROM job_titles WHERE job_title = ?";
            int jobTitleId;
            
            try (PreparedStatement stmt = conn.prepareStatement(getJobSql)) {
                stmt.setString(1, jobTitle);
                ResultSet rs = stmt.executeQuery();
                if (!rs.next()) {
                    System.err.println("Job title not found: " + jobTitle);
                    return false;
                }
                jobTitleId = rs.getInt("job_title_id");
            }
            
            // Update or insert into employee_job_titles
            String upsertSql = """
                INSERT INTO employee_job_titles (empid, job_title_id) 
                VALUES (?, ?)
                ON DUPLICATE KEY UPDATE job_title_id = ?
            """;
            
            try (PreparedStatement stmt = conn.prepareStatement(upsertSql)) {
                stmt.setInt(1, empId);
                stmt.setInt(2, jobTitleId);
                stmt.setInt(3, jobTitleId);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error updating job title: " + e.getMessage());
            return false;
        }
    }

    private static final String BASE_EMPLOYEE_QUERY = """
        SELECT 
            e.empid, e.Fname, e.Lname, e.email, e.Salary, e.SSN, e.DOB,
            a.street, a.city_id, a.state_id, a.zip, a.phone_number,
            d.Name as division_name,
            jt.job_title
        FROM employees e
        LEFT JOIN address a ON e.empid = a.empid
        LEFT JOIN employee_job_titles ej ON e.empid = ej.empid
        LEFT JOIN job_titles jt ON ej.job_title_id = jt.job_title_id
        LEFT JOIN employee_division ed ON e.empid = ed.empid
        LEFT JOIN division d ON ed.div_ID = d.ID
    """;



    /**
     * Search for employees by date of birth
     */
    public static List<Employee> searchByDOB(String dob) {
        String sql = BASE_EMPLOYEE_QUERY + "WHERE e.DOB = ?";
        List<Employee> employees = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, dob);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    employees.add(createEmployeeFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching employees by DOB: " + e.getMessage());
        }
        return employees;
    }

    /**
     * Search for employees by SSN
     */
    public static List<Employee> searchBySSN(String ssn) {
        String sql = BASE_EMPLOYEE_QUERY + "WHERE e.SSN = ?";
        List<Employee> employees = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, ssn);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    employees.add(createEmployeeFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching employees by SSN: " + e.getMessage());
        }
        return employees;
    }

    /**
     * Helper method to create an Employee object from a ResultSet
     */
    private static Employee createEmployeeFromResultSet(ResultSet rs) throws SQLException {
        return new Employee(
            rs.getInt("empid"),
            rs.getString("Fname"),
            rs.getString("Lname"),
            rs.getString("email"),
            rs.getString("phone_number"),
            rs.getDouble("Salary"),
            rs.getString("job_title") != null ? rs.getString("job_title") : "Not Available",
            rs.getString("division_name") != null ? rs.getString("division_name") : "Not Available",
            rs.getString("street") != null ? rs.getString("street") : "Not Available",
            rs.getString("city_id") != null ? rs.getString("city_id") : "Not Available",
            rs.getString("state_id") != null ? rs.getString("state_id") : "Not Available",
            rs.getString("zip") != null ? rs.getString("zip") : "Not Available"
        );
    }

    /**
     * Get pay statement history for employees
     * @param empId if not 0, get only this employee's history; if 0, get all employees (admin only)
     * @return List of PayStatement objects sorted by employee ID and pay date
     */
    public static List<PayStatement> getPayStatementHistory(int empId) {
        List<PayStatement> statements = new ArrayList<>();
        
        String sql = """
            SELECT 
                p.empID, p.pay_date, p.earnings as gross_pay, 
                (p.earnings - p.fed_tax - p.fed_med - p.fed_SS - p.state_tax - p.retire_401k - p.health_care) as net_pay,
                e.Fname, e.Lname,
                jt.job_title,
                d.Name as division_name
            FROM payroll p
            JOIN employees e ON p.empID = e.empid
            JOIN job_titles jt ON e.job_title = jt.id
            JOIN divisions d ON e.division = d.id
            """ + (empId != 0 ? " WHERE p.empID = ?" : "") + 
            " ORDER BY p.empID, p.pay_date";
            
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            if (empId != 0) {
                stmt.setInt(1, empId);
            }
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                statements.add(new PayStatement(
                    rs.getInt("empID"),
                    rs.getString("Fname") + " " + rs.getString("Lname"),
                    rs.getDate("pay_date").toLocalDate(),
                    rs.getDouble("gross_pay"),
                    rs.getDouble("net_pay"),
                    0, // hours_worked not tracked in payroll table
                    rs.getString("job_title"),
                    rs.getString("division_name")
                ));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting pay statements: " + e.getMessage());
        }
        
        return statements;
    }

    /**
     * Get total pay by division for a given month and year
     * @param year Year to get total pay for
     * @param month Month to get total pay for (1-12)
     * @return Map of division names to total pay
     */
    public static Map<String, Double> getTotalPayByDivision(int year, int month) {
        Map<String, Double> totals = new HashMap<>();
        
        String sql = """
            SELECT 
                d.Name as division_name,
                SUM(p.earnings) as total_pay
            FROM payroll p
            JOIN employees e ON p.empID = e.empid
            JOIN divisions d ON e.division = d.id
            WHERE YEAR(p.pay_date) = ? AND MONTH(p.pay_date) = ?
            GROUP BY d.Name
            ORDER BY total_pay DESC
            """;
            
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, year);
            stmt.setInt(2, month);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                totals.put(
                    rs.getString("division_name"),
                    rs.getDouble("total_pay")
                );
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting pay by division: " + e.getMessage());
        }
        
        return totals;
    }

    public static Map<String, Double> getTotalPayByJobTitle(int year, int month) {
        Map<String, Double> totals = new HashMap<>();
        
        String sql = """
            SELECT 
                jt.job_title,
                SUM(p.earnings) as total_pay
            FROM payroll p
            JOIN employees e ON p.empID = e.empid
            JOIN job_titles jt ON e.job_title = jt.id
            WHERE YEAR(p.pay_date) = ? AND MONTH(p.pay_date) = ?
            GROUP BY jt.job_title
            ORDER BY total_pay DESC
            """;
            
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, year);
            stmt.setInt(2, month);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                totals.put(
                    rs.getString("job_title"),
                    rs.getDouble("total_pay")
                );
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting pay by job title: " + e.getMessage());
        }
        
        return totals;
    }

    /**
     * Insert a new employee into the database
     * @param fname First name
     * @param lname Last name
     * @param email Email address
     * @param phone Phone number
     * @param address Address
     * @param salary Annual salary
     * @param jobTitle Job title ID
     * @param division Division ID
     * @return The new employee's ID if successful, -1 if failed
     */
    public static int insertEmployee(String fname, String lname, String email, 
                                    String phone, String address, double salary,
                                    int jobTitle, int division) {
        String sql = """
            INSERT INTO employees (Fname, Lname, Email, Phone, Address, Salary, job_title, division)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
            
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, fname);
            stmt.setString(2, lname);
            stmt.setString(3, email);
            stmt.setString(4, phone);
            stmt.setString(5, address);
            stmt.setDouble(6, salary);
            stmt.setInt(7, jobTitle);
            stmt.setInt(8, division);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                return -1;
            }
            
            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("Error inserting new employee: " + e.getMessage());
        }
        
        return -1;
    }

    /**
     * Get all available job titles
     * @return Map of job title IDs to titles
     */
    public static Map<Integer, String> getJobTitles() {
        Map<Integer, String> titles = new HashMap<>();
        String sql = "SELECT id, job_title FROM job_titles ORDER BY job_title";
        
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                titles.put(rs.getInt("id"), rs.getString("job_title"));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting job titles: " + e.getMessage());
        }
        
        return titles;
    }

    /**
     * Get all available divisions
     * @return Map of division IDs to names
     */
    public static Map<Integer, String> getDivisions() {
        Map<Integer, String> divisions = new HashMap<>();
        String sql = "SELECT id, Name FROM divisions ORDER BY Name";
        
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                divisions.put(rs.getInt("id"), rs.getString("Name"));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting divisions: " + e.getMessage());
        }
        
        return divisions;
    }
}



    /**
     * Get total pay for a given month by job title (admin only)
     * @param year year to get totals for
     * @param month month to get totals for (1-12)
     * @return Map of job title to total pay
     */
    public static Map<String, Double> getTotalPayByJobTitle(int year, int month) {
        Map<String, Double> totals = new HashMap<>();
        
        String sql = """
            SELECT 
                jt.job_title,
                SUM(p.earnings) as total_pay
            FROM payroll p
            JOIN employees e ON p.empID = e.empid
            JOIN job_titles jt ON e.job_title = jt.id
            WHERE YEAR(p.pay_date) = ? AND MONTH(p.pay_date) = ?
            GROUP BY jt.job_title
            ORDER BY total_pay DESC
            """;
            
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, year);
            stmt.setInt(2, month);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                totals.put(
                    rs.getString("job_title"),
                    rs.getDouble("total_pay")
                );
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting pay by job title: " + e.getMessage());
        }
        
    try (Connection conn = Database.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setInt(1, year);
        stmt.setInt(2, month);
        
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            totals.put(
                rs.getString("division_name"),
                rs.getDouble("total_pay")
            );
        }
        
    } catch (SQLException e) {
        System.err.println("Error getting pay by division: " + e.getMessage());
    }
    
    return totals;
}

/**
 * Insert a new employee into the database
 * @param fname First name
 * @param lname Last name
 * @param email Email address
 * @param phone Phone number
 * @param address Address
 * @param salary Annual salary
 * @param jobTitle Job title ID
 * @param division Division ID
 * @return The new employee's ID if successful, -1 if failed
 */
public static int insertEmployee(String fname, String lname, String email, 
                                String phone, String address, double salary,
                                int jobTitle, int division) {
    String sql = """
        INSERT INTO employees (Fname, Lname, Email, Phone, Address, Salary, job_title, division)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
    try (Connection conn = Database.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        
        stmt.setString(1, fname);
        stmt.setString(2, lname);
        stmt.setString(3, email);
        stmt.setString(4, phone);
        stmt.setString(5, address);
        stmt.setDouble(6, salary);
        stmt.setInt(7, jobTitle);
        stmt.setInt(8, division);
        
        int affectedRows = stmt.executeUpdate();
        if (affectedRows == 0) {
            return -1;
        }
        
        ResultSet generatedKeys = stmt.getGeneratedKeys();
        if (generatedKeys.next()) {
            return generatedKeys.getInt(1);
        }
        
    } catch (SQLException e) {
        System.err.println("Error inserting new employee: " + e.getMessage());
    }
    
    return -1;
}

/**
 * Get all available job titles
 * @return List of job titles with their IDs
 */
public static Map<Integer, String> getJobTitles() {
    Map<Integer, String> titles = new HashMap<>();
    String sql = "SELECT id, job_title FROM job_titles ORDER BY job_title";
    
    try (Connection conn = Database.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {
        
        while (rs.next()) {
            titles.put(rs.getInt("id"), rs.getString("job_title"));
        }
        
    } catch (SQLException e) {
        System.err.println("Error getting job titles: " + e.getMessage());
    }
    
    return titles;
}

/**
 * Get all available divisions
 * @return List of divisions with their IDs
 */
public static Map<Integer, String> getDivisions() {
    Map<Integer, String> divisions = new HashMap<>();
    String sql = "SELECT id, Name FROM divisions ORDER BY Name";
    
    try (Connection conn = Database.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {
        
        while (rs.next()) {
            divisions.put(rs.getInt("id"), rs.getString("Name"));
        }
        
    } catch (SQLException e) {
        System.err.println("Error getting divisions: " + e.getMessage());
    }
    
    return divisions;
}



/**
 * Get total pay for a given month by job title (admin only)
 * @param year year to get totals for
 * @param month month to get totals for (1-12)
 * @return Map of job title to total pay
 */
public static Map<String, Double> getTotalPayByJobTitle(int year, int month) {
    Map<String, Double> totals = new HashMap<>();
    
    String sql = """
        SELECT 
            jt.job_title,
            SUM(p.earnings) as total_pay
        FROM payroll p
        JOIN employees e ON p.empID = e.empid
        JOIN job_titles jt ON e.job_title = jt.id
        WHERE YEAR(p.pay_date) = ? AND MONTH(p.pay_date) = ?
        GROUP BY jt.job_title
        ORDER BY total_pay DESC
        """;
        
    try (Connection conn = Database.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setInt(1, year);
        stmt.setInt(2, month);
        
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            totals.put(
                rs.getString("job_title"),
                rs.getDouble("total_pay")
            );
        }
        
    } catch (SQLException e) {
        System.err.println("Error getting pay by job title: " + e.getMessage());
    }
    
    return totals;
}

/**
 * Get total pay for a given month by division (admin only)
 * @param year year to get totals for
 * @param month month to get totals for (1-12)
 * @return Map of division to total pay
 */
public static Map<String, Double> getTotalPayByDivision(int year, int month) {
    Map<String, Double> totals = new HashMap<>();
    
    String sql = """
        SELECT 
            d.Name as division_name,
            SUM(p.earnings) as total_pay
        FROM payroll p
        JOIN employees e ON p.empID = e.empid
        JOIN divisions d ON e.division = d.id
        WHERE YEAR(p.pay_date) = ? AND MONTH(p.pay_date) = ?
        GROUP BY d.Name
        ORDER BY total_pay DESC
        """;
        
    try (Connection conn = Database.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setInt(1, year);
        stmt.setInt(2, month);
        
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            totals.put(
                rs.getString("division_name"),
                rs.getDouble("total_pay")
            );
        }
        
    } catch (SQLException e) {
        System.err.println("Error getting pay by division: " + e.getMessage());
    }
    
    return totals;
}

/**
 * Insert a new employee into the database
 * @param fname First name
 * @param lname Last name
 * @param email Email address
 * @param phone Phone number
 * @param address Address
 * @param salary Annual salary
 * @param jobTitle Job title ID
 * @param division Division ID
 * @return The new employee's ID if successful, -1 if failed
 */
public static int insertEmployee(String fname, String lname, String email, 
                                String phone, String address, double salary,
                                int jobTitle, int division) {
    String sql = """
        INSERT INTO employees (Fname, Lname, Email, Phone, Address, Salary, job_title, division)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
    try (Connection conn = Database.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        
        stmt.setString(1, fname);
        stmt.setString(2, lname);
        stmt.setString(3, email);
        stmt.setString(4, phone);
        stmt.setString(5, address);
        stmt.setDouble(6, salary);
        stmt.setInt(7, jobTitle);
        stmt.setInt(8, division);
        
        int affectedRows = stmt.executeUpdate();
        if (affectedRows == 0) {
            return -1;
        }
        
        ResultSet generatedKeys = stmt.getGeneratedKeys();
        if (generatedKeys.next()) {
            return generatedKeys.getInt(1);
        }
        
    } catch (SQLException e) {
        System.err.println("Error inserting new employee: " + e.getMessage());
    }
    
    return -1;
}

public static int updateSalariesInRange(double minSalary, double maxSalary, double percentageChange) {
    String sql = "UPDATE employees SET Salary = Salary * (1 + ?) WHERE Salary BETWEEN ? AND ?";
    try (Connection conn = Database.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setDouble(1, percentageChange / 100.0);
        stmt.setDouble(2, minSalary);
        stmt.setDouble(3, maxSalary);
        
        return stmt.executeUpdate();
        
    } catch (SQLException e) {
        System.err.println("Error updating salaries: " + e.getMessage());
        return 0;
    }
}

/**
 * Update a specific field for an employee
 * @param empId Employee ID
 * @param field Field to update
 * @param value New value
 * @return true if update successful, false otherwise
 */
public static boolean updateEmployee(int empId, String field, String value) {
    String sql = "UPDATE employees SET " + field + " = ? WHERE EmpID = ?";
    try (Connection conn = Database.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        if (field.equals("Salary")) {
            stmt.setDouble(1, Double.parseDouble(value));
        } else {
            stmt.setString(1, value);
        }
        stmt.setInt(2, empId);
        
        int rowsAffected = stmt.executeUpdate();
        return rowsAffected > 0;
        
    } catch (SQLException e) {
        System.err.println("Error updating employee: " + e.getMessage());
        return false;
    }
}

/**
 * Get all available job titles
 * @return Map of job title IDs to titles
 */
public static Map<Integer, String> getJobTitles() {
    Map<Integer, String> titles = new HashMap<>();
    String sql = "SELECT id, job_title FROM job_titles ORDER BY job_title";
    
    try (Connection conn = Database.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {
        
        while (rs.next()) {
            titles.put(rs.getInt("id"), rs.getString("job_title"));
        }
        
    } catch (SQLException e) {
        System.err.println("Error getting job titles: " + e.getMessage());
    }
    
    return titles;
}

/**
 * Get all available divisions
 * @return Map of division IDs to names
 */
public static Map<Integer, String> getDivisions() {
    Map<Integer, String> divisions = new HashMap<>();
    String sql = "SELECT id, Name FROM divisions ORDER BY Name";
    
    try (Connection conn = Database.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {
        
        while (rs.next()) {
            divisions.put(rs.getInt("id"), rs.getString("Name"));
        }
        
    } catch (SQLException e) {
        System.err.println("Error getting divisions: " + e.getMessage());
    }
    
    return divisions;
}
}
