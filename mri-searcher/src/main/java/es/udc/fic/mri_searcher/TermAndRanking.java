package es.udc.fic.mri_searcher;
public class TermAndRanking implements Comparable<TermAndRanking> {
    
    private final String term;
    private final float ranking;
    
    public TermAndRanking(String term, float ranking) {
	this.term = term;
	this.ranking = ranking;
    }

    public String getTerm() {
        return term;
    }

    public float getRanking() {
        return ranking;
    }

    @Override
    public int compareTo(TermAndRanking o) {
	if (ranking > o.getRanking()){
	    return 1;
	} else if (ranking < o.getRanking()){
	    return -1;
	} else {
	    return 0;
	}
	
    }
    
    
}
