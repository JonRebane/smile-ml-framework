package stife.shapelet.evolution;

import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

import org.omg.Messaging.SyncScopeHelper;

import data_structures.Pair;
import stife.shapelet.evolution.evolution.alterers.MutationStrategy;

public class EvolutionEngine<T> {

	private int populationSize;
	private int numGenerations;
	private MutationStrategy<T> mutator;
	private FitnessEvaluator<T> evaluator;
	private SelectionAlgorithm<T> selectionStrategy;
	private PriorityQueue<Pair<T,Double>> bestIndividuals;
	private int numBest;

	public EvolutionEngine(int populationSize, int numGenerations,MutationStrategy<T> mutator,SelectionAlgorithm<T> selectionStrategy,FitnessEvaluator<T> evaluator,int numBest){
		this.populationSize = populationSize;
		this.numGenerations = numGenerations;
		this.mutator = mutator;
		this.evaluator = evaluator;
		this.selectionStrategy = selectionStrategy;
		this.numBest = numBest;
		bestIndividuals = new PriorityQueue<>((p1,p2) -> p1.getSecond().compareTo(p2.getSecond()));
	}
	
	public void runEvolution(List<T> initial){
		assert(initial.size()==populationSize);
		List<T> curGeneration = initial;
		for(int gen = 0; gen<numGenerations;gen++){
			System.out.println("starting generation " + gen);
			curGeneration = curGeneration.stream().map(t -> mutator.mutate(t)).collect(Collectors.toList());
			Map<T,Double> evaluatedPopulation = calculateFitnesses(curGeneration);
			updateBest(evaluatedPopulation);
			curGeneration = selectionStrategy.select(evaluatedPopulation);
		}
	}
	
	public void printBest(){
		bestIndividuals.forEach(p -> System.out.println(p));
	}

	private void updateBest(Map<T, Double> evaluatedPopulation) {
		evaluatedPopulation.keySet().forEach(k -> bestIndividuals.offer(new Pair<>(k,evaluatedPopulation.get(k))));
		//prune best:
		while(bestIndividuals.size()>numBest){
			Pair<T, Double> removed = bestIndividuals.poll();
			System.out.println("removed " + removed.getFirst() + " with fitness " + removed.getSecond() + " from best");
		}
	}

	private Map<T, Double> calculateFitnesses(List<T> curGeneration) {
		return curGeneration.stream().collect(Collectors.toMap(t -> t, t -> evaluator.getFitness(t)));
	}
	
}
