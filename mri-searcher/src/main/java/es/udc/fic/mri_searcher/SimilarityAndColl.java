package es.udc.fic.mri_searcher;

import org.apache.lucene.search.similarities.Similarity;

public class SimilarityAndColl {
    
    private final String coll;
    private final Similarity similarity;
    
    public SimilarityAndColl(String coll, Similarity similarity) {
	
	this.coll = coll;
	this.similarity = similarity;
    }

    public String getColl() {
        return coll;
    }

    public Similarity getSimilarity() {
        return similarity;
    }
    
}
