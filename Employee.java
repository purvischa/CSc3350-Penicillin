public class Employee {
    private int empid;
    private String Fname;
    private String Lname;
    private String email;
    private String phone_number;
    private double Salary;
    private String job_title;
    private String Name; // division name
    private String street;
    private String city_id;
    private String state_id;
    private String zip;
    private String SSN;
    private String DOB;
    private String HireDate;

    // Constructor
    public Employee(int empid, String Fname, String Lname, String email, 
                   String phone_number, double Salary, String job_title, String Name,
                   String street, String city_id, String state_id, String zip,
                   String SSN, String DOB, String HireDate) {
        this.empid = empid;
        this.Fname = Fname;
        this.Lname = Lname;
        this.email = email;
        this.phone_number = phone_number;
        this.Salary = Salary;
        this.job_title = job_title;
        this.Name = Name;
        this.street = street;
        this.city_id = city_id;
        this.state_id = state_id;
        this.zip = zip;
        this.SSN = SSN;
        this.DOB = DOB;
        this.HireDate = HireDate;
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
            rs.getString("Name"), // division name
            rs.getString("street"),
            rs.getString("city_id"),
            rs.getString("state_id"),
            rs.getString("zip"),
            rs.getString("SSN"),
            rs.getString("DOB"),
            rs.getString("HireDate")
        );
    }

    // Getters
    public int getEmpId() { return empid; }
    public String getFname() { return Fname; }
    public String getLname() { return Lname; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phone_number; }
    public double getSalary() { return Salary; }
    public String getJobTitle() { return job_title; }
    public String getDivisionName() { return Name; }
    public String getSSN() { return SSN; }
    public String getDOB() { return DOB; }
    public String getHireDate() { return HireDate; }

    // Setters
    public void setFname(String Fname) { this.Fname = Fname; }
    public void setLname(String Lname) { this.Lname = Lname; }
    public void setEmail(String email) { this.email = email; }
    public void setPhoneNumber(String phone_number) { this.phone_number = phone_number; }
    public void setSalary(double Salary) { this.Salary = Salary; }
    public void setJobTitle(String job_title) { this.job_title = job_title; }
    public void setDivisionName(String Name) { this.Name = Name; }
    public void setSSN(String SSN) { this.SSN = SSN; }
    public void setDOB(String DOB) { this.DOB = DOB; }
    public void setHireDate(String HireDate) { this.HireDate = HireDate; }

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
            empid, Fname, Lname, email, phone_number, job_title, Name, Salary,
            street, city_id, state_id, zip);
    }
}
