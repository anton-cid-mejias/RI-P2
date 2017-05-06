package es.udc.fic.mri_searcher;

import java.util.List;

import org.apache.lucene.search.ScoreDoc;

public class QueryAndResult {
    
    private final String query;
    private final int queryNumber;
    private final List<ScoreDoc> scoreDocs;
    
    public QueryAndResult(String query, int queryNumber,
	    List<ScoreDoc> scoreDocs) {
	this.query = query;
	this.queryNumber = queryNumber;
	this.scoreDocs = scoreDocs;
    }

    public String getQuery() {
        return query;
    }

    public List<ScoreDoc> getScoreDocs() {
        return scoreDocs;
    }

    public int getQueryNumber() {
        return queryNumber;
    }
}
