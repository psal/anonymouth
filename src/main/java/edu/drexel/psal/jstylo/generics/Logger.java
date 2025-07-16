package edu.drexel.psal.jstylo.generics;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;
import java.io.File;

/**
 * Log4J 2 based logger that maintains API compatibility with the original Logger class.
 * Provides the same logging functionality but uses Log4J 2 under the hood.
 *
 * @author Claude (Log4J 2 migration)
 * @author Ariel Stolerman (original implementation)
 * @author Marc Barrowclift (original implementation)
 */
public class Logger {

    private static final String NAME = "( Logger ) - ";
    public static final boolean loggerFlag = true;
    public static boolean logFile = true; // Always true with Log4J 2
    
    private static org.apache.logging.log4j.Logger log4jLogger;
    private static boolean initialized = false;

    /** Enumerator for logger output. */
    public enum LogOut {
        STDOUT, STDERR
    }

    /**
     * Initialize Log4J 2 logger
     */
    private static synchronized void initialize() {
        if (!initialized) {
            try {
                // Ensure log directory exists
                File logDir = new File("./anonymouth_log");
                if (!logDir.exists()) {
                    logDir.mkdirs();
                }
                
                // Get Log4J 2 logger for Anonymouth
                log4jLogger = LogManager.getLogger("edu.drexel.psal.anonymouth");
                initialized = true;
                
                // Don't call logln here to avoid infinite recursion
                if (log4jLogger != null) {
                    log4jLogger.info(NAME + "Log4J 2 logger initialized, session name = Anonymouth");
                }
            } catch (Exception e) {
                // Fallback to console output if Log4J 2 fails
                System.err.println("Failed to initialize Log4J 2 logger: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns current time (for backward compatibility)
     * @return The current time
     */
    public static String time() {
        return java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH-mm-ss"));
    }

    /**
     * Returns current date (for backward compatibility)
     * @return The current date
     */
    public static String date() {
        return java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    /**
     * Initialize logger with session name (for backward compatibility)
     * @param sessionName The session name
     */
    public static void initializeSession(String sessionName) {
        // With Log4J 2, this is handled by the configuration
        logln(NAME + "Logger initialized, session name = " + sessionName);
    }

    /**
     * Prints output (no new line) to the log
     * @param msg The message to log
     */
    public static void log(String msg) {
        if (loggerFlag) {
            if (!initialized) {
                initialize();
            }
            if (log4jLogger != null) {
                // Use info level and remove newlines since we're mimicking the original behavior
                log4jLogger.info(msg.replace("\n", ""));
            }
        }
    }

    /**
     * Prints output (no new line) to the specified target
     * @param msg The message to log
     * @param target Output target (STDOUT or STDERR)
     */
    public static void log(String msg, LogOut target) {
        if (loggerFlag) {
            if (!initialized) {
                initialize();
            }
            if (log4jLogger != null) {
                String cleanMsg = msg.replace("\n", "");
                switch (target) {
                    case STDOUT:
                        log4jLogger.info(cleanMsg);
                        break;
                    case STDERR:
                        log4jLogger.error(cleanMsg);
                        break;
                    default:
                        log4jLogger.info(cleanMsg);
                        break;
                }
            }
        }
    }

    /**
     * Prints output with new line to the log
     * @param msg The message to log
     */
    public static void logln(String msg) {
        if (loggerFlag) {
            if (!initialized) {
                initialize();
            }
            if (log4jLogger != null) {
                log4jLogger.info(msg);
            }
        }
    }

    /**
     * Prints output with new line to the specified target
     * @param msg The message to log
     * @param target Output target (STDOUT or STDERR)
     */
    public static void logln(String msg, LogOut target) {
        if (loggerFlag) {
            if (!initialized) {
                initialize();
            }
            if (log4jLogger != null) {
                switch (target) {
                    case STDOUT:
                        log4jLogger.info(msg);
                        break;
                    case STDERR:
                        log4jLogger.error(msg);
                        break;
                    default:
                        log4jLogger.info(msg);
                        break;
                }
            }
        }
    }

    /**
     * Logs an exception with stack trace
     * @param e The exception to log
     */
    public static void logln(Exception e) {
        if (loggerFlag) {
            if (!initialized) {
                initialize();
            }
            if (log4jLogger != null) {
                log4jLogger.error(">>>>>>>>>>>>>>>>>>>>>>>   LOGGING STACK TRACE   <<<<<<<<<<<<<<<<<<<<<<<<<");
                log4jLogger.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Logs an exception with stack trace to specified target
     * @param e The exception to log
     * @param target Output target (STDOUT or STDERR)
     */
    public static void logln(Exception e, LogOut target) {
        if (loggerFlag) {
            if (!initialized) {
                initialize();
            }
            if (log4jLogger != null) {
                switch (target) {
                    case STDOUT:
                        log4jLogger.info(">>>>>>>>>>>>>>>>>>>>>>>   LOGGING STACK TRACE   <<<<<<<<<<<<<<<<<<<<<<<<<");
                        log4jLogger.info(e.getMessage(), e);
                        break;
                    case STDERR:
                        log4jLogger.error(">>>>>>>>>>>>>>>>>>>>>>>   LOGGING STACK TRACE   <<<<<<<<<<<<<<<<<<<<<<<<<");
                        log4jLogger.error(e.getMessage(), e);
                        break;
                    default:
                        log4jLogger.error(">>>>>>>>>>>>>>>>>>>>>>>   LOGGING STACK TRACE   <<<<<<<<<<<<<<<<<<<<<<<<<");
                        log4jLogger.error(e.getMessage(), e);
                        break;
                }
            }
        }
    }

    /**
     * Close logger (for backward compatibility)
     */
    public static void close() {
        if (log4jLogger != null) {
            logln(NAME + "Logger shutting down");
            LogManager.shutdown();
        }
    }

    /**
     * Flush logger (for backward compatibility)
     */
    public static void flush() {
        // Log4J 2 handles flushing automatically
    }

    /**
     * Initialize log file (for backward compatibility)
     */
    public static void initLogFile() {
        // With Log4J 2, this is handled by configuration
        if (!initialized) {
            initialize();
        }
    }

    /**
     * Set file prefix (for backward compatibility)
     * @param prefix The file prefix to set
     */
    public static void setFilePrefix(String prefix) {
        // With Log4J 2, filename is configured in log4j2.xml
        // This method is kept for compatibility but has no effect
        logln(NAME + "File prefix set to: " + prefix + " (managed by Log4J 2)");
    }
}