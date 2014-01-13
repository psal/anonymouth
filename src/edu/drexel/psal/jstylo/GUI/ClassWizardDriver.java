package edu.drexel.psal.jstylo.GUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JTextField;

import edu.drexel.psal.jstylo.GUI.ClassWizard.Argument;

public class ClassWizardDriver {

	public static void initListeners(final ClassWizard cw){
		
		//Apply changes
		cw.applyJButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				int i=0;
				ArrayList<Argument> args = cw.getArgs();
				String argString = "";
				
				for (JTextField input : cw.optionFields){
					cw.args.get(i).setValue(input.getText());
					i++;
				}
				
				for (Argument a: args){
					if (a.getValue()==null ||a.getValue().equals("")|| a.getValue().equals(" ")){ //if the arg doesn't have any value at all, don't add it
						;				
						//if the flag is the only arg the classifier takes, just add the flag
					} else if (a.getValue().equalsIgnoreCase("<ON/OFF>")||a.getValue().equalsIgnoreCase("<ON>")||a.getValue().equalsIgnoreCase("<OFF>")){ 
						argString+=(a.getFlag()+" ");
					} else { //if it's a normal arg, add the flag, a space, then the arg
						argString+=(a.getFlag()+" "+a.getValue()+" ");
					}
				}
				
				argString.trim(); //remove trailing whitespace
				String[] argArray = argString.split(" "); //create arg array
			
				//set args and arg string in the GUI
				cw.tmpAnalyzer.setOptions(argArray);
				cw.parent.classAvClassArgsJTextField.setText(edu.drexel.psal.jstylo.GUI.ClassTabDriver.getOptionsStr(cw.tmpAnalyzer.getOptions()));
				cw.dispose();
				
			}		
		});
		
		//Cancel changes
		cw.cancelJButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				cw.dispose();
			}
		});
	}
	
}
