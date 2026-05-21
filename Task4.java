import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Task4 {

    private DataSource dataSource;

    public List<ReportEntry> fetchMonthlyReport(String accountId,
                                                 int month, int year)
                                                 throws SQLException {

        // FIX: Wrapped Connection, PreparedStatement, and ResultSet in a
        //      try-with-resources block so they are closed automatically when
        //      the block exits — whether normally or because an exception was
        //      thrown.  The original code returned on the happy path without
        //      ever calling close() on any of the three resources, and had no
        //      finally block, so every method invocation permanently consumed
        //      one connection from the pool. After ~6 hours under moderate load
        //      the pool was completely exhausted and the application hung waiting
        //      for a connection that was never returned.
        //
        //      Closure order inside try-with-resources is the reverse of
        //      declaration order (last declared = first closed), which gives us:
        //        ResultSet  → closed first
        //        PreparedStatement → closed second
        //        Connection → closed last
        //      This matches the required order stated in the task brief.
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM report_entries " +
                     "WHERE account_id = ? AND MONTH(entry_date) = ? " +
                     "AND YEAR(entry_date) = ?");) {

            ps.setString(1, accountId);
            ps.setInt(2, month);
            ps.setInt(3, year);

            // FIX: ResultSet is also declared inside try-with-resources so it
            //      is guaranteed to close before PreparedStatement, which in
            //      turn closes before Connection.
            try (ResultSet rs = ps.executeQuery()) {
                List<ReportEntry> entries = new ArrayList<>();
                while (rs.next()) {
                    entries.add(mapRow(rs));
                }
                return entries; // conn, ps, rs are never closed
            }
        }
        // No finally block required — the compiler-generated synthetic
        // finally clauses from try-with-resources handle all cleanup paths.
    }


    // This code added so the file compiles standalone
    private ReportEntry mapRow(ResultSet rs) throws SQLException {
        return new ReportEntry(); // existing logic — not modified per constraint
    }

    static class ReportEntry {}
}
