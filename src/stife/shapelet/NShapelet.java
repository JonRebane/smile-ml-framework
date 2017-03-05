package stife.shapelet;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import stife.shapelet_size2.Shapelet_Size2;
import stife.shapelet_size2_new.AbstractShapeletSize2;
import stife.shapelet_size2_new.ShapeletSize2;

public class NShapelet {

	List<Integer> eventIds = new ArrayList<>();
	List<Integer> relationships = new ArrayList<>();
	
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
		newEventIds.forEach(i -> newEventIds.add(i));
		newRelationships.forEach(i -> relationships.add(i));
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
}
