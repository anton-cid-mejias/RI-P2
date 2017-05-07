package es.udc.fic.mri_searcher;

public class DocumentTerm implements Comparable<DocumentTerm>{
    
    private final Double tfIdf;
    private final double tf;
    private final double idf;
    private final String termString;
    
    public DocumentTerm(double tfIdf, double tf, double idf, String termString) {
	this.tfIdf = tfIdf;
	this.termString = termString;
	this.tf = tf;
	this.idf = idf;
    }

    public double getTf() {
        return tf;
    }

    public double getIdf() {
        return idf;
    }

    public double getTfIdf() {
        return tfIdf;
    }

    public String getTermString() {
        return termString;
    }

    @Override
    public int compareTo(DocumentTerm o) {
	int lastCmp = tfIdf.compareTo(o.getTfIdf());
	return (lastCmp != 0 ? lastCmp : termString.compareTo(o.getTermString()));
    }
}
