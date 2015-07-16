//Jingpeng Wu NLP HW1
//This program recognizes dates using a finite state machine

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.HashSet;

public class DateRecognizer {
	//Args[0] is the name of the file.
	public static void main(String[] args) throws FileNotFoundException {
		State start = initializeFSM();
		State current = start;
		String date = "";
		Scanner s = new Scanner(new File(args[0]));
		boolean match = false;
		boolean potentialYear = false;
		
		while(s.hasNextLine()) {
			Scanner ls = new Scanner(s.nextLine());
			
			while(ls.hasNext()) {
				String word = ls.next(); 
				
				//from pervious iteration, the year was on next line.
				if(potentialYear) {
					System.out.println(word);
					if(isYear(word)) {
						date = date.replace("\n", "");
						date += word;
						potentialYear = false;
					}					
				}
					
				boolean hasComma = false;
				boolean hasPeriod = false;
				if(word.contains(",")) {
					hasComma = true;
				} else if (word.contains(".")) {
					hasPeriod = true;
				}				
				
				//if word matches an edge set, traverse edge and record word
				for(Edge e : current.getEdges()) {  
					HashSet<String> words = e.getWords();

					if(words.contains((word)) || //punctuation
						(words.contains(word.substring(0, word.length() - 1)))
								&& (hasComma || hasPeriod)) {
						current = e.getDestination();
						date += word + " ";
						match = true;
						break;
					}
					match = false;
					
				}
				
				//if no match after entering FSM, reset FSM.
				if(!match) {
					current = start;
					date = "";
				}	
				if(current.isFinal()) {
					//if there's no period, there might be a year if not a holiday
					if(!current.isHoliday()) {
						potentialYear = !hasPeriod;
					}
					if(potentialYear && ls.hasNext()) {
						String year = ls.next();
						if(isYear(year)) {
							date += year;
						}
						potentialYear = false;
					}
					
					System.out.print(date);
					
					if(!potentialYear) { //print \n only if the year is done
						System.out.println();
					}
					date ="";
					current = start;
				}
				//no matches, nothing happens
			}
			ls.close();
		}
		s.close();
	}
	
	//Initializes the data for the finite state machine and returns the starting node.
	/*The FSM constructed from the example:	 												
	 													 __________
	 													|          |
														|          E4*
														V		   |
	Start >---E1---> S1 >---E2---> S2 >---E3---> S3(Final) >-------|
	V	V												^
	|	|												|
	|	| >---E5---> S4 >---E6--------------------------|
	|	
	|---H1 (holidays.. no examples?)---> SH1 >---H2---> SH2 (Final)
	
	Also two branches for MLK and New Year's day (not shown)
		
		
		E1: day (+- st/nd/rd/th)
		E2: of
		E3: month
		E4: (+-,) year   *implemented differently due to being optional
		E5: month
		E6: day (+- st/nd/rd/th)
	*/
	
	public static State initializeFSM() {
		
		//HashSet of months
		HashSet<String> months = new HashSet<String>();
		months.add("January");
		months.add("February");
		months.add("March");
		months.add("April");
		months.add("May");
		months.add("June");
		months.add("July");
		months.add("August");
		months.add("September");
		months.add("October");
		months.add("November");
		months.add("December");
		
		//HashSet of days (+- th)
		HashSet<String> days = new HashSet<String>();
		for(int i = 1; i <= 31; i++) {
			String word = i + "";
			days.add(word);
			if(i > 3 && i < 21) {
				word += "th";
			} else if ( i % 10 == 1) {
				word += "st";
			} else if ( i % 10 == 2) {
				word += "nd";
			} else if ( i % 10 == 3) {
				word += "rd";
			} else {
				word += "th";
			}
			days.add(word);
		}
		
		//HashSet of 'of'
		HashSet<String> of = new HashSet<String>();
		of.add("of");
		
		//build FSM backwards
		
		State S3 = new State(true);
		State S2 = new State();
		S2.addEdge(new Edge(months, S3));    //E3
		State S4 = new State();
		S4.addEdge(new Edge(days, S3));      //E6
		State S1 = new State();
		S1.addEdge(new Edge(of, S2));        //E2
		State start = new State();
		start.addEdge(new Edge(days, S1));   //E1
		start.addEdge(new Edge(months, S4)); //E5
		
		//holidays
		HashSet<String> day = new HashSet<String>();
		day.add("Day");
		
		HashSet<String> holidays1 = new HashSet<String>();
		holidays1.add("Christmas");
		holidays1.add("Thanksgiving");
		holidays1.add("Veterans");
		holidays1.add("Columbus");
		holidays1.add("Labor");
		holidays1.add("Independence ");
		holidays1.add("Memorial");
		holidays1.add("President's");
		State SH2 = new State(true, true);
		State SH1 = new State(false, true);
		SH1.addEdge(new Edge(day, SH2));
		start.addEdge(new Edge(holidays1, SH1));
		
		//MLK & New Year's
		HashSet<String> martin = new HashSet<String>();
		martin.add("Martin");
		HashSet<String> luther = new HashSet<String>();
		luther.add("Luther");
		HashSet<String> king = new HashSet<String>();
		king.add("King");
		HashSet<String> n = new HashSet<String>();
		n.add("New");
		HashSet<String> years = new HashSet<String>();
		years.add("Year's");

		State M3 = new State(false, true);
		M3.addEdge(new Edge(day, SH2));
		State M2 = new State(false, true);
		M2.addEdge(new Edge(king, M3));
		State M1 = new State(false, true);
		M1.addEdge(new Edge(luther, M2));
		start.addEdge(new Edge(martin, M1));

		State N2 = new State(false, true);
		N2.addEdge(new Edge(day, SH2));
		State N1 = new State(false, true);
		N1.addEdge(new Edge(years, N2));
		start.addEdge(new Edge(n, N1));
		
		return start;
	}
	
	//returns true if the string is a year
	public static boolean isYear(String s) {
		boolean hasPunctuation = false;
		if(s.contains(".") || s.contains(",") || s.contains(";")) {
			hasPunctuation = true;
		}
		if(hasPunctuation) {
			String shortened = s.substring(0, s.length() - 1);
			if(isInteger(shortened) && shortened.length() > 3) {
				return true;
			}
		} else {
			return isInteger(s) && s.length() > 3;
		}
		return false;
	}
	
	public static boolean isInteger(String s) {
	    try { 
	        Integer.parseInt(s); 
	    } catch(NumberFormatException e) { 
	        return false; 
	    }
	    return true;
	}	
	
}
