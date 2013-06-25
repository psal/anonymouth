package edu.drexel.psal.anonymouth.utils;

import java.util.*;
import com.jgaap.generics.Document;
import edu.drexel.psal.jstylo.generics.Logger;

/**
 * Tags documents 
 * @author Andrew W.E. McDonald
 *
 */
public class DocumentTagger {
	
	private final String NAME = "( "+this.getClass().getName()+" ) - ";
	
	public ArrayList<TaggedDocument> tagDocs(List<Document> docs, boolean loadIfExists) throws Exception{
		String currentAuthor;
		String docTitle;
		String fullDoc = "";
		ArrayList<TaggedDocument> outMap = new ArrayList<TaggedDocument>();
		for(Document d:docs){
			currentAuthor = d.getAuthor();
			docTitle = d.getTitle();
			System.out.println("Author: "+currentAuthor+" Title: docTitle");
			TaggedDocument td = null;
			/*
			if(ObjectIO.objectExists(currentAuthor+"_"+docTitle,ThePresident.GRAMMAR_DIR) == true && loadIfExists){
				td = ObjectIO.readTaggedDocument(docTitle+"_"+currentAuthor, ThePresident.GRAMMAR_DIR, false);
			}
			else{
			*/
				d.load();
				fullDoc = d.stringify();//.replaceAll("\\p{C}"," ");// get rid of unicode control chars (causes parse errors).
				td = new TaggedDocument(fullDoc,docTitle,currentAuthor);
				/*
				if (ThePresident.SAVE_TAGGED_DOCUMENTS == true)
					td.writeSerializedSelf(ThePresident.GRAMMAR_DIR);
		}
		*/
			outMap.add(td);
		}
		Logger.logln(NAME+"Document set tagged.");
		return outMap;
	}
}