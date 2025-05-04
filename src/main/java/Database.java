import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.sql.*;
import java.util.List;

/**
 * Database utility: loads config from .env, optionally dynamically loads
 * the MySQL driver JAR if present, otherwise falls back to the driver on
 * the classpath.
 */
public class Database {
    // Config defaults (overridden by .env)
    private static String DB_HOST = "localhost";
    private static String DB_PORT = "3306";
    private static String DB_NAME = "employeeData";
    private static String DB_USER = "root";
    private static String DB_PASS = "root";
    private static String JDBC_DRIVER_JAR;  // path to the .jar

    // Final JDBC URL
    private static final String JDBC_URL;

    static {
        // 1) Read .env if present
        File envFile = new File(".env");
        if (envFile.exists()) {
            try {
                List<String> lines = Files.readAllLines(envFile.toPath());
                for (String raw : lines) {
                    String line = raw.trim();
                    if (line.isEmpty() || line.startsWith("#") || !line.contains("=")) {
                        continue;
                    }
                    String[] parts = line.split("=", 2);
                    String key = parts[0].trim(), val = parts[1].trim();
                    switch (key) {
                        case "DB_HOST"         -> DB_HOST = val;
                        case "DB_PORT"         -> DB_PORT = val;
                        case "DB_NAME"         -> DB_NAME = val;
                        case "DB_USER"         -> DB_USER = val;
                        case "DB_PASS"         -> DB_PASS = val;
                        case "JDBC_DRIVER_JAR" -> JDBC_DRIVER_JAR = val;
                    }
                }
            } catch (IOException e) {
                throw new ExceptionInInitializerError("Failed reading .env: " + e.getMessage());
            }
        }

        // 2) Fallback for driver JAR path if not set
        if (JDBC_DRIVER_JAR == null || JDBC_DRIVER_JAR.isBlank()) {
            JDBC_DRIVER_JAR = System.getProperty("user.dir")
                            + File.separator + "lib"
                            + File.separator + "mysql-connector-j-9.1.0.jar";
        }

        // 3) Load & register the driver
        File jarFile = new File(JDBC_DRIVER_JAR);
        if (jarFile.exists()) {
            // Dynamically load the external JAR
            try {
                URL jarUrl = jarFile.toURI().toURL();
                URLClassLoader loader = new URLClassLoader(
                    new URL[]{ jarUrl },
                    Thread.currentThread().getContextClassLoader()
                );
                Class<?> dc = Class.forName("com.mysql.cj.jdbc.Driver", true, loader);
                Driver driver = (Driver) dc.getDeclaredConstructor().newInstance();
                DriverManager.registerDriver(new DriverShim(driver));
            } catch (Exception e) {
                throw new ExceptionInInitializerError("Could not load/register JDBC driver: " + e);
            }
        } else {
            // Fallback to driver on the classpath
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                throw new ExceptionInInitializerError(
                    "JDBC driver not on classpath and jar not found at: " + JDBC_DRIVER_JAR
                );
            }
        }

        // 4) Build JDBC URL
        JDBC_URL = String.format(
            "jdbc:mysql://%s:%s/%s?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC",
            DB_HOST, DB_PORT, DB_NAME
        );
    }

    /** Returns a new Connection using the loaded driver & config */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS);
    }

    /** Quietly close */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try { conn.close(); }
            catch (SQLException ignored) {}
        }
    }

    /**
     * A thin shim so DriverManager can talk to our dynamically loaded driver,
     * avoiding classloader issues.
     */
    private static class DriverShim implements Driver {
        private final Driver driver;
        DriverShim(Driver d) { this.driver = d; }
        public boolean acceptsURL(String u)    throws SQLException { return driver.acceptsURL(u); }
        public Connection connect(String u, java.util.Properties p) throws SQLException {
            return driver.connect(u, p);
        }
        public int getMajorVersion()          { return driver.getMajorVersion(); }
        public int getMinorVersion()          { return driver.getMinorVersion(); }
        public DriverPropertyInfo[] getPropertyInfo(String u, java.util.Properties p) throws SQLException {
            return driver.getPropertyInfo(u, p);
        }
        public boolean jdbcCompliant()        { return driver.jdbcCompliant(); }
        public java.util.logging.Logger getParentLogger() {
            try {
                return driver.getParentLogger();
            } catch (Exception e) {
                throw new UnsupportedOperationException(e);
            }
        }
    }
}