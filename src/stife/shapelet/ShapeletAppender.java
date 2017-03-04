package stife.shapelet;

import java.util.Random;

import data_structures.Sequence;
import stife.shapelet_size2.Shapelet_Size2;

public class ShapeletAppender implements MutationStrategy<NShapelet> {

	private Random random;
	private int maxEventId;

	public ShapeletAppender(Random random,int maxEventId){
		this.random = random;
		this.maxEventId =maxEventId;
	}
	
	@Override
	public NShapelet mutate(NShapelet t) {
		NShapelet newSHaplet = t.deepCopy();
		int newEventId = random.nextInt(maxEventId);
		int newRelationShipID = random.nextInt(Sequence.NUM_RELATIONSHIPS);
		newSHaplet.append(newEventId,newRelationShipID);
		return newSHaplet;
	}

}
