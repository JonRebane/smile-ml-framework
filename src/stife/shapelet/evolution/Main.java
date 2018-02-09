package stife.shapelet.evolution;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
import stife.shapelet.evolution.evolution.alterers.ShapeletResetter;
import stife.shapelet.evolution.evolution.alterers.SmartShapeletAppender;
import stife.shapelet.evolution.evolution.alterers.WeightedCompositeMutator;
import stife.shapelet_size2.Shapelet_Size2;

public class Main {

	private static int epsilon = 5;
	private static int tournamentSize = 2;
	private static double p = 1.0;
	private static int populationSize = 500;
	private static int numFeatures = 75;
	private static Random random;
	private static int numGenerations = 200;
	private static File experimentResultFile = new File("experimentResults/shapeletEvolutionResult.csv");
	private static int sequenceDuration;

	public static void main(String[] args) throws Exception{
		//TODO: use secure random!
		List<File> allFiles = Arrays.stream(new File("data/singleLabelDatasets/").listFiles()).collect(Collectors.toList());
		for(File file :allFiles.stream().filter(f -> "BLOCKS".equals(f.getName())).collect(Collectors.toList())){
//			if(!Arrays.asList("AUSLAN2","BLOCKS","CONTEXT").contains(file.getName())){
				random = new Random(13);
				System.out.println("starting " + file.getName());
				runExperiment(file.getName());
//			}
		}
		allFiles = Arrays.stream(new File("data/multiLabelDatasets/").listFiles()).collect(Collectors.toList());
//		for(File file :allFiles){
//			random = new Random(13);
//			System.out.println("starting " + file.getName());
//			runMultiClassExperiment(file.getName());
//		}
	}

	private static void runMultiClassExperiment(String datasetName) throws TimeScaleException, InvalidEventTableDimensionException, ClassificationException, Exception {
		File testData = new File("data/multiLabelDatasets/"+datasetName);
		List<Sequence> database = IOService.readMultiLabelSequenceData(testData);
		sequenceDuration = Sequence.getMaxDuration(database);
		List<List<Integer>> classIds = IOService.readMultiLabelClassData(testData);
		runMultiClassExperiment(datasetName, database, classIds);
	}

	private static void runExperiment(String datasetName) throws IOException, Exception, TimeScaleException,
			InvalidEventTableDimensionException, ClassificationException {
		File testData = new File("data/singleLabelDatasets/"+datasetName);
		List<Sequence> database = IOService.readSequenceData(testData);
		sequenceDuration = Sequence.getMaxDuration(database);
		List<Integer> classIds = IOService.readClassData(testData);
		runExperiment(datasetName, database, classIds);
	}

	private static void runExperiment(String datasetName, List<Sequence> database, List<Integer> classIds)
			throws Exception, TimeScaleException, InvalidEventTableDimensionException, ClassificationException,
			IOException {
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
			//ExperimentResult exhaustiveResult = executeExhaustiveFold(database,classIds,maxEventLabel,allIndices,i,k);
			//result.setExhaustiveAccuracy(exhaustiveResult.getExhaustiveAccuracy());
			//result.setExhaustiveTrainingTime(exhaustiveResult.getExhaustiveTrainingTime());
		}
		double avgEvolvedAccuracy = allResults.stream().mapToDouble(d -> d.getEvolvedAccuracy()).average().getAsDouble();
		double avgExhaustiveAccuracy = allResults.stream().mapToDouble(d -> d.getExhaustiveAccuracy()).average().getAsDouble();
		double avgNum2Shapelets = allResults.stream().mapToInt(d -> d.getNumSize2ShapeletsInEvolved()).average().getAsDouble();
		double avgEvolvedTime = allResults.stream().mapToLong(r -> r.getTrainingTime()).average().getAsDouble();
		double avgExhaustiveTime = allResults.stream().mapToLong(r -> r.getExhaustiveTrainingTime()).average().getAsDouble();
		appendToResultFile(datasetName,avgEvolvedAccuracy,avgExhaustiveAccuracy,avgNum2Shapelets,avgEvolvedTime,avgExhaustiveTime);
	}

	private static void runMultiClassExperiment(String datasetName, List<Sequence> database, List<List<Integer>> classIds)
			throws Exception, TimeScaleException, InvalidEventTableDimensionException, ClassificationException,
			IOException {
		//mutator
		List<Pair<MutationStrategy<NShapelet>, Double>> operators = new ArrayList<>();
		int maxEventLabel = Sequence.getDimensionSet(database).descendingIterator().next();
		//training and test data split
		List<Integer> allIndices = ExperimentUtil.getShuffledIndices(database, random);
		int k=10;
		ArrayList<ExperimentResult> allResults = new ArrayList<>();
		for(int i=0;i<k;i++){
			ExperimentResult result = executeMultiClassFold(database, classIds, operators, maxEventLabel, allIndices, i,k);
			allResults.add(result);
			//ExperimentResult exhaustiveResult = executeMultiClassExhaustiveFold(database,classIds,maxEventLabel,allIndices,i,k);
			//result.setExhaustiveAccuracy(exhaustiveResult.getExhaustiveAccuracy());
			//result.setExhaustiveTrainingTime(exhaustiveResult.getExhaustiveTrainingTime());
		}
		double avgEvolvedAccuracy = allResults.stream().mapToDouble(d -> d.getEvolvedAccuracy()).average().getAsDouble();
		double avgExhaustiveAccuracy = allResults.stream().mapToDouble(d -> d.getExhaustiveAccuracy()).average().getAsDouble();
		double avgNum2Shapelets = allResults.stream().mapToInt(d -> d.getNumSize2ShapeletsInEvolved()).average().getAsDouble();
		double avgEvolvedTime = allResults.stream().mapToLong(r -> r.getTrainingTime()).average().getAsDouble();
		double avgExhaustiveTime = allResults.stream().mapToLong(r -> r.getExhaustiveTrainingTime()).average().getAsDouble();
		appendToResultFile(datasetName,avgEvolvedAccuracy,avgExhaustiveAccuracy,avgNum2Shapelets,avgEvolvedTime,avgExhaustiveTime);
	}


	private static ExperimentResult executeExhaustiveFold(List<Sequence> database, List<Integer> classIds, int maxEventLabel,List<Integer> allIndices, int foldNum, int numFolds) throws Exception {
		List<Integer> trainIndices = ExperimentUtil.getTrainingIndices(allIndices, foldNum,numFolds);
		List<Sequence> train = ExperimentUtil.getAll(database,trainIndices);
		List<Integer> trainClassIds = ExperimentUtil.getAll(classIds,trainIndices);
		List<Integer> testIndices = ExperimentUtil.getTestIndices(allIndices,trainIndices);
		List<Sequence> test = ExperimentUtil.getAll(database,testIndices);
		List<Integer> testClassIds = ExperimentUtil.getAll(classIds,testIndices);
		long before = ExperimentUtil.getCpuTime();
		ShapeletSize2RF a = new ShapeletSize2RF(train,trainClassIds,maxEventLabel,epsilon,numFeatures);
		long after = ExperimentUtil.getCpuTime();
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
		return new ExperimentResult(0.0, accuracy, 0, after-before,after-before);
	}

	private static ExperimentResult executeMultiClassExhaustiveFold(List<Sequence> database, List<List<Integer>> classIds, int maxEventLabel,List<Integer> allIndices, int foldNum, int numFolds) throws Exception {
		List<Integer> trainIndices = ExperimentUtil.getTrainingIndices(allIndices, foldNum,numFolds);
		List<Sequence> train = ExperimentUtil.getAll(database,trainIndices);
		List<List<Integer>> trainClassIds = ExperimentUtil.getAll(classIds,trainIndices);
		List<Integer> testIndices = ExperimentUtil.getTestIndices(allIndices,trainIndices);
		List<Sequence> test = ExperimentUtil.getAll(database,testIndices);
		List<List<Integer>> testClassIds = ExperimentUtil.getAll(classIds,testIndices);
		long before = ExperimentUtil.getCpuTime();
		ShapeletSize2RF a = new MultiClassShapeletSize2RF(train,trainClassIds,maxEventLabel,epsilon,numFeatures);
		long after = ExperimentUtil.getCpuTime();
		int numCorrect = 0;
		System.out.println(testIndices);
		for(int i=0;i<test.size();i++){
			Integer result = a.classify(test.get(i));
			List<Integer> actualClass = testClassIds.get(i);
			if(actualClass.contains(result)){
				numCorrect++;
			}
		}
		double accuracy = numCorrect / ((double) test.size());
		System.out.println("accuracy: " + accuracy);
		return new ExperimentResult(0.0, accuracy, 0, after-before,after-before);
	}

	private static void appendToResultFile(String datasetName,double evolvedAccuracy,double exhaustiveAccuracy,double numSize2ShapeletsInEvolved, double avgEvolvedTime, double avgExhaustiveTime) throws IOException {
		if(!experimentResultFile.exists()){
			String header = "dataset,populationSize,numGenerations,numFeatures,avgNumSize2ShapeletsInEvolved,tournamentSize,tournamentP,epsilon,evolvedRFAccuracy,exhaustiveRFAccuracy,avgEvolvedTime,avgExhaustiveTime";
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
				+ exhaustiveAccuracy + ","
				+ avgEvolvedTime + ","
				+ avgExhaustiveTime;
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

		long before = ExperimentUtil.getCpuTime();
		operators.add(new Pair<>(new SmartShapeletAppender(train,random, maxEventLabel,epsilon ),  0.25));
		operators.add(new Pair<>(new ShapeletEndEventMutator(random, maxEventLabel ),  0.25));
		operators.add(new Pair<>(new ShapeletEndRelationshipMutator(random),  0.25));
		operators.add(new Pair<>(new ShapeletEndRemover(random),  0.25));
		//operators.add(new Pair<>(new ShapeletResetter(random,maxEventLabel),  1.0));

		WeightedCompositeMutator mutator = new WeightedCompositeMutator(random, operators );
		SelectionAlgorithm<NShapelet> selectionStrategy = new TournamentSelection(tournamentSize, p, random);
		FitnessEvaluator<NShapelet> evaluator = new NShapeletFitnessEvaluator(train, trainClassIds, epsilon );
		EvolutionEngine<NShapelet> engine = new EvolutionEngine<>(populationSize , numGenerations, mutator, selectionStrategy , evaluator , numFeatures,NShapelet.nShapeletComparator);
		engine.runEvolution(getInitialGeneration(train));
		AbstractRF a = new STIFE_NSHAPELET_RFSingleLabelClassifier(engine.getBestFeatures(),train,trainClassIds,maxEventLabel,sequenceDuration,epsilon);
		long after = ExperimentUtil.getCpuTime();
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
		return new ExperimentResult(accuracy, exhaustiveAccuracy, num2Shapelets, after-before,after-before);
	}

	private static ExperimentResult executeMultiClassFold(List<Sequence> database, List<List<Integer>> classIds,
			List<Pair<MutationStrategy<NShapelet>, Double>> operators, int maxEventLabel, List<Integer> allIndices,
			int foldNum, int numFolds) throws Exception, TimeScaleException, InvalidEventTableDimensionException,
			ClassificationException, IOException {
		List<Integer> trainIndices = ExperimentUtil.getTrainingIndices(allIndices, foldNum,numFolds);
		List<Sequence> train = ExperimentUtil.getAll(database,trainIndices);
		List<List<Integer>> trainClassIds = ExperimentUtil.getAll(classIds,trainIndices);
		List<Integer> testIndices = ExperimentUtil.getTestIndices(allIndices,trainIndices);
		List<Sequence> test = ExperimentUtil.getAll(database,testIndices);
		List<List<Integer>> testClassIds = ExperimentUtil.getAll(classIds,testIndices);

		long before = ExperimentUtil.getCpuTime();
		operators.add(new Pair<>(new SmartShapeletAppender(train,random, maxEventLabel,epsilon ),  0.25));
		operators.add(new Pair<>(new ShapeletEndEventMutator(random, maxEventLabel ),  0.25));
		operators.add(new Pair<>(new ShapeletEndRelationshipMutator(random),  0.25));
		operators.add(new Pair<>(new ShapeletEndRemover(random),  0.25));
		operators.add(new Pair<>(new ShapeletResetter(random,maxEventLabel),  1.0));

		WeightedCompositeMutator mutator = new WeightedCompositeMutator(random, operators );
		SelectionAlgorithm<NShapelet> selectionStrategy = new TournamentSelection(tournamentSize, p, random);
		FitnessEvaluator<NShapelet> evaluator = NShapeletFitnessEvaluator.create(train, trainClassIds, epsilon );
		EvolutionEngine<NShapelet> engine = new EvolutionEngine<>(populationSize , numGenerations, mutator, selectionStrategy , evaluator , numFeatures,NShapelet.nShapeletComparator);
		engine.runEvolution(getInitialGeneration(train));
		AbstractRF a = new STIFE_NSHAPELET_RFMultiLabelClassifier(engine.getBestFeatures(),train,trainClassIds,maxEventLabel,sequenceDuration,epsilon);
		long after = ExperimentUtil.getCpuTime();
		int numCorrect = 0;
		System.out.println(testIndices);
		for(int i=0;i<test.size();i++){
			Integer result = a.classify(test.get(i));
			List<Integer> actualClass = testClassIds.get(i);
			if(actualClass.contains(result)){
				numCorrect++;
			}
		}
		double accuracy = numCorrect / ((double) test.size());
		System.out.println("accuracy: " + accuracy);
		int num2Shapelets = (int) engine.getBestFeatures().stream().filter(sh -> sh.numTwoShapelets()==1).count();
		double exhaustiveAccuracy = 0.0; //TODO: change next!
		return new ExperimentResult(accuracy, exhaustiveAccuracy, num2Shapelets, after-before,after-before);
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
