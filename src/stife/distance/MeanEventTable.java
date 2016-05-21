package stife.distance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import algorithms.Algorithms;
import data_structures.CompressedEventTable;
import stife.distance.exceptions.InvalidEventTableDimensionException;
import stife.distance.exceptions.TimeScaleException;

/***
 * This is some really nasty code-dublication, since this is basically the same class as EventTable, just that floats are used as the data basis.
 * Since however performance absolutely matters here since this is a DataMining application, we decided against using Generics, since they don't work with primitive types and auto-boxing is harmful to us
 * @author Leon Bornemann
 *
 */
public class MeanEventTable {

	/***
	 * contains all relevant event vectors
	 */
	private double[][] eventVectors;
	
	/***
	 * contains the points of time at which the eventVectors occur.
	 * More specifically: the event Vector (column) eventVectors[][i] starts at point of time timeAxis[i] and the eventTable stays that way until timeAxis[i+1]-1. See class description for motivation and example.
	 */
	private int[] timeAxis;

	/***
	 * Creates the mean event table of the given collection
	 * @param cluster
	 * @throws TimeScaleException 
	 */
	public MeanEventTable(Collection<CompressedEventTable> tables) throws TimeScaleException {
		Map<CompressedEventTable,Integer> tableColumnIndices = new HashMap<>(); 
		TreeSet<Integer> eventTimes = new TreeSet<>();
		int expectedDuration = tables.iterator().next().duration();
		int expectedRowCount = tables.iterator().next().nrows();
		for(CompressedEventTable table : tables){
			if(table.duration() != expectedDuration || expectedRowCount != table.nrows()){
				throw new TimeScaleException("All event tables must have the same duration");
			}
			if(table.beginning() != 1){
				throw new TimeScaleException("All Event tables must start at 1");
			}
			//we start at -1 because every index will be incremented once at the start
			tableColumnIndices.put(table,-1);
			//get all points of time during which we have changes:
			for(Integer pointOfTime : table.getTimeAxis()){
				eventTimes.add(pointOfTime);
			}
		}
		List<double[]> meanEventVectorsList = new ArrayList<>();
		List<Integer> timeAxisList = new ArrayList<>();
		Iterator<Integer> it = eventTimes.iterator();
		while(it.hasNext()){
			Integer curTime = it.next();
			//update Indices: 
			for(CompressedEventTable table : tableColumnIndices.keySet()){
				Integer curColInd = tableColumnIndices.get(table);
				//move any index who points too far to the left
				if(table.getTimeAxis().length > curColInd+1 && table.getTimeAxis()[curColInd+1] <= curTime ){
					incColIndexForTable(tableColumnIndices, table);
					assert(curColInd+2 == table.getTimeAxis().length || table.getTimeAxis()[curColInd+2] > curTime);
				}
			}
			timeAxisList.add(curTime);
			//calculate mean for this point of time:
			meanEventVectorsList.add(calculateMean(tableColumnIndices));
		}
		//copy to our data structures:
		timeAxis = new int[timeAxisList.size()];
		eventVectors = new double[meanEventVectorsList.get(0).length][timeAxisList.size()];
		for(int col=0;col<timeAxisList.size();col++){
			timeAxis[col] = timeAxisList.get(col);
			for(int row =0;row<meanEventVectorsList.get(col).length;row++){
				eventVectors[row][col] = meanEventVectorsList.get(col)[row];
			}
		}
	}

	private double[] calculateMean(Map<CompressedEventTable, Integer> tableColumnIndices) {
		int rows = tableColumnIndices.keySet().iterator().next().nrows();
		double[] mean = new double[rows];
		for(CompressedEventTable table :tableColumnIndices.keySet()){
			add(mean,table.getEventVectors(),tableColumnIndices.get(table));
		}
		Algorithms.vectorDivision(mean,rows);
		return mean;
	}

	private void add(double[] mean, byte[][] eventVectors2, Integer col) {
		assert(eventVectors2.length==mean.length);
		for(int row=0;row<mean.length;row++){
			mean[row] += eventVectors2[row][col];
		}
	}

	private void incColIndexForTable(Map<CompressedEventTable, Integer> tableColumnIndices, CompressedEventTable table) {
		tableColumnIndices.put(table,tableColumnIndices.get(table)+1);
	}

	public double[][] getEventVectors() {
		return eventVectors;
	}

	public int[] getTimeAxis() {
		return timeAxis;
	}
	
	/***
	 * SUPER UGLY: This is almost an exact copy of the code in eventTable but there is no real way to avoid this since we want to use 2-dim arrays and they must have primitives, thus we can not make the whole thing generic
	 * 
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
		double squaredErrorSum = 0;
		int curTime = 1;
		assert(timeAxis[0]==1 && otherTimeAxis[0]==1);
		int myColInd = 0;
		int otherColInd = 0;
		while(true){
			double squaredError = calcPartialSquaredErrorSum(eventVectors,otherEventVectors,myColInd,otherColInd);
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

	private double calcPartialSquaredErrorSum(double[][] centroid, byte[][] other, int centroidColInd,int otherColInd) {
		double squaredErrorSum = 0;
		for(int row=0;row<centroid.length;row++){
			double err = centroid[row][centroidColInd] - other[row][otherColInd];
			squaredErrorSum += err*err;
		}
		return squaredErrorSum;
	}
	
}
