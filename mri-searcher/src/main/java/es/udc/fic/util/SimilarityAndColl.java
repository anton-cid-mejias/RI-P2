package es.udc.fic.util;

import org.apache.lucene.search.similarities.Similarity;

public class SimilarityAndColl {
    
    private final String coll;
    private final Similarity similarity;
    private final float lambdaOrMu;
    
    public SimilarityAndColl(String coll, Similarity similarity,
	    float lambdaOrMu) {
	this.coll = coll;
	this.similarity = similarity;
	this.lambdaOrMu = lambdaOrMu;
    }


    public float getLambdaOrMu() {
        return lambdaOrMu;
    }


    public String getColl() {
        return coll;
    }

    public Similarity getSimilarity() {
        return similarity;
    }
    
}
