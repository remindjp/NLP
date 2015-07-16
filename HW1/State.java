//Represents a state in a finite state machine. Each state may be a final
//state and and has a set of outbound edges.

import java.util.HashSet;

public class State {
	private HashSet<Edge> edges;
	private boolean isFinal;
	private boolean isHoliday;
	
	public State(boolean isFinal, boolean isHoliday) {
		this.edges = new HashSet<Edge>();
		this.isFinal = isFinal;
		this.isHoliday = isHoliday;
	}
	
	public State(boolean isFinal) {
		this(isFinal, false);
	}
	public State() {
		this(false, false);
	}
	
	public void addEdge(Edge e) {
		edges.add(e);
	}
	
	public HashSet<Edge> getEdges() {
		return edges;
	}
	
	public boolean isFinal() {
		return isFinal;
	}
	
	public String toString() {
		return edges.toString();
	}

	public boolean isHoliday() {
		return isHoliday;
	}
}
