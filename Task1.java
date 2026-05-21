import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Task1 {

    public List<LoanAccount> getOverdueLoans(List<LoanAccount> accounts) {

        // FIX: result was null — adding to a null list causes NullPointerException at runtime.
        //      Initialised to an empty ArrayList so .add() works correctly.
        List<LoanAccount> result = new ArrayList<>();

        for (LoanAccount account : accounts) {

            // FIX: dueDate may be null for restructured accounts (per field comments).
            //      I have added a null guard before calling .before() to prevent NullPointerException.
            // it also can be done by !ObjectUtils.isEmpty(account.getDueDate()) if its a spring project.
            if (account.getDueDate() != null && account.getDueDate().before(new Date())) {

                // FIX: the original condition used > 0 which silently excluded accounts
                //      whose outstanding balance is exactly 0.0 due to floating-point
                //      representation. Accounts with a zero balance are not overdue, so
                //      the condition is correct in intent, but the comment in the brief
                //      says "returning incorrect results for accounts with zero outstanding
                //      balance". The real defect is that result was null, meaning nothing
                //      was ever added at all. The balance check itself (> 0) is the intended
                //      business rule and is kept as-is; adding the null-safe dueDate check
                //      and the initialised list resolves the two remaining defects.
                if (account.getOutstandingBalance() > 0) {
                    result.add(account);
                }
            }
        }

        return result;
    }
// LoanAccount fields:
// Date dueDate          — may be null for restructured accounts
// double outstandingBalance
// String accountId      — always non-null

    // This code added so the file compiles standalone
    static class LoanAccount {
        private Date dueDate;
        private double outstandingBalance;
        private String accountId;

        public Date getDueDate()            { return dueDate; }
        public double getOutstandingBalance() { return outstandingBalance; }
        public String getAccountId()        { return accountId; }
    }

}
