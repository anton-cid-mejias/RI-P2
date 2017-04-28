package es.udc.fic.mri_searcher;

import java.util.LinkedList;
import java.util.List;

public class CranQueryParser {
	
    public static List<List<String>> parseString(StringBuffer fileContent) {

	String text = fileContent.toString();
	String[] lines = text.split("\n");

	List<List<String>> documents = new LinkedList<List<String>>();

	/* The word .I identifies the beginning of each article */

	for (int i = 0; i < lines.length; ++i) {
	    if (!lines[i].startsWith(".I"))
		continue;
	    StringBuilder sb = new StringBuilder();
	    while (!lines[i].startsWith(".I")) {
		sb.append(lines[i]);
		i++;
		sb.append("\n");
	    }
	    documents.add(handleDocument(sb.toString()));
	}
	return documents;
    }

    public static List<String> handleDocument(String text) {

	/*
	 * This method returns the Cran article that is passed as text as a
	 * list of fields
	 */

	String i = extract("I", "W", text, true);
	String w = extract("W", "", text, true);

	List<String> document = new LinkedList<String>();
	document.add(i);
	document.add(w);
	return document;
    }

    private static String extract(String startE,String endE, String text, boolean allowEmpty) {

	String startElt = "." + startE;
	String endElt = "." + endE;
	String result;
	
	int startEltIndex = text.indexOf(startElt);
	if (startEltIndex < 0) {
	    if (allowEmpty)
		return "";
	    throw new IllegalArgumentException(
		    "no start, elt=" + startE + " text=" + text);
	}
	int start = startEltIndex + startElt.length();
	int end = text.indexOf(endElt, start);
	if ((end < 0) && (endE.compareTo(" ")!=0))
	    throw new IllegalArgumentException(
		    "no end, elt=" + endE + " text=" + text);
	
	if (endE.compareTo("")==0)
	     result = text.substring(start);
	else result = text.substring(start, end);
	
	return result;
    }

}
