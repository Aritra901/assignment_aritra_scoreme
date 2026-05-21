public class Task2Analysis {

    /*
     * =========================================================================
     * TASK 2 — ConcurrentModificationException Root Cause Analysis
     * =========================================================================
     *
     * QUESTION 1: What is the exact cause of ConcurrentModificationException?
     * ------------------------------------------------------------------------->
     * Java's ArrayList (and most other java.util collection(Like Map etc.) iterators) maintains
     * an internal modCount field that tracks/notes how many structural modifications
     * (add, remove, clear, etc.) have been made to the list since it was created.
     * When an iterator is obtained, it snapshots that count as 'expectedModCount'.
     * On every call to iterator.next() the iterator calls checkForComodification(),
     * which compares the live modCount against expectedModCount. If they differ,
     * it throws ConcurrentModificationException immediately — this is the
     * "fail-fast" mechanism in the Java Collections Framework.
     *
     * Mainly, this exception is thrown even in a single-threaded context if
     * the list is structurally modified while in a for-each loop/using iterator (which internally uses an iterator).
     *
     *
     * QUESTION 2: What code pattern at line 142 most likely triggered this?
     * ------------------------------------------------------------------------->
     * The stack trace/log shows the exception originates inside filterTransactions()
     * at line 142, called from processStatement(). The most common pattern that
     * produces this is removing elements from an ArrayList while iterating over
     * it with a for-each loop, for example:
     *
     *   for (Transaction tx : transactionList) {         // line ~98 area
     *       if (someCondition(tx)) {
     *           transactionList.remove(tx);              // line 142 — structural modification during iteration.
     *       }
     *   }
     *
     * Each call to transactionList.remove() increments modCount. The iterator's
     * next subsequent call to next() detects the mismatch and throws the exception.
     *
     *
     * QUESTION 3: Minimal code change that resolves this safely
     * -------------------------------------------------------------------------
     * Replace the for-each loop with an explicit Iterator from transactionList and use
     * iterator.remove() — the only safe way to remove from a collection
     * while iterating it with a traditional iterator:
     *
     *   // BEFORE (broken):
     *   for (Transaction tx : transactionList) {
     *       if (someCondition(tx)) {
     *           transactionList.remove(tx);   // throws ConcurrentModificationException
     *       }
     *   }
     *
     *   // AFTER (safe — two-line change):
     *   Iterator<Transaction> it = transactionList.iterator();
     *   while (it.hasNext()) {
     *       Transaction tx = it.next();
     *       if (someCondition(tx)) {
     *           it.remove();   // FIX: removes via the iterator itself; keeps modCount in sync
     *       }
     *   }
     *
     * Alternatively, on Java 8+, a one-liner using the Collection API works:
     *
     *   // FIX: removeIf updates modCount only once after the predicate sweep,
     *   //      so no iterator is ever left in an inconsistent state.
     *   transactionList.removeIf(tx -> someCondition(tx));
     *
     * Either approach is safe. iterator.remove() is preferred when the surrounding
     * logic cannot be easily converted to a lambda predicate/functional interface style.
     * =========================================================================
     */
}
