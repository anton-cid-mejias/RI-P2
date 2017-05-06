package es.udc.fic.mri_searcher;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class Processor {

    public static List<TermTfIdf> getTfIdf(IndexReader indexReader,
	    List<String> fields) throws IOException {
	Terms terms;
	TermsEnum termsIterator;
	BytesRef term;
	List<TermTfIdf> listTerms = new ArrayList<>();
	int numberDocuments = indexReader.numDocs();

	for (String field : fields) {
	    terms = MultiFields.getTerms(indexReader, field);
	    termsIterator = terms.iterator();

	    while ((term = termsIterator.next()) != null) {
		String termString = term.utf8ToString();

		TermTfIdf termTfIdf;
		if (!listTerms
			.contains(new TermTfIdf(termString, numberDocuments))) {
		    termTfIdf = new TermTfIdf(termString, numberDocuments);
		    termTfIdf.setTf(new HashMap<>());
		    listTerms.add(termTfIdf);
		} else {
		    termTfIdf = new TermTfIdf(termString, numberDocuments);
		    int i = listTerms.indexOf(termTfIdf);
		    termTfIdf = listTerms.get(i);
		}
		
		PostingsEnum postingsEnum = MultiFields.getTermDocsEnum(
			indexReader, field, term, PostingsEnum.FREQS);
		int i;
		while ((i = postingsEnum
			.nextDoc()) != PostingsEnum.NO_MORE_DOCS) {
		    int freq = postingsEnum.freq();
		    
		    if (termTfIdf.getTf().containsKey(i)) {
			int tf = termTfIdf.getTf().get(i);
			tf += freq;
			termTfIdf.getTf().put(i, tf);
		    } else {
			termTfIdf.plusOneDf();
			termTfIdf.getTf().put(i, freq);
		    }

		}
	    }
	}
	for (TermTfIdf t:listTerms){
	    t.calculateIdf();
	}
	Collections.sort(listTerms);
	
	return listTerms;
    }

    public static List<DocumentTerm>  getBestTfIdfTerms(List<TermTfIdf> listTerms,
	    List<Integer> docs) {

	List<DocumentTerm> documentsTermsList = new ArrayList<>();
	boolean done = false;

	for (TermTfIdf term : listTerms) {
	    for (int doc : docs) {
		if (!done) {
		    if (term.getTf().containsKey(doc)) {
			double tf = term.getTf().get(doc);
			tf = 1 + Math.log(tf);
			double tfIdf = term.getIdf() * tf;
			documentsTermsList
				.add(new DocumentTerm(tfIdf, term.getTerm()));
			done = true;
		    }
		}
	    }
	    done = false;
	}
	Collections.sort(documentsTermsList);
	return documentsTermsList;
    }
    
    public static int getDocLength(int doc, List<String> fields, DirectoryReader reader) throws IOException{
	Document document = reader.document(doc);
	int n = 0;
	
	for (String field : fields){
	    if (field.equals("T")){
		n += Integer.parseInt((document.get("TTokens")));
	    }
	    if (field.equals("W")){
		n += Integer.parseInt((document.get("WTokens")));
	    }
	}
	return n;
    }
    
    public static int getColLength(List<String> fields, DirectoryReader reader) throws IOException{
	int n = 0;
	Document document;
	
	for (int i=0; i<reader.numDocs();i++){
	    
	    document = reader.document(i);
	    
	    for (String field : fields){
		    if (field.equals("T")){
			n += Integer.parseInt((document.get("TTokens")));
		    }
		    if (field.equals("W")){
			n += Integer.parseInt((document.get("WTokens")));
		    }
		}
	}
	return n;
    }
    
    public static void main(String[] args) throws IOException {

	String dir = "/home/anton/lucene/Cran/indice";
	Directory indir = FSDirectory.open(Paths.get(dir));
	DirectoryReader reader = DirectoryReader.open(indir);
	List<String> fields = new ArrayList<>();
	fields.add("T");
	fields.add("W");
	List<TermTfIdf> listTerms = getTfIdf(reader, fields);
	List<Integer> docs = new ArrayList<>();
	docs.add(1);
	docs.add(2);
	getBestTfIdfTerms(listTerms,docs);
	int n = getColLength(fields,reader);
	int n2 = getDocLength(0,fields,reader);
	System.out.println(n);
	System.out.println(n2);
    }
}
