package experiment;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;

import data_structures.Sequence;
import experiment.classifier.AbstractCompressedIBSM1NN;
import experiment.classifier.AbstractIBSM1NN;
import experiment.classifier.AbstractSTIFERFClassifier;
import experiment.classifier.SingleLabelCompressedIBSM1NN;
import experiment.classifier.SingleLabelIBSM1NN;
import experiment.classifier.SingleLabelSTIFERFClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;

public class SyntheticDataExperiment extends Experiment{
	Function<Instances, Classifier> RANDOM_FOREST_BUILDER = (trainInstances) -> {
		Classifier classifier = new RandomForest();
		Integer numFeaturesPerTree = (int) Math.sqrt(trainInstances.numAttributes() - 1);
		try {
			classifier.setOptions(new String[]{"-I", "500", "-K", numFeaturesPerTree.toString(), "-S", "123"});
		} catch (Exception e) {
			return null;
		}
		return classifier;
	};

	private static final int MAX_REAL_DURATION = 5901;
	private static final int MAX_REAL_NUM_DIMENSIONS = 63;
	private static final int MAX_REAL_INTERVAL_COUNT = 93;
	private static final int MAX_REAL_TRAINING_SIZE = 498;
	
	private int shapeletFeatureCount;
	private int epsilon;
	private ExecutorService pool;
	private String outPath = "syntheticExperimentResults/sequenceDuration.txt";

	public SyntheticDataExperiment(ExecutorService pool, int epsilon, int shapeletFeatureCount) {
		this.pool = pool;
		this.epsilon = epsilon;
		this.shapeletFeatureCount = shapeletFeatureCount;
	}

	public void runExperiment() throws Exception {
		Random random = new Random(13);
		int numTrainingSequences = MAX_REAL_TRAINING_SIZE;
		int numDimensions = MAX_REAL_NUM_DIMENSIONS;
		int intervalCount = MAX_REAL_INTERVAL_COUNT;
		int numTestSequences = 100; 
		List<Integer> values = Arrays.asList(10,20,30,40,50,100,150,200,250,300,350,400,450,500,600,700,800,900,1000,1500,2000,2500,3000,3500,4000,4500,5000,6000,7000,8000,9000,10000,15000,20000,25000,30000,35000,40000,45000,50000,55000,60000,65000,70000,75000);
		System.out.println("sequenceDuration \t IBSM \t Com-IBSM \t STIFERF");
		for(int sequenceDuration : values){
			Map<String,List<ClassifierResult>> results = new LinkedHashMap<>();
			ClassifierResult ibsmResult = new ClassifierResult(AbstractIBSM1NN.getName());
			ClassifierResult compressedIBSMResult = new ClassifierResult(AbstractCompressedIBSM1NN.getName());
			ClassifierResult stifeResult = new ClassifierResult(AbstractSTIFERFClassifier.getName());
			//data generation
			SequenceGenerator gen = new SequenceGenerator(intervalCount, sequenceDuration, numDimensions, random);
			List<Sequence> database = gen.generate(numTrainingSequences+numTestSequences);
			List<Sequence> train = database.subList(0, numTrainingSequences);
			List<Integer> trainClassIds = ExperimentUtil.generateRandomBinaryClassIds(train.size(),random);
			List<Sequence> test = database.subList(numTrainingSequences, database.size());
			List<Integer> testClassIds = ExperimentUtil.generateRandomBinaryClassIds(test.size(),random);
			//classifier training:
			measureSingleLabelClassificationPerformance(test,testClassIds,new SingleLabelIBSM1NN(train, trainClassIds, numDimensions, sequenceDuration),ibsmResult);
			measureSingleLabelClassificationPerformance(test,testClassIds,new SingleLabelCompressedIBSM1NN(train, trainClassIds, numDimensions, sequenceDuration),compressedIBSMResult);
			measureSingleLabelClassificationPerformance(test,testClassIds,new SingleLabelSTIFERFClassifier(random, RANDOM_FOREST_BUILDER, train, trainClassIds, numDimensions, sequenceDuration,epsilon,shapeletFeatureCount,75, "1+2+3+4+", pool),stifeResult);
			PrintWriter out = new PrintWriter(new FileWriter( new File(outPath ),true));
			String resultString = sequenceDuration +" " + ibsmResult.meanClassificationTimeMS() + " " + compressedIBSMResult.meanClassificationTimeMS() + " " + stifeResult.meanClassificationTimeMS();
			out.println(resultString);
			System.out.println(resultString);
			out.close();
			//results.put("artificial data", Arrays.asList(ibsmResult,compressedIBSMResult,stifeResult));
			//print results
			//printExperimentResults(results);
		}
	}
	
}
