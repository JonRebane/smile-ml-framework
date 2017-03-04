package stife.shapelet_size2_new;

import java.util.ArrayList;
import java.util.List;

import data_structures.Interval;
import data_structures.Sequence;

public class Meets extends AbstractShapeletSize2 {

	public Meets(int eventId1, int eventId2) {
		super(eventId1, eventId2);
	}

	@Override
	public int getRelationship() {
		return AbstractShapeletSize2.MEET;
	}

	@Override
	public List<Integer> getOccurrences(Sequence sequence, int intervalId, int epsilon) {
		Interval first = sequence.getInterval(intervalId);
		assert(first.getDimension()== getEventId1());
		int curIntervalId = intervalId +1;
		int endTime = first.getEnd();
		//look forward:
		List<Integer> allOccurrences = new ArrayList<>();
		while(curIntervalId <sequence.intervalCount() && isEqualWithTolerance(endTime , sequence.getInterval(curIntervalId).getStart(), epsilon)){
			Interval curInterval = sequence.getInterval(curIntervalId);
			if(curInterval.getDimension()==getEventId2()){
				allOccurrences.add(curInterval.getEnd());
			}
			curIntervalId++;
		}
		//look backwards:
		curIntervalId = intervalId-1;
		while(curIntervalId >0 && isEqualWithTolerance(endTime , sequence.getInterval(curIntervalId).getStart(), epsilon)){
			Interval curInterval = sequence.getInterval(curIntervalId);
			if(curInterval.getDimension()==getEventId2()){
				allOccurrences.add(curInterval.getEnd());
			}
			curIntervalId--;
		}
		return allOccurrences;
	}

}
