package edu.drexel.psal.anonymouth.gooie;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.LayoutStyle;

import javax.swing.WindowConstants;
import javax.swing.SwingUtilities;

import edu.drexel.psal.jstylo.generics.Logger;


/**
* This code was edited or generated using CloudGarden's Jigloo
* SWT/Swing GUI Builder, which is free for non-commercial
* use. If Jigloo is being used commercially (ie, by a corporation,
* company or business for any purpose whatever) then you
* should purchase a license for each developer using Jigloo.
* Please visit www.cloudgarden.com for details.
* Use of Jigloo implies acceptance of these licensing terms.
* A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
* THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
* LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
*/
public class verboseConsole extends javax.swing.JFrame {
	private final String NAME = "( "+this.getClass().getName()+" ) - ";
	
	private final PipedInputStream pin=new PipedInputStream(); 
	private final PipedInputStream pin2=new PipedInputStream(); 
	private Thread reader;
	private Thread reader2;
	private boolean quit;
	
	{
		//Set Look & Feel
		try {
			javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private JScrollPane viewerPane;
	private JButton verboseCloseButton;
	private JTextArea verboseTextArea;
	private JButton verboseSaveButton;

	/**
	* Auto-generated main method to display this JFrame
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				verboseConsole inst = new verboseConsole();
				inst.setLocationRelativeTo(null);
				inst.setVisible(true);
			}
		});
	}
*/	
	public verboseConsole() {
		super();
		initGUI();
	}
	
	private void initGUI() {
		Logger.logln(NAME+"Console initizalized");
		try {
			GroupLayout thisLayout = new GroupLayout((JComponent)getContentPane());
			getContentPane().setLayout(thisLayout);
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			this.setTitle("Anonymouth - Verbose Output");
			{
				viewerPane = new JScrollPane();
				viewerPane.setAutoscrolls(true);
				{
					verboseTextArea = new JTextArea();
					viewerPane.setViewportView(getVerboseTextArea());
					verboseTextArea.setText("*** Verbose Output ***");
					verboseTextArea.setEditable(false);
				}
			}
			{
				verboseCloseButton = new JButton();
				verboseCloseButton.setText("close");
			}
			{
				verboseSaveButton = new JButton();
				verboseSaveButton.setText("save...");
			}
				thisLayout.setVerticalGroup(thisLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(viewerPane, 0, 332, Short.MAX_VALUE)
					.addGroup(thisLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					    .addComponent(verboseCloseButton, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					    .addComponent(verboseSaveButton, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED));
				thisLayout.setHorizontalGroup(thisLayout.createSequentialGroup()
					.addContainerGap(16, 16)
					.addGroup(thisLayout.createParallelGroup()
					    .addComponent(viewerPane, GroupLayout.Alignment.LEADING, 0, 587, Short.MAX_VALUE)
					    .addGroup(GroupLayout.Alignment.LEADING, thisLayout.createSequentialGroup()
					        .addGap(0, 393, Short.MAX_VALUE)
					        .addComponent(verboseSaveButton, GroupLayout.PREFERRED_SIZE, 97, GroupLayout.PREFERRED_SIZE)
					        .addComponent(verboseCloseButton, GroupLayout.PREFERRED_SIZE, 97, GroupLayout.PREFERRED_SIZE)))
					.addContainerGap(13, 13));
			pack();
			this.setSize(616, 390);
			try
			{
				PipedOutputStream pout=new PipedOutputStream(this.pin);
				System.setOut(new PrintStream(pout,true)); 
			} 
			catch (java.io.IOException io)
			{
				verboseTextArea.append("Couldn't redirect STDOUT to this console\n"+io.getMessage());
			}
			catch (SecurityException se)
			{
				verboseTextArea.append("Couldn't redirect STDOUT to this console\n"+se.getMessage());
		    } 
			
			try 
			{
				PipedOutputStream pout2=new PipedOutputStream(this.pin2);
				System.setErr(new PrintStream(pout2,true));
			} 
			catch (java.io.IOException io)
			{
				verboseTextArea.append("Couldn't redirect STDERR to this console\n"+io.getMessage());
			}
			catch (SecurityException se)
			{
				verboseTextArea.append("Couldn't redirect STDERR to this console\n"+se.getMessage());
		    } 		
				
			quit=false; // signals the Threads that they should exit
					
			// Starting two seperate threads to read from the PipedInputStreams				
			//
			reader=new Thread();
			reader.setDaemon(true);	
			reader.start();	
			//
			reader2=new Thread();	
			reader2.setDaemon(true);	
			reader2.start();
					
		} catch (Exception e) {
		    //add your error handling code here
			e.printStackTrace();
		}
	}
	
	public JScrollPane getViewerPane() {
		return viewerPane;
	}
	
	public JButton getVerboseCloseButton() {
		return verboseCloseButton;
	}
	
	public JButton getVerboseSaveButton() {
		return verboseSaveButton;
	}
	
	public JTextArea getVerboseTextArea() {
		return verboseTextArea;
	}
	
	


		
	
		
	
	public synchronized void windowClosed(WindowEvent evt)
	{
		quit=true;
		this.notifyAll(); // stop all threads
		try { reader.join(1000);pin.close();   } catch (Exception e){}		
		try { reader2.join(1000);pin2.close(); } catch (Exception e){}
		//System.exit(0);
	}		
		
	public synchronized void windowClosing(WindowEvent evt)
	{
		this.setVisible(false); // default behaviour of JFrame	
		this.dispose();
	}
	
	public synchronized void actionPerformed(ActionEvent evt)
	{
		verboseTextArea.setText("");
	}

	public synchronized void run()
	{
		try
		{			
			while (Thread.currentThread()==reader)
			{
				try { this.wait(100);}catch(InterruptedException ie) {}
				if (pin.available()!=0)
				{
					String input=this.readLine(pin);
					verboseTextArea.append(input);
				}
				if (quit) return;
			}
		
			while (Thread.currentThread()==reader2)
			{
				try { this.wait(100);}catch(InterruptedException ie) {}
				if (pin2.available()!=0)
				{
					String input=this.readLine(pin2);
					verboseTextArea.append(input);
				}
				if (quit) return;
			}			
		} catch (Exception e)
		{
			verboseTextArea.append("\nConsole reports an Internal error.");
			verboseTextArea.append("The error is: "+e);			
		}

	}
	
	public synchronized String readLine(PipedInputStream in) throws IOException
	{
		String input="";
		do
		{
			int available=in.available();
			if (available==0) break;
			byte b[]=new byte[available];
			in.read(b);
			input=input+new String(b,0,b.length);														
		}while( !input.endsWith("\n") &&  !input.endsWith("\r\n") && !quit);
		return input;
	}	

}
