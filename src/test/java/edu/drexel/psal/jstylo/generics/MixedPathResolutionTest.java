package edu.drexel.psal.jstylo.generics;

import static org.junit.Assert.*;

import com.jgaap.generics.Document;
import java.io.File;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Comprehensive test for mixed absolute and relative path resolution in ProblemSet.
 * This test recreates and formalizes the ad-hoc testing done during development
 * to ensure the path resolution fix works correctly with various path types.
 */
public class MixedPathResolutionTest {

    private static final String MIXED_PATH_PROBLEM_SET =
        "src/test/resources/problem_sets/Anonymouth_docSet.xml";
    private static final String RELATIVE_PATH_PROBLEM_SET =
        "src/test/resources/problem_sets/drexel_1_copy.xml";

    @Before
    public void setUp() {
        // Ensure we're running from the correct directory
        File testResources = new File("src/test/resources");
        if (!testResources.exists()) {
            fail(
                "Tests must be run from project root directory where src/test/resources/ exists"
            );
        }
    }

    @After
    public void tearDown() {
        // Cleanup if needed
    }

    @Test
    public void testMixedAbsoluteAndRelativePathHandling() throws Exception {
        System.out.println("=== TESTING MIXED ABSOLUTE/RELATIVE PATHS ===");

        // Load the problem set that contains mixed path types
        ProblemSet ps = new ProblemSet(MIXED_PATH_PROBLEM_SET);

        int totalTrainDocs = 0;
        int existingTrainDocs = 0;
        int missingTrainDocs = 0;
        int originalAbsolutePaths = 0;
        int originalRelativePaths = 0;

        // Analyze training documents
        for (String author : ps.getAuthors()) {
            List<Document> docs = ps.getTrainDocs(author);
            if (docs != null) {
                for (Document doc : docs) {
                    totalTrainDocs++;
                    String docPath = doc.getFilePath();
                    File docFile = new File(docPath);

                    // All resolved paths should now be absolute
                    assertTrue(
                        "All resolved paths should be absolute: " + docPath,
                        docPath.startsWith("/") ||
                        docPath.matches("^[A-Za-z]:.*")
                    );

                    // Track file existence
                    if (docFile.exists()) {
                        existingTrainDocs++;
                        assertTrue(
                            "Existing files should be readable",
                            docFile.canRead()
                        );
                        assertTrue(
                            "Existing files should have content",
                            docFile.length() > 0
                        );
                    } else {
                        missingTrainDocs++;
                        System.out.println("Missing document: " + docPath);
                    }

                    // Categorize by likely original path type based on content patterns
                    if (
                        docPath.contains("/jsan_resources/") &&
                        !docPath.contains("/./")
                    ) {
                        // Likely was originally absolute
                        originalAbsolutePaths++;
                    } else {
                        // Likely was originally relative
                        originalRelativePaths++;
                    }
                }
            }
        }

        // Analyze test documents
        int totalTestDocs = 0;
        int existingTestDocs = 0;
        for (String author : ps.getTestDocs().keySet()) {
            List<Document> docs = ps.getTestDocs().get(author);
            if (docs != null) {
                for (Document doc : docs) {
                    totalTestDocs++;
                    File docFile = new File(doc.getFilePath());
                    if (docFile.exists()) {
                        existingTestDocs++;
                    }
                }
            }
        }

        // Assertions
        assertTrue(
            "Should have processed some training documents",
            totalTrainDocs > 0
        );
        assertTrue(
            "Should have processed some test documents",
            totalTestDocs > 0
        );
        assertTrue(
            "Most documents should be accessible",
            existingTrainDocs >= totalTrainDocs * 0.8
        );
        assertTrue("Test document should be accessible", existingTestDocs > 0);

        System.out.println(
            "Training documents: " +
            existingTrainDocs +
            "/" +
            totalTrainDocs +
            " accessible"
        );
        System.out.println(
            "Test documents: " +
            existingTestDocs +
            "/" +
            totalTestDocs +
            " accessible"
        );
        System.out.println("Mixed path handling: SUCCESS");
    }

    @Test
    public void testPathTypeAnalysis() throws Exception {
        System.out.println("=== ANALYZING PATH TYPES ===");

        ProblemSet ps = new ProblemSet(MIXED_PATH_PROBLEM_SET);

        int absolutePathCount = 0;
        int relativePathCount = 0;

        // Check all documents for path characteristics
        for (String author : ps.getAuthors()) {
            List<Document> docs = ps.getTrainDocs(author);
            if (docs != null) {
                for (Document doc : docs) {
                    String path = doc.getFilePath();
                    System.out.println(
                        "Document: " + doc.getTitle() + " -> " + path
                    );

                    // All paths should now be absolute after resolution
                    if (path.startsWith("/") || path.matches("^[A-Za-z]:.*")) {
                        absolutePathCount++;
                    } else {
                        relativePathCount++;
                        fail(
                            "Found non-absolute path after resolution: " + path
                        );
                    }
                }
            }
        }

        System.out.println(
            "Absolute paths after resolution: " + absolutePathCount
        );
        System.out.println(
            "Relative paths after resolution: " + relativePathCount
        );

        assertTrue(
            "All paths should be absolute after resolution",
            relativePathCount == 0
        );
        assertTrue("Should have found some documents", absolutePathCount > 0);
    }

    @Test
    public void testDocumentContentAccessibility() throws Exception {
        System.out.println("=== TESTING DOCUMENT CONTENT ACCESS ===");

        ProblemSet ps = new ProblemSet(MIXED_PATH_PROBLEM_SET);

        boolean foundAccessibleDocument = false;

        // Test that we can actually read content from resolved paths
        for (String author : ps.getAuthors()) {
            List<Document> docs = ps.getTrainDocs(author);
            if (docs != null) {
                for (Document doc : docs) {
                    File docFile = new File(doc.getFilePath());
                    if (docFile.exists()) {
                        assertTrue(
                            "Document should be readable",
                            docFile.canRead()
                        );
                        assertTrue(
                            "Document should have content",
                            docFile.length() > 0
                        );

                        // Try to read a few bytes to ensure it's actually accessible
                        try (
                            java.io.FileInputStream fis =
                                new java.io.FileInputStream(docFile);
                        ) {
                            int firstByte = fis.read();
                            assertTrue(
                                "Should be able to read document content",
                                firstByte != -1
                            );
                            foundAccessibleDocument = true;
                        }
                        break; // Just test one document per author
                    }
                }
                if (foundAccessibleDocument) break;
            }
        }

        assertTrue(
            "Should have found at least one accessible document",
            foundAccessibleDocument
        );
    }

    @Test
    public void testRelativePathOnlyProblemSet() throws Exception {
        System.out.println("=== TESTING RELATIVE-PATH-ONLY PROBLEM SET ===");

        // Test with drexel_1_copy.xml which contains only relative paths
        ProblemSet ps = new ProblemSet(RELATIVE_PATH_PROBLEM_SET);

        int totalDocs = 0;
        int accessibleDocs = 0;

        for (String author : ps.getAuthors()) {
            List<Document> docs = ps.getTrainDocs(author);
            if (docs != null) {
                for (Document doc : docs) {
                    totalDocs++;
                    String path = doc.getFilePath();

                    // Should be resolved to absolute path
                    assertTrue(
                        "Path should be absolute: " + path,
                        path.startsWith("/") || path.matches("^[A-Za-z]:.*")
                    );

                    File docFile = new File(path);
                    if (docFile.exists()) {
                        accessibleDocs++;
                    }
                }
            }
        }

        assertTrue("Should have found documents", totalDocs > 0);
        assertTrue(
            "Most documents should be accessible",
            accessibleDocs >= totalDocs * 0.9
        );

        System.out.println(
            "Relative-only dataset: " +
            accessibleDocs +
            "/" +
            totalDocs +
            " accessible"
        );
    }

    @Test
    public void testErrorHandlingWithMissingFiles() throws Exception {
        System.out.println("=== TESTING ERROR HANDLING ===");

        // This tests that the path resolution doesn't crash even with missing files
        ProblemSet ps = new ProblemSet(MIXED_PATH_PROBLEM_SET);

        // The problem set should load successfully even if some files are missing
        assertNotNull("Problem set should load despite missing files", ps);
        assertTrue("Should have some authors", ps.getAuthors().size() > 0);

        // Document paths should still be properly resolved
        for (String author : ps.getAuthors()) {
            List<Document> docs = ps.getTrainDocs(author);
            if (docs != null) {
                for (Document doc : docs) {
                    String path = doc.getFilePath();
                    // Path should be properly formatted even if file doesn't exist
                    assertNotNull("Path should not be null", path);
                    assertFalse(
                        "Path should not be empty",
                        path.trim().isEmpty()
                    );

                    // Should not contain ./ artifacts
                    assertFalse(
                        "Path should not contain ./ artifacts",
                        path.contains("/./")
                    );
                }
            }
        }

        System.out.println("Error handling: PASSED");
    }

    @Test
    public void testPathNormalization() throws Exception {
        System.out.println("=== TESTING PATH NORMALIZATION ===");

        ProblemSet ps = new ProblemSet(MIXED_PATH_PROBLEM_SET);

        for (String author : ps.getAuthors()) {
            List<Document> docs = ps.getTrainDocs(author);
            if (docs != null) {
                for (Document doc : docs) {
                    String path = doc.getFilePath();

                    // Check for proper path normalization
                    assertFalse(
                        "Path should not contain /./ segments",
                        path.contains("/./")
                    );
                    assertFalse(
                        "Path should not contain \\ separators",
                        path.contains("\\")
                    );
                    assertFalse(
                        "Path should not have double slashes",
                        path.contains("//")
                    );

                    // Should use forward slashes consistently
                    if (
                        path.contains(File.separator) &&
                        !File.separator.equals("/")
                    ) {
                        fail("Path should use normalized separators: " + path);
                    }
                }
            }
        }

        System.out.println("Path normalization: PASSED");
    }

    @Test
    public void testBackwardCompatibility() throws Exception {
        System.out.println("=== TESTING BACKWARD COMPATIBILITY ===");

        // Test that both old and new format problem sets work
        ProblemSet mixedPs = new ProblemSet(MIXED_PATH_PROBLEM_SET);
        ProblemSet relativePs = new ProblemSet(RELATIVE_PATH_PROBLEM_SET);

        // Both should load successfully
        assertNotNull("Mixed path problem set should load", mixedPs);
        assertNotNull("Relative path problem set should load", relativePs);

        assertTrue("Mixed path PS should have authors", mixedPs.hasAuthors());
        assertTrue(
            "Relative path PS should have authors",
            relativePs.hasAuthors()
        );

        assertTrue(
            "Mixed path PS should have test docs",
            mixedPs.hasTestDocs()
        );
        assertTrue(
            "Relative path PS should have test docs",
            relativePs.hasTestDocs()
        );

        System.out.println("Backward compatibility: PASSED");
    }
}
