package edu.drexel.psal.anonymouth.gooie;

import java.awt.*;

import javax.swing.*;

import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import edu.drexel.psal.jstylo.generics.Logger;

/**
 * Used to display the state of the application when the "Process" button is pressed.
 * NOTE: Currently a structure to accurately track the progress of the process is NOT in place. Initially, we solved this by having
 * the bar be indeterminable, but due to a bug in the JRE 7, the indeterminable progress bar does not update on OS X. As such,
 * Andrew created a quick hack to have the bar cycle to being full then repeat until the process is done.
 * @author Marc Barrowclift
 *
 */
public class ProgressWindow extends JDialog implements PropertyChangeListener, Runnable {

	private static final long serialVersionUID = 1L;
	private GUIMain main;
	protected Thread t;
	private Task task;
	private JLabel firstMessage;
	private String progressMessage;
//	private Boolean continueLoop;
	private JProgressBar editorProgressBar;
	private JLabel editingProgressBarLabel;

	/**
	 * Constructor
	 * @param title - The title of the window
	 * @param main - an instance of GUIMain
	 */
	public ProgressWindow(String title, GUIMain main) {
		super(main, title, null); // MODELESS lets it stay on top, but not block any processes
		this.main = main;
		main.setEnabled(false);
		
		firstMessage = new JLabel("<html><link rel=\"stylesheet\" type=\"text/css\" href=\"mystyles.css\" media=\"screen\" />" +
				"<center>Document currently processing, depending on the size of your dataset and computer this may take " +
				"several minutes. Please wait.</center></html>");
		
		progressMessage = "";
//		continueLoop = true;
		
		editingProgressBarLabel = new JLabel();
		editingProgressBarLabel.setText("<html><center>Editing Progress:</center></html>");
		editingProgressBarLabel.setHorizontalAlignment(SwingConstants.CENTER);
		
		editorProgressBar = new JProgressBar();
		editorProgressBar.setIndeterminate(false);
		
		JPanel progressPanel = new JPanel(new BorderLayout(0, 5));
		progressPanel.add(editingProgressBarLabel, BorderLayout.NORTH);
		progressPanel.add(editorProgressBar, BorderLayout.SOUTH);
		
		JPanel completePanel = new JPanel(new BorderLayout());
		completePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		completePanel.add(firstMessage, BorderLayout.NORTH);
		completePanel.add(progressPanel, BorderLayout.SOUTH);
		
		this.add(completePanel);
		this.setResizable(false);
		this.setSize(320, 150);
		this.setLocationRelativeTo(null);
	}

	/**
	 * The "hack" Andrew made to get around the JRE 7 bug discussed above
	 * @author Andrew W.E. McDonald
	 *
	 */
	class Task extends SwingWorker<Void, Void> {
		@Override
		public Void doInBackground() {
			t = new Thread();
			editingProgressBarLabel.setText(progressMessage);
			editorProgressBar.setEnabled(true);
			int i = 0;
			while (true) {
				for (i = 0; i < 101; i++) {
					editingProgressBarLabel.setText(progressMessage);
					editorProgressBar.setValue(i);
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
//					if (!continueLoop)
//						break;
				}
			}
		}

		@Override
		public void done() {
			Toolkit.getDefaultToolkit().beep();
			setCursor(null); //turn off the wait cursor
		}
	}

	/**
	 * Invoked when task's progress property changes.
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		if ("progress" == evt.getPropertyName()) {
			int progress = (Integer) evt.getNewValue();
			editorProgressBar.setValue(progress);
		} 
	}

	@Override
	public void run() {
		this.setVisible(true);
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		task = new Task();
		task.addPropertyChangeListener(this);
		task.execute();
	}
	
	/**
	 * Sets the progress bar's label
	 * @param progressMessage - whatever you want to display
	 */
	public void setText(String progressMessage) {
		this.progressMessage = progressMessage;
	}
	
	/**
	 * Stops the progress bar and trashes the window
	 */
	public void stop() {
		Logger.logln("Stopping ProgressBar");
		main.setEnabled(true); // to ensure its enabled, even if we didn't disable it to begin with
		//continueLoop = false;
		t.interrupt();
		WindowEvent wev = new WindowEvent(this, WindowEvent.WINDOW_CLOSING);
		Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);
	}
}