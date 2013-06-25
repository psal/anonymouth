package edu.drexel.psal.anonymouth.gooie;

import java.awt.Color;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import net.miginfocom.swing.MigLayout;

/**
 * A modified version of the existing ClusterPanel such that it will be removed from the main window and relocated to it's own window
 * accessed by the pull-down menu.
 * @author Marc Barrowclift
 */
public class ClustersWindow extends JFrame {

	@SuppressWarnings("unused")
	private final String NAME = "( "+this.getClass().getSimpleName()+" ) - ";
	private static final long serialVersionUID = 1L;
	protected ScrollablePanel clusterHolderPanel;
	private JPanel clustersPanel;
	private JScrollPane clusterScrollPane;
	private JLabel clustersLabel;
	protected JScrollPane subFeaturesListScrollPane;
	
	protected ArrayList<ArrayList<String>> subfeatures = new ArrayList<ArrayList<String>>();
	protected ArrayList<String> features = new ArrayList<String>();
	
	public ClustersWindow() {
		init();
		this.setVisible(false);
	}
	
	/**
	 * initializes all the data needed to display the clusters
	 */
	private void init() {
		clustersPanel = new JPanel();
		clustersPanel.setLayout(new MigLayout(
				"wrap, ins 0",
				"grow, fill",
				"0[]0[grow, fill][]0"));
		
		{ // --------------cluster panel components
			clustersLabel = new JLabel("Clusters:");
			clustersLabel.setHorizontalAlignment(SwingConstants.CENTER);
			clustersLabel.setFont(GUIMain.titleFont);
			clustersLabel.setOpaque(true);
			clustersLabel.setBackground(new Color(252,242,206));
			clustersLabel.setBorder(GUIMain.rlborder);
			
			clusterHolderPanel = new ScrollablePanel() {
				private static final long serialVersionUID = 1L;

				public boolean getScrollableTracksViewportWidth() {
					return true;
				}
			};
			clusterHolderPanel.setScrollableUnitIncrement(SwingConstants.VERTICAL, ScrollablePanel.IncrementType.PIXELS, 74);
			clusterHolderPanel.setAutoscrolls(true);
			clusterHolderPanel.setOpaque(true);
			BoxLayout clusterHolderPanelLayout = new BoxLayout(clusterHolderPanel, javax.swing.BoxLayout.Y_AXIS);
			clusterHolderPanel.setLayout(clusterHolderPanelLayout);
			clusterScrollPane = new JScrollPane(clusterHolderPanel);
			clusterScrollPane.setOpaque(true);
			
			clustersPanel.add(clusterScrollPane, "growy");
		}
		
		this.add(clustersPanel);
		this.setSize(400, 860);
		this.setLocationRelativeTo(null);
		this.setTitle("Clusters Viewer");
	}
	
	/**
	 * Displays the window
	 */
	public void openWindow() {
		this.setLocationRelativeTo(null); // makes it form in the center of the screen
		this.setVisible(true);
	}
}
