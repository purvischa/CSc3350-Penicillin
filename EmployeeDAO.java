import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
                SELECT e.empid, e.Fname, e.Lname 
                FROM employees e 
                WHERE CONCAT(e.Fname, '_', e.Lname) = ? 
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
            SELECT e.*
            FROM employees e
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
                        "Not Available",  // job_title
                        "Not Available"   // division_name
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting employee: " + e.getMessage());
        }
        return null;
    }

    /**
     * Update employee's basic information
     */
    public static boolean updateEmployee(Employee emp) {
        String sql = """
            UPDATE employees 
            SET Fname = ?, Lname = ?, email = ?, phone_number = ?
            WHERE empid = ?
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, emp.getFirstName());
            stmt.setString(2, emp.getLastName());
            stmt.setString(3, emp.getEmail());
            stmt.setString(4, emp.getPhoneNumber());
            stmt.setInt(5, emp.getId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating employee: " + e.getMessage());
            return false;
        }
    }

    /**
     * Search for employees by name
     */
    public static List<Employee> searchEmployees(String name) {
        List<Employee> employees = new ArrayList<>();
        String sql = """
            SELECT e.*, j.job_title, d.division_name
            FROM employees e
            LEFT JOIN employee_job_titles ej ON e.empid = ej.empid
            LEFT JOIN job_titles j ON ej.job_title_id = j.job_title_id
            LEFT JOIN divisions d ON e.division_id = d.division_id
            WHERE CONCAT(e.Fname, ' ', e.Lname) LIKE ?
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "%" + name + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    employees.add(new Employee(
                        rs.getInt("empid"),
                        rs.getString("Fname"),
                        rs.getString("Lname"),
                        rs.getString("email"),
                        rs.getString("phone_number"),
                        rs.getDouble("Salary"),
                        rs.getString("job_title"),
                        rs.getString("division_name")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching employees: " + e.getMessage());
        }
        return employees;
    }
}
