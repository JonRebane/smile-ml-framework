package stife.shapelet_size2_new;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import data_structures.Interval;
import data_structures.Pair;
import data_structures.Sequence;

public class Overlaps extends AbstractShapeletSize2 {

	public Overlaps(int eventId1, int eventId2) {
		super(eventId1, eventId2);
	}

	@Override
	public int getRelationship() {
		return OVERLAP;
	}

	@Override
	public List<Integer> getOccurrences(Sequence sequence, int firstEventIntervalId, int epsilon) {		
		Interval firstInterval = sequence.getInterval(firstEventIntervalId);
		List<Interval> allIntervals = sequence.getAllIntervals();
		List<Integer> allOverlapIds = new ArrayList<>();
		for(int i = firstEventIntervalId+1;i<allIntervals.size();i++){
			Interval curInterval = allIntervals.get(i);
			if(curInterval.getStart() >= firstInterval.getEnd()-epsilon){
				break;
			}
			if(curInterval.getDimension() == getEventId2() && curInterval.getStart() > firstInterval.getStart() + epsilon && curInterval.getEnd() > firstInterval.getEnd()+epsilon){
				allOverlapIds.add(i);
			}
		}
		return allOverlapIds;
	}
	
	@Override
	public String getRelationshipName() {
		return "overlaps";
	}

}
