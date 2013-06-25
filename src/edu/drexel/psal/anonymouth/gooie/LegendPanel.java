package edu.drexel.psal.anonymouth.gooie;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;

import javax.swing.JPanel;

public class LegendPanel extends JPanel {
	
	private final String NAME = "( "+this.getClass().getName()+" ) - ";
	
	private int spacing = 10;
	private int dim = 12;
	private Color transPurple =new Color(0.75f,0.1f,0.9f,0.55f);
	private Color highlightColor = new Color(0f,1.0f,0,0.6f);	
	private Color transRed = new Color(1.0f,0f,0f,.9f);
	private Color transBlue = new Color(0f,0f,1.0f,.9f);
	private Color gr = Color.green;
	private Color blk = Color.black;
	
	
	protected void paintComponent(Graphics g){
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		int xPos = spacing;
		int height = getHeight();
		double yPos = (height/2)-(dim/2);
		int yID = (int) yPos+ 11;
		
		g2.setColor(blk);
		g2.fill(new Ellipse2D.Double(xPos,yPos,dim,dim));
		g2.setColor(blk);
		xPos += dim;
		String id = " = your present value "; 
		g2.drawString(id,xPos,yID);
		
		xPos+= spacing +(id.length()*7);
		g2.setColor(transRed);
		g2.fill(new Ellipse2D.Double(xPos,yPos,dim,dim));
		g2.setColor(blk);
		xPos += dim;
		id = " = your normal range (move black dot away from here) ";
		g2.drawString(id,xPos,yID);
		
		xPos += spacing +(id.length()*7);
		g2.setColor(highlightColor);
		g2.fill(new Ellipse2D.Double(xPos,yPos,dim,dim));
		g2.setColor(blk);
		xPos += dim;
		id = " = a safe zone (move black dot to here) ";
		g2.drawString(id,xPos,yID);
		/*
		xPos += spacing +(id.length()*7);
		Stroke defaultStroke = g2.getStroke();
		Stroke biggerStroke = new BasicStroke(2);
		g2.setStroke(biggerStroke);
		g2.setColor(gr);
		g2.draw(new Ellipse2D.Double(xPos,yPos,dim,dim));
		g2.setStroke(defaultStroke);
		g2.setColor(blk);
		xPos += dim;
		id = " = clusters (non-user)";
		g2.drawString(id,xPos,yID);
		
		xPos += spacing +(id.length()*7);
		g2.setColor(blk);
		g2.setStroke(biggerStroke);
		g2.draw(new Ellipse2D.Double(xPos,yPos,dim,dim));
		g2.setStroke(defaultStroke);
		xPos += dim;
		id = " = cluster element average";
		g2.drawString(id,xPos,yID);
		
		xPos += spacing +(id.length()*7);
		g2.setColor(transBlue);
		g2.fill(new Ellipse2D.Double(xPos,yPos,dim,dim));
		g2.setColor(blk);
		xPos += dim;
		id = " = non-user value";
		g2.drawString(id,xPos,yID);
		*/
				
		
		
	}

}
