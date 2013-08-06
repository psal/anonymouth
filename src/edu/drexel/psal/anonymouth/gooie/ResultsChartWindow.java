package edu.drexel.psal.anonymouth.gooie;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 * Displays an enlarged, more detailed version of the results graph shown in the main window.
 * 
 * TODO Make Results window organized, consolidate into ResultsWindow and ResultsDriver
 * @author Marc Barrowclift
 *
 */
public class ResultsChartWindow extends JDialog {

	private static final long serialVersionUID = 1L;
	private GUIMain main;
	private Font labelFont;
	private BufferedImage chartImage;
	private JFreeChart chart;
	private BufferedImage panelImage;
	private JFreeChart panelChart;
	protected JPanel drawingPanel;
	protected JLabel resultsLabel;
	private ArrayList<String> authors;
	private ArrayList<Integer> percent;
	private DefaultCategoryDataset dataSet;
	private CategoryItemRenderer renderer;
	private int width;
	
	/**
	 * Constructor
	 * @param main - An instantace of GUIMain
	 */
	public ResultsChartWindow(GUIMain main) {
		super(main, "Process Results", Dialog.ModalityType.APPLICATION_MODAL);
		this.main = main;
		init();
		this.setVisible(false);
	}
	
	/**
	 * Displays the window
	 */
	public void openWindow() {
		this.setLocationRelativeTo(null); // makes it form in the center of the screen
		this.setVisible(true);
		this.repaint();
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
         * Returns the paint for an item.  Overrides the default behaviour inherited from
         * AbstractSeriesRenderer.
         *
         * @param row  the series.
         * @param column  the category.
         *
         * @return The item color.
         */
        public Paint getItemPaint(final int row, final int column) {
            return this.colors[column % this.colors.length];
        }
    }

	/**
	 * Should be called only by the resultsMainPanel to get the appropriately sized image for it to display.
	 * @param width - the width of the resultsMainPanel panel
	 * @param height - the height the developer is willing the scroll panel to have
	 * @return panelImage - A BufferedImage of the chart
	 */
	public BufferedImage getPanelChart(int width, int height) {
		if (panelImage == null) {
			panelChart = ChartFactory.createBarChart(
					null, null, null,
	                dataSet, PlotOrientation.HORIZONTAL, false, true, false);
			panelChart.getCategoryPlot().setRenderer(renderer);
			
			panelImage = panelChart.createBufferedImage(width, height);
		}
		
		return panelImage; 
	}
	
	/**
	 * Makes the data set and main chart from the data given. Must be called first before painting any windows or panels.
	 */
	public void makeChart() {
		dataSet = new DefaultCategoryDataset();
		
		for (int i = 0; i < authors.size(); i++)
			dataSet.setValue(percent.get(i).intValue(), "", authors.get(i));
		
		chart = ChartFactory.createBarChart(
				"Chance of Documents Ownership", "Authors", "Percent Chance",
                dataSet, PlotOrientation.VERTICAL, false, true, false);
		
		renderer = new CustomRenderer(
				new Paint[] {Color.red, Color.blue, Color.green,
						Color.yellow, Color.orange, Color.cyan,
						Color.magenta, Color.blue}
				);
        chart.getCategoryPlot().setRenderer(renderer);
		
		if (authors.size() < 10)
			width = 100 * authors.size();
		else
			width = 1000;
		
		this.setSize(width, 522);
		chartImage = chart.createBufferedImage(width, 478);
	}
	
	/**
	 * Initializes the data we need.
	 */
	@SuppressWarnings("serial")
	private void init() {
		authors = new ArrayList<String>();
		percent = new ArrayList<Integer>();
		
		drawingPanel = new JPanel(new BorderLayout()) {
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
		
		this.setSize(500, 500);
		this.add(drawingPanel);
		this.setLocationRelativeTo(null);
		this.setTitle("Process Results");
	}
	
	/**
	 * Add an attribute to the data set
	 * @param author - The author name
	 * @param percentage - the author's percent chance of owning the test document
	 */
	public void addAttrib(String author, int percentage) {
		if (!authors.contains(author)) {
			authors.add(author);
			percent.add((Integer)percentage);
		}
	}
	
	/**
	 * Checks to see if the author and percentage data has been acquired and returns the appropriate boolean.
	 * @return
	 */
	public Boolean isReady() {
		if (authors.size() == 0)
			return false;
		else
			return true;
	}
	
	/**
	 * Returns the author ArrayList size
	 * @return
	 */
	public int getAuthorSize() {
		return authors.size();
	}
	
	/**
	 * Resets all values and clears all graphs, to be used for re-processing
	 */
	public void reset() {
		authors.clear();
		percent.clear();
		main.resultsMainPanel.setPreferredSize(new Dimension(175, 110));
		panelImage = null;
	}
	
	protected static void updateResultsPrepColor(GUIMain main) {
		if (main.resultsAreReady())
			main.resultsTableLabel.setBackground(main.ready);
		else
			main.resultsTableLabel.setBackground(main.blue);
	}
}
