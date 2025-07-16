package edu.drexel.psal.anonymouth.aspects;

import java.awt.event.ActionEvent;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JMenuItem;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import edu.drexel.psal.jstylo.generics.Logger;

/**
 * AspectJ aspect to log button clicks and action listener invocations.
 * This aspect intercepts ActionListener.actionPerformed() calls and logs
 * relevant information about button clicks throughout the application.
 * 
 * @author AspectJ Integration
 */
@Aspect
public class ButtonClickLoggingAspect {

    private static final String NAME = "( ButtonClickAspect ) - ";

    /**
     * Pointcut that matches ActionListener.actionPerformed() method executions
     * for all action listeners in the application.
     */
    @Pointcut("execution(* java.awt.event.ActionListener.actionPerformed(..))")
    public void actionListenerExecution() {}

    /**
     * Pointcut that matches ActionListener.actionPerformed() method executions
     * specifically within the anonymouth package structure.
     */
    @Pointcut("execution(* edu.drexel.psal.anonymouth..*.actionPerformed(..))")
    public void anonymouthActionListenerExecution() {}

    /**
     * Advice that runs after any ActionListener.actionPerformed() method
     * to log button click information.
     * 
     * @param joinPoint The join point representing the method execution
     */
    @After("actionListenerExecution() || anonymouthActionListenerExecution()")
    public void logButtonClick(JoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            if (args.length > 0 && args[0] instanceof ActionEvent) {
                ActionEvent event = (ActionEvent) args[0];
                Object source = event.getSource();
                
                String buttonInfo = extractButtonInfo(source);
                String listenerClass = joinPoint.getTarget().getClass().getSimpleName();
                String methodName = joinPoint.getSignature().getName();
                
                Logger.logln(NAME + "Button clicked: " + buttonInfo + 
                           " | Listener: " + listenerClass + 
                           " | Method: " + methodName +
                           " | Action Command: " + event.getActionCommand());
                           
            }
        } catch (Exception e) {
            // Silently handle any logging errors to avoid disrupting UI
            Logger.logln(NAME + "Error logging button click: " + e.getMessage());
        }
    }

    /**
     * Extracts readable information from button source objects.
     * 
     * @param source The button or component that triggered the action
     * @return A string description of the button
     */
    private String extractButtonInfo(Object source) {
        if (source instanceof JButton) {
            JButton button = (JButton) source;
            String text = button.getText();
            String name = button.getName();
            return "JButton[text='" + (text != null ? text : "null") + 
                   "', name='" + (name != null ? name : "null") + "']";
                   
        } else if (source instanceof JMenuItem) {
            JMenuItem menuItem = (JMenuItem) source;
            String text = menuItem.getText();
            String name = menuItem.getName();
            return "JMenuItem[text='" + (text != null ? text : "null") + 
                   "', name='" + (name != null ? name : "null") + "']";
                   
        } else if (source instanceof AbstractButton) {
            AbstractButton button = (AbstractButton) source;
            String text = button.getText();
            String name = button.getName();
            String className = button.getClass().getSimpleName();
            return className + "[text='" + (text != null ? text : "null") + 
                   "', name='" + (name != null ? name : "null") + "']";
                   
        } else {
            return source.getClass().getSimpleName() + "[" + source.toString() + "]";
        }
    }
}