package es.udc.fic.mri_searcher;

public class TermIdf implements Comparable<TermIdf>{
    
    private final Double idf;
    private final String term;
    
    TermIdf(String term, double idf){
	this.idf = idf;
	this.term = term;
    }

    public double getIdf() {
        return idf;
    }

    public String getTerm() {
        return term;
    }

    @Override
    public int compareTo(TermIdf o) {
	int lastCmp = idf.compareTo(o.getIdf());
	return (lastCmp != 0 ? lastCmp : term.compareTo(o.getTerm()));
    }

    @Override
    public String toString() {
	return "idf=" + idf + "	term=" + term;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((term == null) ? 0 : term.hashCode());
	return result;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	TermIdf other = (TermIdf) obj;
	if (term == null) {
	    if (other.term != null)
		return false;
	} else if (!term.equals(other.term))
	    return false;
	return true;
    }
    
    
}
