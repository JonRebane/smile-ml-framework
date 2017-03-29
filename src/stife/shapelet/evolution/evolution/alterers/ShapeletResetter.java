package stife.shapelet.evolution.evolution.alterers;

import java.util.Random;

import data_structures.Sequence;
import stife.shapelet.evolution.NShapelet;
import stife.shapelet_size2.Shapelet_Size2;

public class ShapeletResetter implements MutationStrategy<NShapelet> {

	private Random random;
	private int maxDimensionLabel;

	public ShapeletResetter(Random random, int maxDimensionLabel){
		this.random = random;
		this.maxDimensionLabel = maxDimensionLabel;
	}
	
	@Override
	public NShapelet mutate(NShapelet t) {
		Shapelet_Size2 basis = new Shapelet_Size2(
				random.nextInt(maxDimensionLabel),
				random.nextInt(maxDimensionLabel),
				random.nextInt(Sequence.NUM_RELATIONSHIPS));
		return new NShapelet(basis);
	}

}
