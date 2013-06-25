package edu.drexel.psal.anonymouth.gooie;

import javax.swing.GroupLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * The code in this class was taken from 'DictionaryConsole', and has been slightly modified to allow continuously creating new JScrollPanes that hold/display
 * the results of searches.
 * @author Andrew W.E. McDonald
 *
 */
public class ViewerTabGenerator {
	
	private final String NAME = "( "+this.getClass().getName()+" ) - ";

	protected JScrollPane jScrollPane1;
	protected JTextArea displayArea;
	protected JPanel viewer;
	
	/**
	 * Generates the JScrollPane along with its parts, using the provided text to set the text of the JTextArea.
	 * @param text - String of text that will fill the JTextArea ... text area
	 * @return
	 * 	the instance of ViewerTabGenerator used to generate the JScrollPane - in order to be able to access the JScrollPane's parameters
	 */
	public ViewerTabGenerator generateTab(String text){
	viewer = new JPanel();
	GroupLayout viewerLayout = new GroupLayout((JComponent)viewer);
	viewer.setLayout(viewerLayout);
	viewer.setPreferredSize(new java.awt.Dimension(618, 307));
	{
		jScrollPane1 = new JScrollPane();
		{
			displayArea = new JTextArea();
			jScrollPane1.setViewportView(displayArea);
			displayArea.setText(text);
			displayArea.setEditable(false);
		}
	}
viewerLayout.setVerticalGroup(viewerLayout.createSequentialGroup()
	.addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 268, GroupLayout.PREFERRED_SIZE));
viewerLayout.setHorizontalGroup(viewerLayout.createSequentialGroup()
		.addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 680, GroupLayout.PREFERRED_SIZE));
	return this;
	}


}
