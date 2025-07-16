package edu.drexel.psal.anonymouth.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javax.swing.SwingUtilities;
import javax.swing.DefaultListModel;
import javax.swing.JList;

/**
 * AspectJ aspect for diagnosing JList model modification threading issues.
 * This aspect logs critical information about list model operations to help
 * identify EDT violations that could cause ArrayIndexOutOfBoundsException.
 */
@Aspect
public class ListModelDiagnosticAspect {
    
    private static final Logger logger = LogManager.getLogger(ListModelDiagnosticAspect.class);
    
    /**
     * Pointcut for DefaultListModel modification methods
     */
    @Pointcut("execution(* javax.swing.DefaultListModel.addElement(..))")
    public void addElementOperation() {}
    
    @Pointcut("execution(* javax.swing.DefaultListModel.removeElement(..))")
    public void removeElementOperation() {}
    
    @Pointcut("execution(* javax.swing.DefaultListModel.removeElementAt(..))")
    public void removeElementAtOperation() {}
    
    @Pointcut("execution(* javax.swing.DefaultListModel.removeAllElements())")
    public void removeAllElementsOperation() {}
    
    @Pointcut("execution(* javax.swing.DefaultListModel.insertElementAt(..))")
    public void insertElementAtOperation() {}
    
    @Pointcut("execution(* javax.swing.DefaultListModel.setElementAt(..))")
    public void setElementAtOperation() {}
    
    /**
     * Pointcut for JList model assignment
     */
    @Pointcut("execution(* javax.swing.JList.setModel(..))")
    public void setModelOperation() {}
    
    /**
     * Combined pointcut for all list model modifications
     */
    @Pointcut("addElementOperation() || removeElementOperation() || removeElementAtOperation() || " +
              "removeAllElementsOperation() || insertElementAtOperation() || setElementAtOperation()")
    public void listModelModification() {}
    
    /**
     * Log before any list model modification
     */
    @Before("listModelModification()")
    public void logBeforeModelModification(JoinPoint joinPoint) {
        boolean isEDT = SwingUtilities.isEventDispatchThread();
        String methodName = joinPoint.getSignature().getName();
        Object target = joinPoint.getTarget();
        
        // Get the current size of the model
        int currentSize = -1;
        if (target instanceof DefaultListModel) {
            currentSize = ((DefaultListModel<?>) target).size();
        }
        
        // Log thread information and operation details
        logger.warn("LIST_MODEL_MODIFICATION: method={}, isEDT={}, thread={}, modelSize={}, target={}", 
                   methodName, 
                   isEDT, 
                   Thread.currentThread().getName(),
                   currentSize,
                   target.getClass().getSimpleName());
        
        // Log stack trace for non-EDT operations
        if (!isEDT) {
            logger.error("EDT_VIOLATION: List model modification on non-EDT thread!");
            logger.error("Stack trace:", new RuntimeException("EDT Violation Stack Trace"));
        }
        
        // Log arguments
        Object[] args = joinPoint.getArgs();
        if (args != null && args.length > 0) {
            logger.debug("LIST_MODEL_ARGS: {}", java.util.Arrays.toString(args));
        }
    }
    
    /**
     * Log after any list model modification
     */
    @After("listModelModification()")
    public void logAfterModelModification(JoinPoint joinPoint) {
        Object target = joinPoint.getTarget();
        
        // Get the new size of the model
        int newSize = -1;
        if (target instanceof DefaultListModel) {
            newSize = ((DefaultListModel<?>) target).size();
        }
        
        logger.debug("LIST_MODEL_MODIFIED: method={}, newSize={}", 
                    joinPoint.getSignature().getName(), 
                    newSize);
    }
    
    /**
     * Log JList model assignments
     */
    @Before("setModelOperation()")
    public void logModelAssignment(JoinPoint joinPoint) {
        boolean isEDT = SwingUtilities.isEventDispatchThread();
        Object target = joinPoint.getTarget();
        Object[] args = joinPoint.getArgs();
        
        logger.warn("JLIST_MODEL_ASSIGNMENT: isEDT={}, thread={}, target={}, newModel={}", 
                   isEDT, 
                   Thread.currentThread().getName(),
                   target.getClass().getSimpleName(),
                   args != null && args.length > 0 ? args[0].getClass().getSimpleName() : "null");
        
        if (!isEDT) {
            logger.error("EDT_VIOLATION: JList model assignment on non-EDT thread!");
            logger.error("Stack trace:", new RuntimeException("EDT Violation Stack Trace"));
        }
    }
    
    /**
     * Log JList UI operations that could trigger the exception
     */
    @Pointcut("execution(* javax.swing.plaf.basic.BasicListUI.updateLayoutState(..))")
    public void updateLayoutStateOperation() {}
    
    @Before("updateLayoutStateOperation()")
    public void logUpdateLayoutState(JoinPoint joinPoint) {
        boolean isEDT = SwingUtilities.isEventDispatchThread();
        
        logger.debug("BASIC_LIST_UI_UPDATE: isEDT={}, thread={}", 
                    isEDT, 
                    Thread.currentThread().getName());
        
        if (!isEDT) {
            logger.warn("POTENTIAL_ISSUE: BasicListUI.updateLayoutState called from non-EDT thread!");
        }
    }
    
    /**
     * Pointcut for methods in Anonymouth gooie package that might modify lists
     */
    @Pointcut("execution(* edu.drexel.psal.anonymouth.gooie..*(..))")
    public void anonymouthGooieOperations() {}
    
    /**
     * Log any JList operations in the gooie package
     */
    @Before("anonymouthGooieOperations() && (call(* javax.swing.DefaultListModel.*(..)) || " +
            "call(* javax.swing.JList.*(..)))")
    public void logGooieListOperations(JoinPoint joinPoint) {
        boolean isEDT = SwingUtilities.isEventDispatchThread();
        String className = joinPoint.getTarget() != null ? 
                          joinPoint.getTarget().getClass().getSimpleName() : "Unknown";
        
        logger.debug("GOOIE_LIST_OPERATION: class={}, method={}, isEDT={}, thread={}", 
                    className,
                    joinPoint.getSignature().getName(),
                    isEDT, 
                    Thread.currentThread().getName());
    }
}