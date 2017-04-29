package es.udc.fic.mri_searcher;

import java.util.List;

public class RelevantDocumentsAndHits {
    private final int hits10;
    private final int hits20;
    private final List<Integer> relevantDocs;
    
    public RelevantDocumentsAndHits(int hits10, int hits20,
	    List<Integer> relevantDocs) {
	this.hits10 = hits10;
	this.hits20 = hits20;
	this.relevantDocs = relevantDocs;
    }

    public int getHits10() {
        return hits10;
    }

    public int getHits20() {
        return hits20;
    }

    public List<Integer> getRelevantDocs() {
        return relevantDocs;
    }
    
    
}