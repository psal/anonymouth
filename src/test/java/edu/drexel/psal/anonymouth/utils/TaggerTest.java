package edu.drexel.psal.anonymouth.utils;

import static org.junit.Assert.*;

import java.io.File;
import org.junit.Before;
import org.junit.Test;
import edu.drexel.psal.ANONConstants;

/**
 * Test class for Tagger to ensure proper initialization and error handling.
 */
public class TaggerTest {

    @Before
    public void setUp() {
        // Reset tagger before each test
        Tagger.mt = null;
    }

    @Test
    public void testTaggerInitialization() {
        // Test that tagger can be initialized
        boolean result = Tagger.initTagger();
        
        // The result depends on whether the model file exists
        File taggerFile = new File(ANONConstants.EXTERNAL_RESOURCE_PACKAGE + "english-left3words-distsim.tagger");
        if (taggerFile.exists()) {
            assertTrue("Tagger should initialize successfully when model file exists", result);
            assertNotNull("Tagger.mt should not be null after successful initialization", Tagger.mt);
        } else {
            assertFalse("Tagger should fail to initialize when model file is missing", result);
        }
    }

    @Test
    public void testTaggerDoubleInitialization() {
        // Test that calling initTagger twice doesn't cause issues
        boolean firstResult = Tagger.initTagger();
        boolean secondResult = Tagger.initTagger();
        
        // Second call should return same result as first
        assertEquals("Second initialization should return same result", firstResult, secondResult);
    }

    @Test
    public void testTaggerCreation() {
        // Test that Tagger object can be created
        Tagger tagger = new Tagger();
        assertNotNull("Tagger object should be created successfully", tagger);
    }
}