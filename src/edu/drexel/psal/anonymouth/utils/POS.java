package edu.drexel.psal.anonymouth.utils;

import java.io.Serializable;

import edu.drexel.psal.jstylo.generics.Logger;

/**
 * Mapping between POS tags and their description, taken from the Penn Treebank Project
 * @author Andrew W.E. McDonald
 *
 */
public class POS implements Serializable {
	
	private final String NAME = "( "+this.getClass().getName()+" ) - ";

	/**
	 * Enumeration of the POS tags used by the Standford MaxentTagger /Penn Treebank. These are used as input to the method
	 * 'tagToDescription'
	 */
	public enum TheTags {CC,CD,DT,EX,FW,IN,JJ,JJR,JJS,LS,MD,NN,NNS,NNP,NNPS,PDT,POS,PRP,PRP$,RB,RBR,
		RBS,RP,SYM,TO,UH,VB,VBD,VBG,VBN,VBP,VBZ,WDT,WP,WP$,WRB,}
	
	/**
	 * Accepts an enumerated tag value from 'TheTags' enumeration, and returns the description of that tag
	 * @param tag the enumerated POS tag value from the enumeration 'TheTags'
	 * @return
	 * 	String that describes the input tag, or the input tag itself as a string if the tag wasn't found.
	 */
	public static String tagToDescription(TheTags tag){
		switch(tag){
		case CC: return "Coordinating conjunction";
		case CD: return "Cardinal number";
		case DT: return "Determiner";
		case EX: return "Existential 'there'";
		case FW: return "Foreign word";
		case IN: return "Preposition or subordinating conjunction";
		case JJ: return "Adjective";
		case JJR: return "Adjective, comparative";
		case JJS: return "Adjective, superlative";
		case LS: return "List item marker";
		case MD: return "Model";
		case NN: return "Noun, singular or mass";
		case NNS: return "Noun, plural";
		case NNP: return "Proper noun, singular";
		case NNPS: return "Proper noun, plural";
		case PDT: return "Predeterminer";
		case POS: return "Possesive ending";
		case PRP: return "Personal pronoun";
		case PRP$: return "Possesive pronoun";
		case RB: return "Adverb";
		case RBR: return "Adverb, comparative";
		case RBS: return "Adverb, superlative";
		case RP: return "Particle";
		case SYM: return "Symbol";
		case TO: return "'to'";
		case UH: return "Interjection";
		case VB: return "Verb, base form";
		case VBD: return "Verb, past tense";
		case VBG: return "Verb, gerund or present participle";
		case VBN: return "Verb, past participle";
		case VBP: return "Verb, non-3rd person singular present";
		case VBZ: return "Verb, 3rd person singular present";
		case WDT: return "Wh-determiner";
		case WP: return "Wh-pronoun";
		case WP$: return "Possesive wh-pronoun";
		case WRB: return "Wh-adverb";
		
		}
		return tag.toString(); // This can't happen.
	}
	
	public static String tagToString(String tag){
		Logger.logln("(POS) - Tag: "+tag);
		String pos="";
		if(tag.startsWith("NN")){
			return "noun";
		}
		else if(tag.startsWith("JJ")){
			return "adj.";
		}
		else if(tag.startsWith("V")){
			return "verb";
		}
		else if(tag.startsWith("RB")){
			return "adv.";
		}
		return pos;		
	}
}


