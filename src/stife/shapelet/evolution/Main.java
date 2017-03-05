package stife.shapelet.evolution;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import data_structures.Pair;
import data_structures.Sequence;
import io.IOService;
import stife.shapelet.evolution.evolution.alterers.MutationStrategy;
import stife.shapelet.evolution.evolution.alterers.ShapeletAppender;
import stife.shapelet.evolution.evolution.alterers.ShapeletEndEventMutator;
import stife.shapelet.evolution.evolution.alterers.ShapeletEndRelationshipMutator;
import stife.shapelet.evolution.evolution.alterers.ShapeletEndRemover;
import stife.shapelet.evolution.evolution.alterers.WeightedCompositeMutator;
import stife.shapelet_size2.Shapelet_Size2;

public class Main {

	private static int epsilon = 5;
	private static int tournamentSize = 2;
	private static double p = 1.0;
	private static int populationSize = 100;
	private static Random random = new Random(13);
	private static int numGenerations = 100;

	public static void main(String[] args) throws IOException{
		//TODO: use secure random!
		File testData = new File("data/singleLabelDatasets/HEPATITIS");
		List<Sequence> database = IOService.readSequenceData(testData);
		List<Integer> classes = IOService.readClassData(testData);
		//mutator
		List<Pair<MutationStrategy<NShapelet>, Double>> operators = new ArrayList<>();
		int maxEventLabel = Sequence.getDimensionSet(database).descendingIterator().next();
		operators.add(new Pair<>(new ShapeletAppender(random, maxEventLabel ),  0.02));
		operators.add(new Pair<>(new ShapeletEndEventMutator(random, maxEventLabel ),  0.4));
		operators.add(new Pair<>(new ShapeletEndRelationshipMutator(random),  0.4));
		operators.add(new Pair<>(new ShapeletEndRemover(random),  0.02));
		
		WeightedCompositeMutator mutator = new WeightedCompositeMutator(random, operators );
		SelectionAlgorithm<NShapelet> selectionStrategy = new TournamentSelection(tournamentSize, p, random);
		FitnessEvaluator<NShapelet> evaluator = new NShapeletFitnessEvaluator(database, classes, epsilon );
		EvolutionEngine<NShapelet> engine = new EvolutionEngine<>(populationSize , numGenerations, mutator, selectionStrategy , evaluator , 75);
		engine.runEvolution(getInitialGeneration(database));
		engine.printBest();
	}

	private static List<NShapelet> getInitialGeneration(List<Sequence> database) {
		List<Integer> dims = new ArrayList<>(Sequence.getDimensionSet(database));
		return IntStream.range(0, populationSize).boxed()
			.map(i -> {
				Shapelet_Size2 basis = new Shapelet_Size2(
						dims.get(random.nextInt(dims.size())),
						dims.get(random.nextInt(dims.size())),
						random.nextInt(Sequence.NUM_RELATIONSHIPS));
				return new NShapelet(basis);
			})
			.collect(Collectors.toList());
	}
}
