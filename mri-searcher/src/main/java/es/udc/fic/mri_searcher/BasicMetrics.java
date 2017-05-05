package es.udc.fic.mri_searcher;

import java.util.ArrayList;
import java.util.List;

public class BasicMetrics {

    public static RelevantDocumentsAndMetrics relevanceHits(int queryNumber,
	    List<Integer> queryDocs, List<QueryNumberRelevanceDoc> cranRel, int cut) {
	
	List<Integer> relevanceDocs = null;
	List<Integer> relevanceDocsHits = new ArrayList<>();
	int hits = 0;
	int hits10 = 0;
	int hits20 = 0;
	int doc;
	float precisionSum = 0;
	int numberRelevanceDocs = 0;

	for (QueryNumberRelevanceDoc d : cranRel) {
	    if (d.getQueryNumber() == queryNumber) {
		relevanceDocs = d.getRelevanceDoc();
		break;
	    }
	}
	
	numberRelevanceDocs = relevanceDocs.size();

	for (int i = 0; i < queryDocs.size(); i++) {
	    doc = queryDocs.get(i);
	    if (relevanceDocs.contains(queryDocs.get(i))) {
		relevanceDocsHits.add(doc);
		hits++;
		if (i<cut){
		    precisionSum += hits/(i+1);
		}
	    }
	    
	    if (i==9){
		hits10 = hits;
	    }
	    if (i==19){
		hits20 = hits;
	    }
	}
	
	float[] p = new float[2];
	p[0] = (float) (hits10/10.0);
	p[1] = (float) (hits20/20.0);
	
	float[] recall = new float[2];
	recall[0] = (float)(hits10/(float)numberRelevanceDocs);
	recall[1] = hits20/(float)numberRelevanceDocs;
	
	float average_precision = precisionSum/cut;
	
	return new RelevantDocumentsAndMetrics(hits10,hits20,relevanceDocsHits,numberRelevanceDocs,p,recall,average_precision);
    }
}
