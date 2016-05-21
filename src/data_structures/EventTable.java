package data_structures;

import java.util.List;

import stife.distance.exceptions.InvalidEventTableDimensionException;
import stife.distance.exceptions.TimeScaleException;

/***
 * This class implements the uncompressed, unsampled event-table representation of sequences to do distance calculations as described in the paper IBSM.
 * @author leon bornemann
 *
 */
public class EventTable implements SequenceRepresentation<EventTable> {

	/***
	 * contains all event vectors
	 */
	private byte[][] eventVectors;
	
	/***
	 * Constructs the event table for the given sequence
	 * @param seq
	 */
	public EventTable(Sequence seq, int numDimensions){
		eventVectors = new byte[numDimensions][seq.duration()];
		List<Interval> intervals = seq.getAllIntervals();
		for(Interval interval : intervals){
			int row = interval.getDimension()-1;
			for(int col=interval.getStart()-1;col<interval.getEnd();col++){
				eventVectors[row][col]++;
			}
		}
	}
	
	/***
	 * calculates the euclidian distance between both tables as defined in the paper about Interval Based Sequence Matching (IBSM)
	 * @param other the event Table to calculate the distance to
	 * @return
	 * @throws TimeScaleException
	 * @throws InvalidEventTableDimensionException
	 */
	public double euclidianDistance(EventTable other) throws TimeScaleException, InvalidEventTableDimensionException{
		assert(nCols() == other.nCols());
		assert(nrows() == other.nrows());
		long squaredErrorSum = 0;
		for(int row = 0;row<eventVectors.length;row++){
			for(int col = 0;col<eventVectors[row].length;col++){
				int difference = eventVectors[row][col] - other.eventVectors[row][col];
				squaredErrorSum += difference*difference;
			}
		}
		return Math.sqrt(squaredErrorSum);
	}

	public int nCols() {
		return eventVectors[0].length;
	}

	public byte[][] getEventVectors() {
		return eventVectors;
	}

	public int nrows() {
		return eventVectors.length;
	}

}
