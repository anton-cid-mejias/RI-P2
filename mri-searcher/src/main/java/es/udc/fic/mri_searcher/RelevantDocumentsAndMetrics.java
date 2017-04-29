package es.udc.fic.mri_searcher;

import java.util.List;

public class RelevantDocumentsAndMetrics {
    
    private final int hits10;
    private final int hits20;
    private final List<Integer> relevantDocs;
    private final int totalRelevants;
    //precision[0] = P@10;  precision[1] = P@20; 
    private final float[] precision;
    //recall[0] = Recall@10;  recall[1] = Recall@20; 
    private final float[] recall;
    private final float averagePrecision;
    
    public RelevantDocumentsAndMetrics(int hits10, int hits20,
	    List<Integer> relevantDocs, int totalRelevants, float[] precision,
	    float[] recall, float averagePrecision) {
	super();
	this.hits10 = hits10;
	this.hits20 = hits20;
	this.relevantDocs = relevantDocs;
	this.totalRelevants = totalRelevants;
	this.precision = precision;
	this.recall = recall;
	this.averagePrecision = averagePrecision;
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
    public int getTotalRelevants() {
        return totalRelevants;
    }
    public float[] getPrecision() {
        return precision;
    }
    public float[] getRecall() {
        return recall;
    }
    public float getAveragePrecision() {
        return averagePrecision;
    }
    
    
}
