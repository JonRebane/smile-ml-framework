package stife.shapelet_size2_new;

import java.util.List;
import java.util.stream.Collectors;

import data_structures.Interval;
import data_structures.Pair;
import data_structures.Sequence;

public class FollowedBy extends AbstractShapeletSize2 {

	public FollowedBy(int eventId1, int eventId2) {
		super(eventId1, eventId2);
		// TODO Auto-generated constructor stub
	}

	@Override
	public int getRelationship() {
		return FOLLOWEDBY;
	}

	@Override
	public List<Integer> getOccurrences(Sequence sequence, int firstEventIntervalId, int epsilon) {
		Interval firstInterval = sequence.getInterval(firstEventIntervalId);
		List<Pair<Integer, Interval>> potentialFOllowedBys = sequence.getIntervalsInTimeRange(firstInterval.getEnd()+epsilon+1, Integer.MAX_VALUE,firstEventIntervalId);
		return potentialFOllowedBys.stream().filter(p -> p.getSecond().getDimension()==getEventId2())
			.map(p -> p.getFirst()).collect(Collectors.toList());
	}
	
	@Override
	public String getRelationshipName() {
		return "followedBy";
	}

}
