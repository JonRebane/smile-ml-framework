package stife.shapelet_size2_new;

import java.util.List;
import java.util.stream.Collectors;

import data_structures.Interval;
import data_structures.Pair;
import data_structures.Sequence;

public class RightContains extends AbstractShapeletSize2 {

	public RightContains(int eventId1, int eventId2) {
		super(eventId1, eventId2);
		// TODO Auto-generated constructor stub
	}

	@Override
	public int getRelationship() {
		return RGHTCONTAINS;
	}

	@Override
	public List<Integer> getOccurrences(Sequence sequence, int firstEventIntervalId, int epsilon) {
		Interval firstInterval = sequence.getInterval(firstEventIntervalId);
		List<Pair<Integer, Interval>> potentialRightContains = sequence.getIntervalsInTimeRange(firstInterval.getStart()+epsilon, firstInterval.getEnd()+epsilon,firstEventIntervalId);
		return potentialRightContains.stream().filter(p -> p.getSecond().getDimension()==getEventId2() &&
				firstInterval.getStart() +epsilon < p.getSecond().getStart() &&
				isEqualWithTolerance(firstInterval.getEnd(), p.getSecond().getEnd(), epsilon))
			.map(p -> p.getFirst()).collect(Collectors.toList());
	}
	
	@Override
	public String getRelationshipName() {
		return "rightContains";
	}

}
