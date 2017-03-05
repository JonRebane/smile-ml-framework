package stife.shapelet.evolution.evolution.alterers;

import java.util.Random;

import data_structures.Sequence;
import stife.shapelet.evolution.NShapelet;

public class ShapeletEndEventMutator implements MutationStrategy<NShapelet> {

	private Random random;
	private int maxEventId;

	public ShapeletEndEventMutator(Random random,int maxEventId){
		this.random = random;
		this.maxEventId =maxEventId;
	}
	
	@Override
	public NShapelet mutate(NShapelet t) {
		NShapelet newSHaplet = t.deepCopy();
		int lastId = t.getEventId(newSHaplet.numEvents()-1);
		int newEventId = lastId;
		while(newEventId == lastId){
			newEventId = random.nextInt(maxEventId);
		}
		newSHaplet.setEventId(newSHaplet.numEvents()-1,newEventId);
		return newSHaplet;
	}

}
