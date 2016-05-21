package representations;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;

import algorithms.Algorithms;
import distance.feature.extraction.Event;
import distance.feature.extraction.EventType;
import shapelet.extraction.Shapelet;
import shapelet.extraction.ShapeletFeatureMatrix;

/***
 * 
 * @author leon bornemann
 *
 */
public class Sequence {

	public static int getMaxDuration(List<Sequence> database) {
		int maxDuration = -1;
		for(Sequence seq : database){
			maxDuration = Math.max(maxDuration,seq.duration());
		}
		return maxDuration;
	}
	
	/***
	 * Reads all sequences in a file and returns them as an unmodifiable list
	 * @param sequenceFilePath path to the input file.
	 * @return
	 * @throws IOException
	 */
	public static List<Sequence> readSequenceData(String sequenceFilePath) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(new File(sequenceFilePath)));
		List<Sequence> sequences = new ArrayList<Sequence>();
		List<Interval> eventsOfSameSequence = new LinkedList<>();
		int curId = -1;
		while(true){
			String line = br.readLine();
			if(line==null ||line.equals("")){
				sequences.add(new Sequence(eventsOfSameSequence));
				break;
			}
			String[] tokens = line.split(" ");
			assert(tokens.length==4);
			int curSeqId = Integer.parseInt(tokens[0]);
			if(curSeqId == curId || curId==-1){
				eventsOfSameSequence.add(new Interval(line));
			} else{
				sequences.add(new Sequence(eventsOfSameSequence));
				eventsOfSameSequence.clear();
				eventsOfSameSequence.add(new Interval(line));
			}
			curId = curSeqId;
		}
		br.close();
		return Collections.unmodifiableList(sequences);
	}
	
	public static TreeSet<Integer> getDimensionSet(List<Sequence> sequences) {
		TreeSet<Integer> dimensions = new TreeSet<>();
		for(Sequence seq : sequences){
			dimensions.addAll(seq.getAllDimensions());
		}
		//small consistency test of the data, if this does not hold, we have a problem with the algorithms:
		//assert(dimensions.descendingIterator().next() == dimensions.size());
		return dimensions;
	}
	
	/***
	 * uses a table and not a list of Event-objects to optimize performance, one line in this table is basically an event object
	 * first column: event dimension, second column: start-time, third column: end-time
	 */
	private int[][] intervals;

	private boolean isSorted = false;
	
	//number of temporal relationships between intervals is fixed.
	public static final int NUM_RELATIONSHIPS = 7;
	
	public Sequence(List<Interval> intervalList) {
		assert(intervalList.size()>0);
		intervals = new int[intervalList.size()][3];
		fillIntervals(intervalList);
	}

	private void fillIntervals(List<Interval> intervalList) {
		assert(intervalList.size()==intervals.length);
		for(int i=0;i<intervalList.size();i++){
			Interval e = intervalList.get(i);
			intervals[i][0] = e.getDimension();
			intervals[i][1] = e.getStart();
			intervals[i][2] = e.getEnd();
		}
	}

	/***
	 * Creates a deep-copy of the specified sequence
	 * @param seq
	 */
	public Sequence(Sequence seq) {
		intervals = new int[seq.intervalCount()][3];
		for(int i=0;i<seq.intervals.length;i++){
			intervals[i][0] = seq.intervals[i][0];
			intervals[i][1] = seq.intervals[i][1];
			intervals[i][2] = seq.intervals[i][2];
		}
	}

	public Collection<Integer> getAllDimensions() {
		Set<Integer> dimensions = new HashSet<>();
		for(int i=0;i<intervals.length;i++){
			dimensions.add(intervals[i][0]);
		}
		return dimensions;
	}

	/***
	 * Counts all shapelet occurences in this sequence and writes it to the shapeletFeatureMatrix
	 * @param seqId the id of this sequence, aka the row in the shapeletFeatureMatrix, to which the shapelet counts will be written
	 * @param shapeletFeatureMatrix
	 * @param epsilon maximum time span that points of time may differ from each other to still be considered equal
	 */
	public void countAllShapelets(Integer seqId, ShapeletFeatureMatrix shapeletFeatureMatrix, int epsilon) {
		//epsilon:
		int e = epsilon;
		for(int i=0;i<intervals.length;i++){
			int aId = intervals[i][0];
			int aStart = intervals[i][1];
			int aEnd = intervals[i][2];
			//all events after A are always relevant
			for(int j=i+1;j<intervals.length;j++){
				int bId = intervals[j][0];
				int bStart = intervals[j][1];
				int bEnd = intervals[j][2];
				int relationshipId = getRelationship(aStart,aEnd,bStart,bEnd,e);
				shapeletFeatureMatrix.incAt(seqId, aId, bId, relationshipId);
			}
			//some events that start at the same time as A may be before A in the order, we need to consider those as well:
			for(int j=i-1;j>=0;j--){
				int bId = intervals[j][0];
				int bStart = intervals[j][1];
				int bEnd = intervals[j][2];
				if(aStart - e > bStart){
					break;
				} else{
					int relationshipId = getRelationship(aStart,aEnd,bStart,bEnd,e);
					shapeletFeatureMatrix.incAt(seqId, aId, bId, relationshipId);
				}
			}
		}
	}
	
	/***
	 * Method is only public, so unit-testing is possible
	 * @param aStart start of event A
	 * @param aStop end of event A
	 * @param bStart start of event B
	 * @param bStop end of event B
	 * @param e maximum tolerance two time values can be apart to still be considered equal
	 * @return An int ranging from 1 to 7 representing the temporal relationship between Interval A and B as follows:
	 * 1 - meet
	 * 2 - match
	 * 3 - overlap
	 * 4 - leftContains
	 * 5 - contains
	 * 6 - rightContains
	 * 7 - followedBy
	 */
	public int getRelationship(int aStart, int aStop, int bStart, int bStop, int e) {
		assert(aStart-e <=bStart);
		//Aliases to make returns more readable:
		int meet = 1; int match = 2; int overlap = 3; int leftContains = 4; int contains = 5;int rightContains = 6; int followedBy = 7;
		//results = list(meet=1,match=2,overlap=3,leftContains=4,contains=5,rightContains=6,followedBy=7)
		boolean startsMatch = bStart >= aStart - e && bStart <= aStart + e;
		boolean endsMatch = bStop >= aStop - e && bStop <= aStop + e;
		if(startsMatch){
	        // can be either left-contain, match or overlap
	        if(endsMatch){
	            return match;
	        } else if(bStop < aStop-e){
	            return leftContains;
	        } else{
	        	assert(bStop > aStop + e);
	        	return overlap;
	        }
	    } else{
	        // can be either right-contain, overlap,contain, meet or follow
	        boolean endMatchesStart = bStart >= aStop -e && bStart <= aStop+e;
	        if(endsMatch){
	            return rightContains;
	        } else if(endMatchesStart){
	            return meet;
	        } else if(bStop < aStop -e){
	            return contains;
	        } else if(bStart < aStop-e){
	        	assert(bStop > aStop + e);
	            return overlap;
	        } else{
	        	assert(bStart > aStop + e);
	            return followedBy;
	        }
	    }	
	}
	
	public int intervalCount() {
		return intervals.length;
	}

	public int duration() {
		if(!isSorted){
			int max = -1;
			for(int i=0;i<intervals.length;i++){
				if(intervals[i][2] > max){
					max = intervals[i][2];
				}
			}
			return max;
		} else{
			return intervals[intervals.length-1][2];
		}
	}
	
	public int earliestStart() {
		if(!isSorted){
			int min = Integer.MAX_VALUE;
			for(int i=0;i<intervals.length;i++){
				if(intervals[i][1] < min){
					min = intervals[i][1];
				}
			}
			return min;
		} else{
			return intervals[0][1];
		}
	}

	public Interval getInterval(int i) {
		return new Interval(intervals[i][0],intervals[i][1],intervals[i][2]);
	}

	public List<Interval> getAllIntervals() {
		List<Interval> intervalList = new ArrayList<>();
		for( int i=0;i<intervalCount();i++){
			intervalList.add(getInterval(i));
		}
		return intervalList;
	}
	
	public void rescaleTimeAxis(int newScaleMin, int newScaleMax){
		int xmin = earliestStart();
		int xmax = duration();
		for(int i=0;i<intervals.length;i++){
			intervals[i][1] = Algorithms.linearInterpolation(xmin,xmax,intervals[i][1],newScaleMin,newScaleMax);
			intervals[i][2] = Algorithms.linearInterpolation(xmin,xmax,intervals[i][2],newScaleMin,newScaleMax);
		}
	}

	public short countShapeletOccurance(Shapelet shapelet,int epsilon) {
		//TODO: Test this oh man oh man :D
		int e = epsilon;
		short count = 0;
		List<Interval> relevantIntervals = new ArrayList<>();
		for(int i=0;i<intervals.length;i++){
			int eventId = intervals[i][0];
			if(eventId == shapelet.getEventId1() || eventId==shapelet.getEventId2()){
				relevantIntervals.add(new Interval(eventId,intervals[i][1],intervals[i][2]));
			}
		}
		for(int i=0;i<relevantIntervals.size();i++){
			int aId = relevantIntervals.get(i).getDimension();
			if(aId != shapelet.getEventId1()){
				continue;
			}
			//if we get here we know event A is of the right dimension
			int aStart = relevantIntervals.get(i).getStart();
			int aEnd = relevantIntervals.get(i).getEnd();
			//all intervals after A are always relevant
			for(int j=i+1;j<relevantIntervals.size();j++){
				int bId = relevantIntervals.get(j).getDimension();
				if(bId != shapelet.getEventId2()){
					continue;
				}
				int bStart = relevantIntervals.get(j).getStart();
				int bEnd = relevantIntervals.get(j).getEnd();
				int relationshipId = getRelationship(aStart,aEnd,bStart,bEnd,e);
				if(relationshipId == shapelet.getRelationshipId()){
					count++;
				}
			}
			//some intervals that start at the same time as A may be before A in the order, we need to consider those as well:
			for(int j=i-1;j>=0;j--){
				int bId = relevantIntervals.get(j).getDimension();
				int bStart = relevantIntervals.get(j).getStart();
				int bEnd = relevantIntervals.get(j).getEnd();
				if(aStart - e > bStart){
					break;
				} else if(bId != shapelet.getEventId2()){
					continue;
				} else{
					int relationshipId = getRelationship(aStart,aEnd,bStart,bEnd,e);
					if(relationshipId == shapelet.getRelationshipId()){
						count++;
					}
				}
			}
		}
		return count;
	}

	/***
	 * Orders Intervals in this sequence according to the following scheme: startTime, (if startTime is equal: endTime), if both of these are equal, dimension
	 */
	public void sortIntervals() {
		List<Interval> intervalsSorted = getAllIntervals();
		Collections.sort(intervalsSorted, new StandardIntervalComparator());
		fillIntervals(intervalsSorted);
		isSorted  = true;
	}

	public Map<Integer, Integer> getDimensionOccurances() {
		Map<Integer,Integer> occuranceMap = new HashMap<>();
		for(int i=0;i<intervals.length;i++){
			int curDim = intervals[i][0];
			if(occuranceMap.containsKey(curDim)){
				occuranceMap.put(curDim, occuranceMap.get(curDim)+1);
			} else{
				occuranceMap.put(curDim, 1);
			}
		}
		return occuranceMap;
	}

	public long getDensity() {
		long density = 0;
		for(int i=0;i<intervals.length;i++){
			density += intervals[i][2] - intervals[i][1];
		}
		return density;
	}

	public int getMaxConcurrentIntervalCount() {
		//TODO: test this
		PriorityQueue<Integer> endTimes = new PriorityQueue<>();
		for(int i=0;i<intervals.length;i++){
			endTimes.add(intervals[i][2]);
		}
		int searchStartRow = 0;
		int maxCount = -1;
		int ends = 0;
		while(!endTimes.isEmpty()){
			Integer curEnd = endTimes.remove();
			int intervalCount = 0;
			for(int i=searchStartRow;i<intervals.length;i++){
				int curStart = intervals[i][1];
				if(curStart > curEnd || i==intervals.length-1){
					if(i==intervals.length-1){
						intervalCount = i+1-ends;
					} else{
						intervalCount = i-ends;
						searchStartRow = i;
					}
					break;
				}
			}
			ends++;
			assert(intervalCount>0 || endTimes.isEmpty());
			if(intervalCount>maxCount){
				maxCount = intervalCount;
			}	
		}
		return maxCount;
	}

	public int getConcurrentIntervalDuration(int numConcurrentIntervals) {
		// TODO test this
		List<Event> startAndStopEvents = getSortedEventList();
		int curEventsActive = 0;
		int maxDuration = 0;
		for(int i=0;i<startAndStopEvents.size();i++){
			Event curEvent = startAndStopEvents.get(i);
			if(curEvent.getEventType()==EventType.Start){
				curEventsActive++;
			} else{
				curEventsActive--;
			}
			assert(curEventsActive<=numConcurrentIntervals);
			if(curEventsActive == numConcurrentIntervals){
				Event nextEvent = startAndStopEvents.get(i+1);
				assert(nextEvent.getEventType()==EventType.End);
				int curDuration = nextEvent.getPointOfTime() - curEvent.getPointOfTime();
				if(curDuration>maxDuration){
					maxDuration = curDuration;
				}
			}
		}
		return maxDuration;
	}

	private List<Event> getSortedEventList() {
		List<Event> startAndStopEvents = new ArrayList<>();
		for(int i=0;i<intervals.length;i++){
			startAndStopEvents.add(new Event(intervals[i][1], intervals[i][0], EventType.Start));
			startAndStopEvents.add(new Event(intervals[i][2], intervals[i][0], EventType.End));
		}
		Collections.sort(startAndStopEvents);
		return startAndStopEvents;
	}

	public int getSummedPauseTime() {
		List<Event> startAndStopEvents = getSortedEventList();
		int curEventsActive = 0;
		int duration = 0;
		for(int i=0;i<startAndStopEvents.size();i++){
			Event curEvent = startAndStopEvents.get(i);
			if(curEvent.getEventType()==EventType.Start){
				curEventsActive++;
			} else{
				curEventsActive--;
			}
			assert(curEventsActive>=0);
			if(curEventsActive==0 && i!= startAndStopEvents.size()-1){
				Event nextEvent = startAndStopEvents.get(i+1);
				assert(nextEvent.getEventType()==EventType.Start);
				duration += nextEvent.getPointOfTime() - curEvent.getPointOfTime();
			}
		}
		return duration;
	}

	public void reassignDimensionIds(HashMap<Integer, Integer> newDimensionMapping) {
		for(int i=0;i<intervals.length;i++){
			intervals[i][0] = newDimensionMapping.get(intervals[i][0]).intValue();
		}
	}
}
