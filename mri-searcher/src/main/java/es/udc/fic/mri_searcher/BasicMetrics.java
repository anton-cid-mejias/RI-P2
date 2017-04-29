package es.udc.fic.mri_searcher;

import java.util.ArrayList;
import java.util.List;

public class BasicMetrics {

    public static void P(RelevantDocumentsAndHits rel) {
	
	System.out.println("P@10 = " + rel.getHits10()/10);
	System.out.println("P@20 = " + rel.getHits20()/20);
	
    }

    public static void Recall(RelevantDocumentsAndHits rel) {

	System.out.println("Recall@10 = " + rel.getHits10()/rel.getTotalRelevants());
	System.out.println("Recall@20 = " + rel.getHits20()/rel.getTotalRelevants());
    }

    public static RelevantDocumentsAndHits RelevanceHits(int queryNumber,
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
	
	return new RelevantDocumentsAndHits(hits10,hits20,relevanceDocsHits,relevanceDocs.size());
    }
}
