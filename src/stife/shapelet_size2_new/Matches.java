package stife.shapelet_size2_new;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import data_structures.Interval;
import data_structures.Pair;
import data_structures.Sequence;

public class Matches extends AbstractShapeletSize2 {

	public Matches(int eventId1, int eventId2) {
		super(eventId1, eventId2);
	}

	@Override
	public int getRelationship() {
		return MATCH;
	}

	@Override
	public List<Integer> getOccurrences(Sequence sequence, int intervalId, int epsilon) {
		Interval first = sequence.getInterval(intervalId);
		List<Pair<Integer,Interval>> searchSpace = sequence.getIntervalsInTimeRange(first.getStart()-epsilon,first.getEnd()+epsilon,intervalId);
		return searchSpace.stream().filter(p -> p.getSecond().getDimension()==getEventId2() &&
					isEqualWithTolerance(first.getStart(), p.getSecond().getStart(),epsilon) &&
					isEqualWithTolerance(first.getEnd(), p.getSecond().getEnd(),epsilon)
				).map(p -> p.getFirst())
				.collect(Collectors.toList());
	}
	
	@Override
	public List<Pair<Integer,Integer>> getAllOccurrences(Sequence sequence,int epsilon){
		if(getEventId1()==getEventId2()){
			List<Pair<Integer,Integer>> allOccurrences = super.getAllOccurrences(sequence, epsilon);
			return allOccurrences.stream().filter(p -> p.getFirst()<p.getSecond()).collect(Collectors.toList()); // avoid dublicate matches
		} else{
			return super.getAllOccurrences(sequence, epsilon);
		}
	}
	
	@Override
	public String getRelationshipName() {
		return "matches";
	}

}
