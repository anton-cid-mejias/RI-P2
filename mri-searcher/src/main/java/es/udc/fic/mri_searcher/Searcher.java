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

public class Searcher {

    //
    public static void run(String index, SimilarityAndColl simColl, int int1,
	    int int2, int cut, int top, String[] fieldsProcs,
	    String[] fieldsVisual, int tq, int td, int ndr, int nd, int nw,
	    boolean explain) throws IOException, ParseException {

	List<String> fieldsVisualList = Arrays.asList(fieldsVisual);
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

	int lastQuery = Integer
		.parseInt(queries.get(queries.size() - 1).get(0));
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

	// Parses T or W or both
	MultiFieldQueryParser parser = new MultiFieldQueryParser(fieldsProcs,
		new StandardAnalyzer());

	Iterator<List<String>> itr = queries.iterator();
	List<String> actualQuery = itr.next();
	int actualQueryIndex = Integer.parseInt(actualQuery.get(0));
	Query query = null;
	TopDocs topDocs = null;
	Document doc = null;
	List<ScoreDoc> scoreDocs = null;
	List<Integer> queryDocs = null;
	RelevantDocumentsAndMetrics relDocsandMetrics = null;
	List<Integer> relevantDocs = null;
	int j = 0;

	// Metrics
	float[] precision = null;
	float[] recall = null;
	float fullPrecision10 = 0;
	float fullPrecision20 = 0;
	float fullRecall10 = 0;
	float fullRecall20 = 0;
	float fullAveragePrecision = 0;
	int realNumberOfQueries = 0;

	for (int i = int1; i <= int2; i++) {
	    if (i == actualQueryIndex) {
		realNumberOfQueries++;
		query = parser.parse(actualQuery.get(1));
		topDocs = searcher.search(query, top);
		scoreDocs = Arrays.asList(topDocs.scoreDocs);

		queryDocs = new ArrayList<Integer>();
		for (ScoreDoc scoreDoc : scoreDocs) {
		    queryDocs.add(scoreDoc.doc);
		}
		relDocsandMetrics = BasicMetrics.RelevanceHits(actualQueryIndex,
			queryDocs, queriesRelevance);
		relevantDocs = relDocsandMetrics.getRelevantDocs();

		// Iterating queries
		System.out.println("Query: " + query.toString());
		j = 0;
		for (ScoreDoc scoreDoc : scoreDocs) {
		    j++;
		    doc = reader.document(scoreDoc.doc);
		    System.out.println("Document number " + j + ": ");
		    for (String field : fieldsVisualList) {
			System.out.println(field + ": " + doc.get(field));
		    }
		    System.out.println("Score: " + scoreDoc.score);
		    System.out.print("Relevant: ");
		    if (relevantDocs.contains(scoreDoc.doc)) {
			System.out.println("yes");
		    } else {
			System.out.println("no");
		    }
		}
		// Printing metrics
		precision = relDocsandMetrics.getPrecision();
		recall = relDocsandMetrics.getRecall();
		System.out.println("P@10 = " + precision[0]);
		System.out.println("P@20 = " + precision[1]);
		System.out.println("Recall@10 = " + recall[0]);
		System.out.println("Recall@20 = " + recall[1]);
		System.out.println();
		fullPrecision10 += precision[0];
		fullPrecision20 += precision[1];
		fullRecall10 += recall[0];
		fullRecall20 += recall[1];
		fullAveragePrecision += relDocsandMetrics.getAveragePrecision();

		actualQuery = itr.next();
		actualQueryIndex = Integer.parseInt(actualQuery.get(0));
	    }
	}

	if (realNumberOfQueries != 0) {
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
	
	if((tq > 0) && (td > 0) && (ndr >0)){
	    //rf1
	}
	if((tq < 0) && (td < 0) && (ndr >0)){
	    //rf2
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
}
