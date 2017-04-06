import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.PriorityQueue;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.ArrayList;

public class ProcessLog {

	public static void main(String[] args) throws Exception {

	    String line, host, time, resource, tHttp, tBytes;
	    int http, bytes;

	    //Feature 1
	    HashMap<String, Integer> totalVisits = new HashMap<String, Integer>();

	    //Feature 2
	    HashMap<String, Integer> resourceFreq = new HashMap<String, Integer>();
	    HashMap<String, Integer> resourceBytes = new HashMap<String, Integer>();

	    //Feature 3
	    TreeMap<String, Integer> frequencyVisited = new TreeMap<String, Integer>();

	    //Feature 4
	    LinkedHashMap<String, LoginAttempt> userAuth = new LinkedHashMap<String, LoginAttempt>();
	    ArrayList<String> blockedUsers = new ArrayList<String>();

	    FileReader in = new FileReader(args[0]);
	    BufferedReader br = new BufferedReader(in);

	    while ((line = br.readLine()) != null) {

	    	String[] tokens = line.split("\\s");


	    	//Retrieve relevant data from the line read in from file
	    	host = tokens[0];
	    	time = tokens[3].substring(1)+" "+tokens[4].substring(0, tokens[4].length()-1);
	    	resource = tokens[6].replaceAll("\"", "");

	    	if (tokens[tokens.length-1].equals("-")) {
    			tHttp = tokens[tokens.length-2];
    			tBytes = null;
    		} else {
    			tHttp = tokens[tokens.length-2];
    			tBytes = tokens[tokens.length-1];
    		}

	    	//Convert numerical values to integers
	    	http = Integer.parseInt(tHttp);
	    	if (tBytes != null)
	    		bytes = Integer.parseInt(tBytes);
	    	else
	    		bytes = 0;

	    	//Count total number of times a host was active
	    	if (totalVisits.containsKey(host)) {
			int total = totalVisits.get(host);
			totalVisits.put(host, total+1);
		} else {
			totalVisits.put(host, 1);
		}

	    	//Count total number of times a resource is accessed
	    	//for use in bandwidth consumption calculation
	    	//while simultaneously tracking the number of bytes
	    	//associated with each resource
	    	if (tBytes != null) {
		        if (resourceFreq.containsKey(resource)) {
				int total = resourceFreq.get(resource);
				resourceFreq.put(resource, total+1);
			} else {
				resourceFreq.put(resource, 1);
				resourceBytes.put(resource, bytes);
			}
	       }

	    	//Counts total number of times there was activity at each given time
	    	if (!frequencyVisited.containsKey(time))
	    		frequencyVisited.put(time, 1);
	    	else
	    		frequencyVisited.put(time, frequencyVisited.get(time)+1);

	    	//Ensure previously blocked users that have been blocked
	    	//for 5 minutes are unblocked before processing
	    	if (userAuth.containsKey(host)) {
	    		if (userAuth.get(host).getIsBlocked())
	    			if (!(userAuth.get(host)).withinFiveMin(time))
	    			userAuth.remove(host);
	    	}

	    	//Ensure users that made failed login attempts before a
	    	//successful login attempt within 20 second are unblocked
	    	if (http == 200 && userAuth.containsKey(host))
    			if ((userAuth.get(host)).getCounter() < 3)
	    			userAuth.remove(host);

	    	//Process failed login attempts accordingly by calling validate
	    	if (http == 401) {
	    		if (userAuth.containsKey(host)) {
	    			int attempt = (userAuth.get(host)).validate(time);
	    			if (attempt == 0) {
	    				//Compile list of blocked users and write to file blocked.txt
					String temp = "";
	    				for (int i = 0; i < tokens.length; i++){
						if (i > 0 && i < tokens.length)
							temp += " ";
							temp += tokens[i];
						}
	    				blockedUsers.add(temp);
				} else if (attempt == -1)
	    				//Unblock
		    			(userAuth.get(host)).clearBlock (time);
	    		} else {
	    			userAuth.put(host, new LoginAttempt (host, time));
	    		}
	    	}

	    }
	    in.close();

	    //Retrieve 10 highest values and write to file hosts.txt
	    writeTopTen(totalVisits, args[1]);

	    //Retrieve 10 resources that consume the most bandwidth and write to file resources.txt
	    writeTopTen(resourceFreq, resourceBytes, args[2]);

	    //Retrieve 10 busiest hours and write to file hours.txt
	    writeTopTen(frequencyVisited, args[3]);

	    //Retrieve list of blocked users and write to file blocked.txt
	    writeBlocked(blockedUsers, args[4]);

	}

	//Writes 10 most active hosts using a HashMap (utilizing efficient updates/retrievals
	//for two logical data pairs and a priority queue for efficient retrieval of top 10 values
	private static void writeTopTen(HashMap<String, Integer> dataMap, String file) {
		PriorityQueue<MappedPair> topHosts = toQueue (dataMap);
		try {
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));

			int i = 0;
			while (i < 10 && !topHosts.isEmpty()) {
	            	bufferedWriter.write(topHosts.peek().getKey()+","+topHosts.peek().getValue());
	            	topHosts.poll();
	            	if (i < 9)
	            		bufferedWriter.newLine();
	            	i++;
	            }
	            bufferedWriter.close();
			} catch(IOException ex) {
	            System.out.println("Error writing to file");
	        }
	}

	//Writes 10 busiest hours to file using a TreeMap (utilizing sorted time keys) and a priority queue
	//for efficient retrieval of top 10 mapped values
	private static void writeTopTen(TreeMap<String, Integer> dataMap, String file) throws ParseException {

		TreeMap<String, Integer> frequency = performTimeComparisons(dataMap);
		PriorityQueue<MappedPair> topHours = toQueue (frequency);

		try {
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
			int i = 0;
			while (i < 10 && !topHours.isEmpty()) {
	            	bufferedWriter.write(topHours.peek().getKey()+","+topHours.peek().getValue());
	            	topHours.poll();
	            	if (i < 9)
	            		bufferedWriter.newLine();
	            	i++;
	            }
	            bufferedWriter.close();
	        }
	        catch(IOException ex) {
	            System.out.println("Error writing to file");
	        }
	}

	//Writes 10 most costly resources using a HashMap (utilizing efficient updates/retrievals
	//for two logical data pairs and a priority queue for efficient retrieval of top 10 values
	private static void writeTopTen(HashMap<String, Integer> resourceFreq, HashMap<String, Integer> resourceBytes,
			String file) {

		PriorityQueue<MappedPair> resourceConsumption = toQueue(resourceFreq, resourceBytes);
		try {
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));

			int i = 0;
           		while (i < 10 && !resourceConsumption.isEmpty()) {
            			bufferedWriter.write(resourceConsumption.peek().getKey());
            			resourceConsumption.poll();
            			if (i < 9)
            				bufferedWriter.newLine();
            			i++;
            		}
           		bufferedWriter.close();
		} catch(IOException ex) {
            		System.out.println("Error writing to file");
        	}
	}

	//Writes all blocked users as in order of processing
	private static void writeBlocked(ArrayList<String> blockedUsers, String file) {

		try {
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));

            		for (int i = 0; i < blockedUsers.size(); i++) {
            			bufferedWriter.write(blockedUsers.get(i));
            			if (i < blockedUsers.size() - 1)
            				bufferedWriter.newLine();
            		}
            	bufferedWriter.close();
        	} catch(IOException ex) {
            		System.out.println("Error writing to file");
        	}

	}

	//Returns a hash map of values mapping a starting time to the total times activity was detected during
	//hour time frame, calculated using a modified key set starting from time to time+one hour to increase efficiency
	//over simply iterating through the entire key set. After timing, the modified version took about 1/3 of
	//the time on a sample set of ~250 lines
	private static TreeMap<String, Integer> performTimeComparisons(TreeMap<String, Integer> frequencyVisited) throws ParseException {

		TreeMap<String, Integer> freq = new TreeMap<String, Integer>();
		SortedMap<String, Integer> modKeySet;
		//String startTimeCount = "01/Jul/1995:00:00:01 -0400";
		//Retrieve the first time window in map frequencyVisited, utilizing ordered keys to ensure it is smallest key
		String startTimeCount = frequencyVisited.firstEntry().getKey();
		String toReplace = startTimeCount.substring(12, 20);
		startTimeCount.replace(toReplace, "00:00:01");

		SimpleDateFormat format = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z");
		//Covert startTimeCount to Date object for comparisons
		Date start = format.parse(startTimeCount);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(start);
		calendar.add(Calendar.HOUR, 1);
		//Add one hour to startTimeCount retrieve Date object for end time
		Date end = calendar.getTime();
		//Retrieve the last time in map frequencyVisited, utilizing ordered keys to ensure it is largest key
		Date lastTimeInMap = format.parse(frequencyVisited.lastEntry().getKey());

		while (start.before(lastTimeInMap) || start.equals(lastTimeInMap)) {
			int total = 0;
			String s = format.format(start), e = format.format(end), modE = format.format(lastTimeInMap);
			//System.out.println("START: "+s+" END: "+e+" MODEND: "+modE);
			//Retrieve modified set of keys to iterate through that only include keys within the time range				//and add all of their frequencies for total times accessed during the hour time period

			if (lastTimeInMap.before(end) || lastTimeInMap.equals(end))
				modKeySet = frequencyVisited.subMap(s, true, modE, true);
			else
				modKeySet = frequencyVisited.subMap(s, true, e, true);

			for (String key2 : modKeySet.keySet())
				total += frequencyVisited.get(key2);

			//Add values to hash map of (time frame, total) pairs
			freq.put(s, total);

			//Obtain indices for next hour time frame
			calendar.setTime(start);
			calendar.add(Calendar.SECOND, 1);
			start = calendar.getTime();
			calendar.setTime(start);
			calendar.add(Calendar.HOUR, 1);
			end = calendar.getTime();
			}

		return freq;
	}

	//Returns the end index for a modified key set starting from time to time+one hour to increase efficiency
	//over simply iterating through the entire key set - after timing, the modified version took about 1/3 of
	//the time on a sample set of ~250 lines
	private static String getModifiedKeySetIndex (TreeMap<String, Integer> frequencyVisited, String time) throws ParseException {

		SimpleDateFormat format = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z");
		Date time1 = format.parse(time);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(time1);
		calendar.add(Calendar.HOUR, 1);

		return format.format(calendar.getTime());
	}

	public static PriorityQueue<MappedPair> toQueue (HashMap<String, Integer> mapToConvert) {
		PriorityQueue<MappedPair> newPriorityQueue = new PriorityQueue<MappedPair>();
		for (String key : mapToConvert.keySet()) {
			newPriorityQueue.add (new MappedPair(key, mapToConvert.get(key)));
		}
		return newPriorityQueue;
	}

	public static PriorityQueue<MappedPair> toQueue (TreeMap<String, Integer> mapToConvert) {
		PriorityQueue<MappedPair> newPriorityQueue = new PriorityQueue<MappedPair>();
		for (String key : mapToConvert.keySet()) {
			newPriorityQueue.add (new MappedPair(key, mapToConvert.get(key)));
		}
		return newPriorityQueue;
	}

	private static PriorityQueue<MappedPair> toQueue(HashMap<String, Integer> resourceFreq,
			HashMap<String, Integer> resourceBytes) {

		PriorityQueue<MappedPair> resourceConsumption = new PriorityQueue<MappedPair>();
		for (String key : resourceFreq.keySet()) {
			//Calculates bandwidth consumption by multiplying frequency by number of bytes
			resourceConsumption.add(new MappedPair(key, resourceFreq.get(key)*resourceBytes.get(key)));
        	}
		return resourceConsumption;
	}
}
