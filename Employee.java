public class Employee {
    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private double salary;
    private String jobTitle;
    private String division;
    private String street;
    private String cityId;
    private String stateId;
    private String zip;

    // Constructor
    public Employee(int id, String firstName, String lastName, String email, 
                   String phoneNumber, double salary, String jobTitle, String division,
                   String street, String cityId, String stateId, String zip) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.salary = salary;
        this.jobTitle = jobTitle;
        this.division = division;
        this.street = street;
        this.cityId = cityId;
        this.stateId = stateId;
        this.zip = zip;
    }

    /**
     * Create an Employee object from a ResultSet row.
     * @param rs The ResultSet positioned at the desired row
     * @return Employee object with fields populated from the ResultSet
     * @throws SQLException if a database access error occurs
     */
    public static Employee fromResultSet(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new Employee(
            rs.getInt("empid"),
            rs.getString("Fname"),
            rs.getString("Lname"),
            rs.getString("email"),
            rs.getString("phone_number"),
            rs.getDouble("Salary"),
            rs.getString("job_title"),
            rs.getString("Division"),
            rs.getString("street"),
            rs.getString("city_id"),
            rs.getString("state_id"),
            rs.getString("zip")
        );
    }

    // Getters
    public int getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public double getSalary() { return salary; }
    public String getJobTitle() { return jobTitle; }
    public String getDivision() { return division; }

    // Setters
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setEmail(String email) { this.email = email; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setSalary(double salary) { this.salary = salary; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
    public void setDivision(String division) { this.division = division; }

    @Override
    public String toString() {
        return String.format("""
            Employee Information:
            ID: %d
            Name: %s %s
            Email: %s
            Phone: %s
            Job Title: %s
            Division: %s
            Salary: $%.2f
            
            Address Information:
            Street: %s
            City ID: %s
            State ID: %s
            ZIP: %s
            """, 
            id, firstName, lastName, email, phoneNumber, jobTitle, division, salary,
            street, cityId, stateId, zip);
    }
}
