package edu.drexel.psal.anonymouth.gooie;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import net.miginfocom.swing.MigLayout;
import edu.drexel.psal.ANONConstants;
import edu.drexel.psal.anonymouth.helpers.ScrollToTop;
import edu.drexel.psal.anonymouth.utils.TaggedSentence;
import edu.drexel.psal.anonymouth.utils.TranslatorThread;
import edu.drexel.psal.jstylo.generics.Logger;

/**
 * Handles all swing components relating to the Translations Tab's 
 * 
 * @author Marc Barrowclift
 */
public class TranslationsPanel {
	
	//Constants
	private final String NAME = "( "+this.getClass().getSimpleName()+" ) - ";
	protected final int SWAP_WIDTH = 30;
	
	//Anonymouth Class Instances
	private GUIMain main;
	protected TranslationsDriver driver;
	protected TaggedSentence current;
	
	//Various Variables
	protected Map<String, TaggedSentence> translationsMap;
	protected int numTranslations;
	
	//Swing components
	protected JTextPane[] translationTextAreas;
	protected SwapButtonPanel[] translationButtons;
	protected JPanel[] finalPanels;
	protected JLabel[] languageLabels;
	
	/**
	 * Constructor, initializes the translations driver.
	 * 
	 * @param main
	 * 		GUIMain instance
	 */
	public TranslationsPanel(GUIMain main) {
		this.main = main;
		driver = new TranslationsDriver(this, main);
	}
	
	/**
	 * Displays the translations of the given sentence in the translations holder panel.
	 * 
	 * @param sentence
	 * 		The TaggedSentence to show the translations for
	 */
	public void showTranslations(TaggedSentence sentence) {
		// remove all the current translations shown
		if (main.documentPane.isEnabled()) {
			main.translationsHolderPanel.removeAll();
		}
		
		//If any of these conditions are met, we just want to clear things that need to be cleared and return.
		if (TranslatorThread.noInternet || TranslatorThread.accountsUsed || !PropertiesUtil.getDoTranslations()) {
			if (TranslatorThread.noInternet) {
				main.notTranslated.setText("Translations unavailable: No Internet connection\n\n" +
						"If you wish to recieve translation suggestions you must connect to the internet" +
						"and re-process your document.");
				main.translationsHolderPanel.add(main.notTranslated, "");
				main.translateSentenceButton.setEnabled(false);
			} else if (TranslatorThread.accountsUsed) {
				main.notTranslated.setText("The account used for translations has expired.\n\n" +
						"In order to continue recieving translations, you must restart in order for the " +
						"account change to be reflected.");
				main.translationsHolderPanel.add(main.notTranslated, "");
				main.translateSentenceButton.setEnabled(false);
				main.resetTranslator.setEnabled(false);
			} else {
				main.notTranslated.setText("");
				main.translationsHolderPanel.add(main.notTranslated, "");
				main.translateSentenceButton.setEnabled(false);
			}
				
			// revalidates and repaints so the GUI updates
			main.translationsHolderPanel.revalidate();
			main.translationsHolderPanel.repaint();
			return;
		}
		
		if (sentence.hasTranslations()) {
			Logger.logln(NAME+"Showing translations for sentence: " + sentence.getUntagged(false));
			
			boolean translated = sentence.isTranslated();
			if (translated && main.translationsTopPanelShown != ANONConstants.TRANSLATIONS.DONE) {
				switchToEmptyPanel();
			}
			if (!translated && main.translationsTopPanelShown != ANONConstants.TRANSLATIONS.PROCESSING) {
				switchToProgressPanel();
			}

			current = sentence;

			// retrieve the translation information
			ArrayList<String> translationNames = current.getTranslationNames();
			ArrayList<TaggedSentence> translations = current.getTranslations();
			translationsMap = new HashMap<String, TaggedSentence>();
			numTranslations = translations.size();

			// initialize the GUI components
			translationTextAreas = new JTextPane[numTranslations];// everySingleCluster.size()
			languageLabels = new JLabel[numTranslations];
			finalPanels = new JPanel[numTranslations];
			translationButtons = new SwapButtonPanel[numTranslations];
			
			// for each translation, initialize a title label, and a text area that will hold the translation
			// then add those two to a final panel, which will be added to the translation list panel.
			for (int i = 0; i < numTranslations; i++) {
				translationsMap.put(translationNames.get(i), translations.get(i));
				
				if (ANONConstants.SHOW_TRANSLATION_NAME_LABELS) {
					languageLabels[i] = new JLabel(translationNames.get(i));
					languageLabels[i].setFont(main.BANNER_FONT);
					languageLabels[i].setHorizontalAlignment(SwingConstants.CENTER);
					languageLabels[i].setOpaque(true);
					languageLabels[i].setBackground(main.BANNER_BACKGROUND_COLOR);
					languageLabels[i].setForeground(main.BANNER_FOREGROUND_COLOR);
				}
				
				translationsMap.put(translationNames.get(i), translations.get(i));
				
				// set up translation text area
				translationTextAreas[i] = new JTextPane();
				translationTextAreas[i].setBorder(BorderFactory.createEmptyBorder(1,3,1,3));
				translationTextAreas[i].setText(translations.get(i).getUntagged(false).trim());
				translationTextAreas[i].setEditable(false);
				translationTextAreas[i].setFocusable(false);

				translationButtons[i] = new SwapButtonPanel(this);
				translationButtons[i].setColor(i, numTranslations);
				translationButtons[i].setToolTipText("Click to replace selected sentence with this translation");
				translationButtons[i].setActionCommand(translationNames.get(i));
				translationButtons[i].addMouseListener(driver);

				// set up final panel, which will hold the previous two components
				MigLayout layout = new MigLayout(
						"wrap, ins 0",
						"",
						"");
				finalPanels[i] = new JPanel(layout);
				
				if (ANONConstants.SHOW_TRANSLATION_NAME_LABELS) {
					finalPanels[i].add(languageLabels[i], "grow, h 20!, north");
				} else if (i != 0) {
					JPanel separator = new JPanel();
					separator.setBackground(Color.GRAY);
					finalPanels[i].add(separator, "north, growx, h 1!, wrap");
				}
				
				finalPanels[i].add(translationButtons[i], "west, w " + SWAP_WIDTH + "!");
				finalPanels[i].add(translationTextAreas[i], "east, w 100:283:283");

				// add final panel to the translations list panel
				main.translationsHolderPanel.add(finalPanels[i], "");
			}
		} else {
			switchToButtonPanel();
		}
		
		// revalidates and repaints so the GUI updates
		main.translationsHolderPanel.revalidate();
		main.translationsHolderPanel.repaint();
		
		for (int i = 0; i < numTranslations; i++) {
			if (translationButtons[i].getMousePosition() != null) {
				translationButtons[i].readyImmediateOnThread();
				translationButtons[i].immediateOn.execute();
				break;
			}
		}
	}
	
	/**
	 * Swaps the TranslationsTopPanel to the "Empty" state, meaning it's still in the translations
	 * tab but contains no components and has no size, so it's essentially invisible. This state should
	 * be used for sentences that contain all translations.<br><br>
	 * 
	 * Please note that it's a good idea to wrap this method call in an invokeLater runnable thread as
	 * in some instances if this is not done this call fails to actually revalidate and repaint the window.
	 */
	public void switchToEmptyPanel() {
		main.translationsTopPanel.removeAll();
		main.translationsTopPanel.revalidate();
		main.translationsTopPanel.repaint();
		
		main.translationsTopPanelShown = ANONConstants.TRANSLATIONS.DONE;
		SwingUtilities.invokeLater(new ScrollToTop(new Point(0, 0), main.translationsHolderPanel));
	}
	
	/**
	 * Swaps the TranslationsTopPanel to the "Progress" state, meaning that for the currently selected sentence
	 * translations are still being obtained and we want to show it's current progress and nothing else.<br><br>
	 * 
	 * Please note that it's a good idea to wrap this method call in an invokeLater runnable thread as
	 * in some instances if this is not done this call fails to actually revalidate and repaint the window.
	 */
	public void switchToProgressPanel() {
		main.translationsTopPanel.removeAll();
		main.translationsTopPanel.setLayout(new MigLayout(
				"wrap, ins 0, gap 0",
				"[grow, fill]",
				"[]"));
		main.translationsTopPanel.add(main.translationsProgressBar, "grow, h 30!");
		main.translationsTopPanel.revalidate();
		main.translationsTopPanel.repaint();
		
		main.translationsTopPanelShown = ANONConstants.TRANSLATIONS.PROCESSING;
	}
	
	/**
	 * Swaps the TranslationsTopPanel to the "Button" state, meaning that the currently selected sentence
	 * is not translated and it's not in the process of translating. As a result, the button state offers users
	 * the "Translate Sentence" button, "Help" button, and "Reset" button (if shown)<br><br>
	 * 
	 * Please note that it's a good idea to wrap this method call in an invokeLater runnable thread as
	 * in some instances if this is not done this call fails to actually revalidate and repaint the window.
	 */
	public void switchToButtonPanel() {
		main.translationsTopPanel.removeAll();
		main.translationsTopPanel.setLayout(new MigLayout(
				"wrap, ins 0, gap 0",
				"[grow, fill][]",
				"[]"));
		main.translationsTopPanel.add(main.translateSentenceButton, "split, grow");
		main.translationsTopPanel.add(main.translationHelpButton, "wrap");
		main.translationsTopPanel.revalidate();
		main.translationsTopPanel.repaint();
		
		main.translationsTopPanelShown = ANONConstants.TRANSLATIONS.EMPTY;
	}
	
	/**
	 * Resets all variables and clears panel of all translations, to be used for re-processing
	 */
	public void reset(boolean reprocessing) {
		Logger.logln(NAME+"Reset");
		switchToEmptyPanel();
	}
}

/**
 * The Translation Swap "Button" JPanel, doesn't actually contain any JButtons, it's just
 * a JPanel with a paintComponent that paints the arrow (so we can get nice animations and also
 * so we're not relying on external, non-resizable arrow images and instead using 100% Java 2D
 * Graphics).
 * 
 * @author Marc Barrowclift
 *
 */
class SwapButtonPanel extends JPanel {

	//(most) Constants
	private static final long serialVersionUID = 1L;
	private final int[] ARROW_X = {5, 18, 18};
	private int[] arrow_Y = {this.getHeight()/2, (this.getHeight()/2)-15, (this.getHeight()/2)+15};
	private final Color BAD_ARROW_COLOR = Color.RED;
	private final Color GOOD_ARROW_COLOR = Color.GREEN;
	private final int ANIMATION_FRAMES = 50;
	private final int ANIMATION_SPEED = 3;
	
	//Arrow Colors
	private Color arrowColor;
	private Color default_Arrow_Color;
	private final Color HOVER_ARROW_COLOR = Color.WHITE;
	
	//Panel Background Colors
	private Color backgroundColor;
	private final Color DEFAULT_BACKGROUND_COLOR = new Color(237,237,237);
	private Color hover_Background_Color;
	
	//Threads
	protected SwingWorker<Void, Void> hoverOn;
	protected SwingWorker<Void, Void> hoverOff;
	protected SwingWorker<Void, Void> immediateOn;
	private boolean animatingHoverOn;
	private boolean animatingHoverOff;
	
	//Various Variables
	private String actionCommand;
	private TranslationsPanel translationsPanel;
	private SwapButtonPanel swapButtonPanel;
	private int translationNum = 0;
	private int height = 0;
	
	/**
	 * Constructor, initializes to default colors and prepares all the animation threads
	 * 
	 * @param translationsPanel
	 * 		TranslationsPanel instance
	 */
	public SwapButtonPanel(TranslationsPanel translationsPanel) {
		this.translationsPanel = translationsPanel;
		this.setPreferredSize(new Dimension(translationsPanel.SWAP_WIDTH, 50));
		swapButtonPanel = this;
		
		default_Arrow_Color = new Color(0,0,0);
		arrowColor = default_Arrow_Color;
		
		hover_Background_Color = new Color(0,0,0);
		backgroundColor = DEFAULT_BACKGROUND_COLOR;
		
		animatingHoverOn = false;
		animatingHoverOff = false;
		
		initThreads();
	}
	
	/**
	 * Initializes all Swing Worker threads
	 */
	private void initThreads() {
		readyHoverOnThread();
		readyHoverOffThread();
		readyImmediateOnThread();
	}
	
	/**
	 * Prepares the immediateOn SwingWorker that, when executed, will immediately
	 * change the colors to the end of the "hover" animation and repaint() the panel.<br><br>
	 * 
	 * This thread is for when translations are being added one at a time to the scroll pane.
	 * If the user has their mouse in a position such that when a new translation is added and
	 * they are now hovering over one of the swap arrows, we want to have that button's first
	 * paint color be the end of the hover animation. The best way to see what I mean is try it
	 * yourself.
	 */
	protected void readyImmediateOnThread() {
		immediateOn = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				arrowColor = HOVER_ARROW_COLOR;
				backgroundColor = hover_Background_Color;

				swapButtonPanel.repaint();
				
				return null;
			}
		};
	}
	
	/**
	 * Prepares the hoverOn SwingWorker thread that, when executed, will begin the animation loop
	 * to slowly change the panel's colors to the "hover" ones.
	 */
	protected void readyHoverOnThread() {
		hoverOn = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				animatingHoverOn = true;
				double curPercent = 0.0;

				for (int i = 0; i < ANIMATION_FRAMES ; i++) {
					curPercent = (double)i / (double) ANIMATION_FRAMES;

					int r = (int)(HOVER_ARROW_COLOR.getRed() * curPercent + default_Arrow_Color.getRed() * (1 - curPercent));
					int g = (int)(HOVER_ARROW_COLOR.getGreen() * curPercent + default_Arrow_Color.getGreen() * (1 - curPercent));
					int b = (int)(HOVER_ARROW_COLOR.getBlue() * curPercent + default_Arrow_Color.getBlue() * (1 - curPercent));
					arrowColor = new Color(r, g, b);

					r = (int)(hover_Background_Color.getRed() * curPercent + DEFAULT_BACKGROUND_COLOR.getRed() * (1 - curPercent));
					g = (int)(hover_Background_Color.getGreen() * curPercent + DEFAULT_BACKGROUND_COLOR.getGreen() * (1 - curPercent));
					b = (int)(hover_Background_Color.getBlue() * curPercent + DEFAULT_BACKGROUND_COLOR.getBlue() * (1 - curPercent));
					backgroundColor = new Color(r, g, b);

					swapButtonPanel.repaint();
					
					try {
						Thread.sleep(ANIMATION_SPEED);
					} catch (Exception e3) {}
				}
				
				animatingHoverOn = false;
				return null;
			}
		};
	}
	
	/**
	 * Prepares the hoverOff SwingWorker thread that, when executed, will begin the animation loop
	 * to slowly change the panel colors back to their defaults from the hover.
	 */
	protected void readyHoverOffThread() {
		hoverOff = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				animatingHoverOff = true;
				double curPercent = 0.0;

				for (int i = 0; i < ANIMATION_FRAMES ; i++) {
					curPercent = (double)i / (double) ANIMATION_FRAMES;

					int r = (int)(default_Arrow_Color.getRed() * curPercent + HOVER_ARROW_COLOR.getRed() * (1 - curPercent));
					int g = (int)(default_Arrow_Color.getGreen() * curPercent + HOVER_ARROW_COLOR.getGreen() * (1 - curPercent));
					int b = (int)(default_Arrow_Color.getBlue() * curPercent + HOVER_ARROW_COLOR.getBlue() * (1 - curPercent));
					arrowColor = new Color(r, g, b);

					r = (int)(DEFAULT_BACKGROUND_COLOR.getRed() * curPercent + hover_Background_Color.getRed() * (1 - curPercent));
					g = (int)(DEFAULT_BACKGROUND_COLOR.getGreen() * curPercent + hover_Background_Color.getGreen() * (1 - curPercent));
					b = (int)(DEFAULT_BACKGROUND_COLOR.getBlue() * curPercent + hover_Background_Color.getBlue() * (1 - curPercent));
					backgroundColor = new Color(r, g, b);

					swapButtonPanel.repaint();

					try {
						Thread.sleep(ANIMATION_SPEED);
					} catch (Exception e) {}
				}
				
				animatingHoverOff = false;
				return null;
			}
		};
	}
	
	/**
	 * Back when the translation swap button was still an actual JButton, I used the setActionCommand() method
	 * to determine what language the translation was so that we could easily get the correct translation
	 * taggedSentence from the translationsMap in TranslationsDriver. I'm basically just replicating it here. 
	 * 
	 * @param command
	 * 		The name of the language represented by this SwapButtonPanel instance (Japanese, French, etc.)
	 */
	public void setActionCommand(String command) {
		actionCommand = command;
	}

	/**
	 * See setActionCommand()'s documentation for more info.
	 * 
	 * @return
	 * 		The name of the language represented by this SwapButtonPanel instance (Japanese, French, etc.)
	 */
	public String getActionCommand() {
		return actionCommand;
	}
	
	/**
	 * Determines whether or not the panel is ready to begin a new animation thread (we don't want two going at
	 * the same time)
	 * 
	 * @return
	 * 		True or false, depending on whether or not there are any animation threads going on.
	 */
	protected boolean isReady() {
		return (animatingHoverOn || animatingHoverOff);
	}
	
	/**
	 * Updates the vertical arrow dimensions with the passed height so that the arrow can be perfectly in the middle
	 * no matter how large or small the component's height is. Also saved a local copy of the height for future reference
	 * in the paintComponent() method.<br><br>
	 * 
	 * MUST only be called in the paintComponent() method, because anywhere else Swing is dumb as shit and doesn't actually
	 * know the component's dimensions yet. The only place I've found where we can reliably get the final height of the component
	 * is in it's paintComponent method().
	 * 
	 * @param height
	 * 		The determined height of the component
	 */
	public void updateSize(int height) {
		this.height = height;
		arrow_Y[0] = height/2;
		arrow_Y[1] = arrow_Y[0]-10;
		arrow_Y[2] = arrow_Y[0]+10;
	}
	
	/**
	 * Calculate's the arrow's base color and the background's hover color based on the translation's ranking with respect to
	 * other translations anonymity index (the higher it is the greener it is, meaning it's the "better choice" than the lower red
	 * ones).
	 * 
	 * @param position
	 * 		The translation's position with respect to the other translations
	 * @param numTranslations
	 * 		The number of other translations (anywhere from 1 to 15)
	 */
	public void setColor(int position, int numTranslations) {
		double curPercent = (double)position / (double)numTranslations;
		translationNum = position;
		
		if (curPercent <= 0) {
			default_Arrow_Color = new Color(0, 255, 0);
		} else if (curPercent >= 100) {
			default_Arrow_Color = new Color(255, 0, 0);
		} else {
			int r = (int)(BAD_ARROW_COLOR.getRed() * curPercent + GOOD_ARROW_COLOR.getRed() * (1 - curPercent));
			int g = (int)(BAD_ARROW_COLOR.getGreen() * curPercent + GOOD_ARROW_COLOR.getGreen() * (1 - curPercent));
			int b = (int)(BAD_ARROW_COLOR.getBlue() * curPercent + GOOD_ARROW_COLOR.getBlue() * (1 - curPercent));
			
			default_Arrow_Color = new Color(r, g, b);
		}
		
		arrowColor = default_Arrow_Color;
		hover_Background_Color = default_Arrow_Color;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		updateSize(translationsPanel.translationTextAreas[translationNum].getHeight());
		g2d.setColor(backgroundColor);
		g2d.fillRect(0, 0, translationsPanel.SWAP_WIDTH, height);
		g2d.setColor(arrowColor);
		g2d.fillPolygon(ARROW_X, arrow_Y, 3);
	}
}