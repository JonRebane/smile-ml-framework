package stife.shapelet.evolution.evolution.alterers;

import java.util.Random;

import stife.shapelet.evolution.NShapelet;

public class ShapeletEndRemover implements MutationStrategy<NShapelet> {

	private Random random;

	public ShapeletEndRemover(Random random){
		this.random = random;
	}
	
	@Override
	public NShapelet mutate(NShapelet t) {
		NShapelet newShapelet = t.deepCopy();
		if(newShapelet.numTwoShapelets()==1){
			return newShapelet;
		} else{
			int toRemove = newShapelet.numTwoShapelets()-1;
			newShapelet.remove2Shapelet(toRemove);
			return newShapelet;
		}
	}

}
