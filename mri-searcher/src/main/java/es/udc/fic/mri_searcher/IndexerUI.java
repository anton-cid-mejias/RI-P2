package es.udc.fic.mri_searcher;

import java.io.IOException;

import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.Similarity;

import es.udc.fic.util.IndexingModelWriter;

public class IndexerUI {

    public static void main(String[] args) throws IOException {

	if ((args.length == 0) || (args.length > 0
		&& ("-h".equals(args[0]) || "-help".equals(args[0])))) {
	    print_usage_and_exit();
	}

	String openmodeString = null;
	OpenMode openmode = OpenMode.CREATE_OR_APPEND;
	String index = null;
	String coll = null;
	//Null = default, true for jm lambda, false for dir mu
	Boolean indexingModel = null;
	Float modelNumber = null;
	Similarity similarity = new BM25Similarity();

	for (int i = 0; i < args.length; i++) {
	    if ("-openmode".equals(args[i])) {
		openmodeString = args[i + 1];
		if ((!openmodeString.equals("append"))
			&& (!openmodeString.equals("create"))
			&& (!openmodeString.equals("create_or_append"))) {
		    System.err.println(
			    "Openmode must be: append, create or append_or_create.");
		    System.exit(-1);
		}
		i++;
	    } else if ("-index".equals(args[i])) {
		index = args[i + 1];
		i++;
	    } else if ("-coll".equals(args[i])) {
		coll = args[i + 1];
		i++;
	    } else if ("-indexingmodel".equals(args[i])) {
		if ("jm".equals(args[i+1])) {
		    indexingModel = true;
		    modelNumber = Float.valueOf(args[i+2]);
		    i = i+2;
		} else if ("dir".equals(args[i])){
		    indexingModel = false;
		    modelNumber = Float.valueOf(args[i+2]);
		    i = i+2;
		} else {
		    i++;
		}
	    }
	}
	
	if ((index == null) || (coll == null)){
	    System.out.println("Options -index and -coll are required to run this program");
	}

	// Openmode will be create_or_append by default
	if (openmodeString != null) {
	    if (openmodeString.equals("create")) {
		openmode = OpenMode.CREATE;
	    } else if (openmodeString.equals("append")) {
		openmode = OpenMode.APPEND;
	    }
	}
	
	//Cambiar entre default, jm lambda, dir mu
	if (indexingModel != null){ //default
	    if (indexingModel){ //lambda
		similarity = new LMJelinekMercerSimilarity(modelNumber);
	    } else { //mu
		similarity = new LMDirichletSimilarity(modelNumber);
	    }
	}
	
	Indexer.run(openmode, index, coll, similarity);
	IndexingModelWriter.writeIndexingModel(indexingModel, modelNumber, index); 

    }

    private static void print_usage_and_exit() {
	String usage = "Usage: Indexing options [-openmode openmode] [-index pathname] [-coll pathname] "
		+ "[-indexingmodel default| jm lambda | dir mu] \n";
	System.out.println(usage);
	System.exit(0);
    }

}
