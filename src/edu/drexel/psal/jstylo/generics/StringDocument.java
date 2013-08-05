package edu.drexel.psal.jstylo.generics;


import com.jgaap.generics.*;

/**
 * Class for JGAAP Document object from string (rather than a file).
 * 
 * @author Ariel Stolerman
 *
 */
public class StringDocument extends Document
{
	public StringDocument()
	{
		super();
	}
	
	/**
	 * Copy constructor. Can be used to break object references and protect a
	 * Document instance from being modified by other classes.
	 * 
	 * @param document
	 *            The document to be copied
	 */
	public StringDocument(StringDocument document) {
		super(document);
		setText(document.getProcessedText());
	}

	/**
	 * Constructor that takes three arguments: file path, file author, file
	 * title
	 * 
	 * @param text
	 *            The text of the document
	 * @param author
	 *            The author of the document
	 * @param title
	 *            The title of the document
	 * @throws Exception
	 */
	public StringDocument(String text, String author, String title){
		super("string",
				author == null || author.equals("") ? null : author,
				title);
		setText(text.toCharArray());
	}
	
	@Override
	public void load() throws Exception {}
	
	
	/** Returns the full filepath of the current document **/
	@Override
	public String getFilePath() {
		return "";
	}
}
