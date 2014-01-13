package edu.drexel.psal.jstylo.GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;

import edu.drexel.psal.jstylo.generics.Analyzer;
/**
 * 
 * Created in ClassTabDriver, this popup will allow for the editing of a classifier's arguments, and will
 * display basic information about that classifier to the user.
 * 
 * @author Travis Dutko
 */
public class ClassWizard extends javax.swing.JFrame {
	private static final long serialVersionUID = 1L;
	protected Font defaultLabelFont = new Font("Verdana",0,14); 
	protected static int cellPadding = 5;
	protected static Border defaultBorder = BorderFactory.createLineBorder(Color.BLACK);
	
	//data
	protected ArrayList<Argument> args;
	protected Analyzer tmpAnalyzer;
	protected GUIMain parent;
	protected String analyzerDesc;
	protected String analyzerName;
	
	//panels
	protected JPanel mainPanel;
	protected JPanel descriptionPanel;
	protected JPanel optionsPanel;
	protected JPanel buttonPanel;
	protected JPanel editorPanel;
	
	//description panel
	protected JLabel summaryJLabel;
	protected JTextArea descriptionJTextArea;
	protected JScrollPane descriptionJScrollPane; 
	
	//optionsPanel
	protected ArrayList<JTextField> optionFields; //is used by ClassWizardDriver to set the new option string
	protected JScrollPane optionsJScrollPane;
	
	//button panels
	protected JButton applyJButton;
	protected JButton cancelJButton;
	
	/**
	 * ClassWizard does not have an empty/default constructor. It requires a parent and an Analyzer in order to function.
	 * @param parent 
	 * @param tmpAnalyzer the analyzer to be edited
	 */
	public ClassWizard(GUIMain parent,Analyzer tmpAnalyzer){
		super(tmpAnalyzer.getName());
		this.parent = parent;
		this.tmpAnalyzer = tmpAnalyzer;
		analyzerName = this.tmpAnalyzer.getName();
		analyzerDesc = this.tmpAnalyzer.analyzerDescription();
		optionFields = new ArrayList<JTextField>();
		if (this.tmpAnalyzer.getOptions()!=null)
			initArgumentList(this.tmpAnalyzer.getOptions(),this.tmpAnalyzer.optionsDescription());
		initGUI();
	}

	/**
	 * Initializes a list of Argument objects so that they can be examined/modified.
	 * @param currentOps the current arg string
	 * @param optionsDesc the descriptions and flags for all possible options the classifier can take
	 */
	protected void initArgumentList(String[] currentOps, String[] optionsDesc){
		ArrayList<Argument> argList = new ArrayList<Argument>();
		
		//Go over option flags, creating new Ags and adding them to the argList with their descriptions attached already
		for (int i=0; i<optionsDesc.length;i++){

			//parse the string for the flag and description
			String[] components = optionsDesc[i].split("<ARG>");
			if (components!=null && !(components.length==0) && !components[0].matches("[a-z][A-Z]")){
				String flag = components[0].trim();
				String desc = components[1].trim();
				String[] descParts = desc.split("\\s");
				desc="";
				for (String s:descParts){
					desc+=s+" ";
				}
				desc.trim();
				//used to tidy up some of the longer descriptions
				desc.replaceAll("\\s"," ");
				desc.replaceAll("\\t"," ");
				Argument tempArg = new Argument(flag,desc);
				argList.add(tempArg);
			}
		}
		
		//iterate over the current options, adding the correct ones based on matching the flags
		for (int i=0; i<currentOps.length; i++){
			
			//iterate over the arg list to see if we can find a matching flag
			for (Argument a: argList){
				//if one is found, add the value to that arg
				if (a.getFlag().equalsIgnoreCase(currentOps[i])){
					
					//if the next arg is a flag, this arg is an enable/disable
					if (currentOps[i+1].charAt(0)=='-' && currentOps[i+1].substring(1,2).matches("[a-zA-Z]")){
						a.setValue("<ON/OFF>");
						break;
					} else {
						a.setValue(currentOps[i+1]);
						i++;
						break;
					}
				}
			}
			
		}
		
		//iterate over the argList. Any arg which does not yet have any value for the option gets an empty string
		for (Argument a: argList){
			if (a.getValue()==null || a.getValue()=="" || a.getValue()==" ")
				a.setValue("");
		}
		
		args = argList;
	}
	
	/**
	 * Window contents and format depends on how many options the classifier has
	 */
	protected void initGUI(){
		setPreferredSize(new Dimension(600,800));	
		
		try{
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			{
				
				mainPanel = new JPanel(new BorderLayout(cellPadding,cellPadding));
				mainPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
				add(mainPanel);
				
				{
					// =================
					// Description panel
					// =================
					descriptionPanel = new JPanel(new BorderLayout(cellPadding,cellPadding));
					descriptionPanel.setPreferredSize(new Dimension(550,300));
					descriptionPanel.setBorder(BorderFactory.createCompoundBorder(defaultBorder,BorderFactory.createEmptyBorder(10,10,10,10)));
					

					if (args==null || args.size()<=5){ //if there are no options, make the description the main focus of the window
						mainPanel.add(descriptionPanel,BorderLayout.CENTER);
					}
					else //otherwise the description is delegated to the top of the window
						mainPanel.add(descriptionPanel,BorderLayout.NORTH);
					
					{
						descriptionJTextArea = new JTextArea();
						descriptionJTextArea.setText(analyzerDesc);
						descriptionJTextArea.setEditable(false);
						descriptionJTextArea.setLineWrap(true);
						descriptionJTextArea.setWrapStyleWord(true);						
						descriptionJScrollPane = new JScrollPane(descriptionJTextArea,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
						descriptionJScrollPane.setPreferredSize(new Dimension(500,300));
						descriptionPanel.add(descriptionJScrollPane,BorderLayout.CENTER);
					}
					{
						summaryJLabel = new JLabel();
						summaryJLabel.setText("<html><p>" +
								"<font size=12pt><b>Editing a Classifier</b></font><br>" +
								"To edit the arguments given to the classifier, simply change the values in the text field below.<br>" +
								"If the text field is blank, the arg is optional. You can leave it blank to ignore it, or fill it in with an appropriate value<br>" +
								"Clicking the \"Apply Changes\" button will change the arg string and close the window.<br>"+
								"Clicking the \"Cancel\" button will undo any changes.<br><br>" +
								"NOTE: For arguments which are toggled on/off please use either \"&lt;ON/OFF&gt;\" as the argument<br>" +
								"<br></p></html>");
						descriptionPanel.add(summaryJLabel,BorderLayout.NORTH);
					}
				}
				{
					// =============
					// Options panel
					// =============
					editorPanel = new JPanel(new BorderLayout(cellPadding,cellPadding));
					
					optionsPanel = new JPanel(new GridLayout(0,1,0,0));
					optionsPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
					optionsJScrollPane = new JScrollPane(optionsPanel,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
					optionsJScrollPane.setBorder(defaultBorder);
					
					editorPanel.add(optionsJScrollPane,BorderLayout.CENTER);
					
					if (args!=null && args.size()>0){
						if (args.size()<=3){
							optionsPanel.setPreferredSize(new Dimension(500,150));
							optionsJScrollPane.setPreferredSize(new Dimension(500,125));
							mainPanel.add(editorPanel,BorderLayout.SOUTH);
						}
						else if (args.size()<=5){
							optionsPanel.setPreferredSize(new Dimension(500,200));
							mainPanel.add(editorPanel,BorderLayout.SOUTH);
							optionsJScrollPane.setPreferredSize(new Dimension(500,200));
						}
						else if (args.size()<=8){
							optionsPanel.setPreferredSize(new Dimension(500,600));
							mainPanel.add(editorPanel,BorderLayout.CENTER);
							optionsJScrollPane.setPreferredSize(new Dimension(500,400));
						} else if (args.size()>8){
							optionsPanel.setPreferredSize(new Dimension(500,600));
							mainPanel.add(editorPanel,BorderLayout.CENTER);
							optionsJScrollPane.setPreferredSize(new Dimension(500,500));
						}
						
						//loop through options, adding a new option-description pair for each one
						for (int i=0; i<args.size();i++){
							
							JTextField tempLabel = new JTextField("\n"+args.get(i).getFlag()+" : "+args.get(i).getDescription());
							tempLabel.setPreferredSize(new Dimension(550,30));
							tempLabel.setEditable(false);
							
							JTextField tempField = new JTextField(" "+args.get(i).getValue());
							tempField.setPreferredSize(new Dimension(550,30));
							tempField.setEditable(true);
							 							
							optionsPanel.add(tempLabel);
							optionsPanel.add(tempField);
							optionFields.add(tempField);
						}
						
					}
					
					else{
						mainPanel.add(optionsPanel,BorderLayout.NORTH);
						{
							JLabel temp = new JLabel("<html><font color=\"FF0000\">This analyzer has no options/arguments to edit.</color></html>");
							optionsPanel.add(temp);
						}
					}
						
					{
						// ============
						// Button panel
						// ============
						if (args!=null && args.size()!=0){
							buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
							buttonPanel.setPreferredSize(new Dimension(500,30));
							
							if (args.size()<=5)
								editorPanel.add(buttonPanel,BorderLayout.SOUTH);
							else
								mainPanel.add(buttonPanel,BorderLayout.SOUTH);
							{
								applyJButton = new JButton("Apply Changes");
								buttonPanel.add(applyJButton);
							}
							{
								cancelJButton = new JButton("Cancel");
								buttonPanel.add(cancelJButton);
							}
						}
					}
				}
			}
			
			if (args!=null && args.size()!=0)
				ClassWizardDriver.initListeners(this);
			
			pack();
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * A simple data concatenation class consisting of a flag, a description, and a value. <br>
	 * Used to modify the arguments in the ClassWizard
	 */
	public class Argument{
		
		private String value;
		private String description;
		private String flag;
		
		public Argument (String f,String d){
			setFlag(f);
			setDescription(d);
		}
		
		
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
		public String getDescription() {
			return description;
		}
		public void setDescription(String description) {
			this.description = description;
		}
		public String getFlag() {
			return flag;
		}
		public void setFlag(String flag) {
			this.flag = "-"+flag;
		}
	
	}
	public ArrayList<Argument> getArgs(){
		return args;
	}
}
