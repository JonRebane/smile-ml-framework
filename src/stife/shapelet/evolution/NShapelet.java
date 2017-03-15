package stife.shapelet.evolution;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import stife.shapelet_size2.Shapelet_Size2;
import stife.shapelet_size2_new.AbstractShapeletSize2;
import stife.shapelet_size2_new.ShapeletSize2;

public class NShapelet {

	List<Integer> eventIds = new ArrayList<>();
	List<Integer> relationships = new ArrayList<>();
	public static Comparator<NShapelet> nShapeletComparator = (a,b) -> a.compareToOther(b);
	
	public NShapelet(Shapelet_Size2 basis){
		eventIds.add(basis.getEventId1());
		eventIds.add(basis.getEventId2());
		relationships.add(basis.getRelationshipId());
	}

	public NShapelet(List<Integer> eventIds, List<Integer> relationships) {
		super();
		this.eventIds = eventIds;
		this.relationships = relationships;
	}

	public NShapelet deepCopy() {
		ArrayList<Integer> newEventIds = new ArrayList<Integer>();
		List<Integer> newRelationships = new ArrayList<>();
		eventIds.forEach(i -> newEventIds.add(i));
		relationships.forEach(i -> newRelationships.add(i));
		return new NShapelet(newEventIds,newRelationships);
	}

	public void append(int newEventId, int newRelationShipID) {
		eventIds.add(newEventId);
		relationships.add(newRelationShipID);
	}

	public int getEvent(int i) {
		return eventIds.get(i);
	}

	public int getRelationship(int i) {
		return relationships.get(i);
	}

	public ShapeletSize2 get2Shapelet(int i) {
		return AbstractShapeletSize2.create(eventIds.get(i),eventIds.get(i+1),relationships.get(i));
	}

	public int numTwoShapelets() {
		return relationships.size();
	}

	@Override
	public String toString(){
		return IntStream.range(0, numTwoShapelets()).boxed()
			.map(i -> get2Shapelet(i).toString())
			.collect(Collectors.toList()).toString();
	}

	public void remove2Shapelet(int i) {
		assert(relationships.size()>i && relationships.size()>1);
		eventIds.remove(i+1);
		relationships.remove(i);
		
	}

	public int getLastEventId() {
		return eventIds.get(eventIds.size()-1);
	}

	public int numEvents() {
		assert(eventIds.size()-1==relationships.size());
		return eventIds.size();
	}

	public int getEventId(int i) {
		return eventIds.get(i);
	}

	public void setEventId(int i, int newEventId) {
		eventIds.set(i, newEventId);
	}

	public int numRelationships() {
		assert(relationships.size()>0);
		return relationships.size();
	}

	public void setRelationship(int i, int newRelationship) {
		relationships.set(i, newRelationship);
	}

	public boolean isEqualTo(NShapelet other) {
		return relationships.equals(other.relationships) && eventIds.equals(other.eventIds);
	}

	public Integer compareToOther(NShapelet b) {
		if(eventIds.size() > b.eventIds.size()){
			return 1;
		} else if(eventIds.size() < b.eventIds.size()){
			return -1;
		} 
		for(int i=0;i<eventIds.size();i++){
			if(eventIds.get(i)>b.eventIds.get(i)){
				return 1;
			} else if(eventIds.get(i)<b.eventIds.get(i)){
				return -1;
			}
		}
		//if we arrive here all event ids are equal
		assert(relationships.size() == b.relationships.size());
		for(int i=0;i<relationships.size();i++){
			if(relationships.get(i)>b.relationships.get(i)){
				return 1;
			} else if(relationships.get(i)<b.relationships.get(i)){
				return -1;
			}
		}
		return 0;
		
	}
}
