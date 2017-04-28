package es.udc.fic.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.Similarity;

public class IndexingModelWriter {

    public static void writeIndexingModel(Boolean indexingModel, Float modelNumber, String index) {
	if (indexingModel == null) {
	    write("default", null, index);
	} else if (indexingModel) {
	    write("jm", modelNumber, index);
	} else {
	    write("dir", modelNumber, index);
	}

    }

    public static void write(String indexingModel, Float modelNumber, String index) {
	PrintWriter writer = null;
	try {
	    Path path = Paths.get(index, "IndexingModel.txt");
	    writer = new PrintWriter(path.toString(), "UTF-8");
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	} catch (UnsupportedEncodingException e) {
	    e.printStackTrace();
	}
	writer.println(indexingModel);
	if (modelNumber != null) {
	    writer.println(modelNumber);
	}
	writer.close();
    }
    
    public static Similarity readIndexingModel(String index) {
	Similarity similarity = new BM25Similarity();
	
	Path indexingModel = Paths.get(index, "IndexingModel.txt");
	Scanner in = null;
	try {
	    in = new Scanner(new FileReader(indexingModel.toString()));
	} catch (FileNotFoundException e) {
	    System.out.println("The indexing model file was not found");
	    e.printStackTrace();
	}
	String model = in.next();
	if (model.equals("default")){
	    //nothing
	} else if (model.equals("jm")){
	    similarity = new LMJelinekMercerSimilarity(in.nextFloat());
	} else if (model.equals("dir")){
	    similarity = new LMDirichletSimilarity(in.nextFloat());
	} else {
	    System.out.println("The indexing model file was wrong");
	    System.exit(1);
	}
	
	return similarity;
    }

}
