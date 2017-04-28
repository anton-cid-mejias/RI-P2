package es.udc.fic.util;

import java.io.IOException;
import java.io.PrintWriter;

import org.apache.lucene.search.similarities.Similarity;

public class IndexingModelWriter {

    public void write (Similarity similarity){
	
	
	try{
	    PrintWriter writer = new PrintWriter("IndexingModel.txt", "UTF-8");
	    writer.println("The first line");
	    writer.println("The second line");
	    writer.close();
	} catch (IOException e) {
	   // do something
	}
    }
}
