//Represents an edge in a finite state machine.
//Each edge has a set of words and the state that it points to.

import java.util.HashSet;

public class Edge {
	private HashSet<String> words;
	private State destination;
	
	public Edge(HashSet<String> words, State destination) {
		this.words = words;
		this.destination = destination;
	}
	
	public HashSet<String> getWords() {
		return words;
	}

	public State getDestination() {
		return destination;
	}
	
	public String toString() {
		return words.toString();
	}
}
