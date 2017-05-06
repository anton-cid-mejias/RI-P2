package es.udc.fic.mri_searcher;

public class PriorityTerm implements Comparable<PriorityTerm>{
    
    private final Integer priority;
    private final String term;
    
    public PriorityTerm(int priority, String term) {
	super();
	this.priority = priority;
	this.term = term;
    }
    public int getPriority() {
        return priority;
    }
    public String getTerm() {
        return term;
    }
    
    @Override
    public int compareTo(PriorityTerm o) {
	return this.priority.compareTo(o.getPriority());
    }
    
    
}
