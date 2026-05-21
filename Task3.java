import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Task3 {
    class BankStatementBatchProcessor {

    // FIX: Replaced plain int with AtomicInteger.
    //
    //      Root cause of the race condition:
    //      The expression processedCount++ compiles to three separate JVM operations:
    //        1. READ  — load the current value from main memory 
    //        2. ADD   — increment the value by 1
    //        3. WRITE — store the result back to main memory
    //
    //      With 10 threads executing concurrently, two or more threads can READ the
    //      same stale value before any of them has finished the WRITE step. Each
    //      then writes back the same incremented value, so multiple real increments
    //      collapse into a single counted increment. This is a classic 
    //      race condition. The JVM does not guarantee atomicity for non-volatile
    //      int reads/writes, and even volatile would not help here because
    //      volatile only guarantees visibility, not compound read-modify-write
    //      atomicity.
    //
    //      AtomicInteger.incrementAndGet() uses a Compare-And-Swap (CAS) CPU
    //      instruction which performs the read, add, and write as one indivisible
    //      hardware operation, eliminating the race entirely without any locks.
    private final AtomicInteger processedCount = new AtomicInteger(0);

    public void process(List<StatementRecord> records) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(10);

        for (StatementRecord record : records) {
            executor.submit(() -> {
                processRecord(record);
                // FIX: incrementAndGet() is an atomic operation; no two threads can
                //      interleave their read-modify-write steps, so every processed
                //      record is counted exactly once.
                processedCount.incrementAndGet();  
            });
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.MINUTES);
    }

    public int getProcessedCount() {
        // FIX: .get() on AtomicInteger returns the current value with full
        //      memory visibility — no additional synchronisation needed here.
        return processedCount.get();
    }


    // This code added so the file compiles standalone

    private void processRecord(StatementRecord record) {
        // existing logic — not modified per constraint
    }

    class StatementRecord {}
}
}
