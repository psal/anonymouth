package edu.drexel.psal.anonymouth.gooie;

import java.awt.*;
import java.awt.geom.*;

import javax.swing.*;

import edu.drexel.psal.anonymouth.engine.Cluster;

public class ClusterPainter extends JPanel {

	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	private final String NAME = "( "+this.getClass().getName()+" ) - ";
	
	//double[] features = {3.3, 3.5, 2.1, 7.8, 9.5, 5.5, 6.1,14.9, 18.0,19.6};
	
	double xoffset = 20;
	double yoffset;
	double dim = 6;
	double minimum;
	double maximum;
	double authorMin;
	double authorMax;
	double presentValue;
	double scale;
	static int count = 0;
	private int featureNumber;
	//private Color transPurple =new Color(0.75f,0.1f,0.9f,0.55f);
	private Color highlightColor = new Color(0f,1.0f,0,0.6f);	
	private Color transRed = new Color(1.0f,0f,0f,.9f);
	//private Color transBlue = new Color(0f,0f,1.0f,.9f);
	
	Cluster[] clusters;
	int numClusters;
	
	public ClusterPainter(Cluster[] clusters,int featureNumber, double minimum, double maximum, double authorMin, double authorMax, double presentValue) {
		this.clusters = clusters;
		this.numClusters = clusters.length;
		this.minimum = minimum;
		this.maximum = maximum;
		this.authorMin = authorMin;
		this.authorMax = authorMax;
		this.presentValue = presentValue;
		this.featureNumber = featureNumber;
		this.setBackground(Color.WHITE);
	}
	
	public double transform(double value, boolean noOffset) {
		double temp = value - minimum;
		if(noOffset == false)
			temp = temp*scale + xoffset;
		else
			temp = temp*scale;
		return temp;
	}
	
	public String roundToString(int precision, double value) {
		// precision is some multiple of 10
		return  Double.toString(Math.floor(value*precision+.5)/precision);
	}
		
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
				
		setPreferredSize(new Dimension(800,50));
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		int width = getWidth();
		int height = getHeight();
		yoffset = height/2;
		g2.draw(new Line2D.Double(xoffset,yoffset,width-xoffset,yoffset));
		if(maximum <1 )
			maximum = 1.0; // prevent divide by zero, or creating too large a scale by dividing by a decimal.... FIXME - there is probably a better way...
		if(minimum <0)
			minimum = 0;
		scale = (double) ((double)width-2*xoffset)/(maximum-minimum);
		int i=0;
		double transAuthorMin = transform(authorMin,false);
		double transAuthorMax = transform(authorMax,false);
		double transPresentValue = transform(presentValue,false);
		g2.drawString(roundToString(100,minimum),(int)xoffset/2,(int)(.75*yoffset));
		g2.drawString(roundToString(100,maximum),(int)(width-3*xoffset),(int)(.75*yoffset));
		double[][] thisClustersMinsAndMaxes = new double[numClusters][2]; // [min,max] (index == cluster number) => used to update cluster colors
		
		for (i = 0; i < numClusters; i++) {
			Cluster current = clusters[i];
			double maxValue = transform(current.getMaxValue(),false);
			double minValue = transform(current.getMinValue(),false);
			//double centroid = transform(current.getCentroid(),false);
			
			/*
			g2.setPaint(transBlue);
			int j=0;
			Pair[] pRay = current.getElements();
			int lenPRay = pRay.length;
			for(j=0; j < lenPRay; j++){
				double position = transform(pRay[j].value,false);
				g2.fill(new Ellipse2D.Double(position-Math.sqrt(dim), yoffset-Math.sqrt(dim),dim,dim));
			}
			
			g2.setColor(Color.black);
			dim = 4;
			g2.fill(new Ellipse2D.Double(centroid-Math.sqrt(dim), yoffset-Math.sqrt(dim),dim,dim));
			*/
			
			int selectedCluster = ClustersDriver.selectedClustersByFeature[featureNumber];

			/**
			 * this needs to be offset by 1, because a '1' was added to the cluster numbers to avoid a cluster being number '0'.
			 */
			if (i == selectedCluster-1) {
				g2.setColor(highlightColor);
				g2.fill(new Ellipse2D.Double(minValue,yoffset*.68,maxValue - minValue,yoffset*.75));
			}
//			else {
//				g2.setColor(Color.green);
//				g2.draw(new Ellipse2D.Double(minValue,yoffset*.75,maxValue - minValue,yoffset*.5));
//			}
			
			thisClustersMinsAndMaxes[i][0] = minValue;
			thisClustersMinsAndMaxes[i][1] = maxValue;
			
		}
		
		g2.setColor(transRed);
		g2.fill(new Ellipse2D.Double(transAuthorMin,yoffset*.78,transAuthorMax-transAuthorMin,yoffset*.5));
		
		g2.setColor(Color.black);
		dim = 7;
		g2.fill(new Ellipse2D.Double(transPresentValue-Math.sqrt(dim), yoffset-Math.sqrt(dim),dim,dim));
	}
}

