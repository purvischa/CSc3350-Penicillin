import java.time.LocalDate;

/**
 * Represents a pay statement for an employee
 */
public class PayStatement {
    private int employeeId;
    private String employeeName;
    private LocalDate payDate;
    private double grossPay;
    private double netPay;
    private double hoursWorked;
    private String jobTitle;
    private String divisionName;

    public PayStatement(int employeeId, String employeeName, LocalDate payDate, 
                       double grossPay, double netPay, double hoursWorked,
                       String jobTitle, String divisionName) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.payDate = payDate;
        this.grossPay = grossPay;
        this.netPay = netPay;
        this.hoursWorked = hoursWorked;
        this.jobTitle = jobTitle;
        this.divisionName = divisionName;
    }

    // Getters
    public int getEmployeeId() { return employeeId; }
    public String getEmployeeName() { return employeeName; }
    public LocalDate getPayDate() { return payDate; }
    public double getGrossPay() { return grossPay; }
    public double getNetPay() { return netPay; }
    public double getHoursWorked() { return hoursWorked; }
    public String getJobTitle() { return jobTitle; }
    public String getDivisionName() { return divisionName; }

    @Override
    public String toString() {
        return String.format("""
            Employee: %s (ID: %d)
            Pay Date: %s
            Job Title: %s
            Division: %s
            Hours Worked: %.2f
            Gross Pay: $%.2f
            Net Pay: $%.2f
            """,
            employeeName, employeeId, payDate, 
            jobTitle, divisionName, hoursWorked,
            grossPay, netPay
        );
    }
}
