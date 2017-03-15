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
import experiment.ExperimentUtil;
import experiment.classifier.ClassificationException;
import io.IOService;
import stife.distance.exceptions.InvalidEventTableDimensionException;
import stife.distance.exceptions.TimeScaleException;
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
	private static int populationSize = 500;
	private static int numFeatures = 75;
	private static Random random = new Random(13);
	private static int numGenerations = 200;
	private static File experimentResultFile = new File("experimentResults/shapeletEvolutionResult.csv");

	public static void main(String[] args) throws Exception{
		//TODO: use secure random!
		String datasetName = "HEPATITIS";
		File testData = new File("data/singleLabelDatasets/"+datasetName);
		List<Sequence> database = IOService.readSequenceData(testData);
		List<Integer> classIds = IOService.readClassData(testData);
		//mutator
		List<Pair<MutationStrategy<NShapelet>, Double>> operators = new ArrayList<>();
		int maxEventLabel = Sequence.getDimensionSet(database).descendingIterator().next();
		//training and test data split
		List<Integer> allIndices = ExperimentUtil.getShuffledIndices(database, random);
		int k=10;
		ArrayList<ExperimentResult> allResults = new ArrayList<>();
		for(int i=0;i<k;i++){
			ExperimentResult result = executeFold(database, classIds, operators, maxEventLabel, allIndices, i,k);
			allResults.add(result);
		}
		double avgEvolvedAccuracy = allResults.stream().mapToDouble(d -> d.getEvolvedAccuracy()).average().getAsDouble();
		double avgExhaustiveAccuracy = allResults.stream().mapToDouble(d -> d.getExhaustiveAccuracy()).average().getAsDouble();
		double avgNum2Shapelets = allResults.stream().mapToInt(d -> d.getNumSize2ShapeletsInEvolved()).average().getAsDouble();
		appendToResultFile(datasetName,avgEvolvedAccuracy,avgExhaustiveAccuracy,avgNum2Shapelets);
	}

	private static void appendToResultFile(String datasetName,double evolvedAccuracy,double exhaustiveAccuracy,double numSize2ShapeletsInEvolved) throws IOException {
		if(!experimentResultFile.exists()){
			String header = "dataset,populationSize,numGenerations,numFeatures,avgNumSize2ShapeletsInEvolved,tournamentSize,tournamentP,epsilon,evolvedRFAccuracy,exhaustiveRFAccuracy";
			IOService.appendLineToFile(experimentResultFile,header);
		}
		String results = datasetName + ","
				+ populationSize + ","
				+ numGenerations + ","
				+ numFeatures + ","
				+ numSize2ShapeletsInEvolved + ","
				+ tournamentSize + ","
				+ p + ","
				+ epsilon + ","
				+ evolvedAccuracy + ","
				+ exhaustiveAccuracy;
		IOService.appendLineToFile(experimentResultFile,results);
		
	}

	private static ExperimentResult executeFold(List<Sequence> database, List<Integer> classIds,
			List<Pair<MutationStrategy<NShapelet>, Double>> operators, int maxEventLabel, List<Integer> allIndices,
			int foldNum, int numFolds) throws Exception, TimeScaleException, InvalidEventTableDimensionException,
			ClassificationException, IOException {
		List<Integer> trainIndices = ExperimentUtil.getTrainingIndices(allIndices, foldNum,numFolds);
		List<Sequence> train = ExperimentUtil.getAll(database,trainIndices);
		List<Integer> trainClassIds = ExperimentUtil.getAll(classIds,trainIndices);
		List<Integer> testIndices = ExperimentUtil.getTestIndices(allIndices,trainIndices);
		List<Sequence> test = ExperimentUtil.getAll(database,testIndices);
		List<Integer> testClassIds = ExperimentUtil.getAll(classIds,testIndices);
		
		
		operators.add(new Pair<>(new ShapeletAppender(random, maxEventLabel ),  0.25));
		operators.add(new Pair<>(new ShapeletEndEventMutator(random, maxEventLabel ),  0.25));
		operators.add(new Pair<>(new ShapeletEndRelationshipMutator(random),  0.25));
		operators.add(new Pair<>(new ShapeletEndRemover(random),  0.25));
		
		WeightedCompositeMutator mutator = new WeightedCompositeMutator(random, operators );
		SelectionAlgorithm<NShapelet> selectionStrategy = new TournamentSelection(tournamentSize, p, random);
		FitnessEvaluator<NShapelet> evaluator = new NShapeletFitnessEvaluator(train, trainClassIds, epsilon );
		EvolutionEngine<NShapelet> engine = new EvolutionEngine<>(populationSize , numGenerations, mutator, selectionStrategy , evaluator , numFeatures,NShapelet.nShapeletComparator);
		engine.runEvolution(getInitialGeneration(train));
		ShapeletRf a = new ShapeletRf(engine.getBestFeatures(),train,trainClassIds,epsilon);
		int numCorrect = 0;
		System.out.println(testIndices);
		for(int i=0;i<test.size();i++){
			Integer result = a.classify(test.get(i));
			Integer actualClass = testClassIds.get(i);
			if(result == actualClass){
				numCorrect++;
			}
		}
		double accuracy = numCorrect / ((double) test.size());
		System.out.println("accuracy: " + accuracy);
		int num2Shapelets = (int) engine.getBestFeatures().stream().filter(sh -> sh.numTwoShapelets()==1).count();
		double exhaustiveAccuracy = 0.0; //TODO: change next!
		return new ExperimentResult(accuracy, exhaustiveAccuracy, num2Shapelets);
	}

	private static List<NShapelet> getInitialGeneration(List<Sequence> database) {
		List<Integer> dims = new ArrayList<>(Sequence.getDimensionSet(database));
		List<NShapelet> shapelets = new ArrayList<>();
		for(int i=0;i<populationSize;i++){
			Shapelet_Size2 basis = new Shapelet_Size2(
					dims.get(random.nextInt(dims.size())),
					dims.get(random.nextInt(dims.size())),
					random.nextInt(Sequence.NUM_RELATIONSHIPS));
			shapelets.add(new NShapelet(basis));
		}
		return shapelets;
//		return IntStream.range(0, populationSize).boxed()
//			.map(i -> {
//				Shapelet_Size2 basis = new Shapelet_Size2(
//						dims.get(random.nextInt(dims.size())),
//						dims.get(random.nextInt(dims.size())),
//						random.nextInt(Sequence.NUM_RELATIONSHIPS));
//				return new NShapelet(basis);
//			})
//			.collect(Collectors.toList());
	}
}
