package edu.drexel.psal.anonymouth.gooie;
//
// A simple Java Console for your application (Swing version)
// Requires Java 1.1.5 or higher
//
// Disclaimer the use of this source is at your own risk. 
//
// Permision to use and distribute into your own applications
//
// RJHM van den Bergh , rvdb@comweb.nl

import edu.drexel.psal.ANONConstants;
import edu.drexel.psal.jstylo.generics.Logger;
import edu.drexel.psal.jstylo.generics.Logger.LogOut;

import java.io.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

/**
 * allows viewing of stdout and stderr in console window.
 * @author Andrew W.E. McDonald, modified from RJHM van den Bergh's 'simple Java Console'
 *
 */
public class Console extends WindowAdapter implements WindowListener, ActionListener, Runnable {
	
	private final String NAME = "( "+this.getClass().getName()+" ) - ";
	
	private JFrame frame;
	private Thread reader;
	private Thread reader2;
	private boolean quit;
	private JScrollPane viewerPane;
	private JButton verboseCloseButton;
	private JTextArea verboseTextArea;
	private JButton verboseSaveButton;
					
	private final PipedInputStream pin=new PipedInputStream(); 
	private final PipedInputStream pin2=new PipedInputStream(); 

	
	public Console()
	{
		// create all components and add them
		frame=new JFrame();
		frame.setBackground(new Color(0,0,0,125));
		
		try {
			GroupLayout thisLayout = new GroupLayout((JComponent)frame.getContentPane());
			frame.getContentPane().setLayout(thisLayout);
			frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			frame.setTitle("Anonymouth - Verbose Output");
			{
				viewerPane = new JScrollPane();
				viewerPane.setAutoscrolls(true);
				{
					verboseTextArea = new JTextArea();
					viewerPane.setViewportView(getVerboseTextArea());
					verboseTextArea.setText("");
					verboseTextArea.setEditable(false);
					verboseTextArea.setAutoscrolls(true);
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
			frame.pack();
			frame.setSize(616, 390);
		frame.setVisible(true);		
		
		frame.addWindowListener(this);		
		verboseCloseButton.addActionListener(this);
		verboseSaveButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser save = new JFileChooser();
				save.addChoosableFileFilter(ANONConstants.TXT);
				int answer = save.showSaveDialog(frame);
				
				if (answer == JFileChooser.APPROVE_OPTION) {
					File f = save.getSelectedFile();
					String path = f.getAbsolutePath();
					if (!path.toLowerCase().endsWith(".txt"))
						path += ".txt";
					try {
						BufferedWriter bw = new BufferedWriter(new FileWriter(path));
						bw.write(verboseTextArea.getText());
						bw.flush();
						bw.close();
						Logger.log("Saved contents of console to "+path);
					} catch (IOException exc) {
						Logger.logln(NAME+"Failed opening "+path+" for writing",LogOut.STDERR);
						Logger.logln(NAME+exc.toString(),LogOut.STDERR);
						JOptionPane.showMessageDialog(null,
								"Failed saving contents of console into:\n"+path,
								"Save Problem Set Failure",
								JOptionPane.ERROR_MESSAGE);
					}
				} else {
		            Logger.logln(NAME+"Save contents of console canceled");
		        }
			}
		
		});
		}
		catch(Exception e){
			e.printStackTrace();
		}
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
		reader=new Thread(this);
		reader.setDaemon(true);	
		reader.start();	
		//
		reader2=new Thread(this);	
		reader2.setDaemon(true);	
		reader2.start();
				
	}
	
	public synchronized void windowClosed(WindowEvent evt)
	{
		quit=true;
		this.notifyAll(); // stop all threads
		try { reader.join(1000);pin.close();   } catch (Exception e){}		
		try { reader2.join(1000);pin2.close(); } catch (Exception e){}
		EditorDriver.consoleDead = true;
	}		
		
	public synchronized void windowClosing(WindowEvent evt)
	{
		frame.setVisible(false); // default behaviour of JFrame	
		frame.dispose();
	}
	
	
	
	public synchronized void actionPerformed(ActionEvent evt)
	{
		//verboseTextArea.setText("");
		frame.setVisible(false);
		frame.dispose();
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
					verboseTextArea.setCaretPosition(verboseTextArea.getDocument().getLength());
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
					verboseTextArea.setCaretPosition(verboseTextArea.getDocument().getLength());
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
		
	public static void main(String[] arg)
	{
		new Console(); // create console with not reference	
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
}