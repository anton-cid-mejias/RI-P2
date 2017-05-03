package es.udc.fic.mri_searcher;

public class PairTermTfIdf implements Comparable<PairTermTfIdf>{
    private final String term;
    private final Double tfIdf;
    
    public PairTermTfIdf(String term, Double tfIdf) {
	this.term = term;
	this.tfIdf = tfIdf;
    }

    public String getTerm() {
        return term;
    }

    public Double getTfIdf() {
        return tfIdf;
    }

    @Override
    public int compareTo(PairTermTfIdf o) {
	int lastCmp = tfIdf.compareTo(o.getTfIdf());
	return (lastCmp != 0 ? lastCmp : term.compareTo(o.getTerm()));
    }

}
