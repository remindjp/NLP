import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.HashMap;

//Jingpeng Wu
//NLP HW2


public class Bigram {
	private static int totalWords;
	private static HashMap<String, Integer> count; // unique count
	private static HashMap<String, Double> smoothed; // used to hold 0->1 addOne smoothing values
	private static Double turingValue; // used to hold turing 0->1 smoothing prob
	
	public static void main(String[] args) throws FileNotFoundException {
		System.out.println(System.getProperty("sun.arch.data.model")) ;
		totalWords = 0;
		turingValue = 0.0;
		
		//*** Corpus Bigram Creation *** 
		//raw counts
		HashMap<String, HashMap<String, Double>> bigram = parseCorpus("test.txt");
		//System.out.println(bigram);
		//percents instead of raw counts
		HashMap<String, HashMap<String, Double>> percent = getPercent(deepCopy(bigram));
		//System.out.println(percent);
		//bigram after add one smoothing except for the 0 to 1 case
		HashMap<String, HashMap<String, Double>> smooth = addOneSmoothing(deepCopy(bigram));
		//the 0 to 1 case
		HashMap<String, Double> smoothed = smoothingValues();
		//System.out.println(smoothed);
		//bigram after turing discounting
		HashMap<String, HashMap<String, Double>> turing = turingDiscounting(deepCopy(bigram), "test");
		//System.out.println(turing);
		//System.out.println("turing value " + turingValue.get("test"));	
		
		
		System.out.println("Probabilities of the sentences using the corpus bigram");
		Scanner s = new Scanner(new File("s1.txt"));
		String line = s.nextLine(); //Assumes one line
		double P0_s1 = compare(line, bigram, 0);
		System.out.println("Probability of S1 with no smoothing:" + P0_s1);
		
		double P1_s1 = compare(line, smooth, 1);
		System.out.println("Probability of S1 with addOne smoothing:" + P1_s1);
		
		double P2_s1 = compare(line, turing, 2);
		System.out.println("Probability of S1 with turing smoothing:" + P2_s1);
		System.out.println();
		
		Scanner s2 = new Scanner(new File("s2.txt"));
		String line2 = s2.nextLine(); //Assumes one line
		double P0_s2 = compare(line2, bigram, 0);
		System.out.println("Probability of S2 with no smoothing:" + P0_s2);
		
		double P1_s2 = compare(line2, smooth, 1);
		System.out.println("Probability of S2 with addOne smoothing:" + P1_s2);
		
		double P2_s2 = compare(line2, turing, 2);
		System.out.println("Probability of S2 with turing smoothing:" + P2_s2);
		System.out.println();		
		
	}
	
	//compares the string sentence with the bigram to find probability
	// type: 0 = no smoothing, 1: addOne, 2:turing
	// the type is needed since the 0 -> 1 smoothing case is stored once instead of n times
	public static double compare(String s, HashMap<String, HashMap<String, Double>> bigram,
			int type) throws FileNotFoundException {
		Scanner scan = new Scanner(s);
		double p = 0.0;
		String prev = scan.next();
		//initial word probability
		if(type == 0) {
			p += (double)count.get(prev) / totalWords;
		} else if (type == 1) {
			p += (double)count.get(prev) / (totalWords + count.size());
		} else { // type == 2
			//assume initial is the same for turing
			p += (double)count.get(prev) / totalWords;
		}
		
		while(scan.hasNext()) {
			String word = scan.next();
			HashMap<String, Double> temp = bigram.get(prev);
			if(type == 0) {
				if(temp.containsKey(word)) {
					p *= temp.get(word);
				} else {
					//return 0
					return 0.0;
				}
			} else if (type == 1) {
				if(temp.containsKey(word)) {
					p *= temp.get(word);
				} else {
					//use smoothed value
					p *= smoothed.get(word);
				}				
			} else {
				if(temp.containsKey(word)) {
					p *= temp.get(word);
				} else {
					//return 0
					p *= turingValue;
				}
			}
			prev = word;
		}
		
		return p;
	}
	
	//Parses the corpus into a bigram
	public static HashMap<String, HashMap<String, Double>> parseCorpus(String filename) throws FileNotFoundException {
		Scanner s = new Scanner(new File(filename));
		
		//get rid of \n
		StringBuffer sb = new StringBuffer();
		while(s.hasNextLine()) {
			sb.append(s.nextLine());
			sb.append(" ");
		}
		s.close();
		
		//bigram & word count maps
		HashMap<String, HashMap<String, Double>> bigram = new HashMap<String, HashMap<String, Double>>();
		count = new HashMap<String, Integer>();
		
		Scanner sbs = new Scanner(sb.toString());
		String prev = sbs.next();
		count.put(prev, 1);
		
		while(sbs.hasNext()) {
			String next = sbs.next();
			totalWords++;
			if(count.containsKey(next)) { // old word
				count.put(next, count.get(next) + 1);
			} else { //new word
				count.put(next, 1);
			}
			if(bigram.containsKey(prev)) { // first word is there
				HashMap<String, Double> temp = bigram.get(prev);
				if(temp.containsKey(next)) { // second word is there
					temp.put(next, temp.get(next) + 1);
				} else { // second word is not there
					temp.put(next, 1.0);
				}
			}  else { // first word not there
				HashMap<String, Double> temp = new HashMap<String, Double>();
				temp.put(next, 1.0);
				bigram.put(prev, temp);
			}
			prev = next;
		}	
		
		sbs.close();

		return bigram;
	}
	
	//returns the percentages for a bigram without smoothing
	public static HashMap<String, HashMap<String, Double>> getPercent(HashMap<String, HashMap<String, Double>> bigram) {
		HashMap<String, HashMap<String, Double>> p = new HashMap<String, HashMap<String, Double>>(bigram);
		//compute percentages. P(a|b) = count of bigram(a,b) / count (b)
		for(String k1 : p.keySet()) {
			HashMap<String, Double> temp = p.get(k1);
			for(String k2 : temp.keySet()) {
				temp.put(k2, temp.get(k2) / count.get(k1));
			}
		}
		return p;
	}
	//Returns a bigram with add one smoothing
	public static HashMap<String, HashMap<String, Double>> addOneSmoothing(HashMap<String, HashMap<String, Double>> bigram) {
		HashMap<String, HashMap<String, Double>> smooth = new HashMap<String, HashMap<String, Double>>(bigram);
		//add one to every bigram combo
		for(String k1 : smooth.keySet()) {
			HashMap<String, Double> temp = smooth.get(k1);
			for(String k2 : smooth.keySet()) {
				if(temp.keySet().contains(k2)) { // old
					temp.put(k2, (temp.get(k2) + 1) / (count.get(k1) + count.size()));
				} else { // new
					//Instead of storing all the values, just store it once since its the same if the word
					//is not present.
					//temp.put(k2, 1.0 / (count.get(k1) + count.size()));
				}
			}
		}
		return smooth;
	}
	
	//Stores the value of any smoothed 0 - > 1 entries for a word in the bigram since they are identical
	public static HashMap<String, Double> smoothingValues() {
		smoothed = new HashMap<String, Double>();
		for(String k : count.keySet()) {
			smoothed.put(k, 1.0 / (count.get(k) + count.size()));
		}
		return smoothed;
	}
	
	
	//Returns a bigram with Turing Discounting
	public static HashMap<String, HashMap<String, Double>> turingDiscounting(HashMap<String, 
			HashMap<String, Double>> bigram, String name) {
		HashMap<String, HashMap<String, Double>> turing = new HashMap<String, HashMap<String, Double>>(bigram);

		//Maps the bigram count to frequency, ex (1,10) means 10 bigrams have 1 frequency.
		TreeMap<Integer, Integer> freq = new TreeMap<Integer, Integer>();
		for(String k1 : turing.keySet()) {
			HashMap<String, Double> temp = turing.get(k1);
			for(String k2 : temp.keySet()) {
				int c = temp.get(k2).intValue();
				if(freq.containsKey(c)) { // old
					freq.put(c, freq.get(c) + 1);
				} else { // new
					freq.put(c, 1);
				}
			}
		}
		
		//Note, for the 0's the new probability is just N1/N
		int N = 0;
		for(int k1 : freq.keySet()) {
			N += k1;
		}
		turingValue = (double)freq.firstKey();
				
				
				
		for(String k1 : turing.keySet()) {
			HashMap<String, Double> temp = turing.get(k1);
			for(String k2 : temp.keySet()) {
				int value = temp.get(k2).intValue();
				if(value < freq.lastKey()) { // don't smooth if not max
					int valueNext = freq.higherKey(value);
					int nNext = freq.get(valueNext);
					temp.put(k2, (double)valueNext * nNext / (value * totalWords));
				} else { //for max just do normally
					temp.put(k2, temp.get(k2) / count.get(k1));
				}
				
			}
		}
		return turing;
	}
	
	//Creates a deep copy since normal copy methods fail with nested hash map
	public static HashMap<String, HashMap<String, Double>> deepCopy(HashMap<String, HashMap<String, Double>> a) {
		HashMap<String, HashMap<String, Double>> b = new HashMap<String, HashMap<String, Double>>();
		for(String k1 : a.keySet()) {
			HashMap<String, Double> temp = a.get(k1);
			HashMap<String, Double> bTwo = new HashMap<String, Double>();
			for(String k2: temp.keySet()) {
				bTwo.put(k2, temp.get(k2));
			}
			b.put(k1, bTwo);
		}
		return b;
	}
	
}
