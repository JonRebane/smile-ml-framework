package stife.shapelet_size2_new;

import java.util.List;
import java.util.stream.Collectors;

import data_structures.Interval;
import data_structures.Pair;
import data_structures.Sequence;

public class Contains extends AbstractShapeletSize2 {

	public Contains(int eventId1, int eventId2) {
		super(eventId1, eventId2);
		// TODO Auto-generated constructor stub
	}

	@Override
	public int getRelationship() {
		return CONTAINS;
	}

	@Override
	public List<Integer> getOccurrences(Sequence sequence, int firstEventIntervalId, int epsilon) {
		Interval firstInterval = sequence.getInterval(firstEventIntervalId);
		List<Pair<Integer, Interval>> potentialContains = sequence.getIntervalsInTimeRange(firstInterval.getStart()+epsilon, firstInterval.getEnd()-epsilon,firstEventIntervalId);
		return potentialContains.stream().filter(p -> p.getSecond().getDimension()==getEventId2() &&
				firstInterval.getStart() +epsilon < p.getSecond().getStart() && 
				firstInterval.getEnd() -epsilon > p.getSecond().getEnd())
			.map(p -> p.getFirst()).collect(Collectors.toList());
	}

	@Override
	public String getRelationshipName() {
		return "contains";
	}

}
