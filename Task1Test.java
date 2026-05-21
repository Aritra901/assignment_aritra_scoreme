import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bonus JUnit 5 tests for Task 1 — LoanAccountService.getOverdueLoans()
 *
 * Each test targets one of the three defects that existed in the original code.
 * The tests are written so they would FAIL against the original buggy method
 * and PASS against the fixed version in Task1.java.
 */
public class Task1Test {

    // -----------------------------------------------------------------------
    // Helper — builds a past date (yesterday) so accounts appear overdue
    // -----------------------------------------------------------------------
    private Date yesterday() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_YEAR, -1);
        return c.getTime();
    }

    // -----------------------------------------------------------------------
    // Helper — builds a future date so accounts appear not yet overdue
    // -----------------------------------------------------------------------
    private Date tomorrow() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_YEAR, 1);
        return c.getTime();
    }

    // -----------------------------------------------------------------------
    // Defect 1: result was initialised to null — NullPointerException on add()
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("Bug 1: result list was null — should not throw NullPointerException")
    void shouldNotThrowNPE_whenValidOverdueAccountIsPresent() {
        Task1 service = new Task1();

        Task1.LoanAccount overdue = new Task1.LoanAccount("A001", yesterday(), 500.0);
        List<Task1.LoanAccount> input = List.of(overdue);

        // Original code: result = null, so result.add(account) throws NPE.
        // Fixed code: result = new ArrayList<>(), so this completes normally.
        assertDoesNotThrow(() -> service.getOverdueLoans(input),
                "getOverdueLoans must not throw NullPointerException when result list is used");
    }

    // -----------------------------------------------------------------------
    // Defect 2: getDueDate() called without null check — NPE for restructured accounts
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("Bug 2: null dueDate — should not throw NullPointerException")
    void shouldNotThrowNPE_whenDueDateIsNull() {
        Task1 service = new Task1();

        // Restructured account has null dueDate (as documented in field comments)
        Task1.LoanAccount restructured = new Task1.LoanAccount("A002", null, 1000.0);
        List<Task1.LoanAccount> input = List.of(restructured);

        // Original code: account.getDueDate().before(...) throws NPE when dueDate is null.
        // Fixed code: null guard prevents the call, account is simply skipped.
        assertDoesNotThrow(() -> service.getOverdueLoans(input),
                "getOverdueLoans must handle null dueDate without throwing");
    }

    @Test
    @DisplayName("Bug 2: account with null dueDate must not appear in overdue results")
    void shouldExcludeAccount_whenDueDateIsNull() {
        Task1 service = new Task1();

        Task1.LoanAccount restructured = new Task1.LoanAccount("A002", null, 1000.0);
        List<Task1.LoanAccount> input = List.of(restructured);

        List<Task1.LoanAccount> result = service.getOverdueLoans(input);

        assertTrue(result.isEmpty(),
                "Accounts with null dueDate must not be included in overdue list");
    }

    // -----------------------------------------------------------------------
    // Defect 3: result list was null — even valid overdue accounts were never returned
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("Bug 3: overdue account with positive balance must be returned")
    void shouldReturnOverdueAccount_whenDueDatePastAndBalancePositive() {
        Task1 service = new Task1();

        Task1.LoanAccount overdue = new Task1.LoanAccount("A003", yesterday(), 250.0);
        List<Task1.LoanAccount> input = List.of(overdue);

        List<Task1.LoanAccount> result = service.getOverdueLoans(input);

        assertEquals(1, result.size(), "Expected exactly 1 overdue account");
        assertEquals("A003", result.get(0).getAccountId());
    }

    // -----------------------------------------------------------------------
    // Additional correctness checks
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("Account not yet due must not appear in results")
    void shouldExcludeAccount_whenDueDateIsInFuture() {
        Task1 service = new Task1();

        Task1.LoanAccount notDue = new Task1.LoanAccount("A004", tomorrow(), 300.0);
        List<Task1.LoanAccount> input = List.of(notDue);

        List<Task1.LoanAccount> result = service.getOverdueLoans(input);

        assertTrue(result.isEmpty(), "Future-due account must not be in overdue list");
    }

    @Test
    @DisplayName("Account with zero balance must not appear in results even if overdue by date")
    void shouldExcludeAccount_whenBalanceIsZero() {
        Task1 service = new Task1();

        Task1.LoanAccount zeroBal = new Task1.LoanAccount("A005", yesterday(), 0.0);
        List<Task1.LoanAccount> input = List.of(zeroBal);

        List<Task1.LoanAccount> result = service.getOverdueLoans(input);

        assertTrue(result.isEmpty(), "Zero-balance account must not appear in overdue list");
    }

    @Test
    @DisplayName("Mixed list — only qualifying accounts are returned")
    void shouldReturnOnlyQualifyingAccounts_fromMixedList() {
        Task1 service = new Task1();

        Task1.LoanAccount overdue1   = new Task1.LoanAccount("A010", yesterday(), 100.0);
        Task1.LoanAccount overdue2   = new Task1.LoanAccount("A011", yesterday(), 200.0);
        Task1.LoanAccount notDue     = new Task1.LoanAccount("A012", tomorrow(), 300.0);
        Task1.LoanAccount nullDate   = new Task1.LoanAccount("A013", null, 400.0);
        Task1.LoanAccount zeroBal    = new Task1.LoanAccount("A014", yesterday(), 0.0);

        List<Task1.LoanAccount> input = new ArrayList<>();
        input.add(overdue1);
        input.add(overdue2);
        input.add(notDue);
        input.add(nullDate);
        input.add(zeroBal);

        List<Task1.LoanAccount> result = service.getOverdueLoans(input);

        assertEquals(2, result.size(), "Only 2 accounts should qualify as overdue");
        assertTrue(result.stream().anyMatch(a -> "A010".equals(a.getAccountId())));
        assertTrue(result.stream().anyMatch(a -> "A011".equals(a.getAccountId())));
    }
}
