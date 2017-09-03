package edu.drexel.psal.anonymouth.gooie;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import edu.drexel.psal.anonymouth.helpers.ImageLoader;
import edu.drexel.psal.jstylo.generics.Logger;

/**
 * Displays an enlarged, more detailed version of the results graph shown in the main window.
 * 
 * @author Marc Barrowclift
 *
 */
public class ResultsWindow extends JFrame {

	//Constants
	private static final long serialVersionUID = 1L;
	private final String NAME = "( ResultsWindow ) - ";
	private final String RESULTS_ICON = "resultsButton.png";
	private final int MAX_HEIGHT = 522;
	
	//Images/charts/renders
	private CategoryItemRenderer renderer;
	private BufferedImage chartImage;
	private JFreeChart chart;
	
	//Various Variables
	protected JScrollPane drawingScrollPane;
	protected JPanel drawingPanel;
	public JLabel resultsLabel;
	private ArrayList<String> authors;
	private ArrayList<Integer> percent;
	private DefaultCategoryDataset dataSet;
	private Font labelFont;
	private int pictureWidth; //The width of the graph picture, NOT necessarily the width of the window holding it
	
	/**
	 * Constructor, initializes the results window and all variables needed to properly
	 * display it.
	 * 
	 * @param main
	 *		GUIMain instance
	 */
	public ResultsWindow(GUIMain main) {
		initComponents();
		initWindow();
	}
	
	/**
	 * Initializes all variables and things relating to the window itself and not it's components
	 */
	private void initWindow() {
		this.setSize(500, 500); //Default size
		this.add(drawingScrollPane);
		this.setLocationRelativeTo(null);
		this.setTitle("Process Results");
		this.setResizable(true);
		this.setVisible(false);
	}
	
	/**
	 * Initializes all the components we will need
	 */
	private void initComponents() {
		authors = new ArrayList<String>();
		percent = new ArrayList<Integer>();
		
		drawingPanel = new JPanel(new BorderLayout()) {
			private static final long serialVersionUID = 1L;

			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				
				Graphics2D g2d = (Graphics2D)g;
				g2d.drawImage(chartImage, 0, 0, null);
			}
		};
		
		labelFont = new Font("Helvatica", Font.BOLD, 15);
		resultsLabel = new JLabel("Process your document to see ownership probability");
		resultsLabel.setFont(labelFont);
		resultsLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		resultsLabel.setHorizontalAlignment(SwingConstants.CENTER);
		drawingPanel.add(resultsLabel, BorderLayout.SOUTH);
		drawingPanel.setBackground(Color.WHITE);
		
		drawingScrollPane = new JScrollPane(drawingPanel);
	}
	
	/**
	 * Displays the window
	 */
	public void showResultsWindow() {
		this.setLocationRelativeTo(null);
		this.setVisible(true);
		this.repaint();
	}
	
	/**
	 * Makes the data set and main chart from the data given and creates the image that will be painted to the window
	 * when displayed.<br><br>
	 * 
	 * MUST be called first before displaying any windows otherwise it will be displaying the old results image and not
	 * a new image of the newly obtained results!
	 */
	public void makeChart() {
		Logger.logln(NAME+"Creating new Results Chart");
		dataSet = new DefaultCategoryDataset();
		
		for (int i = 0; i < authors.size(); i++)
			dataSet.setValue(percent.get(i).intValue(), "", authors.get(i));
		
		chart = ChartFactory.createBarChart(
				"Chance of Document Ownership", "Authors", "Percent Chance",
                dataSet, PlotOrientation.VERTICAL, false, true, false);
		
		renderer = new CustomRenderer(
				new Paint[] {Color.red, Color.blue, Color.green,
						Color.yellow, Color.orange, Color.cyan,
						Color.magenta, Color.blue}
				);
        chart.getCategoryPlot().setRenderer(renderer);
		
		pictureWidth = 100 * authors.size();
		
		/**
		 * This (hopefully) allows us to have the image itself be larger than the window if it's too big
		 * to all fit nicely in the screen size.
		 */
		int screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
		this.setMaximumSize(new Dimension(screenWidth - 300, MAX_HEIGHT));
		this.setSize(new Dimension(screenWidth - 300, MAX_HEIGHT));
		
		drawingPanel.setPreferredSize(new Dimension(pictureWidth, 478));
		chartImage = chart.createBufferedImage(pictureWidth, 478);
	}
	
	/**
	 * Returns a BufferedImage that should be used for the results button in the main window.<br><br>
	 * 
	 * MUST be called after makeChart() otherwise it will be displaying the old results image and not
	 * a new image of the newly obtained results, or even worse chart will be null!
	 */
	protected Icon getButtonIcon() {
		Logger.logln(NAME+"Obtaining Results button icon");
		
		return ImageLoader.getIcon(RESULTS_ICON);
	}
	
	/**
	 * Add an attribute to the data set which consists of the author and their percent
	 * probability of being the document to anonymize author.
	 * 
	 * @param author
	 * 		The author name
	 * @param percentage
	 * 		The author's percent chance of owning the test document
	 */
	public void addAttrib(String author, int percentage) {
		if (!authors.contains(author)) {
			authors.add(author);
			percent.add((Integer)percentage);
		}
	}
	
	/**
	 * Checks to see if the author and percentage data has been acquired and returns the appropriate boolean.
	 * 
	 * @return
	 * 		Whether or not the chart has been created (i.e., do we have any authors)
	 */
	public Boolean isReady() {
		if (authors.size() == 0)
			return false;
		else
			return true;
	}
	
	/**
	 * Returns the number of authors currently included in the results data
	 * 
	 * @return
	 * 		The number of authors currently included in the results data
	 */
	public int getAuthorSize() {
		return authors.size();
	}
	
	/**
	 * Resets all values and clears all graphs, to be used for reprocessing to
	 * make sure everything's new
	 */
	public void reset() {
		authors.clear();
		percent.clear();
		chartImage = null;
	}
	
	/**
	 * Checks to see if the author and percentage data has been acquired and the data's ready
	 * to be made into a chart
	 * 
	 * @return
	 * 		Whether or not the chart has been created (i.e., do we have any authors)
	 */
	public boolean resultsAreReady() {
		boolean ready = true;

		try {
			if (authors.size() == 0)
				ready = false;
		} catch (Exception e) {
			ready = false;
		}

		return ready;
	}
	
	/**
	 * Big thanks to JFreeChart, Object Refinery Limited and Contributors, and MaVRoSCy from StackOverflow for
	 * the help.
	 * 
     * A custom renderer that returns a different color for each item in a single series.
     */
    class CustomRenderer extends BarRenderer {

		private static final long serialVersionUID = 1L;
        private Paint[] colors;

        /**
         * Creates a new renderer.
         *
         * @param colors  the colors.
         */
        public CustomRenderer(final Paint[] colors) {
            this.colors = colors;
        }

        /**
         * Returns the paint for an item.  Overrides the default behavior inherited from
         * AbstractSeriesRenderer.
         *
         * @param row
         * 		The series.
         * @param column
         * 		The category.
         *
         * @return
         * 		The item color.
         */
        public Paint getItemPaint(final int row, final int column) {
            return this.colors[column % this.colors.length];
        }
    }
}
