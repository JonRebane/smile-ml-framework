package stife.shapelet.evolution.evolution.alterers;

import java.util.Random;

import data_structures.Sequence;
import stife.shapelet.evolution.NShapelet;

public class ShapeletEndRelationshipMutator implements MutationStrategy<NShapelet> {

	private Random random;

	public ShapeletEndRelationshipMutator(Random random){
		this.random = random;
	}

	@Override
	public NShapelet mutate(NShapelet t) {
		NShapelet newShapelet = t.deepCopy();
		int oldRelationship = newShapelet.getRelationship(newShapelet.numRelationships()-1);
		int newRelationship = oldRelationship;
		while(newRelationship == oldRelationship){
			newRelationship = random.nextInt(Sequence.NUM_RELATIONSHIPS);
		}
		newShapelet.setRelationship(newShapelet.numRelationships()-1,newRelationship);
		return newShapelet;
	}

}
