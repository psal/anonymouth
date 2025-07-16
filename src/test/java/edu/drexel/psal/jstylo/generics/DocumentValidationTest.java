package edu.drexel.psal.jstylo.generics;

import static org.junit.Assert.*;

import com.jgaap.generics.Document;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for comprehensive document validation functionality.
 * This recreates and formalizes the ad-hoc document existence and
 * accessibility tests that were performed during development.
 */
public class DocumentValidationTest {

    private static final String DREXEL_PROBLEM_SET =
        "src/test/resources/problem_sets/drexel_1_copy.xml";
    private static final String ANONYMOUTH_PROBLEM_SET =
        "src/test/resources/problem_sets/Anonymouth_docSet.xml";

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
    public void testDocumentExistenceValidation() throws Exception {
        System.out.println("=== TESTING DOCUMENT EXISTENCE VALIDATION ===");

        ProblemSet ps = new ProblemSet(DREXEL_PROBLEM_SET);

        int totalTrainDocs = 0;
        int existingTrainDocs = 0;
        int missingTrainDocs = 0;
        List<String> missingFiles = new ArrayList<>();

        // Validate training documents
        for (String author : ps.getAuthors()) {
            List<Document> docs = ps.getTrainDocs(author);
            System.out.println(
                "Author: " +
                author +
                " has " +
                ps.numTrainDocs(author) +
                " training documents"
            );

            if (docs != null) {
                for (Document doc : docs) {
                    totalTrainDocs++;
                    String docPath = doc.getFilePath();
                    File docFile = new File(docPath);

                    if (!docFile.exists()) {
                        System.err.println(
                            "  MISSING: " + doc.getTitle() + " -> " + docPath
                        );
                        missingTrainDocs++;
                        missingFiles.add(docPath);
                    } else {
                        System.out.println(
                            "  EXISTS: " +
                            doc.getTitle() +
                            " (" +
                            docFile.length() +
                            " bytes)"
                        );
                        existingTrainDocs++;
                    }
                }
            }
        }

        // Validate test documents
        int totalTestDocs = 0;
        int existingTestDocs = 0;

        if (ps.hasTestDocs()) {
            System.out.println("\nTest documents:");
            for (String author : ps.getTestDocs().keySet()) {
                List<Document> docs = ps.getTestDocs().get(author);
                if (docs != null) {
                    for (Document doc : docs) {
                        totalTestDocs++;
                        String docPath = doc.getFilePath();
                        File docFile = new File(docPath);

                        if (!docFile.exists()) {
                            System.err.println(
                                "  MISSING TEST: " +
                                doc.getTitle() +
                                " -> " +
                                docPath
                            );
                            missingFiles.add(docPath);
                        } else {
                            System.out.println(
                                "  EXISTS TEST: " +
                                doc.getTitle() +
                                " (" +
                                docFile.length() +
                                " bytes)"
                            );
                            existingTestDocs++;
                        }
                    }
                }
            }
        }

        // Print summary
        System.out.println("\n=== VALIDATION SUMMARY ===");
        System.out.println("Total training documents: " + totalTrainDocs);
        System.out.println("Existing training documents: " + existingTrainDocs);
        System.out.println("Missing training documents: " + missingTrainDocs);
        System.out.println("Total test documents: " + totalTestDocs);
        System.out.println("Existing test documents: " + existingTestDocs);

        // Assertions
        assertTrue("Should have found training documents", totalTrainDocs > 0);
        assertTrue("Should have found test documents", totalTestDocs > 0);

        // For Drexel dataset, we expect all documents to exist
        assertEquals(
            "All training documents should exist for Drexel dataset",
            0,
            missingTrainDocs
        );
        assertEquals(
            "All test documents should exist for Drexel dataset",
            totalTestDocs,
            existingTestDocs
        );

        if (!missingFiles.isEmpty()) {
            fail("Missing files found: " + missingFiles);
        }

        System.out.println("Document existence validation: PASSED");
    }

    @Test
    public void testDocumentAccessibilityValidation() throws Exception {
        System.out.println("=== TESTING DOCUMENT ACCESSIBILITY ===");

        ProblemSet ps = new ProblemSet(ANONYMOUTH_PROBLEM_SET);

        int totalChecked = 0;
        int accessibleCount = 0;
        int readableCount = 0;
        int nonEmptyCount = 0;

        for (String author : ps.getAuthors()) {
            List<Document> docs = ps.getTrainDocs(author);
            if (docs != null) {
                for (Document doc : docs) {
                    totalChecked++;
                    String docPath = doc.getFilePath();
                    File docFile = new File(docPath);

                    if (docFile.exists()) {
                        accessibleCount++;
                        System.out.println("Checking: " + doc.getTitle());

                        // Test readability
                        if (docFile.canRead()) {
                            readableCount++;
                            System.out.println("  ✓ Readable");

                            // Test content
                            if (docFile.length() > 0) {
                                nonEmptyCount++;
                                System.out.println(
                                    "  ✓ Has content (" +
                                    docFile.length() +
                                    " bytes)"
                                );

                                // Try to read first few bytes
                                try (
                                    FileInputStream fis = new FileInputStream(
                                        docFile
                                    );
                                ) {
                                    byte[] buffer = new byte[100];
                                    int bytesRead = fis.read(buffer);
                                    if (bytesRead > 0) {
                                        String preview = new String(
                                            buffer,
                                            0,
                                            Math.min(bytesRead, 50)
                                        );
                                        System.out.println(
                                            "  ✓ Content preview: " +
                                            preview.replaceAll("\\s+", " ")
                                        );
                                    }
                                } catch (IOException e) {
                                    System.err.println(
                                        "  ✗ Failed to read content: " +
                                        e.getMessage()
                                    );
                                }
                            } else {
                                System.err.println("  ✗ File is empty");
                            }
                        } else {
                            System.err.println("  ✗ Not readable");
                        }
                    } else {
                        System.err.println(
                            "Missing: " + doc.getTitle() + " -> " + docPath
                        );
                    }
                }
            }
        }

        // Print accessibility summary
        System.out.println("\n=== ACCESSIBILITY SUMMARY ===");
        System.out.println("Total documents checked: " + totalChecked);
        System.out.println("Accessible documents: " + accessibleCount);
        System.out.println("Readable documents: " + readableCount);
        System.out.println("Non-empty documents: " + nonEmptyCount);

        // Assertions
        assertTrue("Should have checked some documents", totalChecked > 0);
        assertTrue(
            "Most documents should be accessible",
            accessibleCount >= totalChecked * 0.8
        );
        assertEquals(
            "All accessible documents should be readable",
            accessibleCount,
            readableCount
        );
        assertEquals(
            "All readable documents should be non-empty",
            readableCount,
            nonEmptyCount
        );

        System.out.println("Document accessibility validation: PASSED");
    }

    @Test
    public void testDocumentSizeValidation() throws Exception {
        System.out.println("=== TESTING DOCUMENT SIZE VALIDATION ===");

        ProblemSet ps = new ProblemSet(DREXEL_PROBLEM_SET);

        long totalSize = 0;
        int documentCount = 0;
        long minSize = Long.MAX_VALUE;
        long maxSize = 0;
        String minSizeDoc = "";
        String maxSizeDoc = "";

        for (String author : ps.getAuthors()) {
            List<Document> docs = ps.getTrainDocs(author);
            if (docs != null) {
                for (Document doc : docs) {
                    File docFile = new File(doc.getFilePath());
                    if (docFile.exists()) {
                        long size = docFile.length();
                        totalSize += size;
                        documentCount++;

                        if (size < minSize) {
                            minSize = size;
                            minSizeDoc = doc.getTitle();
                        }
                        if (size > maxSize) {
                            maxSize = size;
                            maxSizeDoc = doc.getTitle();
                        }

                        System.out.println(
                            "Document: " +
                            doc.getTitle() +
                            " = " +
                            size +
                            " bytes"
                        );
                    }
                }
            }
        }

        double averageSize = documentCount > 0
            ? (double) totalSize / documentCount
            : 0;

        System.out.println("\n=== SIZE STATISTICS ===");
        System.out.println("Total documents: " + documentCount);
        System.out.println("Total size: " + totalSize + " bytes");
        System.out.println(
            "Average size: " + String.format("%.2f", averageSize) + " bytes"
        );
        System.out.println(
            "Minimum size: " + minSize + " bytes (" + minSizeDoc + ")"
        );
        System.out.println(
            "Maximum size: " + maxSize + " bytes (" + maxSizeDoc + ")"
        );

        // Assertions
        assertTrue("Should have found documents", documentCount > 0);
        assertTrue("Total size should be positive", totalSize > 0);
        assertTrue("Average size should be reasonable", averageSize > 100); // At least 100 bytes per doc
        assertTrue("Minimum size should be positive", minSize > 0);
        assertTrue(
            "Maximum size should be larger than minimum",
            maxSize > minSize
        );

        System.out.println("Document size validation: PASSED");
    }

    @Test
    public void testDocumentContentValidation() throws Exception {
        System.out.println("=== TESTING DOCUMENT CONTENT VALIDATION ===");

        ProblemSet ps = new ProblemSet(ANONYMOUTH_PROBLEM_SET);

        int validContentCount = 0;
        int totalSampled = 0;
        int maxSampleSize = 5; // Limit sampling for performance

        for (String author : ps.getAuthors()) {
            List<Document> docs = ps.getTrainDocs(author);
            if (docs != null) {
                int sampledFromAuthor = 0;
                for (Document doc : docs) {
                    if (sampledFromAuthor >= maxSampleSize) break;

                    File docFile = new File(doc.getFilePath());
                    if (docFile.exists() && docFile.canRead()) {
                        totalSampled++;
                        sampledFromAuthor++;

                        try (
                            FileInputStream fis = new FileInputStream(docFile);
                        ) {
                            // Read first 1KB to check content validity
                            byte[] buffer = new byte[1024];
                            int bytesRead = fis.read(buffer);

                            if (bytesRead > 0) {
                                String content = new String(
                                    buffer,
                                    0,
                                    bytesRead
                                );

                                // Basic content validation
                                boolean hasText = content.trim().length() > 0;
                                boolean hasWords =
                                    content.split("\\s+").length > 1;
                                boolean isNotBinary = content
                                    .chars()
                                    .allMatch(c -> c < 128); // ASCII check

                                if (hasText && hasWords && isNotBinary) {
                                    validContentCount++;
                                    System.out.println(
                                        "✓ Valid content: " + doc.getTitle()
                                    );
                                } else {
                                    System.err.println(
                                        "✗ Invalid content: " +
                                        doc.getTitle() +
                                        " (hasText=" +
                                        hasText +
                                        ", hasWords=" +
                                        hasWords +
                                        ", isNotBinary=" +
                                        isNotBinary +
                                        ")"
                                    );
                                }
                            }
                        } catch (IOException e) {
                            System.err.println(
                                "✗ Failed to read: " +
                                doc.getTitle() +
                                " - " +
                                e.getMessage()
                            );
                        }
                    }
                }
            }
        }

        System.out.println("\n=== CONTENT VALIDATION SUMMARY ===");
        System.out.println("Documents sampled: " + totalSampled);
        System.out.println("Valid content: " + validContentCount);
        System.out.println(
            "Success rate: " +
            (totalSampled > 0
                    ? ((100.0 * validContentCount) / totalSampled)
                    : 0) +
            "%"
        );

        // Assertions
        assertTrue("Should have sampled some documents", totalSampled > 0);
        assertTrue(
            "Most sampled documents should have valid content",
            validContentCount >= totalSampled * 0.8
        );

        System.out.println("Document content validation: PASSED");
    }

    @Test
    public void testCrossDatasetValidation() throws Exception {
        System.out.println("=== TESTING CROSS-DATASET VALIDATION ===");

        // Test both datasets
        ProblemSet drexelPs = new ProblemSet(DREXEL_PROBLEM_SET);
        ProblemSet anonymouthPs = new ProblemSet(ANONYMOUTH_PROBLEM_SET);

        // Drexel dataset validation
        int drexelDocs = 0;
        int drexelAccessible = 0;
        for (String author : drexelPs.getAuthors()) {
            List<Document> docs = drexelPs.getTrainDocs(author);
            if (docs != null) {
                for (Document doc : docs) {
                    drexelDocs++;
                    if (new File(doc.getFilePath()).exists()) {
                        drexelAccessible++;
                    }
                }
            }
        }

        // Anonymouth dataset validation
        int anonymouthDocs = 0;
        int anonymouthAccessible = 0;
        for (String author : anonymouthPs.getAuthors()) {
            List<Document> docs = anonymouthPs.getTrainDocs(author);
            if (docs != null) {
                for (Document doc : docs) {
                    anonymouthDocs++;
                    if (new File(doc.getFilePath()).exists()) {
                        anonymouthAccessible++;
                    }
                }
            }
        }

        System.out.println(
            "Drexel dataset: " +
            drexelAccessible +
            "/" +
            drexelDocs +
            " accessible"
        );
        System.out.println(
            "Anonymouth dataset: " +
            anonymouthAccessible +
            "/" +
            anonymouthDocs +
            " accessible"
        );

        // Assertions
        assertTrue("Drexel dataset should have documents", drexelDocs > 0);
        assertTrue(
            "Anonymouth dataset should have documents",
            anonymouthDocs > 0
        );
        assertTrue(
            "Drexel dataset should be mostly accessible",
            drexelAccessible >= drexelDocs * 0.9
        );
        assertTrue(
            "Anonymouth dataset should be mostly accessible",
            anonymouthAccessible >= anonymouthDocs * 0.8
        );

        System.out.println("Cross-dataset validation: PASSED");
    }
}
