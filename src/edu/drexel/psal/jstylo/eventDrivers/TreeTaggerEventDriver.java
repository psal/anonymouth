/**
 * 
 */
package edu.drexel.psal.jstylo.eventDrivers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.annolab.tt4j.TokenHandler;
import org.annolab.tt4j.TreeTaggerException;
import org.annolab.tt4j.TreeTaggerWrapper;

import com.jgaap.generics.Document;
import com.jgaap.generics.Event;
import com.jgaap.generics.EventDriver;
import com.jgaap.generics.EventGenerationException;
import com.jgaap.generics.EventSet;

import edu.drexel.psal.jstylo.generics.Logger;
import edu.drexel.psal.jstylo.generics.Logger.LogOut;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

/**
 * This changes words into their parts of speech in a document, based on TreeTagger-a language independent POS tagger.
 * http://www.ims.uni-stuttgart.de/projekte/corplex/TreeTagger/
 * 
 * @author sadiaafroz
 *
 */
public class TreeTaggerEventDriver  extends EventDriver {


	@Override
	public String displayName() {
		return "TreeTagger- A language independent pos tagger";
	}

	@Override
	public String tooltipText() {
		return "TreeTagger- A language independent pos tagger";
	}

	@Override
	public boolean showInGUI() {
		return false;
	}
	
	protected static TreeTaggerWrapper<String> tagger = null;
	//path of the parameter file
    protected static String taggerPath = null; //"tree-tagger-MacOSX-3.2-intel/models/russian.par";
    //path of the tree tagger binary
	protected static String taggerHome = null;
	
	public static String getTaggerHome() {
		return taggerHome;
	}

	public static void setTaggerHome(String taggerHome) {
		TreeTaggerEventDriver.taggerHome = taggerHome;
	}

	public static String getTaggerPath() {
		return taggerPath;
	}

	public static void setTaggerPath(String taggerPath) {
		TreeTaggerEventDriver.taggerPath = taggerPath;
	}

	@SuppressWarnings("static-access")
	@Override
	public EventSet createEventSet(Document doc) {
		final EventSet es = new EventSet(doc.getAuthor());
		char[] text = doc.getProcessedText();
		String stringText = new String(text);
		
		//System.out.println(doc.getFilePath());
		// initialize tagger and return empty event set if encountered a problem
		
		if (tagger == null) {
			tagger = initTagger();
			if (tagger == null) return es;
		}
		try {
		tagger.setModel(taggerPath);
		tagger.setHandler(new TokenHandler<String>()
				{
					public void token(String token, String pos, String lemma)
					{
						//System.out.println(token + "\t" + pos + "\t" + lemma);
						//if(!pos.equals("SENT"))
							es.addEvent(new Event(pos));
					}
				});
		
			tagger.process(stringText.split("\\s"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TreeTaggerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		
		return es;
	}
	
	/**
	 * Initialize the tagger.
	 * @return
	 */
	public static TreeTaggerWrapper<String> initTagger() {
		// Point TT4J to the TreeTagger installation directory. The executable is expected
				// in the "bin" subdirectory - in this example at "/opt/treetagger/bin/tree-tagger"
				System.setProperty("treetagger.home", "tree-tagger-MacOSX-3.2-intel/");
				tagger = new TreeTaggerWrapper<String>();
				try {
					tagger.setModel(taggerPath);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return tagger;
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
