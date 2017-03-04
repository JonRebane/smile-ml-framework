package stife.shapelet_size2_new;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Generated;

import data_structures.Interval;
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
		List<Interval> searchSpace = sequence.getIntervalsInTimeRange(first.getStart()-epsilon,first.getEnd()-epsilon);
		List<Interval> a = searchSpace.stream().filter(i -> i.getDimension()==getEventId2() &&
				isEqualWithTolerance(first.getStart(), i.getStart(),epsilon) &&
				isEqualWithTolerance(first.getEnd(), i.getEnd(),epsilon)
				).collect(Collectors.toList());
	}

}
