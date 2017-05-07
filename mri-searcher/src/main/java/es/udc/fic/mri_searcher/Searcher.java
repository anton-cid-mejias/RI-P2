package es.udc.fic.mri_searcher;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

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
import es.udc.fic.util.SimilarityAndColl;

public class Searcher {

    //
    public static void run(String index, Boolean indexingModel, int int1,
	    int int2, int cut, int top, List<String> fieldsProcs,
	    List<String> fieldsVisual, int tq, int td, int ndr, int nd, int nw,
	    boolean explain) throws IOException, ParseException {

	SimilarityAndColl simColl = IndexingModelWriter.readIndexingModel(index,
		indexingModel);
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

	List<String> actualQuery = null;
	String queryString = null;
	Query query = null;
	TopDocs topDocs = null;
	List<ScoreDoc> scoreDocs = null;
	RelevantDocumentsAndMetrics relDocsandMetrics = null;

	List<QueryAndResult> queriesAndResults = new ArrayList<>();

	// Metrics
	float[] metrics = null;
	float fullPrecision10 = 0;
	float fullPrecision20 = 0;
	float fullRecall10 = 0;
	float fullRecall20 = 0;
	float fullAveragePrecision = 0;
	float realNumberOfQueries = 0;

	for (int i = int1; i <= int2; i++) {
	    actualQuery = queries.get(i - 1);
	    realNumberOfQueries++;
	    queryString = actualQuery.get(1).replaceAll("\\?", "")
		    .replaceAll("\n", " ");
	    query = parser.parse(queryString);
	    // This was done to extract the words the query used (no stopwords)
	    if (fieldsProcs.contains("T")) {
		queryString = query.toString("T").replaceAll("\\(", "")
			.replaceAll("\\)", "").replaceAll("W:", "");
		queryString = Arrays.stream(queryString.split(" ")).distinct()
			.collect(Collectors.joining(" "));
	    } else if (fieldsProcs.contains("W")) {
		queryString = query.toString("T").replaceAll("\\(", "")
			.replaceAll("\\)", "").replaceAll("T:", "");
		queryString = Arrays.stream(queryString.split(" ")).distinct()
			.collect(Collectors.joining(" "));

	    }
	    topDocs = searcher.search(query, Math.max(20, Math.max(cut, top)));
	    scoreDocs = Arrays.asList(topDocs.scoreDocs);
	    queriesAndResults
		    .add(new QueryAndResult(queryString, i, scoreDocs));

	    metrics = printQueryInfo(i, cut, top, fieldsVisual, query, reader,
		    queriesRelevance, relDocsandMetrics, scoreDocs);

	    fullPrecision10 += metrics[0];
	    fullPrecision20 += metrics[1];
	    fullRecall10 += metrics[2];
	    fullRecall20 += metrics[3];
	    fullAveragePrecision += metrics[4];

	}

	printMeanQueryMetrics(realNumberOfQueries, fullPrecision10,
		fullPrecision20, fullRecall10, fullRecall20,
		fullAveragePrecision);

	// System.out.println("tq: " + tq + " td: " + td + " ndr: " + ndr);
	if ((tq > 0) && (td > 0) && (ndr > 0)) {
	    rf1(int1, int2, tq, td, ndr, reader, searcher, queries, parser,
		    queriesRelevance, cut, top, fieldsVisual, fieldsProcs,
		    explain);
	}

	if ((tq == 0) && (td == 0) && (ndr > 0)) {
	    rf2(ndr, int1, int2, reader, searcher, queries, parser,
		    queriesRelevance, cut, top, fieldsVisual);
	}

	if ((nd > 0) && (nw > 0)) {
	    prfJmOrDir(nd, nw, simColl.getLambdaOrMu(), cut, top, reader,
		    searcher, fieldsVisual, fieldsProcs, queriesRelevance,
		    parser, queriesAndResults, indexingModel, explain);
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

    private static float[] printQueryInfo(int i, int cut, int top,
	    List<String> fieldsVisual, Query query, DirectoryReader reader,
	    List<QueryNumberRelevanceDoc> queriesRelevance,
	    RelevantDocumentsAndMetrics relDocsandMetrics,
	    List<ScoreDoc> scoreDocs) throws IOException {
	List<Integer> relevantDocs = null;
	float[] precision = null;
	float[] recall = null;

	ArrayList<Integer> queryDocs = new ArrayList<Integer>();
	for (ScoreDoc scoreDoc : scoreDocs) {
	    queryDocs.add(scoreDoc.doc + 1);
	}
	relDocsandMetrics = BasicMetrics.relevanceHits(i, queryDocs,
		queriesRelevance, cut);
	relevantDocs = relDocsandMetrics.getRelevantDocs();

	// Iterating queries
	System.out.println("Query number " + i + ": " + query.toString());
	printScoreDocInfo(top, scoreDocs, reader, fieldsVisual, relevantDocs);
	// Printing metrics
	precision = relDocsandMetrics.getPrecision();
	recall = relDocsandMetrics.getRecall();
	printQueryMetrics(precision, recall);

	float[] metrics = new float[5];
	metrics[0] = precision[0];
	metrics[1] = precision[1];
	metrics[2] = recall[0];
	metrics[3] = recall[1];
	metrics[4] = relDocsandMetrics.getAveragePrecision();
	return metrics;
    }

    private static void printScoreDocInfo(int top, List<ScoreDoc> scoreDocs,
	    DirectoryReader reader, List<String> fieldsVisual,
	    List<Integer> relevantDocs) throws IOException {
	int j = 0;
	for (ScoreDoc scoreDoc : scoreDocs) {
	    j++;
	    if (j > top) {
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
	    if (relevantDocs.contains(scoreDoc.doc + 1)) {
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

    private static void rf1(int int1, int int2, int tq, int td, int ndr,
	    DirectoryReader reader, IndexSearcher searcher,
	    List<List<String>> queries, MultiFieldQueryParser parser,
	    List<QueryNumberRelevanceDoc> queriesRelevance, int cut, int top,
	    List<String> fieldsVisual, List<String> fieldsProcs,
	    boolean explain) throws IOException, ParseException {

	List<String> actualQuery = null;
	QueryNumberRelevanceDoc qd = null;
	TopDocs topDocs = null;
	List<ScoreDoc> scoreDocs = null;
	RelevantDocumentsAndMetrics relDocsandMetrics = null;

	// Metrics
	float[] metrics = null;
	float fullPrecision10 = 0;
	float fullPrecision20 = 0;
	float fullRecall10 = 0;
	float fullRecall20 = 0;
	float fullAveragePrecision = 0;
	float realNumberOfQueries = 0;

	List<TermTfIdf> tfIdfList = Processor.getTfIdf(reader, fieldsProcs);
	String[] queryWords;
	Query finalQuery;

	System.out.println();
	System.out.println("Rf1:");
	for (int i = int1; i <= int2; i++) {

	    List<PriorityTerm> priorityList = new ArrayList<>();
	    realNumberOfQueries++;

	    actualQuery = queries.get(i - 1);
	    String query = actualQuery.get(1);
	    queryWords = query.split(" ");

	    for (String term : queryWords) {
		if (term.contains(".")) {
		    continue;
		}
		int counter = 0;
		for (TermTfIdf tfIdf : tfIdfList) {
		    if (tfIdf.getTerm().equals(term)) {
			if (!priorityList.contains(new PriorityTerm(counter,
				term, tfIdf.getIdf()))) {
			    priorityList.add(new PriorityTerm(counter, term,
				    tfIdf.getIdf()));
			}
		    } else {
			counter++;
		    }
		}
	    }
	    Collections.sort(priorityList);

	    StringBuilder newQuery = new StringBuilder();

	    if (!(tq < priorityList.size())) {
		tq = priorityList.size() - 1;
	    }

	    for (int j = 0; j < tq; j++) {
		newQuery.append(priorityList.get(j).getTerm());
		newQuery.append(" ");
	    }

	    List<Integer> docs = new ArrayList<>();
	    qd = queriesRelevance.get(i - 1);

	    if (ndr > (qd.getRelevanceDoc().size() - 1)) {
		ndr = qd.getRelevanceDoc().size() - 1;
	    }
	    for (int j = 0; j < ndr; j++) {
		docs.add(qd.getRelevanceDoc().get(j));
	    }

	    List<DocumentTerm> docList = Processor.getBestTfIdfTerms(tfIdfList,
		    docs);
	    for (int j = 0; j < td; j++) {
		newQuery.append(docList.get(j).getTermString());
		newQuery.append(" ");
	    }

	    finalQuery = parser.parse(newQuery.toString());
	    topDocs = searcher.search(finalQuery,
		    Math.max(20, Math.max(cut, top)));
	    scoreDocs = Arrays.asList(topDocs.scoreDocs);

	    metrics = printQueryInfo(i, cut, top, fieldsVisual, finalQuery,
		    reader, queriesRelevance, relDocsandMetrics, scoreDocs);

	    fullPrecision10 += metrics[0];
	    fullPrecision20 += metrics[1];
	    fullRecall10 += metrics[2];
	    fullRecall20 += metrics[3];
	    fullAveragePrecision += metrics[4];

	    if (explain) {
		explainRf1(priorityList, docList, tq, td);
	    }

	}
	printMeanQueryMetrics(realNumberOfQueries, fullPrecision10,
		fullPrecision20, fullRecall10, fullRecall20,
		fullAveragePrecision);

    }

    private static void explainRf1(List<PriorityTerm> priorityList,
	    List<DocumentTerm> docList, int tq, int td) {
	System.out.println("\nExplain:");
	System.out.println("Query words:");
	for (int i = 0; i < tq; i++) {
	    System.out.println(priorityList.get(i).getTerm() + " idf="
		    + priorityList.get(i).getIdf());
	}
	System.out.println("\nDoc words:");
	for (int j = 0; j < td; j++) {
	    System.out.println(docList.get(j).getTermString() + " tf="
		    + docList.get(j).getTf() + " idf="
		    + docList.get(j).getIdf());
	}
	System.out.println("");
    }

    private static void rf2(int ndr, int int1, int int2, DirectoryReader reader,
	    IndexSearcher searcher, List<List<String>> queries,
	    MultiFieldQueryParser parser,
	    List<QueryNumberRelevanceDoc> queriesRelevance, int cut, int top,
	    List<String> fieldsVisual) throws IOException, ParseException {

	List<String> actualQuery = null;
	Query query = null;
	TopDocs topDocs = null;
	List<ScoreDoc> scoreDocs = null;
	RelevantDocumentsAndMetrics relDocsandMetrics = null;
	QueryNumberRelevanceDoc qd = null;

	// Metrics
	float[] metrics = null;
	float fullPrecision10 = 0;
	float fullPrecision20 = 0;
	float fullRecall10 = 0;
	float fullRecall20 = 0;
	float fullAveragePrecision = 0;
	float realNumberOfQueries = 0;
	String titles = null;

	System.out.println();
	System.out.println("Rf2:");
	for (int i = int1; i <= int2; i++) {
	    realNumberOfQueries++;
	    actualQuery = queries.get(i - 1);

	    titles = "";
	    qd = queriesRelevance.get(i - 1);
	    if (ndr > qd.getRelevanceDoc().size()) {
		ndr = qd.getRelevanceDoc().size();
	    }
	    int ndrCopy = ndr;
	    for (Integer j : qd.getRelevanceDoc()) {
		ndrCopy--;
		titles += " " + searcher.doc(j - 1).get("T");
		if (ndrCopy == 0) {
		    break;
		}
	    }

	    query = parser
		    .parse((actualQuery.get(1) + titles).replaceAll("\\?", ""));
	    topDocs = searcher.search(query, Math.max(20, Math.max(cut, top)));
	    scoreDocs = Arrays.asList(topDocs.scoreDocs);

	    metrics = printQueryInfo(i, cut, top, fieldsVisual, query, reader,
		    queriesRelevance, relDocsandMetrics, scoreDocs);

	    fullPrecision10 += metrics[0];
	    fullPrecision20 += metrics[1];
	    fullRecall10 += metrics[2];
	    fullRecall20 += metrics[3];
	    fullAveragePrecision += metrics[4];

	}

	printMeanQueryMetrics(realNumberOfQueries, fullPrecision10,
		fullPrecision20, fullRecall10, fullRecall20,
		fullAveragePrecision);
    }

    private static void prfJmOrDir(int nd, int nw, float lambdaOrMU, int cut,
	    int top, DirectoryReader reader, IndexSearcher searcher,
	    List<String> fieldsVisual, List<String> fieldsProcs,
	    List<QueryNumberRelevanceDoc> queriesRelevance,
	    MultiFieldQueryParser parser,
	    List<QueryAndResult> queriesAndResults, Boolean indexingModel,
	    boolean explain) throws IOException, ParseException {

	String queryString = null;
	String[] querySplited = null;
	List<ScoreDoc> scoreDocsforPastQuery = null;
	List<Integer> ndDocs = null;
	HashMap<Integer, Integer> tfForDocs;
	HashMap<String, HashMap<Integer, Integer>> queryTermsTfs;
	HashMap<Integer, Float> ndDocAndProb;
	float probForAllTermsQueryInDoc = 1;
	List<TermAndRanking> termsAndRankings;
	TermAndRanking ranking = null;

	List<TermTfIdf> tfIdf = Processor.getTfIdf(reader, fieldsProcs);

	Query query = null;
	TopDocs topDocs = null;
	List<ScoreDoc> scoreDocs = null;
	RelevantDocumentsAndMetrics relDocsandMetrics = null;

	// Metrics
	float[] metrics = null;
	float fullPrecision10 = 0;
	float fullPrecision20 = 0;
	float fullRecall10 = 0;
	float fullRecall20 = 0;
	float fullAveragePrecision = 0;
	float realNumberOfQueries = 0;

	int colLength = Processor.getColLength(fieldsProcs, reader);
	// EmergencyMap will be used on those words that are in no document
	// giving them an effective tf of zero in each document
	HashMap<Integer, Integer> emergencyMap = new HashMap<>();
	for (int i = 0; i < 1400; i++) {
	    emergencyMap.put(i, 0);
	}

	System.out.println();
	if (indexingModel) {
	    System.out.println("PrfJm:");
	} else {
	    System.out.println("PrfDir:");
	}
	for (QueryAndResult qAndR : queriesAndResults) {
	    queryString = qAndR.getQuery();
	    querySplited = queryString.split(" ");
	    scoreDocsforPastQuery = qAndR.getScoreDocs();
	    // Can't get more docs than there are in the previous ranking
	    if (nd > scoreDocsforPastQuery.size()) {
		nd = scoreDocsforPastQuery.size();
	    }
	    ndDocs = new ArrayList<>();
	    for (int i = 0; i < nd; i++) {
		ndDocs.add(scoreDocsforPastQuery.get(i).doc);
	    }

	    // Create hashmap to get the tf-Doc list for each term of the query
	    queryTermsTfs = new HashMap<>();
	    for (String term : querySplited) {
		tfForDocs = getHashMapTfForTerm(tfIdf, term, emergencyMap);
		queryTermsTfs.put(term, tfForDocs);
	    }

	    // ∏=1,n P(qi|D), for each Document
	    ndDocAndProb = new HashMap<>();
	    Integer tf = 0;
	    for (Integer i : ndDocs) {
		probForAllTermsQueryInDoc = 1;
		for (String term : querySplited) {
		    if (queryTermsTfs.get(term) == null) {
			tf = 0;
		    } else {
			tf = queryTermsTfs.get(term).get(i);
			if (tf == null) {
			    tf = 0;
			}
		    }

		    probForAllTermsQueryInDoc = probForAllTermsQueryInDoc
			    * ((float) tf / (float) Processor.getDocLength(i,
				    fieldsProcs, reader));
		    // System.out.println("Tf= " + tf + " for term " + term + "
		    // in doc " + (i + 1));
		    // System.out.println(probForAllTermsQueryInDoc);

		}
		/*
		 * Try to catch ∏=1,n P(qi|D) != 0 if (probForAllTermsQueryInDoc
		 * > 0){ System.out.println("Doc: " +(i+1) + " Value:" +
		 * probForAllTermsQueryInDoc); //System.exit(1); try {
		 * Thread.sleep(4000); } catch (InterruptedException e) {} }
		 */
		ndDocAndProb.put(i, probForAllTermsQueryInDoc);
	    }

	    termsAndRankings = new ArrayList<>();
	    for (String term : querySplited) {
		if (indexingModel) {
		    ranking = getPrfjmProbability(lambdaOrMU, term, ndDocs,
			    ndDocAndProb, searcher, reader, fieldsProcs,
			    colLength, queryTermsTfs.get(term));
		} else {
		    ranking = getPrfdirProbability(lambdaOrMU, term, ndDocs,
			    ndDocAndProb, searcher, reader, fieldsProcs,
			    colLength, queryTermsTfs.get(term));
		}
		termsAndRankings.add(ranking);
	    }

	    Collections.sort(termsAndRankings);
	    // COMPRUEBA QUE ESTÁ BIEN ORDENADO

	    if (nw > termsAndRankings.size()) {
		nw = termsAndRankings.size();
	    }

	    queryString = "";
	    for (int i = 0; i < nw; i++) {
		queryString += " " + termsAndRankings.get(i).getTerm();
	    }

	    //////////////////////
	    realNumberOfQueries++;
	    query = parser.parse(queryString);

	    topDocs = searcher.search(query, Math.max(20, Math.max(cut, top)));
	    scoreDocs = Arrays.asList(topDocs.scoreDocs);

	    metrics = printQueryInfo(qAndR.getQueryNumber(), cut, top,
		    fieldsVisual, query, reader, queriesRelevance,
		    relDocsandMetrics, scoreDocs);

	    fullPrecision10 += metrics[0];
	    fullPrecision20 += metrics[1];
	    fullRecall10 += metrics[2];
	    fullRecall20 += metrics[3];
	    fullAveragePrecision += metrics[4];
	    ///////////////////////

	    if (explain) {
		explainPrf(nw, ndDocs, termsAndRankings);
	    }

	}

	printMeanQueryMetrics(realNumberOfQueries, fullPrecision10,
		fullPrecision20, fullRecall10, fullRecall20,
		fullAveragePrecision);

    }

    private static void explainPrf(int nw, List<Integer> ndDocs,
	    List<TermAndRanking> termsAndRankings) {
	List<Float> probdList = null;
	List<Float> pwdList = null;
	List<Float> pQiDList = null;
	int i = 0;
	int j = 0;
	for (TermAndRanking tR : termsAndRankings) {
	    i++;
	    j = 0;
	    System.out.println("For the term: " + tR.getTerm());
	    probdList = tR.getProbdList();
	    pwdList = tR.getPwdList();
	    pQiDList = tR.getpQiDList();
	    for (Integer d : ndDocs) {
		System.out.println("D=" + (d + 1) + " P(D)=" + probdList.get(j)
			+ " P(w|D)=" + pwdList.get(j) + " ∏=1,n P(qi|D)="
			+ pQiDList.get(j));
		j++;
	    }
	    System.out.println();
	    if (i >= nw) {
		break;
	    }
	}
	System.out.println();
    }

    private static HashMap<Integer, Integer> getHashMapTfForTerm(
	    List<TermTfIdf> tfIdf, String term,
	    HashMap<Integer, Integer> emergencyMap) {
	for (TermTfIdf t : tfIdf) {
	    if (t.getTerm().equals(term)) {
		return (t.getTf());
	    }
	}
	return emergencyMap;
    }

    private static TermAndRanking getPrfjmProbability(float lambdaOrMU,
	    String term, List<Integer> ndDocs,
	    HashMap<Integer, Float> ndDocAndProb, IndexSearcher searcher,
	    DirectoryReader reader, List<String> fieldsProcs, int colLength,
	    HashMap<Integer, Integer> mapDocTf) throws IOException {
	// Pag 32 a 45 de slides tema 7
	// P(w|R) = ∑ D Є PRset P(D) P(w|D) ∏=1,n P(qi|D) =sumProb
	// int P(D) = 1 / PRset.size(); = probD
	// P(w|D) = (1 - lambdaOrMu) P(qi|D) + lambda * P(qi|C) (w=qi) = pwd
	// P(qi|D) = f qi,D / |D| (frecuencia de aparición de la palabra qi en
	// el Documento entre su tamaño)
	// P(qi|C) = f qi,C / |C| (frecuencia de aparición de la palabra qi en
	// la colección entre su tamaño)
	// ∏=1,n P(qi|D) = ndDocAndProb.get(d)

	float probD = (float) 1 / (float) ndDocs.size();
	float pwd = 0;
	float pQiD = 0;
	float allTfCollection = 0;
	for (int i = 0; i < 1400; i++) {
	    if (mapDocTf.containsKey(i)) {
		allTfCollection += (float) mapDocTf.get(i);
	    }
	}

	float tf = 0;
	float sumProb = 0;
	List<Float> probdList = new ArrayList<>();
	List<Float> pwdList = new ArrayList<>();
	List<Float> pQiDList = new ArrayList<>();
	for (Integer d : ndDocs) {
	    if (mapDocTf.containsKey(d)) {
		tf = mapDocTf.get(d);
	    } else {
		tf = 0;
	    }
	    pwd = ((float) 1 - lambdaOrMU) * (tf
		    / (float) Processor.getDocLength(d, fieldsProcs, reader))
		    + lambdaOrMU * (allTfCollection / (float) colLength);
	    pQiD = ndDocAndProb.get(d);
	    sumProb += probD * pwd * pQiD;
	    probdList.add(probD);
	    pwdList.add(pwd);
	    pQiDList.add(pQiD);

	    /*
	     * System.out.println(pwd + " Tf:"+ tf + " |D|:" +
	     * Processor.getDocLength(d, fieldsProcs, reader) +" lambda:" +
	     * lambdaOrMU + "  Tfc:" + allTfCollection + " |C|:" + colLength +
	     * " Term: " + term); System.exit(1);
	     */
	}

	return new TermAndRanking(term, sumProb, probdList, pwdList, pQiDList);
    }

    private static TermAndRanking getPrfdirProbability(float lambdaOrMU,
	    String term, List<Integer> ndDocs,
	    HashMap<Integer, Float> ndDocAndProb, IndexSearcher searcher,
	    DirectoryReader reader, List<String> fieldsProcs, int colLength,
	    HashMap<Integer, Integer> mapDocTf) throws IOException {
	// Pag 32 a 45 de slides tema 7
	// P(w|R) = ∑ D Є PRset P(D) P(w|D) ∏=1,n P(qi|D) =sumProb
	// int P(D) = 1 / PRset.size(); = probD
	// P(w|D) = (f w,D + mu * (f w,C / |C|)) / (|D| + mu) = pwd
	// ∏=1,n P(qi|D) = ndDocAndProb.get(d)

	float probD = (float) 1 / (float) ndDocs.size();
	float pwd = 0;
	float pQiD = 0;
	float allTfCollection = 0;
	for (int i = 0; i < 1400; i++) {
	    if (mapDocTf.containsKey(i)) {
		allTfCollection += (float) mapDocTf.get(i);
	    } else {
	    }
	}

	float sumProb = 0;
	float tf = 0;
	List<Float> probdList = new ArrayList<>();
	List<Float> pwdList = new ArrayList<>();
	List<Float> pQiDList = new ArrayList<>();
	for (Integer d : ndDocs) {
	    if (mapDocTf.containsKey(d)) {
		tf = mapDocTf.get(d);
	    } else {
		tf = 0;
	    }
	    pwd = (tf + lambdaOrMU * (allTfCollection / (float) colLength))
		    / (((float) Processor.getDocLength(d, fieldsProcs, reader))
			    + lambdaOrMU);
	    pQiD = ndDocAndProb.get(d);
	    sumProb += probD * pwd * pQiD;
	    probdList.add(probD);
	    pwdList.add(pwd);
	    pQiDList.add(pQiD);

	    /*
	     * System.out.println(pwd + " Tf:"+ tf + " |D|:" +
	     * Processor.getDocLength(d, fieldsProcs, reader) +" lambda:" +
	     * lambdaOrMU + "  Tfc:" + allTfCollection + " |C|:" + colLength +
	     * " Term: " + term); System.exit(1);
	     */
	}
	return new TermAndRanking(term, sumProb, probdList, pwdList, pQiDList);
    }
}
