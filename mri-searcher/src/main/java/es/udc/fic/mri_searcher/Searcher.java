package es.udc.fic.mri_searcher;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import es.udc.fic.util.IndexingModelWriter;

public class Searcher {

    //
    public static void run(String index, Boolean indexingModel, int int1,
	    int int2, int cut, int top, List<String> fieldsProcs,
	    List<String> fieldsVisual, int tq, int td, int ndr, int nd, int nw,
	    boolean explain) throws IOException, ParseException {
	
	SimilarityAndColl simColl = IndexingModelWriter
		.readIndexingModel(index, indexingModel);
	Directory indir = FSDirectory.open(Paths.get(index));
	DirectoryReader reader = DirectoryReader.open(indir);
	IndexSearcher searcher = new IndexSearcher(reader);
	searcher.setSimilarity(simColl.getSimilarity());

	Path path = Paths.get(simColl.getColl(), "cran.qry");
	InputStream stream = Files.newInputStream(path);
	List<List<String>> queries = CranQueryParser
		.parseString(new StringBuffer(toString(stream)));

	path = Paths.get(simColl.getColl(), "cranqrel");
	stream = Files.newInputStream(path);
	List<QueryNumberRelevanceDoc> queriesRelevance = CranRelParser
		.parseString(new StringBuffer(toString(stream)));

	int lastQuery = queries.size();
	if (int1 == 0) {
	    int1 = 1;
	    int2 = lastQuery;
	} else if (int2 == 0) {
	    int2 = int1;
	} else {
	    if (int1 > lastQuery || int2 > lastQuery) {
		System.out.println("Last query number is " + lastQuery
			+ " use a different range of queries");
		System.exit(1);
	    }
	}

	String[] fieldsProcsArray = fieldsProcs
		.toArray(new String[fieldsProcs.size()]);
	MultiFieldQueryParser parser = new MultiFieldQueryParser(
		fieldsProcsArray, new StandardAnalyzer());

	Iterator<List<String>> itr = queries.iterator();
	List<String> actualQuery = null;
	Query query = null;
	TopDocs topDocs = null;
	List<ScoreDoc> scoreDocs = null;
	List<Integer> queryDocs = null;
	RelevantDocumentsAndMetrics relDocsandMetrics = null;
	List<Integer> relevantDocs = null;

	// Metrics
	float[] precision = null;
	float[] recall = null;
	float fullPrecision10 = 0;
	float fullPrecision20 = 0;
	float fullRecall10 = 0;
	float fullRecall20 = 0;
	float fullAveragePrecision = 0;
	float realNumberOfQueries = 0;

	for (int i = int1; i <= int2; i++) { 
	    actualQuery = itr.next();
	    realNumberOfQueries++;
	    query = parser.parse(actualQuery.get(1).replaceAll("\\?", ""));
	    topDocs = searcher.search(query, Math.max(20, Math.max(cut, top)));
	    scoreDocs = Arrays.asList(topDocs.scoreDocs);

	    queryDocs = new ArrayList<Integer>();
	    for (ScoreDoc scoreDoc : scoreDocs) {
		queryDocs.add(scoreDoc.doc +1);
	    }
	    relDocsandMetrics = BasicMetrics.relevanceHits(i, queryDocs,
		    queriesRelevance);
	    relevantDocs = relDocsandMetrics.getRelevantDocs();

	    // Iterating queries
	    System.out.println("Query number " + i + ": " + query.toString());
	    printScoreDocInfo(top, scoreDocs, reader, fieldsVisual,
			relevantDocs);
	    // Printing metrics
	    precision = relDocsandMetrics.getPrecision();
	    recall = relDocsandMetrics.getRecall();
	    printQueryMetrics(precision, recall);
	    fullPrecision10 += precision[0];
	    fullPrecision20 += precision[1];
	    fullRecall10 += recall[0];
	    fullRecall20 += recall[1];
	    fullAveragePrecision += relDocsandMetrics.getAveragePrecision();

	}

	if (realNumberOfQueries != 0) {
	    printMeanQueryMetrics(realNumberOfQueries, fullPrecision10,
		    fullPrecision20, fullRecall10, fullRecall20,
		    fullAveragePrecision);
	}

	if ((tq > 0) && (td > 0) && (ndr > 0)) {
	    // rf1
	}
	if ((tq < 0) && (td < 0) && (ndr > 0)) {
	    // rf2
	}
	
	if ((nd > 0) && (nw > 0)){
	    if (indexingModel) {
		//prfjm
	    }else{
		//prfdir
	    }
	}

    }

    private static String toString(InputStream stream) throws IOException {
	ByteArrayOutputStream result = new ByteArrayOutputStream();
	byte[] buffer = new byte[1024];
	int length;
	while ((length = stream.read(buffer)) != -1) {
	    result.write(buffer, 0, length);
	}
	return result.toString("UTF-8");
    }

    private static void printScoreDocInfo(int top, List<ScoreDoc> scoreDocs,
	    DirectoryReader reader, List<String> fieldsVisual,
	    List<Integer> relevantDocs) throws IOException {
	int j = 0;
	for (ScoreDoc scoreDoc : scoreDocs) {
	    j++;
	    if (j > top){
		return;
	    }
	    Document doc = reader.document(scoreDoc.doc);
	    System.out.println("Document number " + j + ": ");
	    System.out.println("------");
	    for (String field : fieldsVisual) {
		System.out.println(field + ": " + doc.get(field));
	    }
	    System.out.println("------");
	    System.out.println("Score: " + scoreDoc.score);
	    System.out.print("Relevant: ");
	    if (relevantDocs.contains(scoreDoc.doc)) {
		System.out.println("yes");
	    } else {
		System.out.println("no");
	    }
	    System.out.println();
	}
    }

    private static void printQueryMetrics(float[] precision, float[] recall) {
	System.out.println("Query metrics:");
	System.out.println("P@10 = " + precision[0]);
	System.out.println("P@20 = " + precision[1]);
	System.out.println("Recall@10 = " + recall[0]);
	System.out.println("Recall@20 = " + recall[1]);
	System.out.println();
	System.out.println();
    }

    private static void printMeanQueryMetrics(float realNumberOfQueries,
	    float fullPrecision10, float fullPrecision20, float fullRecall10,
	    float fullRecall20, float fullAveragePrecision) {
	System.out.println(
		"Mean p@10: " + (fullPrecision10 / realNumberOfQueries));
	System.out.println(
		"Mean p@20: " + (fullPrecision20 / realNumberOfQueries));
	System.out.println(
		"Mean recall@10: " + (fullRecall10 / realNumberOfQueries));
	System.out.println(
		"Mean recall@10: " + (fullRecall20 / realNumberOfQueries));
	System.out.println(
		"MAP: " + (fullAveragePrecision / realNumberOfQueries));
    }
}
