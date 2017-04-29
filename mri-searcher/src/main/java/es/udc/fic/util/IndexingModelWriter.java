package es.udc.fic.util;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.Similarity;

import es.udc.fic.mri_searcher.SimilarityAndColl;

public class IndexingModelWriter {

    public static void writeIndexingModel(Boolean indexingModel, Float modelNumber, String index, String coll) {
	if (indexingModel == null) {
	    write("default", null, index, coll);
	} else if (indexingModel) {
	    write("jm", modelNumber, index, coll);
	} else {
	    write("dir", modelNumber, index, coll);
	}

    }

    public static void write(String indexingModel, Float modelNumber, String index, String coll) {
	PrintWriter writer = null;
	try {
	    Path path = Paths.get(index, "IndexingModel.txt");
	    writer = new PrintWriter(path.toString(), "UTF-8");
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	} catch (UnsupportedEncodingException e) {
	    e.printStackTrace();
	}
	writer.println(coll);
	writer.println(indexingModel);
	if (modelNumber != null) {
	    writer.println(modelNumber);
	}
	
	writer.close();
    }
    
    public static SimilarityAndColl readIndexingModel(String index, Boolean indexingModelSearch) {
	Similarity similarity = new BM25Similarity();
	
	Path indexingModelFile = Paths.get(index, "IndexingModel.txt");
	Scanner in = null;
	try {
	    in = new Scanner(new FileReader(indexingModelFile.toString()));
	} catch (FileNotFoundException e) {
	    System.out.println("The indexing model file was not found");
	    e.printStackTrace();
	}
	String coll = in.next();
	String model = in.next();
	if (model.equals("default")){
	    //nothing
	} else if (model.equals("jm")){
	    //Can't use Dirichlet if JelinekMercer was used to index
	    if (!indexingModelSearch){
		System.out.println("Can't use Dirichlet for searching as"
			+ " JelinekMercer was used to index");
		System.exit(1);
	    }
	    similarity = new LMJelinekMercerSimilarity(in.nextFloat());
	} else if (model.equals("dir")){
	    //Can't use JelinekMercer if Dirichlet was used to index
	    if (indexingModelSearch){
		System.out.println("Can't use JelinekMercer for searching as"
			+ " Dirichlet was used to index");
		System.exit(1);
	    }
	    similarity = new LMDirichletSimilarity(in.nextFloat());
	} else {
	    System.out.println("The indexing model file was wrong");
	    System.exit(1);
	}
	
	return new SimilarityAndColl(coll, similarity);
    }

}
