package es.udc.fic.mri_searcher;

import java.util.ArrayList;
import java.util.List;

public class BasicMetrics {

    public static RelevantDocumentsAndMetrics RelevanceHits(int queryNumber,
	    List<Integer> queryDocs, List<QueryNumberRelevanceDoc> cranRel) {
	
	List<Integer> relevanceDocs = null;
	List<Integer> relevanceDocsHits = new ArrayList<>();
	int hits = 0;
	int hits10 = 0;
	int hits20 = 0;
	int doc;

	for (QueryNumberRelevanceDoc d : cranRel) {
	    if (d.getQueryNumber() == queryNumber) {
		relevanceDocs = d.getRelevanceDoc();
		break;
	    }
	}

	for (int i = 0; i < queryDocs.size(); i++) {
	    doc = queryDocs.get(i);
	    if (relevanceDocs.contains(queryDocs.get(i))) {
		relevanceDocsHits.add(doc);
		hits++;
	    }
	    
	    if (i==9){
		hits10 = hits;
	    }
	    if (i==19){
		hits20 = hits;
	    }
	}
	
	float[] p = new float[2];
	p[0] = hits10/10;
	p[1] = hits20/20;
	
	float[] recall = new float[2];
	recall[0] = hits10/relevanceDocs.size();
	recall[1] = hits20/relevanceDocs.size();
	
	return new RelevantDocumentsAndMetrics(hits10,hits20,relevanceDocsHits,relevanceDocs.size(),p,recall,0);
    }
}
