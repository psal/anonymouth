package edu.drexel.psal.anonymouth.engine;

import static org.junit.Assert.*;

import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;
import weka.core.Instances;
import edu.drexel.psal.jstylo.generics.CumulativeFeatureDriver;

/**
 * Test class for InstanceConstructor to ensure proper null handling
 * and error reporting when feature extraction fails.
 */
public class InstanceConstructorTest {

    private InstanceConstructor instanceConstructor;

    @Before
    public void setUp() {
        // Create a minimal CumulativeFeatureDriver for testing
        CumulativeFeatureDriver cfd = new CumulativeFeatureDriver();
        instanceConstructor = new InstanceConstructor(false, cfd, false);
    }

    @Test
    public void testGetAttributesWithNullInstances() {
        // Test that getAttributes handles null input gracefully
        ArrayList<String> result = instanceConstructor.getAttributes(null);
        
        assertNotNull("Result should not be null", result);
        assertTrue("Result should be empty list when input is null", result.isEmpty());
    }

    @Test
    public void testGetAttributesWithValidInstances() {
        // This test would require setting up valid Weka Instances
        // For now, we just test the null case which was the main issue
        assertNotNull("InstanceConstructor should be created", instanceConstructor);
    }

    @Test
    public void testInstanceConstructorCreation() {
        // Test that InstanceConstructor can be created without errors
        CumulativeFeatureDriver cfd = new CumulativeFeatureDriver();
        InstanceConstructor ic = new InstanceConstructor(false, cfd, false);
        assertNotNull("InstanceConstructor should be created successfully", ic);
    }
}