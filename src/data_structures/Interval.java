package data_structures;

/***
 * Basic data class representing an interval
 * @author leon bornemann
 *
 */
public class Interval {

	private int dimension;
	private int start;
	private int end;
	
	public int getDimension() {
		return dimension;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	public Interval(int dimension, int start, int end) {
		super();
		this.dimension = dimension;
		this.start = start;
		this.end = end;
	}

	/***
	 * Constructs an event from a line read from a text-file
	 * 
	 * @param line
	 */
	public Interval(String line) {
		String[] tokens = line.split("\\s");
		assert(tokens.length == 4);
		//first token is sequence id, which is not needed since this interval will be part of only one sequence-object
		this.dimension = Integer.parseInt(tokens[1]);
		this.start = Integer.parseInt(tokens[2]);
		this.end = Integer.parseInt(tokens[3]);
	}

	public Interval deepCopy() {
		return new Interval(dimension,start,end);
	}
	
	@Override
	public String toString(){
		return "(" + dimension +","+ start + ","+end +")";
	}
}
