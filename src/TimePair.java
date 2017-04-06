import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimePair implements Comparable<TimePair> {
	private String key;
	private int value;

	public TimePair (String key, int value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() { return this.key; }
	public int getValue() { return this.value; }

	@Override
    public int compareTo(TimePair other)  {

		SimpleDateFormat format = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z");
		Date key = null, otherKey = null;
			try {
				key = format.parse(this.key);
				otherKey = format.parse(other.key);
			} catch (ParseException e) {
				e.printStackTrace();
			}

		if (this.getValue() < other.getValue())
			return 1;
		else if (this.getValue() > other.getValue())
			return -1;
		else {
			if (key.before(otherKey))
				return -1;
			else
				return 1;
		}
    }
}
