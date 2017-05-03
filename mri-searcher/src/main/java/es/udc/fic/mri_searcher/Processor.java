package es.udc.fic.mri_searcher;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Processor {

    public static void IdfTerms(String indexFile, String field, int n,
	    boolean asc) throws IOException {

	Directory dir = null;
	DirectoryReader indexReader = null;

	dir = FSDirectory.open(Paths.get(indexFile));
	indexReader = DirectoryReader.open(dir);
	int numberDocuments = indexReader.numDocs();
	List<TermIdf> listTerms = new ArrayList<>();
	Map<String, Integer> termMap = new HashMap<>();

	termMap = getTermFrequencies(indexReader, field);
	indexReader.close();

	@SuppressWarnings("rawtypes")
	Iterator it = termMap.entrySet().iterator();
	while (it.hasNext()) {
	    @SuppressWarnings("rawtypes")
	    Map.Entry pair = (Map.Entry) it.next();
	    final double idf = Math
		    .log((float) numberDocuments / ((int) pair.getValue()));
	    listTerms.add(new TermIdf((String) pair.getKey(), idf));
	}
	printIdfTerms(listTerms, n, field, asc);
    }

    public static void TfIdfTerms(String indexFile, String field, int n,
	    boolean asc) throws IOException {
	Directory dir = null;
	DirectoryReader indexReader = null;

	dir = FSDirectory.open(Paths.get(indexFile));
	indexReader = DirectoryReader.open(dir);
	List<TermTfIdf> listTerms = new ArrayList<>();
	Map<String, Integer> termMap = new HashMap<>();

	termMap = getTermFrequencies(indexReader, field);

	listTerms = getListTfIdfTerms(indexReader, field, termMap);

	printTfIdfTerms(listTerms, n, field, asc);
    }

    public static Map<String, Integer> getTermFrequencies(
	    IndexReader indexReader, String field) throws IOException {

	Map<String, Integer> termMap = new HashMap<>();

	for (final LeafReaderContext leaf : indexReader.leaves()) {
	    LeafReader leafReader = leaf.reader();

	    final Fields fields = leafReader.fields();

	    final Terms terms = fields.terms(field);
	    final TermsEnum termsEnum = terms.iterator();

	    while (termsEnum.next() != null) {
		final String tt = termsEnum.term().utf8ToString();
		final int f = termsEnum.docFreq();
		if (termMap.containsKey(tt)) {
		    int lastF = termMap.get(tt);
		    termMap.put(tt, (lastF + f));
		} else {
		    termMap.put(tt, f);
		}
	    }
	}
	return termMap;
    }

    private static List<TermTfIdf> getListTfIdfTerms(IndexReader indexReader,
	    String field, Map<String, Integer> termMap) throws IOException {

	int numberDocuments = indexReader.numDocs();
	List<TermTfIdf> listTerms = new ArrayList<>();

	for (final LeafReaderContext leaf : indexReader.leaves()) {
	    try (LeafReader leafReader = leaf.reader()) {
		@SuppressWarnings("rawtypes")
		Iterator it = termMap.entrySet().iterator();

		while (it.hasNext()) {
		    @SuppressWarnings("rawtypes")
		    Map.Entry pair = (Map.Entry) it.next();
		    final double idf = Math.log(
			    (float) numberDocuments / ((int) pair.getValue()));
		    String termValue = (String) pair.getKey();
		    final Term term = new Term(field, termValue);
		    final PostingsEnum postingsEnum = leafReader.postings(term);

		    if (postingsEnum != null) {
			while ((postingsEnum
				.nextDoc()) != PostingsEnum.NO_MORE_DOCS) {

			    double tf = postingsEnum.freq();
			    if (tf >= 1) {
				tf = 1 + Math.log(tf);
			    }
			    listTerms.add(new TermTfIdf(termValue,
				    postingsEnum.docID(), idf, tf, (tf * idf)));

			}
		    }
		}
	    }

	}
	return listTerms;
    }

    /*
     * private static Map<String, Integer> getTermDocFrequencies( IndexReader
     * reader, int docId, String field) throws IOException { Terms vector =
     * reader.getTermVector(docId, field);
     * 
     * TermsEnum termsEnum = null; termsEnum = vector.iterator(); Map<String,
     * Integer> frequencies = new HashMap<>();
     * 
     * while (termsEnum.next() != null) { String term =
     * termsEnum.term().utf8ToString(); int freq = (int)
     * termsEnum.totalTermFreq(); frequencies.put(term, freq); } return
     * frequencies; }
     */

    public static String getBestTerms(IndexReader indexReader, int docId,
	    String field, int n, Map<String, Integer> termMap)
	    throws IOException {

	int numberDocuments = indexReader.numDocs();
	List<PairTermTfIdf> listTermTfIdf = new ArrayList<>();
	StringBuilder strings = new StringBuilder();

	for (final LeafReaderContext leaf : indexReader.leaves()) {

	    LeafReader leafReader = leaf.reader();
	    @SuppressWarnings("rawtypes")
	    Iterator it = termMap.entrySet().iterator();

	    while (it.hasNext()) {
		@SuppressWarnings("rawtypes")
		Map.Entry pair = (Map.Entry) it.next();

		final double idf = Math
			.log((float) numberDocuments / ((int) pair.getValue()));
		String termValue = (String) pair.getKey();
		final Term term = new Term(field, termValue);
		final PostingsEnum postingsEnum = leafReader.postings(term);

		if (postingsEnum != null) {
		    while ((postingsEnum
			    .nextDoc()) != PostingsEnum.NO_MORE_DOCS) {
			if (postingsEnum.docID() == docId) {
			    double tf = postingsEnum.freq();
			    if (tf >= 1) {
				tf = 1 + Math.log(tf);
			    }
			    double tfIdf = tf * idf;
			    listTermTfIdf
				    .add(new PairTermTfIdf(termValue, tfIdf));
			    break;
			}

		    }
		}

	    }

	}
	Collections.sort(listTermTfIdf, new Comparator<PairTermTfIdf>() {
	    @Override
	    public int compare(PairTermTfIdf a, PairTermTfIdf b) {
		return b.compareTo(a);
	    }
	});
	int size = listTermTfIdf.size();
	if (size < n) {
	    n = size;
	}
	for (int i = 0; i < n; i++) {
	    strings.append(listTermTfIdf.get(i).getTerm());
	    strings.append(" ");
	}
	return strings.toString();
    }

    private static void printIdfTerms(List<TermIdf> list, int n, String field,
	    boolean asc) {

	if (asc) {
	    Collections.sort(list, new Comparator<TermIdf>() {
		@Override
		public int compare(TermIdf a, TermIdf b) {
		    return b.compareTo(a);
		}
	    });
	    System.out.println("\nBest_idf of " + field + ":");
	} else {
	    Collections.sort(list);
	    System.out.println("\nPoor_idf of " + field + ":");
	}

	int size = list.size();
	if (n > size) {
	    n = size;
	}

	for (int i = 0; i < n; i++) {
	    System.out.println(
		    "Nº=" + (i + 1) + "	" + list.get(i).toString());
	}

    }

    private static void printTfIdfTerms(List<TermTfIdf> list, int n,
	    String field, boolean asc) {

	if (asc) {
	    Collections.sort(list, new Comparator<TermTfIdf>() {
		@Override
		public int compare(TermTfIdf a, TermTfIdf b) {
		    return b.compareTo(a);
		}
	    });
	    System.out.println("\nBest_tfidf of " + field + ":");
	} else {
	    Collections.sort(list);
	    System.out.println("\nPoor_tfidf of " + field + ":");
	}

	int size = list.size();
	if (n > size) {
	    n = size;
	}

	for (int i = 0; i < n; i++) {
	    System.out.println(
		    "Nº=" + (i + 1) + "	" + list.get(i).toString());
	}

    }

}
