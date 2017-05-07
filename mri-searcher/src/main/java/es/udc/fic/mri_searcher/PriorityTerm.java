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
	PriorityTerm other = (PriorityTerm) obj;
	if (term == null) {
	    if (other.term != null)
		return false;
	} else if (!term.equals(other.term))
	    return false;
	return true;
    }
    @Override
    public int compareTo(PriorityTerm o) {
	return this.priority.compareTo(o.getPriority());
    }
    
    
}
