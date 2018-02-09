package stife.shapelet.evolution;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeSet;
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
	private TreeSet<Pair<T,Double>> bestIndividuals;
	private int numBest;
	
	private Map<Integer,List<T>> allBest = new HashMap<>();
	
	private Comparator<Pair<T, Double>> pairComparator;
	


	public EvolutionEngine(int populationSize, int numGenerations,MutationStrategy<T> mutator,SelectionAlgorithm<T> selectionStrategy,FitnessEvaluator<T> evaluator,int numBest,Comparator<T> comparator){
		this.populationSize = populationSize;
		this.numGenerations = numGenerations;
		this.mutator = mutator;
		this.evaluator = evaluator;
		this.selectionStrategy = selectionStrategy;
		this.numBest = numBest;
		pairComparator = (p1,p2) -> {
			int doubleCompare = p1.getSecond().compareTo(p2.getSecond());
			if(doubleCompare!=0){
				return doubleCompare;
			} else{
				return comparator.compare(p1.getFirst(),p2.getFirst());
			}
		};
		bestIndividuals = new TreeSet<>(pairComparator);
	}
	
	public void runEvolution(List<T> initial){
		assert(initial.size()==populationSize);
		List<T> curGeneration = initial;
		for(int gen = 0; gen<numGenerations;gen++){
			//System.out.println("generation " + gen + " before mutation:  " +curGeneration);
			//for(int i=0;i<curGeneration.size();i++){
		//		curGeneration.set(i, mutator.mutate(curGeneration.get(i)));
	//		}
			//System.out.println("generation " + gen + " after mutation:  " +curGeneration);
			//curGeneration = curGeneration.stream().map(t -> mutator.mutate(t)).collect(Collectors.toList());
			Map<T,Double> evaluatedPopulation = calculateFitnesses(curGeneration);
			updateBest(evaluatedPopulation,gen);
			//System.out.println("best of generation " + gen + ":  " +bestIndividuals.size());
			curGeneration = new ArrayList<>(selectionStrategy.select(evaluatedPopulation));
		}
		System.out.println("finished Evolution");
	}
	
	public void printBest(){
		bestIndividuals.stream().sorted((p1,p2) -> p1.getSecond().compareTo(p2.getSecond())).forEachOrdered(p -> System.out.println(p));
	}

	private void updateBest(Map<T, Double> evaluatedPopulation, int gen) {
		evaluatedPopulation.keySet().forEach(k -> bestIndividuals.add(new Pair<>(k,evaluatedPopulation.get(k))));
		//prune best:
		while(bestIndividuals.size()>numBest){
			bestIndividuals.remove(bestIndividuals.first());
		}
		allBest.put(gen,getBestFeatures());
	}

	private Map<T, Double> calculateFitnesses(List<T> curGeneration) {
		return curGeneration.stream().collect(Collectors.toMap(t -> t, t -> evaluator.getFitness(t)));
	}

	public List<T> getBestFeatures() {
		return bestIndividuals.stream().sorted(pairComparator).map(p -> p.getFirst()).collect(Collectors.toList());
	}
	
	public Map<Integer, List<T>> getAllBest(){
		return allBest;
	}
	
}
