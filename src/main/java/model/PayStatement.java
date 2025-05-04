package model;

import java.time.LocalDate;

/**
 * Represents a pay statement (pay stub) for an employee,
 * including gross and net pay, hours worked, job title and division.
 */
public class PayStatement {
    private final int employeeId;
    private final String employeeName;
    private final LocalDate payDate;
    private final double grossPay;
    private final double netPay;
    private final double hoursWorked;
    private final String jobTitle;
    private final String divisionName;

    /**
     * Constructs a PayStatement.
     *
     * @param employeeId    the employee’s ID
     * @param employeeName  the employee’s full name
     * @param payDate       the pay date
     * @param grossPay      the gross earnings
     * @param netPay        the net pay after deductions
     * @param hoursWorked   hours worked in the pay period
     * @param jobTitle      the employee’s job title
     * @param divisionName  the division name
     */
    public PayStatement(int employeeId,
                        String employeeName,
                        LocalDate payDate,
                        double grossPay,
                        double netPay,
                        double hoursWorked,
                        String jobTitle,
                        String divisionName) {
        this.employeeId   = employeeId;
        this.employeeName = employeeName;
        this.payDate      = payDate;
        this.grossPay     = grossPay;
        this.netPay       = netPay;
        this.hoursWorked  = hoursWorked;
        this.jobTitle     = jobTitle;
        this.divisionName = divisionName;
    }

    // ── Getters ────────────────────────────────────────────────────────

    /** @return the employee’s ID */
    public int getEmployeeId() {
        return employeeId;
    }

    /** @return the employee’s full name */
    public String getEmployeeName() {
        return employeeName;
    }

    /** @return the pay date */
    public LocalDate getPayDate() {
        return payDate;
    }

    /** @return the gross pay amount */
    public double getGrossPay() {
        return grossPay;
    }

    /** @return the net pay amount */
    public double getNetPay() {
        return netPay;
    }

    /** @return hours worked in this pay period */
    public double getHoursWorked() {
        return hoursWorked;
    }

    /** @return the job title at time of pay */
    public String getJobTitle() {
        return jobTitle;
    }

    /** @return the division name at time of pay */
    public String getDivisionName() {
        return divisionName;
    }

    // ── toString ───────────────────────────────────────────────────────

    @Override
    public String toString() {
        return String.format(
          "Employee: %s (ID: %d)%n" +
          "Pay Date: %s%n" +
          "Job Title: %s%nDivision: %s%n" +
          "Hours Worked: %.2f%n" +
          "Gross Pay: $%.2f%nNet Pay: $%.2f%n",
          employeeName, employeeId, payDate,
          jobTitle, divisionName,
          hoursWorked, grossPay, netPay
        );
    }
}