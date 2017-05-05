package es.udc.fic.mri_searcher;

import java.util.ArrayList;
import java.util.List;

public class QueryNumberRelevanceDoc {
    
    private final int queryNumber;
    private final List<Integer> relevanceDoc;
    
    public QueryNumberRelevanceDoc(int queryNumber, List<Integer> relevanceDoc) {
	this.queryNumber = queryNumber;
	this.relevanceDoc = new ArrayList<Integer>(relevanceDoc);
    }

    public int getQueryNumber() {
        return queryNumber;
    }
    
    public List<Integer> getRelevanceDoc() {
        return relevanceDoc;
    }
}
