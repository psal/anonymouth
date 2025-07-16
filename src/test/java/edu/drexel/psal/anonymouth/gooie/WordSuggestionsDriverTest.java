package edu.drexel.psal.anonymouth.gooie;

import static org.junit.Assert.*;
import java.util.ArrayList;
import org.junit.Test;

/**
 * Unit tests for WordSuggestionsDriver to verify equals comparison bug
 */
public class WordSuggestionsDriverTest {

    /**
     * Test that demonstrates the bug in the equals comparison.
     * The current code compares String[] with String literals, which always returns false.
     */
    @Test
    public void testStringArrayEqualsStringBug() {
        // Create test data similar to what topToRemove contains
        ArrayList<String[]> topToRemove = new ArrayList<>();
        
        // Add some test data including the problematic quote cases
        topToRemove.add(new String[]{"''", "5"});  // single quotes case
        topToRemove.add(new String[]{"``", "3"});  // backticks case  
        topToRemove.add(new String[]{"the", "10"}); // normal word
        topToRemove.add(new String[]{"and", "8"});  // another normal word
        
        // This demonstrates the bug: String[] will never equal String
        String[] quotesArray = topToRemove.get(0); // ["''", "5"]
        String[] backticksArray = topToRemove.get(1); // ["``", "3"]
        
        // These assertions show the bug - they should fail because String[] != String
        assertFalse("String array should not equal string literal", quotesArray.equals("''"));
        assertFalse("String array should not equal string literal", backticksArray.equals("``"));
        
        // The correct comparison should be on the first element of the array
        assertTrue("First element should equal string literal", quotesArray[0].equals("''"));
        assertTrue("First element should equal string literal", backticksArray[0].equals("``"));
        
        // Test the buggy logic from the original code
        int removeSize = topToRemove.size();
        int processedCount = 0;
        
        for (int i = 0; i < removeSize; i++) {
            // This is the buggy condition from line 158 - it will always be true
            // because String[] never equals String
            if (!topToRemove.get(i).equals("''") && !topToRemove.get(i).equals("``")) {
                processedCount++;
            }
        }
        
        // The bug: all items get processed because String[] != String always
        assertEquals("Bug: all items are processed due to incorrect equals", 4, processedCount);
        
        // Test the corrected logic
        int correctedProcessedCount = 0;
        
        for (int i = 0; i < removeSize; i++) {
            // This is the correct condition - compare the first element
            if (!topToRemove.get(i)[0].equals("''") && !topToRemove.get(i)[0].equals("``")) {
                correctedProcessedCount++;
            }
        }
        
        // The fix: only non-quote items should be processed
        assertEquals("Fix: only non-quote items are processed", 2, correctedProcessedCount);
    }

    /**
     * Test edge cases for the equals comparison fix
     */
    @Test 
    public void testEqualsComparisonEdgeCases() {
        ArrayList<String[]> topToRemove = new ArrayList<>();
        
        // Add edge cases
        topToRemove.add(new String[]{"", "1"});     // empty string
        topToRemove.add(new String[]{null, "2"});   // null first element  
        topToRemove.add(new String[]{"'", "3"});    // single quote
        topToRemove.add(new String[]{"`", "4"});    // single backtick
        
        int nullSafeProcessedCount = 0;
        
        for (int i = 0; i < topToRemove.size(); i++) {
            String word = topToRemove.get(i)[0];
            // Null-safe comparison (fixing potential NPE as well)
            if (word != null && !word.equals("''") && !word.equals("``")) {
                nullSafeProcessedCount++;
            }
        }
        
        // Should process empty string, single quote, and single backtick (not the null or double quotes)
        assertEquals("Should handle edge cases correctly", 3, nullSafeProcessedCount);
    }
}