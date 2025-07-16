package edu.drexel.psal.jstylo.generics;

import static org.junit.Assert.*;

import com.jgaap.generics.Document;
import java.io.File;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for ProblemSet functionality, specifically focusing on
 * mixed absolute and relative path handling that was recently fixed.
 */
public class ProblemSetTest {

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
    public void testDrexelProblemSetExists() {
        File problemSetFile = new File(DREXEL_PROBLEM_SET);
        assertTrue(
            "Drexel problem set file should exist",
            problemSetFile.exists()
        );
    }

    @Test
    public void testAnonymouthProblemSetExists() {
        File problemSetFile = new File(ANONYMOUTH_PROBLEM_SET);
        assertTrue(
            "Anonymouth problem set file should exist",
            problemSetFile.exists()
        );
    }

    @Test
    public void testDrexelProblemSetLoading() throws Exception {
        ProblemSet ps = new ProblemSet(DREXEL_PROBLEM_SET);

        assertNotNull("Problem set should not be null", ps);
        assertNotNull("Authors should not be null", ps.getAuthors());
        assertTrue("Should have authors", ps.getAuthors().size() > 0);
        assertTrue("Should have test documents", ps.hasTestDocs());

        // Verify we have the expected number of authors (13 in drexel_1_copy.xml)
        assertEquals("Should have 13 authors", 13, ps.getAuthors().size());
    }

    @Test
    public void testAnonymouthProblemSetLoading() throws Exception {
        ProblemSet ps = new ProblemSet(ANONYMOUTH_PROBLEM_SET);

        assertNotNull("Problem set should not be null", ps);
        assertNotNull("Authors should not be null", ps.getAuthors());
        assertTrue("Should have authors", ps.getAuthors().size() > 0);
        assertTrue("Should have test documents", ps.hasTestDocs());

        // Verify we have the expected number of authors (4 in Anonymouth_docSet.xml)
        assertEquals("Should have 4 authors", 4, ps.getAuthors().size());
    }

    @Test
    public void testMixedPathResolution() throws Exception {
        // Test the Anonymouth dataset which has mixed absolute/relative paths
        ProblemSet ps = new ProblemSet(ANONYMOUTH_PROBLEM_SET);

        int totalDocs = 0;
        int accessibleDocs = 0;

        // Check training documents
        for (String author : ps.getAuthors()) {
            List<Document> docs = ps.getTrainDocs(author);
            if (docs != null) {
                for (Document doc : docs) {
                    totalDocs++;
                    File docFile = new File(doc.getFilePath());
                    if (docFile.exists()) {
                        accessibleDocs++;
                    }

                    // Verify that all paths are now absolute (start with / on Unix)
                    String path = doc.getFilePath();
                    assertTrue(
                        "All resolved paths should be absolute: " + path,
                        path.startsWith("/") || path.matches("^[A-Za-z]:.*")
                    );
                }
            }
        }

        // Check test documents
        for (String author : ps.getTestDocs().keySet()) {
            List<Document> docs = ps.getTestDocs().get(author);
            if (docs != null) {
                for (Document doc : docs) {
                    totalDocs++;
                    File docFile = new File(doc.getFilePath());
                    if (docFile.exists()) {
                        accessibleDocs++;
                    }
                }
            }
        }

        assertTrue("Should have found some documents", totalDocs > 0);
        assertTrue(
            "Most documents should be accessible",
            accessibleDocs >= totalDocs * 0.8
        );
    }

    @Test
    public void testDocumentAccessibility() throws Exception {
        ProblemSet ps = new ProblemSet(DREXEL_PROBLEM_SET);

        // Test that we can access document content
        for (String author : ps.getAuthors()) {
            List<Document> docs = ps.getTrainDocs(author);
            if (docs != null && !docs.isEmpty()) {
                Document firstDoc = docs.get(0);
                File docFile = new File(firstDoc.getFilePath());

                if (docFile.exists()) {
                    assertTrue(
                        "Document should be readable",
                        docFile.canRead()
                    );
                    assertTrue(
                        "Document should have content",
                        docFile.length() > 0
                    );
                }
                break; // Just test the first author's first document
            }
        }
    }

    @Test
    public void testProblemSetStructure() throws Exception {
        ProblemSet ps = new ProblemSet(DREXEL_PROBLEM_SET);

        // Verify basic structure
        assertNotNull(
            "Training corpus name should not be null",
            ps.getTrainCorpusName()
        );
        assertNotNull("Author map should not be null", ps.getAuthorMap());

        // Verify all authors have at least one document
        for (String author : ps.getAuthors()) {
            assertTrue(
                "Author " + author + " should have at least one document",
                ps.numTrainDocs(author) > 0
            );
        }
    }

    @Test(expected = Exception.class)
    public void testInvalidProblemSetPath() throws Exception {
        // This should throw an exception
        new ProblemSet("nonexistent/path/to/problemset.xml");
    }

    @Test
    public void testEmptyProblemSet() {
        ProblemSet ps = new ProblemSet();

        assertFalse(
            "Empty problem set should have no authors",
            ps.hasAuthors()
        );
        assertFalse(
            "Empty problem set should have no test docs",
            ps.hasTestDocs()
        );
        assertEquals(
            "Empty problem set should have 0 authors",
            0,
            ps.getAuthors() == null ? 0 : ps.getAuthors().size()
        );
    }
}
