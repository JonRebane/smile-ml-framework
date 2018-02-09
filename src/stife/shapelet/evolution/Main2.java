package stife.shapelet.evolution;

import data_structures.Pair;
import data_structures.Sequence;
import experiment.ExperimentUtil;
import experiment.classifier.ClassificationException;
import io.IOService;
import stife.distance.exceptions.InvalidEventTableDimensionException;
import stife.distance.exceptions.TimeScaleException;
import stife.shapelet.evolution.evolution.alterers.*;
import stife.shapelet_size2.Shapelet_Size2;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main2 {

    public static final SelectionAlgorithm<Shapelet_Size2> TOP_N = evaluatedPopulation -> {
        List<Pair<Shapelet_Size2, Double>> pairs = new ArrayList<>();
        for (Map.Entry<Shapelet_Size2, Double> s1 : evaluatedPopulation.entrySet()) {
            pairs.add(new Pair<>(s1.getKey(), s1.getValue()));
        }
        Comparator<Pair<Shapelet_Size2, Double>> comparing = Comparator.comparing(Pair::getSecond);
        pairs.sort(comparing.reversed());

        int num2Shapelets = 75;
        List<Shapelet_Size2> bestShapelets = new ArrayList<>();
        for (int i = 0; i < num2Shapelets; ) {
            bestShapelets.add(pairs.get(i).getFirst());
            i++;
        }
        return bestShapelets;

    };

    public static final SelectionAlgorithm<Shapelet_Size2> ALL_N = evaluatedPopulation -> {
        return new ArrayList<>(evaluatedPopulation.keySet());

    };

    public static final SelectionAlgorithm<Shapelet_Size2> SHAPELET_SIZE_2_SELECTION_ALGORITHM = ALL_N;
    private static int epsilon = 5;
    private static int tournamentSize = 2;
    private static double p = 1.0;
    private static int populationSize = 500;
    private static int numFeatures = 75;
    private static Random random;
    private static int numGenerations = 200;
    private static File experimentResultFile = new File("experimentResults/shapeletEvolutionResult.csv");
    private static int sequenceDuration;

    public static void main(String[] args) throws Exception {
        //TODO: use secure random!
        List<File> allFiles = Arrays.stream(new File("data/singleLabelDatasets/").listFiles()).collect(Collectors.toList());
        for (File file : allFiles) { //.filter(f -> "AUSLAN2".equals(f.getName())).collect(Collectors.toList())
    //        if (!Arrays.asList("AUSLAN2", "BLOCKS", "CONTEXT").contains(file.getName())) {
                random = new Random(13);
                System.out.println("starting " + file.getName());
                runExperiment(file.getName());
      //      }
        }
        allFiles = Arrays.stream(new File("data/multiLabelDatasets/").listFiles()).collect(Collectors.toList());
        for (File file : allFiles) {
            random = new Random(13);
            System.out.println("starting " + file.getName());
            runMultiClassExperiment(file.getName());
        }
    }

    private static void runMultiClassExperiment(String datasetName) throws TimeScaleException, InvalidEventTableDimensionException, ClassificationException, Exception {
        File testData = new File("data/multiLabelDatasets/" + datasetName);
        List<Sequence> database = IOService.readMultiLabelSequenceData(testData);
        sequenceDuration = Sequence.getMaxDuration(database);
        List<List<Integer>> classIds = IOService.readMultiLabelClassData(testData);
        runMultiClassExperiment(datasetName, database, classIds);
    }

    private static void runExperiment(String datasetName) throws IOException, Exception, TimeScaleException,
            InvalidEventTableDimensionException, ClassificationException {
        File testData = new File("data/singleLabelDatasets/" + datasetName);
        List<Sequence> database = IOService.readSequenceData(testData);
        sequenceDuration = Sequence.getMaxDuration(database);
        List<Integer> classIds = IOService.readClassData(testData);
        runExperiment(datasetName, database, classIds);
    }

    private static void runExperiment(String datasetName, List<Sequence> database, List<Integer> classIds)
            throws Exception, TimeScaleException, InvalidEventTableDimensionException, ClassificationException,
            IOException {
        //mutator
        List<Pair<MutationStrategy<Shapelet_Size2>, Double>> operators = new ArrayList<>();
        int maxEventLabel = Sequence.getDimensionSet(database).descendingIterator().next();
        //training and test data split
        List<Integer> allIndices = ExperimentUtil.getShuffledIndices(database, random);
        int k = 10;
        ArrayList<ExperimentResult> allResults = new ArrayList<>();

        for (int i = 0; i < k; i++) {
            ExperimentResult result = executeFold(database, classIds, operators, maxEventLabel, allIndices, i, k);
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
        appendToResultFile(datasetName, avgEvolvedAccuracy, avgExhaustiveAccuracy, avgNum2Shapelets, avgEvolvedTime, avgExhaustiveTime);
    }

    private static void runMultiClassExperiment(String datasetName, List<Sequence> database, List<List<Integer>> classIds)
            throws Exception, TimeScaleException, InvalidEventTableDimensionException, ClassificationException,
            IOException {
        //mutator
        List<Pair<MutationStrategy<Shapelet_Size2>, Double>> operators = new ArrayList<>();
        int maxEventLabel = Sequence.getDimensionSet(database).descendingIterator().next();
        //training and test data split
        List<Integer> allIndices = ExperimentUtil.getShuffledIndices(database, random);
        int k = 10;
        ArrayList<ExperimentResult> allResults = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            ExperimentResult result = executeMultiClassFold(database, classIds, operators, maxEventLabel, allIndices, i, k);
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
        appendToResultFile(datasetName, avgEvolvedAccuracy, avgExhaustiveAccuracy, avgNum2Shapelets, avgEvolvedTime, avgExhaustiveTime);
    }


    private static ExperimentResult executeExhaustiveFold(List<Sequence> database, List<Integer> classIds, int maxEventLabel, List<Integer> allIndices, int foldNum, int numFolds) throws Exception {
        List<Integer> trainIndices = ExperimentUtil.getTrainingIndices(allIndices, foldNum, numFolds);
        List<Sequence> train = ExperimentUtil.getAll(database, trainIndices);
        List<Integer> trainClassIds = ExperimentUtil.getAll(classIds, trainIndices);
        List<Integer> testIndices = ExperimentUtil.getTestIndices(allIndices, trainIndices);
        List<Sequence> test = ExperimentUtil.getAll(database, testIndices);
        List<Integer> testClassIds = ExperimentUtil.getAll(classIds, testIndices);
        long before = ExperimentUtil.getCpuTime();
        ShapeletSize2RF a = new ShapeletSize2RF(train, trainClassIds, maxEventLabel, epsilon, numFeatures);
        long after = ExperimentUtil.getCpuTime();
        int numCorrect = 0;
        System.out.println(testIndices);
        for (int i = 0; i < test.size(); i++) {
            Integer result = a.classify(test.get(i));
            Integer actualClass = testClassIds.get(i);
            if (result.equals(actualClass)) {
                numCorrect++;
            }
        }
        double accuracy = numCorrect / ((double) test.size());
        System.out.println("accuracy: " + accuracy);
        return new ExperimentResult(0.0, accuracy, 0, after - before, after - before);
    }

    private static ExperimentResult executeMultiClassExhaustiveFold(List<Sequence> database, List<List<Integer>> classIds, int maxEventLabel, List<Integer> allIndices, int foldNum, int numFolds) throws Exception {
        List<Integer> trainIndices = ExperimentUtil.getTrainingIndices(allIndices, foldNum, numFolds);
        List<Sequence> train = ExperimentUtil.getAll(database, trainIndices);
        List<List<Integer>> trainClassIds = ExperimentUtil.getAll(classIds, trainIndices);
        List<Integer> testIndices = ExperimentUtil.getTestIndices(allIndices, trainIndices);
        List<Sequence> test = ExperimentUtil.getAll(database, testIndices);
        List<List<Integer>> testClassIds = ExperimentUtil.getAll(classIds, testIndices);
        long before = ExperimentUtil.getCpuTime();
        ShapeletSize2RF a = new MultiClassShapeletSize2RF(train, trainClassIds, maxEventLabel, epsilon, numFeatures);
        long after = ExperimentUtil.getCpuTime();
        int numCorrect = 0;
        System.out.println(testIndices);
        for (int i = 0; i < test.size(); i++) {
            Integer result = a.classify(test.get(i));
            List<Integer> actualClass = testClassIds.get(i);
            if (actualClass.contains(result)) {
                numCorrect++;
            }
        }
        double accuracy = numCorrect / ((double) test.size());
        System.out.println("accuracy: " + accuracy);
        return new ExperimentResult(0.0, accuracy, 0, after - before, after - before);
    }

    private static void appendToResultFile(String datasetName, double evolvedAccuracy, double exhaustiveAccuracy, double numSize2ShapeletsInEvolved, double avgEvolvedTime, double avgExhaustiveTime) throws IOException {
        if (!experimentResultFile.exists()) {
            String header = "dataset,populationSize,numGenerations,numFeatures,avgNumSize2ShapeletsInEvolved,tournamentSize,tournamentP,epsilon,evolvedRFAccuracy,exhaustiveRFAccuracy,avgEvolvedTime,avgExhaustiveTime";
            IOService.appendLineToFile(experimentResultFile, header);
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
        IOService.appendLineToFile(experimentResultFile, results);
    }

    private static ExperimentResult executeFold(List<Sequence> database, List<Integer> classIds,
                                                List<Pair<MutationStrategy<Shapelet_Size2>, Double>> operators, int maxEventLabel, List<Integer> allIndices,
                                                int foldNum, int numFolds) throws Exception, TimeScaleException, InvalidEventTableDimensionException,
            ClassificationException, IOException {
        List<Integer> trainIndices = ExperimentUtil.getTrainingIndices(allIndices, foldNum, numFolds);
        List<Sequence> train = ExperimentUtil.getAll(database, trainIndices);
        List<Integer> trainClassIds = ExperimentUtil.getAll(classIds, trainIndices);
        List<Integer> testIndices = ExperimentUtil.getTestIndices(allIndices, trainIndices);
        List<Sequence> test = ExperimentUtil.getAll(database, testIndices);
        List<Integer> testClassIds = ExperimentUtil.getAll(classIds, testIndices);

        long before = ExperimentUtil.getCpuTime();

        MutationStrategy<Shapelet_Size2> mutator = shapelet_size2 -> shapelet_size2;

        FitnessEvaluator<Shapelet_Size2> evaluator = new Shapelet_Size2FitnessEvaluator(train, trainClassIds, epsilon);


        EvolutionEngine<Shapelet_Size2> engine = new EvolutionEngine<>(populationSize, numGenerations, mutator, SHAPELET_SIZE_2_SELECTION_ALGORITHM, evaluator, numFeatures, Comparator.comparing(Shapelet_Size2::getRelationshipId));
        engine.runEvolution(getInitialGeneration(train));
        AbstractRF a = new STIFE_2SHAPELET_RFSingleLabelClassifier(engine.getBestFeatures(), train, trainClassIds, maxEventLabel, sequenceDuration, epsilon);
        long after = ExperimentUtil.getCpuTime();
        int numCorrect = 0;
        System.out.println(testIndices);
        for (int i = 0; i < test.size(); i++) {
            Integer result = a.classify(test.get(i));
            Integer actualClass = testClassIds.get(i);
            if (result.equals(actualClass)) {
                numCorrect++;
            }
        }
        double accuracy = numCorrect / ((double) test.size());
        System.out.println("accuracy: " + accuracy);

        double exhaustiveAccuracy = 0.0; //TODO: change next!
        return new ExperimentResult(accuracy, exhaustiveAccuracy, 75, after - before, after - before);
    }

    private static ExperimentResult executeMultiClassFold(List<Sequence> database, List<List<Integer>> classIds,
                                                          List<Pair<MutationStrategy<Shapelet_Size2>, Double>> operators, int maxEventLabel, List<Integer> allIndices,
                                                          int foldNum, int numFolds) throws Exception, TimeScaleException, InvalidEventTableDimensionException,
            ClassificationException, IOException {
        List<Integer> trainIndices = ExperimentUtil.getTrainingIndices(allIndices, foldNum, numFolds);
        List<Sequence> train = ExperimentUtil.getAll(database, trainIndices);
        List<List<Integer>> trainClassIds = ExperimentUtil.getAll(classIds, trainIndices);
        List<Integer> testIndices = ExperimentUtil.getTestIndices(allIndices, trainIndices);
        List<Sequence> test = ExperimentUtil.getAll(database, testIndices);
        List<List<Integer>> testClassIds = ExperimentUtil.getAll(classIds, testIndices);

        long before = ExperimentUtil.getCpuTime();

        MutationStrategy<Shapelet_Size2> mutator = shapelet_size2 -> shapelet_size2;
        FitnessEvaluator<Shapelet_Size2> evaluator = Shapelet_Size2FitnessEvaluator.create(train, trainClassIds, epsilon);
        EvolutionEngine<Shapelet_Size2> engine = new EvolutionEngine<>(populationSize, numGenerations, mutator, SHAPELET_SIZE_2_SELECTION_ALGORITHM, evaluator, numFeatures, Comparator.comparing(Shapelet_Size2::getRelationshipId));
        engine.runEvolution(getInitialGeneration(train));
        AbstractRF a = new STIFE_2SHAPELET_RFMultiLabelClassifier(engine.getBestFeatures(), train, trainClassIds, maxEventLabel, sequenceDuration, epsilon);

        //operators.add(new Pair<>(new SmartShapeletAppender(train,random, maxEventLabel,epsilon ),  0.25));
        //operators.add(new Pair<>(new ShapeletEndEventMutator(random, maxEventLabel ),  0.25));
        //operators.add(new Pair<>(new ShapeletEndRelationshipMutator(random),  0.25));
        //operators.add(new Pair<>(new ShapeletEndRemover(random),  0.25));
        //operators.add(new Pair<>(new ShapeletResetter(random,maxEventLabel),  1.0));

        //WeightedCompositeMutator mutator = new WeightedCompositeMutator(random, operators );

        long after = ExperimentUtil.getCpuTime();
        int numCorrect = 0;
        System.out.println(testIndices);
        for (int i = 0; i < test.size(); i++) {
            Integer result = a.classify(test.get(i));
            List<Integer> actualClass = testClassIds.get(i);
            if (actualClass.contains(result)) {
                numCorrect++;
            }
        }
        double accuracy = numCorrect / ((double) test.size());
        System.out.println("accuracy: " + accuracy);
        int num2Shapelets = 75;
        double exhaustiveAccuracy = 0.0; //TODO: change next!*/
        return new ExperimentResult(accuracy, exhaustiveAccuracy, num2Shapelets, after - before, after - before);
    }

    private static List<Shapelet_Size2> getInitialGeneration(List<Sequence> database) {
		/*List<Integer> dims = new ArrayList<>(Sequence.getDimensionSet(database));
		List<Shapelet_Size2> shapelets = new ArrayList<>();
		for(int i=0;i<populationSize;i++){
			Shapelet_Size2 basis = new Shapelet_Size2(
					dims.get(random.nextInt(dims.size())),
					dims.get(random.nextInt(dims.size())),
					random.nextInt(Sequence.NUM_RELATIONSHIPS));
			shapelets.add(new Shapelet_Size2(basis));
		}
		return shapelets;*/
        List<Shapelet_Size2> shapelets = new ArrayList<>();
        for (Sequence sequence : database) {
            shapelets.addAll(sequence.getAllShapelets(5));
        }
        Collections.shuffle(shapelets);

        int nShapelets = (int) Math.round(shapelets.size() * 0.05);
        System.out.println("Taking " + nShapelets + " no shapelets out of " + shapelets.size());
        return shapelets.subList(0, 1000);

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
