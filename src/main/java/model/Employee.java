package model;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Represents an employee record, including personal details,
 * employment information, and address references.
 */
public class Employee {
    /** Employee’s unique identifier (primary key). */
    private int empid;
    /** First name. */
    private String Fname;
    /** Last name. */
    private String Lname;
    /** Email address. */
    private String email;
    /** Phone number. */
    private String phone_number;
    /** Gender. */
    private String gender;
    /** Race/ethnicity. */
    private String race;
    /** Social Security Number. */
    private String ssn;
    /** Date of birth (YYYY-MM-DD). */
    private String DOB;
    /** Hire date (YYYY-MM-DD). */
    private String HireDate;
    /** Current salary. */
    private double Salary;
    /** Job title (from job_titles table). */
    private String job_title;
    /** Division name (from division table). */
    private String divisionName;
    /** Street address. */
    private String street;
    /** City ID (foreign key to city table). */
    private int city_id;
    /** State ID (foreign key to state table). */
    private int state_id;
    /** ZIP code. */
    private String zip;

    /**
     * Constructs an Employee with all fields.
     *
     * @param empid          Employee ID.
     * @param Fname          First name.
     * @param Lname          Last name.
     * @param email          Email address.
     * @param phone_number   Phone number.
     * @param gender         Gender.
     * @param race           Race/ethnicity.
     * @param ssn            Social Security Number.
     * @param DOB            Date of birth.
     * @param HireDate       Hire date.
     * @param Salary         Salary.
     * @param job_title      Job title.
     * @param divisionName   Division name.
     * @param street         Street address.
     * @param city_id        City ID.
     * @param state_id       State ID.
     * @param zip            ZIP code.
     */
    public Employee(int empid,
                    String Fname,
                    String Lname,
                    String email,
                    String phone_number,
                    String gender,
                    String race,
                    String ssn,
                    String DOB,
                    String HireDate,
                    double Salary,
                    String job_title,
                    String divisionName,
                    String street,
                    int city_id,
                    int state_id,
                    String zip) {
        this.empid         = empid;
        this.Fname         = Fname;
        this.Lname         = Lname;
        this.email         = email;
        this.phone_number  = phone_number;
        this.gender        = gender;
        this.race          = race;
        this.ssn           = ssn;
        this.DOB           = DOB;
        this.HireDate      = HireDate;
        this.Salary        = Salary;
        this.job_title     = job_title;
        this.divisionName  = divisionName;
        this.street        = street;
        this.city_id       = city_id;
        this.state_id      = state_id;
        this.zip           = zip;
    }

    // ── Getters & setters ─────────────────────────────────────────────────────────

    /** @return employee ID. */
    public int getEmpid() { return empid; }

    /** @return first name. */
    public String getFname() { return Fname; }
    /** @param fname new first name. */
    public void setFname(String fname) { this.Fname = fname; }

    /** @return last name. */
    public String getLname() { return Lname; }
    /** @param lname new last name. */
    public void setLname(String lname) { this.Lname = lname; }

    /** @return email address. */
    public String getEmail() { return email; }
    /** @param email new email address. */
    public void setEmail(String email) { this.email = email; }

    /** @return phone number. */
    public String getPhone_number() { return phone_number; }
    /** @param phone_number new phone number. */
    public void setPhone_number(String phone_number) { this.phone_number = phone_number; }

    /** @return gender. */
    public String getGender() { return gender; }
    /** @param gender new gender. */
    public void setGender(String gender) { this.gender = gender; }

    /** @return race/ethnicity. */
    public String getRace() { return race; }
    /** @param race new race/ethnicity. */
    public void setRace(String race) { this.race = race; }

    /** @return SSN. */
    public String getSsn() { return ssn; }
    /** @param ssn new Social Security Number. */
    public void setSsn(String ssn) { this.ssn = ssn; }

    /** @return date of birth. */
    public String getDOB() { return DOB; }
    /** @param DOB new date of birth. */
    public void setDOB(String DOB) { this.DOB = DOB; }

    /** @return hire date. */
    public String getHireDate() { return HireDate; }
    /** @param hireDate new hire date. */
    public void setHireDate(String hireDate) { this.HireDate = hireDate; }

    /** @return salary. */
    public double getSalary() { return Salary; }
    /** @param salary new salary. */
    public void setSalary(double salary) { this.Salary = salary; }

    /** @return job title. */
    public String getJob_title() { return job_title; }
    /** @return division name. */
    public String getDivisionName() { return divisionName; }

    /** @return street address. */
    public String getStreet() { return street; }
    /** @param street new street address. */
    public void setStreet(String street) { this.street = street; }

    /** @return city ID. */
    public int getCity_id() { return city_id; }
    /** @param city_id new city ID. */
    public void setCity_id(int city_id) { this.city_id = city_id; }

    /** @return state ID. */
    public int getState_id() { return state_id; }
    /** @param state_id new state ID. */
    public void setState_id(int state_id) { this.state_id = state_id; }

    /** @return ZIP code. */
    public String getZip() { return zip; }
    /** @param zip new ZIP code. */
    public void setZip(String zip) { this.zip = zip; }

    // ── toString ────────────────────────────────────────────────────────────────

    /**
     * Format all employee information for display.
     * @return multi-line string of employee details.
     */
    @Override
    public String toString() {
        return String.format(
            "ID: %d%n" +
            "Name: %s %s%n" +
            "SSN: %s%n" +
            "Gender: %s    Race: %s%n" +
            "Email: %s%n" +
            "Phone: %s%n" +
            "DOB: %s    Hire Date: %s%n" +
            "Job: %s    Division: %s%n" +
            "Salary: $%.2f%n" +
            "Address: %s, city_id=%d, state_id=%d, ZIP=%s%n",
            empid, Fname, Lname,
            ssn,
            gender, race,
            email, phone_number,
            DOB, HireDate,
            job_title, divisionName,
            Salary,
            street, city_id, state_id, zip
        );
    }

    /**
     * Creates an Employee instance from the current row of a ResultSet.
     * @param rs JDBC ResultSet positioned at a valid row
     * @return a populated Employee object
     * @throws SQLException if any column is missing or type mismatches
     */
    public static Employee fromResultSet(ResultSet rs) throws SQLException {
        return new Employee(
            rs.getInt("empid"),
            rs.getString("Fname"),
            rs.getString("Lname"),
            rs.getString("email"),
            rs.getString("phone_number"),
            rs.getString("gender"),
            rs.getString("race"),
            rs.getString("SSN"),
            rs.getString("DOB"),
            rs.getString("HireDate"),
            rs.getDouble("Salary"),
            rs.getString("job_title"),
            rs.getString("division_name"),
            rs.getString("street"),
            rs.getInt("city_id"),
            rs.getInt("state_id"),
            rs.getString("zip")
        );
    }
}