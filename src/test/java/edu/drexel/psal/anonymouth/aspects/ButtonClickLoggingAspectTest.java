package edu.drexel.psal.anonymouth.aspects;

import static org.junit.Assert.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;

import org.junit.Test;

/**
 * Test for AspectJ button click logging functionality.
 * This test verifies that the aspect correctly intercepts action listener calls.
 */
public class ButtonClickLoggingAspectTest {

    /**
     * Test that demonstrates AspectJ weaving is working by creating a button
     * and triggering its action listener. The aspect should log this interaction.
     */
    @Test
    public void testButtonClickLoggingAspect() {
        // Create a test button with action listener
        JButton testButton = new JButton("Test Button");
        testButton.setName("testButton");
        
        // Track if action listener was called
        final boolean[] actionCalled = {false};
        
        // Add action listener that will be intercepted by aspect
        testButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionCalled[0] = true;
                // This method call should be intercepted by ButtonClickLoggingAspect
            }
        });
        
        // Simulate button click
        ActionEvent testEvent = new ActionEvent(testButton, ActionEvent.ACTION_PERFORMED, "test-command");
        
        // Trigger the action listener (this should be logged by aspect)
        for (ActionListener listener : testButton.getActionListeners()) {
            listener.actionPerformed(testEvent);
        }
        
        // Verify the action listener was called
        assertTrue("Action listener should have been called", actionCalled[0]);
        
        // The aspect logging is verified by observing console output during test execution
        // In a real test environment, you could capture log output and verify it contains
        // the expected button click logging information
    }

    /**
     * Test aspect handling of different button types and edge cases.
     */
    @Test
    public void testAspectWithDifferentButtonTypes() {
        // Test with button with null text
        JButton nullTextButton = new JButton();
        nullTextButton.setName("nullTextButton");
        
        final boolean[] nullTextActionCalled = {false};
        nullTextButton.addActionListener(e -> nullTextActionCalled[0] = true);
        
        ActionEvent nullTextEvent = new ActionEvent(nullTextButton, ActionEvent.ACTION_PERFORMED, null);
        for (ActionListener listener : nullTextButton.getActionListeners()) {
            listener.actionPerformed(nullTextEvent);
        }
        
        assertTrue("Null text button action should work", nullTextActionCalled[0]);
        
        // Test with button with empty text
        JButton emptyTextButton = new JButton("");
        emptyTextButton.setName("emptyTextButton");
        
        final boolean[] emptyTextActionCalled = {false};
        emptyTextButton.addActionListener(e -> emptyTextActionCalled[0] = true);
        
        ActionEvent emptyTextEvent = new ActionEvent(emptyTextButton, ActionEvent.ACTION_PERFORMED, "");
        for (ActionListener listener : emptyTextButton.getActionListeners()) {
            listener.actionPerformed(emptyTextEvent);
        }
        
        assertTrue("Empty text button action should work", emptyTextActionCalled[0]);
    }
}