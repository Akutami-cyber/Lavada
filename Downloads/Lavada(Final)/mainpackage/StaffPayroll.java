package mainpackage;

import java.util.LinkedHashMap;
import java.util.Map;

final class StaffPayroll {
    private static final Map<String, Double> MONTHLY_SALARIES = new LinkedHashMap<String, Double>();

    static {
        MONTHLY_SALARIES.put("EMP-001", 0.0);
        MONTHLY_SALARIES.put("EMP-002", 0.0);
        MONTHLY_SALARIES.put("EMP-003", 0.0);
        MONTHLY_SALARIES.put("EMP-004", 0.0);
    }

    private StaffPayroll() {
    }

    static double getMonthlySalary(String employeeId) {
        return MONTHLY_SALARIES.getOrDefault(employeeId, 0.0);
    }

    static double getTotalMonthlySalary() {
        double total = 0;
        for (double salary : MONTHLY_SALARIES.values()) {
            total += salary;
        }
        return total;
    }

    static void setMonthlySalary(String employeeId, double salary) {
        if (employeeId == null || employeeId.trim().isEmpty()) {
            throw new IllegalArgumentException("An employee ID is required.");
        }
        if (!Double.isFinite(salary) || salary < 0) {
            throw new IllegalArgumentException("Salary must be a valid non-negative amount.");
        }
        double previous = getMonthlySalary(employeeId);
        double total = getTotalMonthlySalary() - previous + salary;
        if (!Double.isFinite(total)) {
            throw new IllegalArgumentException("The total monthly payroll is too large.");
        }
        MONTHLY_SALARIES.put(employeeId, salary);
    }
}




