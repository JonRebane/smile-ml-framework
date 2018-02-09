package data_structures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import stife.distance.Event;
import stife.distance.EventType;
import stife.distance.exceptions.InvalidEventTableDimensionException;
import stife.distance.exceptions.TimeScaleException;

/***
 * This class implements the event-table representation of sequences to do distance calculations as described in the paper IBSM: Interval-Based Sequence Matching (see that paper for detailed information)
 * Instead of using the suggested pseudo-polynomial approach in the paper, this implementation reduces the space and time necessary by only storing those eventVectors in the event-table in which eventId1 change happens, and the point of time during which this change occurs
 * Small example:
 * For eventId1 sequence with only two intervals I1 = (A,1,3) and I2 = (B,5,8)
 * Instead of storing the matrix with 8 columns (cause overall sequence duration is 8) like this (as proposed by IBSM):
 * Time: | 1 2 3 4 5 6 7 8 9
 * ------------------------- 
 * A:    | 1 1 1 0 0 0 0 0 0
 * B:    | 0 0 0 0 1 1 1 1 0
 * 
 * We store:
 * 
 * Time: | 1 4 5 9
 * --------------------- 
 * A:    | 1 0 0 0 
 * B:    | 0 0 1 0 
 * 
 * which implies that all columns that are not explicitly named in the time axis are the same as the previous one. This greatly reduces memory and computation cost.
 * @author leon bornemann
 *
 */
public class CompressedEventTable implements SequenceRepresentation<CompressedEventTable> {

	/***
	 * contains all relevant event vectors
	 */
	private byte[][] eventVectors;
	
	/***
	 * contains the points of time at which the eventVectors occur.
	 * More specifically: the event Vector (column) eventVectors[][i] starts at point of time timeAxis[i] and the eventTable stays that way until timeAxis[i+1]-1. See class description for motivation and example.
	 */
	private int[] timeAxis;
	
	/***
	 * Constructs the event table for the given sequence
	 * @param seq
	 */
	public CompressedEventTable(Sequence seq, int numDimensions){
		List<Interval> intervals = seq.getAllIntervals();
		List<Event> startAndEndEvents = new ArrayList<>();
		intervals.forEach(e -> {
			startAndEndEvents.add(new Event(e.getStart(), e.getDimension(), EventType.Start));
			//we add plus one to all end times because the borders of the intervals are inclusive meaning an Interval (10,50) is still active at point of time 50, but for the following algorithm we care about the first point in time where it is no longer active, thus 51
			startAndEndEvents.add(new Event(e.getEnd()+1, e.getDimension(), EventType.End));
			});
		Collections.sort(startAndEndEvents);
		List<byte[]> eventVectorList = new ArrayList<>();
		List<Integer> timeAxisList = new ArrayList<>();
		byte[] curEventVector = new byte[numDimensions];
		//first event vector always starts at 1 (if there is no one in the list, it will remain zero
		timeAxisList.add(1);
		eventVectorList.add(curEventVector);
		for(int i=0;i<startAndEndEvents.size();i++){
			Event e = startAndEndEvents.get(i);
			if(e.getPointOfTime() == timeAxisList.get(timeAxisList.size()-1)){
				//same point of time as the previous event, just update the last event-vector:
				updateEventVector(curEventVector, e);
			} else{
				//different point of time: create new vector at new point of time and add both of these to the list
				curEventVector = Arrays.copyOf(curEventVector, curEventVector.length);
				updateEventVector(curEventVector, e);
				timeAxisList.add(e.getPointOfTime());
				eventVectorList.add(curEventVector);
			}
		}
		assert(timeAxisList.size()==eventVectorList.size());
		//copy result to matrix to allow for fast arithmetic operations:
		eventVectors = new byte[numDimensions][eventVectorList.size()];
		timeAxis = new int[timeAxisList.size()];
		for(int i=0;i<timeAxisList.size();i++){
			timeAxis[i] = timeAxisList.get(i);
		}
		for(int row=0;row<numDimensions;row++){
			for(int col = 0;col<eventVectorList.size();col++){
				eventVectors[row][col] = eventVectorList.get(col)[row];
			}
		}
		//The last vector must have all columns at zero:
		for(int i=0;i<eventVectors.length;i++){
			assert(eventVectors[i][eventVectors[0].length-1]==0);
		}
	}
	
	/***
	 * calculates the euclidian distance between both tables as defined in the paper about Interval Based Sequence Matching (IBSM)
	 * Both tables must have the same number of rows(event dimensions) and end at the same point of time, otherwise an Exception is thrown
	 * @param other the event Table to calculate the distance to
	 * @return
	 * @throws TimeScaleException
	 * @throws InvalidEventTableDimensionException
	 */
	public double euclidianDistance(CompressedEventTable other) throws TimeScaleException, InvalidEventTableDimensionException{
		byte[][] otherEventVectors = other.getEventVectors();
		int[] otherTimeAxis = other.getTimeAxis();
		//last time must be equal:
		if(otherTimeAxis[otherTimeAxis.length-1]!=timeAxis[timeAxis.length-1]){
			throw new TimeScaleException("Tables must end at the same point of time to be able to calculate euclidian distance");
		}
		if(otherEventVectors.length != eventVectors.length){
			throw new InvalidEventTableDimensionException("Tables must have the same number of rows to calculate euclidian distance");
		}
		long squaredErrorSum = 0;
		int curTime = 1;
		assert(timeAxis[0]==1 && otherTimeAxis[0]==1);
		int myColInd = 0;
		int otherColInd = 0;
		while(true){
			long squaredError = calcPartialSquaredErrorSum(eventVectors,otherEventVectors,myColInd,otherColInd);
			if(myColInd==timeAxis.length-1 || otherColInd==otherTimeAxis.length-1){
				//since both time axis end at the same point of time, we know that due to the structure of the algorithm below: if one of the column indices is at the end, the other one must be as well, we assert this:
				assert(myColInd==timeAxis.length-1 && otherColInd==otherTimeAxis.length-1);
				//we reached the very end, no multiplication necessary, distance should be zero anyway, since the last vector in every matrix should only be zeros:
				assert(squaredError==0);
				squaredErrorSum += squaredError;
				break;
			} else{
				//multiply by the time difference to the next vector and update column indices
				int myTimeDif = timeAxis[myColInd+1] - curTime;
				int othersTimeDif = otherTimeAxis[otherColInd+1] - curTime;
				//always move the index with the lower time difference to the next vector (next vector = next point of change):
				if(myTimeDif==othersTimeDif){
					//move both
					squaredErrorSum += squaredError*myTimeDif;
					myColInd++;
					otherColInd++;
					curTime = timeAxis[myColInd];
				} else if(myTimeDif > othersTimeDif){
					//move other index
					squaredErrorSum += squaredError*othersTimeDif;
					otherColInd++;
					curTime = otherTimeAxis[otherColInd];
				} else{
					//move our index
					squaredErrorSum += squaredError*myTimeDif;
					myColInd++;
					curTime = timeAxis[myColInd];
				}
			}
		}
		return Math.sqrt(squaredErrorSum);
	}

	/***
	 * Calculates the sum of the squared errors between two columns of two matrices
	 * @param matrixA first matrix
	 * @param matrixB second matrix
	 * @param aColInd column index of the first matrix
	 * @param bColInd column index of the second matrix
	 * @return
	 */
	private long calcPartialSquaredErrorSum(byte[][] eventVectors, byte[][] matrixB, int aColInd, int bColInd) {
		long squaredErrorSum = 0;
		for(int row=0;row<eventVectors.length;row++){
			int err = eventVectors[row][aColInd] - matrixB[row][bColInd];
			squaredErrorSum += err*err;
		}
		return squaredErrorSum;
	}

	private void updateEventVector(byte[] curEventVector, Event e) {
		if(e.isStart()){
			//if an event starts here we increment the counter by one
			curEventVector[e.getDimension()-1]++;
		} else{
			//an event stopped here, so we decrement the counter here
			curEventVector[e.getDimension()-1]--;
			//we always deal with starts before ends, so we can not drop below zero:
			assert(curEventVector[e.getDimension()-1]>=0);
		}
	}

	public byte[][] getEventVectors() {
		return eventVectors;
	}

	public int[] getTimeAxis() {
		return timeAxis;
	}

	public int nrows() {
		return eventVectors.length;
	}

	public int duration() {
		return timeAxis[timeAxis.length-1];
	}

	public int beginning() {
		return timeAxis[0];
	}
}
