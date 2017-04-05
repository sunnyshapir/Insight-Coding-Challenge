
public class MappedPair implements Comparable<MappedPair> {
    private String key;
    private int value;
    
    public MappedPair (String key, int value) {
	this.key = key;
	this.value = value;
    }
    
    public String getKey() { return this.key; } 
    public int getValue() { return this.value; }
    
    @Override
	public int compareTo(MappedPair other) {
	if (this.getValue() < other.getValue())
	    return 1;
	else if (this.getValue() > other.getValue())
	    return -1;
	else
	    return 0;
    }
}