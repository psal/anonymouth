package edu.drexel.psal.anonymouth.gooie;

// Apple-specific imports removed for cross-platform compatibility
// All Apple functionality is accessed via reflection
import edu.drexel.psal.ANONConstants;
import edu.drexel.psal.anonymouth.helpers.FileHelper;
import edu.drexel.psal.anonymouth.helpers.ImageLoader;
import edu.drexel.psal.anonymouth.utils.About;
import edu.drexel.psal.jstylo.generics.Logger;
import java.awt.Image;
import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Scanner;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

/**
 * ThePresident sets up the Application and System fields/preferences prior to
 * calling 'GUIMain'
 *
 * @author Andrew W.E. McDonald
 * @author Marc Barrowclift
 */
@SuppressWarnings("deprecation") // For the Apple Look and Feel code below (If you find something better, please
// replace)
public class ThePresident {

	// Constants
	private final String NAME = "( " + this.getClass().getSimpleName() + " ) - ";
	public final String ANONYMOUTH_LOGO = "anonymouth_LOGO.png";
	public final String ANONYMOUTH_LOGO_LARGE = "anonymouth_LOGO_large.png";
	public final String ANONYMOUTH_LOGO_SMALL = "anonymouth_gui_chooser.png";

	// Anonymouth Icons
	public static Image logo;
	public static ImageIcon aboutLogo;
	public static ImageIcon dialogLogo;
	public static Icon dialogIcon;
	protected static StartWindow startWindow;
	public GUIMain main;

	public static Object app; // For OS X - using Object type for cross-platform compatibility
	public Scanner in = new Scanner(System.in); // xxx just for testing. can be called anywhere in Anonymouth.
	public static String sessionName = "";
	public static boolean classifier_Saved = false;
	public static int max_Features_To_Consider = PropertiesUtil.defaultFeatures;
	public static int num_Tagging_Threads = PropertiesUtil.defaultThreads;
	public static boolean should_Keep_Auto_Saved_Anonymized_Docs = PropertiesUtil.defaultVersionAutoSave;
	public static boolean autosave_Latest_Version = PropertiesUtil.defaultAutoSave;
	public static boolean canDoQuickStart = false;
	public static SplashScreen splash;

	public static void main(String[] args) {
		/**
		 * XXX TODO XXX THIS SHOULD BE INVOKED AS A RUNNABLE! The only reason it's not
		 * is this for some reason screws up the splash screen, and until I figure out
		 * why we'll set run Anonymouth on the main thread, but this should NOT be the
		 * case (all Swing should be done on the EDT)
		 */
		/*
		 * SwingUtilities.invokeLater(new Runnable() { public void run() { new
		 * ThePresident(); } });
		 */

		new ThePresident();
	}

	public ThePresident() {
		splash = new SplashScreen();
		splash.showSplashScreen();

		// Validate required resources before continuing
		if (!FileHelper.validateRequiredResources()) {
			Logger.logln(NAME + "Required resources missing - exiting application");
			System.exit(1);
		}
		
		// Initialize Stanford POS tagger early to catch any initialization issues
		Logger.logln(NAME + "Initializing Stanford POS tagger...");
		if (!edu.drexel.psal.anonymouth.utils.Tagger.initTagger()) {
			Logger.logln(NAME + "CRITICAL ERROR: Failed to initialize Stanford POS tagger");
			splash.hideSplashScreen();
			javax.swing.JOptionPane.showMessageDialog(null, 
				"Failed to initialize Stanford POS tagger.\n" +
				"Please ensure the jsan_resources directory contains the required model files.",
				"Initialization Error", 
				javax.swing.JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
		Logger.logln(NAME + "Stanford POS tagger initialized successfully");

		logo = ImageLoader.getImage(ANONYMOUTH_LOGO_LARGE);
		aboutLogo = ImageLoader.getImageIcon(ANONYMOUTH_LOGO);
		dialogLogo = ImageLoader.getImageIcon(ANONYMOUTH_LOGO_SMALL);
		dialogIcon = ImageLoader.getIcon(ANONYMOUTH_LOGO_SMALL);

		if (ANONConstants.IS_MAC) {
			System.setProperty("WEKA_HOME", "/dev/null");

			Logger.logln(NAME + "We're on a Mac!");
			System.setProperty("apple.laf.useScreenMenuBar", "true");

			initializeAppleIntegration(logo);
		}

		File log_dir = new File(ANONConstants.LOG_DIR); // create log directory if it doesn't exist.
		if (!log_dir.exists()) {
			Logger.logln(NAME + "Creating directory for DocumentMagician to write to...");
			log_dir = log_dir.getAbsoluteFile();
			log_dir.mkdir();
		}

		File dm_write_dir = new File(ANONConstants.DOC_MAGICIAN_WRITE_DIR);
		if (!dm_write_dir.exists()) {
			Logger.logln(NAME + "Creating directory for DocumentMagician to write to...");
			dm_write_dir.mkdir();
		}
		File ser_dir = new File(ANONConstants.SER_DIR);
		if (!ser_dir.exists()) {
			Logger.logln(NAME + "Creating directory to save serialized objects to...");
			ser_dir.mkdir();
		}

		if (!ANONConstants.IS_USER_STUDY) {
			sessionName = "Anonymouth";
		}

		splash.updateText("Preparing Start Window");

		main = new GUIMain();
		startWindow = new StartWindow(main);

		splash.hideSplashScreen();
		startWindow.showStartWindow();
	}

	/**
	 * TEST METHOD will print "sayThis" and then read and return a line from the
	 * user. Useful for stopping the progam at spots.
	 *
	 * @param sayThis
	 * @return
	 */
	public String read(String sayThis) {
		Logger.logln(sayThis);
		return in.nextLine();
	}

	/**
	 * TEST METHOD will print "System waiting for user input:" and then read and
	 * return a line from the user. Useful for stopping the progam at spots.
	 *
	 * @return
	 */
	public String read() {
		Logger.logln("System waiting for user input:");
		return in.nextLine();
	}

	/**
	 * Initialize Apple-specific integration using reflection for cross-platform
	 * compatibility. This method safely handles macOS-specific features without
	 * causing issues on other platforms.
	 */
	private void initializeAppleIntegration(Image logo) {
		try {
			// Get the Application class using reflection
			Class<?> applicationClass = Class.forName("com.apple.eawt.Application");
			Method getApplicationMethod = applicationClass.getMethod("getApplication");
			app = getApplicationMethod.invoke(null);

			// Set dock icon
			Method setDockIconImageMethod = applicationClass.getMethod("setDockIconImage", Image.class);
			setDockIconImageMethod.invoke(app, logo);

			// Create application listener using reflection and proxy
			Class<?> applicationListenerClass = Class.forName("com.apple.eawt.ApplicationListener");
			Object listener = Proxy.newProxyInstance(applicationListenerClass.getClassLoader(),
					new Class[]{applicationListenerClass}, new AppleApplicationHandler());

			// Add the application listener
			Method addApplicationListenerMethod = applicationClass.getMethod("addApplicationListener",
					applicationListenerClass);
			addApplicationListenerMethod.invoke(app, listener);

			// Enable preferences menu
			Method setEnabledPreferencesMenuMethod = applicationClass.getMethod("setEnabledPreferencesMenu",
					boolean.class);
			setEnabledPreferencesMenuMethod.invoke(app, true);

			// Request foreground
			Method requestForegroundMethod = applicationClass.getMethod("requestForeground", boolean.class);
			requestForegroundMethod.invoke(app, true);

			Logger.logln(NAME + "Apple integration initialized successfully");
		} catch (Exception e) {
			Logger.logln(NAME + "Failed to initialize Apple integration: " + e.getMessage());
			// This is expected on non-Mac platforms and should not cause the application to
			// fail
		}
	}

	/**
	 * Handler for Apple application events using reflection and proxy pattern.
	 */
	private class AppleApplicationHandler implements InvocationHandler {

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			String methodName = method.getName();

			try {
				if ("handleQuit".equals(methodName)) {
					if (PropertiesUtil.getWarnQuit() && !main.documentSaved) {
						main.toFront();
						main.requestFocus();
						int confirm = JOptionPane.showOptionDialog(null,
								"Are You Sure to Close Application?\nYou will lose all unsaved changes.",
								"Unsaved Changes Warning", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
								null, null, null);
						if (confirm == 0) {
							System.exit(0);
						}
					} else if (PropertiesUtil.getAutoSave()) {
						main.menuDriver.save(GUIMain.inst);
						System.exit(0);
					} else {
						System.exit(0);
					}
				} else if ("handleAbout".equals(methodName)) {
					// Mark event as handled
					if (args != null && args.length > 0) {
						Object event = args[0];
						Method setHandledMethod = event.getClass().getMethod("setHandled", boolean.class);
						setHandledMethod.invoke(event, true);
					}
					JOptionPane.showMessageDialog(null, About.aboutAnonymouth, "About Anonymouth",
							JOptionPane.INFORMATION_MESSAGE, aboutLogo);
				} else if ("handlePreferences".equals(methodName)) {
					if (main != null && main.preferencesWindow != null) {
						main.preferencesWindow.showWindow();
					}
				}
			} catch (Exception e) {
				Logger.logln(NAME + "Error handling Apple application event: " + e.getMessage());
			}

			return null;
		}
	}
}
