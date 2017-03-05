package stife.shapelet_size2_new;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import data_structures.Interval;
import data_structures.Pair;
import data_structures.Sequence;

public class LeftContains extends AbstractShapeletSize2 {

	public LeftContains(int eventId1, int eventId2) {
		super(eventId1, eventId2);
		// TODO Auto-generated constructor stub
	}

	@Override
	public int getRelationship() {
		return LEFTCONTAINS;
	}

	@Override
	public List<Integer> getOccurrences(Sequence sequence, int firstEventIntervalId, int epsilon) {
		Interval firstInterval = sequence.getInterval(firstEventIntervalId);
		List<Pair<Integer, Interval>> potentialLeftContains = sequence.getIntervalsInTimeRange(firstInterval.getStart()-epsilon, firstInterval.getEnd()-epsilon,firstEventIntervalId);
		return potentialLeftContains.stream().filter(p -> p.getSecond().getDimension()==getEventId2() &&
				isEqualWithTolerance(firstInterval.getStart(), p.getSecond().getStart(), epsilon) && 
				firstInterval.getEnd() -epsilon > p.getSecond().getEnd())
			.map(p -> p.getFirst()).collect(Collectors.toList());
	}
	
	@Override
	public String getRelationshipName() {
		return "leftContains";
	}

}
