public class Employee {
    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private double salary;
    private String jobTitle;
    private String division;

    // Constructor
    public Employee(int id, String firstName, String lastName, String email, 
                   String phoneNumber, double salary, String jobTitle, String division) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.salary = salary;
        this.jobTitle = jobTitle;
        this.division = division;
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
            """, 
            id, firstName, lastName, email, phoneNumber, jobTitle, division, salary);
    }
}
