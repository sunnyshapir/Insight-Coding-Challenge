import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class LoginAttempt {
    private String host;
    private int counter; 
    private Date attemptedTime, fiveMin, twentySec;
    Boolean isBlocked;
    
    public LoginAttempt (String host, String attemptedTime) throws ParseException {
	this.host = host;
	this.counter = 1;
	
	SimpleDateFormat format = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z");
	Date elapsedTime = format.parse(attemptedTime.toString());
	
	this.attemptedTime = elapsedTime;
	
	Calendar calendar = Calendar.getInstance();
	calendar.setTime(elapsedTime);
	calendar.add(Calendar.SECOND, 20);
	this.twentySec = calendar.getTime();
	
	this.fiveMin = null;
	
	this.isBlocked = false;
    }
    
    public String getHost() { return this.host; }
    public int getCounter() { return this.counter; }
    public Date getAttemptedTime() { return this.attemptedTime; }
    public Date getTwentySec() { return this.twentySec; }
    public Date getFiveMin() { return this.fiveMin; }
    public Boolean getIsBlocked() { return this.isBlocked; }
    
    public void clearBlock (String time) throws ParseException {
	this.isBlocked = false;
	this.counter = 1;
	
	SimpleDateFormat format = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z");
	Date elapsedTime = format.parse(time.toString());
	
	this.attemptedTime = elapsedTime;
	
	Calendar calendar = Calendar.getInstance();
	calendar.setTime(elapsedTime);
	calendar.add(Calendar.SECOND, 20);
	this.twentySec = calendar.getTime();
	this.fiveMin = null;
    }
    
    public int validate (String time) throws ParseException {
	if (withinTwentySec(time)) {
	    if (this.isBlocked) {
		if (withinFiveMin(time))
		    return 0; //If within 5 min - still blocked, write
	    } else {
		counter++;
		if (this.counter < 3) {
		    return 1; //If within 20 sec but not blocked
		}
		else if (this.counter == 3) {
		    this.isBlocked = true;
		    SimpleDateFormat format = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z");
		    Date attemptedTime = format.parse(time.toString());
		    Calendar calendar = Calendar.getInstance();
		    calendar.setTime(attemptedTime);
		    calendar.add(Calendar.MINUTE, 5);
		    this.fiveMin = calendar.getTime();
		    return 2; //If within 20 sec and need to block
		}
	    } 
	}
	return -1; //If not within 20 sec and past 5 min - UNBLOCK
    }
    

    public boolean withinFiveMin(String time) throws ParseException {
	SimpleDateFormat format = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z");
	Date convTime = format.parse(time.toString());
	if (convTime.before(this.fiveMin) && convTime.after(this.attemptedTime))
	    return true;
	return false;
    }

    public boolean withinTwentySec(String time) throws ParseException {
	SimpleDateFormat format = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z");
	Date convTime = format.parse(time.toString());
	if (convTime.before(this.twentySec) && convTime.after(this.attemptedTime))
	    return true;
	return false;
    }
}