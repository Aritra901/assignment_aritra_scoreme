import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

public class Task5 {

    // FIX (all issues): Replaced e.printStackTrace() with a proper SLF4J logger.
    //     e.printStackTrace() writes directly to stderr, bypasses the logging
    //     framework, cannot be silenced or redirected, and was flooding production
    //     logs for every routine validation failure. A named logger gives ops teams
    //     control over verbosity via log-level configuration.
    private static final Logger log = LoggerFactory.getLogger(Task5.class);

    public ValidationResult validate(Document doc) {
        try {
            // FIX (issue 1 — partial): Throwing RuntimeException for expected
            //     validation conditions (null doc, empty content) means the catch
            //     block has no way to distinguish a predictable validation failure
            //     from an unexpected infrastructure error (e.g. an IOException from
            //     extractContent()). The fix uses a dedicated checked exception so
            //     callers and the catch block can tell them apart.
            if (doc == null) {
                throw new ValidationException("Document is null");
            }

            String content = doc.extractContent();

            if (content.isEmpty()) {
                throw new ValidationException("Empty content");
            }

            return runValidationRules(content);

        } catch (ValidationException e) {
            // FIX (issue 1): Expected validation failures are logged at WARN
            //     level — informative but not alarming. No stack trace is attached
            //     because there is nothing exceptional to diagnose; the message
            //     alone is sufficient.
            log.warn("Validation failed: {}", e.getMessage());

            // FIX (issue 2): The original code returned null. Callers that
            //     dereference the result without a null-check throw a
            //     NullPointerException (exactly what issue 3 in validateBatch
            //     demonstrates). Returning a failed ValidationResult object
            //     instead makes the contract explicit and eliminates hidden NPEs.
            return ValidationResult.failed(e.getMessage());

        } catch (Exception e) {
            // Unexpected runtime errors (e.g. extractContent() throwing an
            // unchecked exception) are logged at ERROR level with the full
            // stack trace so engineers can investigate them.
            log.error("Unexpected error during document validation", e);
            return ValidationResult.failed("Unexpected error: " + e.getMessage());
        }
    }

    public void validateBatch(List<Document> docs) {
        for (Document doc : docs) {
            try {
                ValidationResult r = validate(doc);

                // FIX (issue 3): validate() previously returned null on failure,
                //     so r.isValid() threw a NullPointerException for any document
                //     that failed validation. Now that validate() always returns a
                //     non-null ValidationResult, this call is safe. The null-return
                //     root cause is fixed in validate() above (issue 2).
                if (r.isValid()) {
                    saveResult(r);
                }

            } catch (Exception e) {
                // FIX (issue 4): The catch block was completely empty, swallowing
                //     every exception without any record that something went wrong.
                //     Silent swallowing makes production incidents invisible and
                //     untraceable. Logging at ERROR level with the document
                //     reference ensures failures are visible while still allowing
                //     the loop to continue processing remaining documents.
                log.error("Failed to process document: {}", doc, e);
            }
        }
    }


    // This code added so the file compiles standalone — not part of the actual fix.

    static class ValidationException extends Exception {
        ValidationException(String message) { super(message); }
    }

    static class Document {
        public String extractContent() { return ""; }
    }

    static class ValidationResult {
        private final boolean valid;
        private ValidationResult(boolean valid) { this.valid = valid; }
        public boolean isValid() { return valid; }
        public static ValidationResult failed(String reason) { return new ValidationResult(false); }
    }

    private ValidationResult runValidationRules(String content) {
        return new ValidationResult(true); // existing logic — not modified
    }

    private void saveResult(ValidationResult r) {
        // existing logic — not modified
    }
}
