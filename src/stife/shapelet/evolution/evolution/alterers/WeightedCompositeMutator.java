package stife.shapelet.evolution.evolution.alterers;

import java.util.List;
import java.util.Map;
import java.util.Random;

import data_structures.Pair;
import stife.shapelet.evolution.NShapelet;

public class WeightedCompositeMutator implements MutationStrategy<NShapelet> {

	private Random random;
	private List<Pair<MutationStrategy<NShapelet>, Double>> operators;
	private double totalWeight;

	public WeightedCompositeMutator(Random random,List<Pair<MutationStrategy<NShapelet>,Double>> operators){
		this.random = random;
		this.operators = operators;
		totalWeight = operators.stream().mapToDouble(p -> p.getSecond()).sum();
	}

	@Override
	public NShapelet mutate(NShapelet t) {
		int randomIndex = -1;
		double random = Math.random() * totalWeight;
		for (int i = 0; i < operators.size(); ++i)
		{
		    random -= operators.get(i).getSecond();
		    if (random <= 0.0d)
		    {
		        randomIndex = i;
		        break;
		    }
		}
		MutationStrategy<NShapelet> chosenStrategy = operators.get(randomIndex).getFirst();
		return chosenStrategy.mutate(t);
	}

}
