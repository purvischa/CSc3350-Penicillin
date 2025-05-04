import java.sql.*;
import java.util.*;
import java.util.LinkedHashMap;
import model.Employee;
import model.PayStatement;
import model.City;
import model.State;

public class EmployeeDAO {
    // Only these fields may be updated via updateEmployee(...)
    private static final Set<String> ALLOWED_FIELDS = Set.of(
        "Fname", "Lname", "email", "phone_number",
        "gender", "race", "SSN", "DOB", "HireDate", "Salary"
    );

    // Base SELECT for Employee queries, now including SSN
    private static final String BASE_QUERY =
        "SELECT e.empid, e.Fname, e.Lname, e.email, e.phone_number, " +
        "e.gender, e.race, e.SSN, e.DOB, e.HireDate, e.Salary, " +
        "a.street, a.city_id, a.state_id, a.zip, " +
        "jt.job_title, d.Name AS division_name " +
        "FROM employees e " +
        "LEFT JOIN address a ON e.empid = a.empid " +
        "LEFT JOIN employee_job_titles ejt ON e.empid = ejt.empid " +
        "LEFT JOIN job_titles jt ON ejt.job_title_id = jt.job_title_id " +
        "LEFT JOIN employee_division ed ON e.empid = ed.empid " +
        "LEFT JOIN division d ON ed.div_ID = d.ID";

    /** Authenticate admin or employee */
    public static String authenticateUser(String username, String password) {
        if ("admin".equals(username) && "admin123".equals(password)) {
            return "admin|0";
        }
        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT empid FROM employees WHERE CONCAT(Fname,'_',Lname)=? AND empid=?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                stmt.setInt(2, Integer.parseInt(password));
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return "employee|" + rs.getInt("empid");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Authentication error: " + e.getMessage());
        }
        return null;
    }

    /** Fetch a single employee (with address & demographics) */
    public static Employee getEmployee(int empId) {
        String sql = BASE_QUERY + " WHERE e.empid = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, empId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Employee.fromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting employee: " + e.getMessage());
        }
        return null;
    }

    /** Search by full or partial name */
    public static List<Employee> searchByName(String name) {
        List<Employee> results = new ArrayList<>();
        String sql = BASE_QUERY + " WHERE CONCAT(e.Fname,' ',e.Lname) LIKE ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + name + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(Employee.fromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching by name: " + e.getMessage());
        }
        return results;
    }

    /** Search by date of birth (YYYY-MM-DD) */
    public static List<Employee> searchByDOB(String dob) {
        List<Employee> results = new ArrayList<>();
        String sql = BASE_QUERY + " WHERE e.DOB = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, dob);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(Employee.fromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching by DOB: " + e.getMessage());
        }
        return results;
    }

    /** Search by SSN */
    public static List<Employee> searchBySSN(String ssn) {
        List<Employee> results = new ArrayList<>();
        String sql = BASE_QUERY + " WHERE e.SSN = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, ssn);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(Employee.fromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching by SSN: " + e.getMessage());
        }
        return results;
    }

    /** Get pay‐statement history (0 = all employees) */
    public static List<PayStatement> getPayStatementHistory(int empId) {
        List<PayStatement> history = new ArrayList<>();
        String sql =
          "SELECT p.empid, CONCAT(e.Fname,' ',e.Lname) AS name, p.pay_date, " +
          "p.earnings, jt.job_title, d.Name AS division_name " +
          "FROM payroll p " +
          "JOIN employees e ON p.empid = e.empid " +
          "LEFT JOIN employee_job_titles ejt ON e.empid = ejt.empid " +
          "LEFT JOIN job_titles jt ON ejt.job_title_id = jt.job_title_id " +
          "LEFT JOIN employee_division ed ON e.empid = ed.empid " +
          "LEFT JOIN division d ON ed.div_ID = d.ID " +
          "WHERE (? = 0 OR p.empid = ?) " +
          "ORDER BY p.empid, p.pay_date DESC";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, empId);
            stmt.setInt(2, empId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    history.add(new PayStatement(
                        rs.getInt("empid"),
                        rs.getString("name"),
                        rs.getDate("pay_date").toLocalDate(),
                        rs.getDouble("earnings"),
                        rs.getDouble("earnings"), // netPay = earnings for now
                        0,                        // hoursWorked (not stored)
                        rs.getString("job_title"),
                        rs.getString("division_name")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching pay statements: " + e.getMessage());
        }
        return history;
    }

    /** Bulk salary update by percentage within a range */
    public static int updateSalariesInRange(double minSalary, double maxSalary, double pct) {
        String sql = "UPDATE employees SET Salary = Salary * (1 + ?/100) WHERE Salary BETWEEN ? AND ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, pct);
            stmt.setDouble(2, minSalary);
            stmt.setDouble(3, maxSalary);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating salaries: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Update a single employee field (including SSN, demographics).
     * Rejects any field not explicitly whitelisted.
     */
    public static boolean updateEmployee(int empId, String fieldName, String value) {
        if (!ALLOWED_FIELDS.contains(fieldName)) {
            System.err.println("Attempt to update invalid field: " + fieldName);
            return false;
        }
        String sql = "UPDATE employees SET " + fieldName + " = ? WHERE empid = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if ("Salary".equalsIgnoreCase(fieldName)) {
                stmt.setDouble(1, Double.parseDouble(value));
            } else {
                stmt.setString(1, value);
            }
            stmt.setInt(2, empId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating employee field: " + e.getMessage());
        }
        return false;
    }

    /** Retrieve all job titles */
    public static Map<Integer,String> getJobTitles() {
        Map<Integer,String> map = new LinkedHashMap<>();
        String sql = "SELECT job_title_id, job_title FROM job_titles ORDER BY job_title";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                map.put(rs.getInt("job_title_id"), rs.getString("job_title"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting job titles: " + e.getMessage());
        }
        return map;
    }

    /** Retrieve all divisions */
    public static Map<Integer,String> getDivisions() {
        Map<Integer,String> map = new LinkedHashMap<>();
        String sql = "SELECT ID, Name FROM division ORDER BY Name";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                map.put(rs.getInt("ID"), rs.getString("Name"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting divisions: " + e.getMessage());
        }
        return map;
    }

    /** Retrieve all cities */
    public static List<City> getCities() {
        List<City> list = new ArrayList<>();
        String sql = "SELECT city_id, name_of_city FROM city ORDER BY name_of_city";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new City(rs.getInt("city_id"), rs.getString("name_of_city")));
            }
        } catch (SQLException e) {
            System.err.println("Error getting cities: " + e.getMessage());
        }
        return list;
    }

    /** Retrieve all states */
    public static List<State> getStates() {
        List<State> list = new ArrayList<>();
        String sql = "SELECT state_id, name_of_state FROM state ORDER BY name_of_state";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new State(rs.getInt("state_id"), rs.getString("name_of_state")));
            }
        } catch (SQLException e) {
            System.err.println("Error getting states: " + e.getMessage());
        }
        return list;
    }

    /**
     * Insert a new employee (with SSN) plus associated job_title,
     * division, and address records. Returns new empid or -1 on failure.
     */
    public static int insertEmployee(
        String fname,
        String lname,
        String email,
        String phone,
        String gender,
        String race,
        String ssn,
        String dob,
        String hireDate,
        double salary,
        int jobTitleId,
        int divisionId,
        String street,
        int cityId,
        int stateId,
        String zip
    ) {
        String sqlEmp =
            "INSERT INTO employees " +
            "(Fname,Lname,email,phone_number,gender,race,SSN,DOB,HireDate,Salary) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlEmp, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, fname);
            stmt.setString(2, lname);
            stmt.setString(3, email);
            stmt.setString(4, phone);
            stmt.setString(5, gender);
            stmt.setString(6, race);
            stmt.setString(7, ssn);
            stmt.setString(8, dob);
            stmt.setString(9, hireDate);
            stmt.setDouble(10, salary);
            int affected = stmt.executeUpdate();
            if (affected == 0) return -1;
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int newId = keys.getInt(1);
                    try (PreparedStatement js = conn.prepareStatement(
                             "INSERT INTO employee_job_titles(empid,job_title_id) VALUES (?,?)")) {
                        js.setInt(1, newId);
                        js.setInt(2, jobTitleId);
                        js.executeUpdate();
                    }
                    try (PreparedStatement ds = conn.prepareStatement(
                             "INSERT INTO employee_division(empid,div_ID) VALUES (?,?)")) {
                        ds.setInt(1, newId);
                        ds.setInt(2, divisionId);
                        ds.executeUpdate();
                    }
                    insertAddress(newId, street, cityId, stateId, zip);
                    return newId;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error inserting new employee: " + e.getMessage());
        }
        return -1;
    }

    /** Insert address row */
    public static boolean insertAddress(int empId, String street, int cityId, int stateId, String zip) {
        String sql = "INSERT INTO address(empid,street,city_id,state_id,zip) VALUES (?,?,?,?,?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, empId);
            stmt.setString(2, street);
            stmt.setInt(3, cityId);
            stmt.setInt(4, stateId);
            stmt.setString(5, zip);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting address: " + e.getMessage());
        }
        return false;
    }

    /** Update address row */
    public static boolean updateAddress(int empId, String street, int cityId, int stateId, String zip) {
        String sql = "UPDATE address SET street=?, city_id=?, state_id=?, zip=? WHERE empid=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, street);
            stmt.setInt(2, cityId);
            stmt.setInt(3, stateId);
            stmt.setString(4, zip);
            stmt.setInt(5, empId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating address: " + e.getMessage());
        }
        return false;
    }

    /** Delete employee and all related records within a single transaction */
    public static boolean deleteEmployee(int empId) {
        String[] deletes = {
            "DELETE FROM address WHERE empid = ?",
            "DELETE FROM payroll WHERE empid = ?",
            "DELETE FROM employee_job_titles WHERE empid = ?",
            "DELETE FROM employee_division WHERE empid = ?",
            "DELETE FROM employees WHERE empid = ?"
        };
        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            for (String sql : deletes) {
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, empId);
                    stmt.executeUpdate();
                }
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Error deleting employee: " + e.getMessage());
        }
        return false;
    }

    /** Total gross pay by job title for a given month */
    public static Map<String,Double> getTotalPayByJobTitle(int year, int month) {
        Map<String,Double> map = new LinkedHashMap<>();
        String sql =
          "SELECT jt.job_title, SUM(p.earnings) AS total_pay " +
          "FROM payroll p " +
          "JOIN employee_job_titles ejt ON p.empid = ejt.empid " +
          "JOIN job_titles jt ON ejt.job_title_id = jt.job_title_id " +
          "WHERE YEAR(p.pay_date)=? AND MONTH(p.pay_date)=? " +
          "GROUP BY jt.job_title " +
          "ORDER BY total_pay DESC";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, year);
            stmt.setInt(2, month);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString("job_title"), rs.getDouble("total_pay"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting total pay by job title: " + e.getMessage());
        }
        return map;
    }

    /** Total gross pay by division for a given month */
    public static Map<String,Double> getTotalPayByDivision(int year, int month) {
        Map<String,Double> map = new LinkedHashMap<>();
        String sql =
          "SELECT d.Name AS division_name, SUM(p.earnings) AS total_pay " +
          "FROM payroll p " +
          "JOIN employee_division ed ON p.empid = ed.empid " +
          "JOIN division d ON ed.div_ID = d.ID " +
          "WHERE YEAR(p.pay_date)=? AND MONTH(p.pay_date)=? " +
          "GROUP BY d.Name " +
          "ORDER BY total_pay DESC";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, year);
            stmt.setInt(2, month);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString("division_name"), rs.getDouble("total_pay"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting total pay by division: " + e.getMessage());
        }
        return map;
    }
}