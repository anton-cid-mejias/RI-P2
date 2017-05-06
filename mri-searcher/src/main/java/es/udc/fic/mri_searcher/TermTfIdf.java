package es.udc.fic.mri_searcher;

import java.util.HashMap;

public class TermTfIdf implements Comparable<TermTfIdf> {

    private int df;
    private final String term;
    private HashMap<Integer,Integer> tf;
    private int numberDocuments;
    private Double idf = 0.0;

    public TermTfIdf(String term, int numberDocuments) {
	this.term = term;
	this.numberDocuments = numberDocuments;
    }

    public void setDf(int df) {
        this.df = df;
    }

    public void setTf(HashMap<Integer, Integer> tf) {
        this.tf = tf;
    }

    public int getDf() {
        return df;
    }

    public String getTerm() {
        return term;
    }
    
    public double getIdf(){
	return idf;
    }

    public void plusOneDf(){
	this.df++;;
    }
    
    public HashMap<Integer, Integer> getTf() {
        return tf;
    }
    
    public void calculateIdf(){
	this.idf = Math.log(numberDocuments/df);
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
	TermTfIdf other = (TermTfIdf) obj;
	if (term == null) {
	    if (other.term != null)
		return false;
	} else if (!term.equals(other.term))
	    return false;
	return true;
    }

    @Override
    public int compareTo(TermTfIdf o) {
	int lastCmp = idf.compareTo(o.getIdf());
	return (lastCmp != 0 ? lastCmp : term.compareTo(o.getTerm()));
    }


}
