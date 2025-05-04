import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import model.City;
import model.PayStatement;
import model.State;
import java.util.*;

public class EmployeeDAOTest {

    @Test
    void testAuthenticateAdmin() {
        assertEquals("admin|0",
                     EmployeeDAO.authenticateUser("admin", "admin123"));
    }

    @Test
    void testAuthenticateInvalid() {
        assertNull(EmployeeDAO.authenticateUser("foo", "bar"));
    }

    @Test
    void testGetEmployee() {
        // assumes empId 1 exists in your test DB
        assertNotNull(EmployeeDAO.getEmployee(1));
    }

    @Test
    void testSearchByName() {
        assertNotNull(EmployeeDAO.searchByName("John"));
    }

    @Test
    void testSearchByDOB() {
        assertNotNull(EmployeeDAO.searchByDOB("1990-01-01"));
    }

    @Test
    void testSearchBySSN() {
        assertNotNull(EmployeeDAO.searchBySSN("123-45-6789"));
    }

    @Test
    void testPayStatementHistoryAll() {
        List<PayStatement> all = EmployeeDAO.getPayStatementHistory(0);
        assertNotNull(all);
    }

    @Test
    void testPayStatementHistorySingle() {
        List<PayStatement> single = EmployeeDAO.getPayStatementHistory(1);
        assertNotNull(single);
    }

    @Test
    void testUpdateSalariesInRange() {
        int updated = EmployeeDAO.updateSalariesInRange(50000, 60000, 1.0);
        assertTrue(updated >= 0);
    }

    @Test
    void testUpdateSingleField() {
        boolean ok = EmployeeDAO.updateEmployee(1, "email", "new.email@example.com");
        assertTrue(ok);
    }

    @Test
    void testGetTotalPayByJobTitle() {
        Map<String, Double> map = EmployeeDAO.getTotalPayByJobTitle(2025, 1);
        assertNotNull(map);
    }

    @Test
    void testGetTotalPayByDivision() {
        Map<String, Double> map = EmployeeDAO.getTotalPayByDivision(2025, 1);
        assertNotNull(map);
    }

    @Test
    void testGetJobTitlesDivisionsCitiesStates() {
        assertNotNull(EmployeeDAO.getJobTitles());
        assertNotNull(EmployeeDAO.getDivisions());
        assertNotNull(EmployeeDAO.getCities());
        assertNotNull(EmployeeDAO.getStates());
    }

    @Test
    void testInsertAndDelete() {
        int id = EmployeeDAO.insertEmployee(
            "Test","User","test@example.com","555-0000",
            "M","TestRace","123-45-6789","2000-01-01","2025-01-01",
            50000,1,1,"123 Test St",1,1,"12345"
        );
        assertTrue(id > 0, "Insert should return a valid new empid");
        boolean deleted = EmployeeDAO.deleteEmployee(id);
        assertTrue(deleted, "Delete should succeed for newly inserted empid");
    }
}