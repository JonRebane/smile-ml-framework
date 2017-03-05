package stife.shapelet_size2_new;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import data_structures.Pair;
import data_structures.Sequence;

public abstract class AbstractShapeletSize2 implements ShapeletSize2 {
	
	private int eventId1;
	private int eventId2;
	
	public AbstractShapeletSize2(int eventId1, int eventId2) {
		super();
		this.eventId1 = eventId1;
		this.eventId2 = eventId2;
	}

	public int getEventId1() {
		return eventId1;
	}

	public int getEventId2() {
		return eventId2;
	}

	public static ShapeletSize2 create(Integer eventId1, Integer eventId2, Integer relationshipId) {
		if(relationshipId == MEET){
			return new Meets(eventId1,eventId2);
		} else if(relationshipId == MATCH){
			return new Matches(eventId1,eventId2);
		} else if(relationshipId == OVERLAP){
			return new Overlaps(eventId1,eventId2);
		} else if(relationshipId == CONTAINS){
			return new Contains(eventId1,eventId2);
		} else if(relationshipId == LEFTCONTAINS){
			return new LeftContains(eventId1,eventId2);
		} else if(relationshipId == RGHTCONTAINS){
			return new RightContains(eventId1,eventId2);
		} else if(relationshipId == FOLLOWEDBY){
			return new FollowedBy(eventId1,eventId2);
		} else{
			throw new AssertionError("unknown shapelet relationship ID");
		}
	}
	
	public List<Pair<Integer,Integer>> getAllOccurrences(Sequence sequence,int epsilon){	
		List<Pair<Integer,Integer>> allOccurrences = new ArrayList<>();
		for(int i = 0;i<sequence.intervalCount();i++){
			if(sequence.getInterval(i).getDimension()==getEventId1()){
				final int firstId = i;
				allOccurrences.addAll(getOccurrences(sequence,firstId, epsilon).stream()
						.map(id -> new Pair<>(firstId,id))
						.collect(Collectors.toList()));
			}
		}
		return allOccurrences;
	}
	
	protected boolean isEqualWithTolerance(int referenceTime, int otherTime, int epsilon) {
		return Math.abs(referenceTime - otherTime) <= epsilon;
	}
	
	@Override
	public String toString(){
		return "(" + eventId1 +" "+ getRelationshipName() +" "+eventId2 + ")";
	}

}
