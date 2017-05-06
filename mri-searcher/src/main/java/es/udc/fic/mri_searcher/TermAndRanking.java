package es.udc.fic.mri_searcher;

import java.util.List;

public class TermAndRanking implements Comparable<TermAndRanking> {
    
    private final String term;
    private final float ranking;
    private final List<Float> probdList;
    private final List<Float> pwdList;
    private final List<Float> pQiDList;
    
    


    public TermAndRanking(String term, float ranking, List<Float> probdList,
	    List<Float> pwdList, List<Float> pQiDList) {
	this.term = term;
	this.ranking = ranking;
	this.probdList = probdList;
	this.pwdList = pwdList;
	this.pQiDList = pQiDList;
    }

    public String getTerm() {
        return term;
    }

    public float getRanking() {
        return ranking;
    }

    public List<Float> getProbdList() {
        return probdList;
    }

    public List<Float> getPwdList() {
        return pwdList;
    }

    public List<Float> getpQiDList() {
        return pQiDList;
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
