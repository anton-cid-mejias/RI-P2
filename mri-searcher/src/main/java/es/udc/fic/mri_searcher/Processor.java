package es.udc.fic.mri_searcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;

public class Processor {


    public static void getTfIdf(IndexReader indexReader, List<String> fields,
	    Map<String, Integer> termMap) throws IOException {
	Terms terms;
	TermsEnum termsIterator;
	BytesRef term;
	List<TermTfIdf> listTerms = new ArrayList<>();
	
	for (String field : fields) {
	    terms = MultiFields.getTerms(indexReader, field);
	    termsIterator = terms.iterator();

	    while ((term = termsIterator.next()) != null) {
		String termString = term.utf8ToString();
		
		TermTfIdf termTfIdf;
		if (!listTerms.contains(new TermTfIdf(termString))){
		    termTfIdf = new TermTfIdf(termString);
		    termTfIdf.setTf(new HashMap<>());
		}else{
		    termTfIdf = new TermTfIdf(termString);
		    int i = listTerms.indexOf(termTfIdf);
		    termTfIdf = listTerms.get(i);
		}
		
		PostingsEnum postingsEnum = MultiFields.getTermDocsEnum(
			indexReader, field, term, PostingsEnum.FREQS);
		int i;
		while ((i = postingsEnum
			.nextDoc()) != PostingsEnum.NO_MORE_DOCS) {
		    int freq = postingsEnum.freq();
		    
		    if (termTfIdf.getTf().containsKey(i)){
			int tf = termTfIdf.getTf().get(i);
			tf += freq;
			termTfIdf.getTf().put(i, tf);
		    }else{
			termTfIdf.plusOneDf();
			termTfIdf.getTf().put(i, freq);
		    }  
		    
		}
	    }
	}
    }
}
