import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/employeedata?allowPublicKeyRetrieval=true&useSSL=false",
            "employeedata",
            "Aga2025tha?!"
        );
    }

    public static void closeConnection(Connection conn) {
        try {
            if (conn != null) conn.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
}
