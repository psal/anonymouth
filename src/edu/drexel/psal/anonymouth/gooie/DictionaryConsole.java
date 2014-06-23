package edu.drexel.psal.anonymouth.gooie;
import java.awt.Color;
import java.awt.Component;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.SwingUtilities;

import edu.drexel.psal.jstylo.generics.Logger;


/**
 * Extends JFrame and allows searching Princeton's Wordnet for both sets of synonyms, as well as words that include specified character grams. 
 * 
* This code was edited or generated using CloudGarden's Jigloo
* SWT/Swing GUI Builder
* 
* @author Andrew W.E. McDonald
*/
public class DictionaryConsole extends javax.swing.JFrame {
	
	private final String NAME = "( "+this.getClass().getName()+" ) - ";
	
	protected JTabbedPane viewerTP;
	protected JLabel componentNameLabel;
	protected JScrollPane jScrollPane1;
	protected JScrollPane jScrollPane2;
	protected JLabel gramSearchLabel;
	protected JLabel wordSearchLabel;
	protected JLabel gramStartLabel;
	protected JButton gramSearchButton;
	protected JButton wordSearchButton;
	protected JButton gramStartSearchButton;
	protected JTextField gramField;
	protected JTextField wordField;
	protected JTextField gramStartField;
	protected JTextArea displayArea;
	protected JPanel viewer;
	protected JButton closeButton;
	protected JButton notFound;

	{
		//Set Look & Feel
		try {
			javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	* Auto-generated main method to display this JFrame

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				DictionaryConsole inst = new DictionaryConsole();
				inst.setLocationRelativeTo(null);
				inst.setVisible(true);
			}
		});
	}
	
*/	
	public DictionaryConsole() {
		super();
		initGUI();
	}
	
	 void initGUI() {
		try {
			Logger.logln(NAME+"Dictionary GUI intializing");
			GroupLayout thisLayout = new GroupLayout((JComponent)getContentPane());
			getContentPane().setLayout(thisLayout);
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			{
				viewerTP = new JTabbedPane();
				viewerTP.setBackground(new java.awt.Color(255,255,255));	
			}
			{
				wordField = new JTextField();
				wordField.setText("word");
				wordField.setForeground(Color.gray);
			}
			{
				gramField = new JTextField();
				gramField.setText("char gram (e.g. ns )");
				gramField.setForeground(Color.gray);
			}
			{
				wordSearchButton = new JButton();
				wordSearchButton.setText("get synonyms");
			}
			
			{
				gramSearchButton = new JButton();
				gramSearchButton.setText("get words");
			}
			{
				gramStartSearchButton = new JButton();
				gramStartSearchButton.setText("get words");
			}
			{
				gramStartField = new JTextField();
				gramStartField.setText("char gram (e.g. ns )");
				gramStartField.setForeground(Color.gray);
			}
			{
				componentNameLabel = new JLabel();
				componentNameLabel.setText("Word Finder");
				componentNameLabel.setFont(new java.awt.Font("Lucida Grande",0,12));
			}
			{
				wordSearchLabel = new JLabel();
				wordSearchLabel.setText("find synonyms for a word/phrase");
			}
			{
				gramStartLabel = new JLabel();
				gramStartLabel.setText("find words starting with a specified string");
			}
			{
				gramSearchLabel = new JLabel();
				gramSearchLabel.setText("find words containing a specified string");
			}
			{
				notFound = new JButton();
				notFound.setText("Not Found! Help!");
			}
			{
				closeButton = new JButton();
				closeButton.setText("close");
			}
			thisLayout.setVerticalGroup(thisLayout.createSequentialGroup()
				.addContainerGap()
				.addGroup(thisLayout.createParallelGroup()
				    .addGroup(GroupLayout.Alignment.LEADING, thisLayout.createSequentialGroup()
//				        .addComponent(componentNameLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				        .addGap(0, 18, Short.MAX_VALUE)) //18
				    .addGroup(thisLayout.createSequentialGroup()
				        .addGap(0, 24, Short.MAX_VALUE)//24
				        .addGroup(thisLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				            .addComponent(gramSearchLabel, GroupLayout.Alignment.TRAILING, 0, 19, Short.MAX_VALUE)
				            .addComponent(gramStartLabel, GroupLayout.Alignment.TRAILING, 0, 19, Short.MAX_VALUE)
				            .addComponent(wordSearchLabel, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))))
				.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
				.addGroup(thisLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				    .addComponent(wordField, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
				    .addComponent(gramStartField, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
				    .addComponent(gramField, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
				.addGroup(thisLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
				    .addComponent(wordSearchButton, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				    .addComponent(gramStartSearchButton, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				    .addComponent(gramSearchButton, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
				.addComponent(viewerTP, GroupLayout.PREFERRED_SIZE, 314, GroupLayout.PREFERRED_SIZE)
				.addGroup(thisLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				    .addComponent(closeButton, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				    .addComponent(notFound, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addContainerGap());
			thisLayout.setHorizontalGroup(thisLayout.createParallelGroup()
				.addComponent(viewerTP, GroupLayout.Alignment.LEADING, 0, 701, Short.MAX_VALUE)//701
				.addGroup(thisLayout.createSequentialGroup()
				    .addPreferredGap(viewerTP, notFound, LayoutStyle.ComponentPlacement.INDENT)
				    .addGroup(thisLayout.createParallelGroup()
				        .addGroup(GroupLayout.Alignment.LEADING, thisLayout.createSequentialGroup()
				            .addComponent(notFound, GroupLayout.PREFERRED_SIZE, 142, GroupLayout.PREFERRED_SIZE)//342
				            )
				        .addGroup(GroupLayout.Alignment.LEADING, thisLayout.createSequentialGroup()
				            .addGroup(thisLayout.createParallelGroup()
				                .addComponent(wordSearchLabel, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 230, GroupLayout.PREFERRED_SIZE)
						  //      .addComponent(wordSearchLabel, GroupLayout.Alignment.LEADING, 0, 260, Short.MAX_VALUE)  
				            	.addGroup(thisLayout.createSequentialGroup()
//				                	.addGap(24)
				                    .addGroup(thisLayout.createParallelGroup()
				                        .addComponent(wordField, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 172, GroupLayout.PREFERRED_SIZE)
				                        .addGroup(GroupLayout.Alignment.LEADING, thisLayout.createSequentialGroup()
				                            .addGap(21)
				                            .addComponent(wordSearchButton, GroupLayout.PREFERRED_SIZE, 129, GroupLayout.PREFERRED_SIZE)
				                            .addGap(22)))
				                   )) 
//				            .addComponent(componentNameLabel, GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE)
				            ))
				    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				    .addGroup(thisLayout.createParallelGroup()
				    	.addGroup(GroupLayout.Alignment.LEADING, thisLayout.createSequentialGroup()
				    		.addComponent(gramStartLabel, GroupLayout.PREFERRED_SIZE, 230, GroupLayout.PREFERRED_SIZE)//220
				    //		.addComponent(gramStartLabel, 0, 260, Short.MAX_VALUE)
				    		.addGap(0,0,Short.MAX_VALUE))
				    	.addGroup(thisLayout.createSequentialGroup()
//				    			.addGap(24)
				    			.addGroup(thisLayout.createParallelGroup()
				    				.addComponent(gramStartField, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 172, GroupLayout.PREFERRED_SIZE)
				    			.addGroup(thisLayout.createSequentialGroup()
				    					.addGap(21)
				    					.addComponent(gramStartSearchButton, GroupLayout.PREFERRED_SIZE, 129, GroupLayout.PREFERRED_SIZE)
				    					.addGap(22)))
				    			.addGap(21))//24
				    )
				    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				    .addGroup(thisLayout.createParallelGroup()
				        .addGroup(thisLayout.createSequentialGroup()
				            .addComponent(gramSearchLabel, GroupLayout.PREFERRED_SIZE, 230, GroupLayout.PREFERRED_SIZE)
				    //    	.addComponent(gramSearchLabel, 0, 260, Short.MAX_VALUE)
				            .addGap(0, 0, Short.MAX_VALUE))
				        .addGroup(thisLayout.createSequentialGroup()
//				            .addGap(44)
				            .addGroup(thisLayout.createParallelGroup()
				                .addGroup(GroupLayout.Alignment.LEADING, thisLayout.createSequentialGroup()
				                    .addComponent(gramField, GroupLayout.PREFERRED_SIZE, 172, GroupLayout.PREFERRED_SIZE)
				                    .addGap(0, 44, Short.MAX_VALUE))
				                .addGroup(thisLayout.createSequentialGroup()
				                    .addGap(21)
				                    .addGroup(thisLayout.createParallelGroup()
				                        .addGroup(GroupLayout.Alignment.LEADING, thisLayout.createSequentialGroup()
				                            .addComponent(gramSearchButton, GroupLayout.PREFERRED_SIZE, 129, GroupLayout.PREFERRED_SIZE)
				                            .addGap(0, 66, Short.MAX_VALUE))
				                        .addGroup(GroupLayout.Alignment.LEADING, thisLayout.createSequentialGroup()
//				                            .addGap(0, 18, Short.MAX_VALUE)// 98
				                            .addComponent(closeButton, GroupLayout.PREFERRED_SIZE, 97, GroupLayout.PREFERRED_SIZE)))))))
				    .addContainerGap(12, 12)));
			thisLayout.linkSize(SwingConstants.VERTICAL, new Component[] {wordField, gramField, gramStartField});
			thisLayout.linkSize(SwingConstants.HORIZONTAL, new Component[] {wordSearchButton, gramSearchButton, gramStartSearchButton, closeButton, notFound});
			thisLayout.linkSize(SwingConstants.HORIZONTAL, new Component[] {wordField, gramField, gramStartField});
			pack();
			this.setSize(751, 499);
			DictionaryBinding.initDictListeners(this);
		} catch (Exception e) {
		    //add your error handling code here
			e.printStackTrace();
		}
	}

	
	public void openWindow() {
		this.setLocationRelativeTo(null); // makes it form in the center of the screen
		this.setVisible(true);
	}
	
}


