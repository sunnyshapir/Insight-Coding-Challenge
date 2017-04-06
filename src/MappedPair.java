import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
    public int compareTo(MappedPair other)  {

		SimpleDateFormat format = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z");
		Date key = null, otherKey = null;

		if (!isAlpha(this.key) && !isAlpha(other.key)) {
			try {
				key = format.parse(this.key);
				otherKey = format.parse(other.key);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

		if (this.getValue() < other.getValue())
			return 1;
		else if (this.getValue() > other.getValue())
			return -1;
		else if (!isAlpha(this.key) && !isAlpha(other.key)) {
			if (key.before(otherKey))
				return -1;
			else
				return 1;
		} else
			return 0;
    }

	public boolean isAlpha(String key) {
	    char[] chars = key.toCharArray();

	    for (char c : chars) {
	        if(!Character.isLetter(c))
            if (!(c == '/'))
              if (!(c == '.'))
	            return false;
	    }

	    return true;
	}
}
