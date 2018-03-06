package experiment;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
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
import experiment.classifier.MultiLabelSTIFERFClassifier;
import experiment.classifier.SingleLabelSTIFERFClassifier;
import io.IOService;
import weka.classifiers.Classifier;
import weka.core.Instances;

public class RealDataExperiment extends Experiment{

	private final Function<Instances, Classifier> classifierBuilder;
	private File singleLabelDataSetPath;
	private int shapeletFeatureCount;
	private int epsilon;
	private ExecutorService pool;
	private Random random;
	private int k;
	private File multLabelDataSetPath;

	public RealDataExperiment(ExecutorService pool, Function<Instances, Classifier> classifierBuilder, int epsilon, int shapeletFeatureCount, File singleLabelDatasetPath, File multLabelDataSetPath, Random random, int k) {
		this.pool = pool;
		this.epsilon = epsilon;
		this.shapeletFeatureCount = shapeletFeatureCount;
		this.singleLabelDataSetPath = singleLabelDatasetPath;
		this.multLabelDataSetPath = multLabelDataSetPath;
		this.random = random;
		this.k=k;
		this.classifierBuilder = classifierBuilder;
	}

	public void runExperiment() throws Exception {
		Map<String,List<ClassifierResult>> results = new LinkedHashMap<>();
		boolean runSingle = true;
		boolean runMultiple = true;
		if (runSingle) {
			for(File dir : singleLabelDataSetPath.listFiles()){
				//if(dir.isDirectory() && "AUSLAN2".equals(dir.getName())){
				System.out.println("Processing " + dir.getName());
				List<ClassifierResult> resultList = singleLabelClassifierEvaluation(dir);
				for (ClassifierResult classifierResult : resultList) {
					System.out.println(classifierResult.getClassifierName() +
							" acc: " + classifierResult.meanAccuracy() +
					        " auc: " + classifierResult.meanAUC());
				}
				results.put(dir.getName(), resultList);
			//}
			}
		}

		if (runMultiple) {
			for(File dir :multLabelDataSetPath.listFiles()){
				System.out.println("Processing " + dir.getName());
				List<ClassifierResult> resultList = multiLabelClassifierEvaluation(dir);
				for (ClassifierResult classifierResult : resultList) {
					System.out.println(classifierResult.getClassifierName() + " acc: " + classifierResult.meanAccuracy());
				}
				results.put(dir.getName(), resultList);

			}
		}
		printExperimentResults(results);
	}

	private List<ClassifierResult> singleLabelClassifierEvaluation(File dir) throws IOException, Exception {
		List<Integer> classIds = IOService.readClassData(dir);
		List<Sequence> database = IOService.readSequenceData(dir);
		for(Sequence seq:database){
			seq.sortIntervals();
		}
		ClassifierResult ibsmResult = new ClassifierResult(AbstractIBSM1NN.getName());
		ClassifierResult compressedIBSMResult = new ClassifierResult(AbstractCompressedIBSM1NN.getName());
		ClassifierResult stifeResult = new ClassifierResult(AbstractSTIFERFClassifier.getName());
		int numDimensions = Sequence.getDimensionSet(database).size();
		int sequenceDuration = Sequence.getMaxDuration(database);
		List<Integer> allIndices = ExperimentUtil.getShuffledIndices(database, random);
		for(int i=0;i<k;i++){
			List<Integer> trainIndices = ExperimentUtil.getTrainingIndices(allIndices, i,k);
			List<Sequence> train = ExperimentUtil.getAll(database,trainIndices);
			List<Integer> trainClassIds = ExperimentUtil.getAll(classIds,trainIndices);
			List<Integer> testIndices = ExperimentUtil.getTestIndices(allIndices,trainIndices);
			List<Sequence> test = ExperimentUtil.getAll(database,testIndices);
			List<Integer> testClassIds = ExperimentUtil.getAll(classIds,testIndices);
			HashSet<Integer> trainWithoutTest = new HashSet<>(testIndices);
			trainWithoutTest.removeAll(trainIndices);
			assert(trainWithoutTest.size()==testIndices.size());

			///// UNCOMMENT IF INCLUDING THE OTHER METHODS
			//measureSingleLabelClassificationPerformance(test,testClassIds, new SingleLabelIBSM1NN(train, trainClassIds, numDimensions, sequenceDuration),ibsmResult);
			//measureSingleLabelClassificationPerformance(test,testClassIds, new SingleLabelCompressedIBSM1NN(train, trainClassIds, numDimensions, sequenceDuration),compressedIBSMResult);





			measureSingleLabelClassificationPerformance(test,testClassIds, new SingleLabelSTIFERFClassifier(random, classifierBuilder, train, trainClassIds, numDimensions, sequenceDuration,epsilon,shapeletFeatureCount,pool),stifeResult);
		}
		//save results:
		List<ClassifierResult> resultList = Arrays.asList(ibsmResult,compressedIBSMResult,stifeResult);
		return resultList;
	}
	
	private List<ClassifierResult> multiLabelClassifierEvaluation(File dir) throws IOException, Exception {
		List<List<Integer>> classIds = IOService.readMultiLabelClassData(dir);
		List<Sequence> database = IOService.readMultiLabelSequenceData(dir);
		for(Sequence seq:database){
			seq.sortIntervals();
		}
		assert(classIds.size()==database.size());
		ClassifierResult ibsmResult = new ClassifierResult(AbstractIBSM1NN.getName());
		ClassifierResult compressedIBSMResult = new ClassifierResult(AbstractCompressedIBSM1NN.getName());
		ClassifierResult stifeResult = new ClassifierResult(AbstractSTIFERFClassifier.getName());
		int numDimensions = Sequence.getDimensionSet(database).size();
		int sequenceDuration = Sequence.getMaxDuration(database);
		List<Integer> allIndices = ExperimentUtil.getShuffledIndices(database, random);
		for(int i=0;i<k;i++){
			List<Integer> trainIndices = ExperimentUtil.getTrainingIndices(allIndices, i,k);
			List<Sequence> train = ExperimentUtil.getAll(database,trainIndices);
			List<List<Integer>> trainClassIds = ExperimentUtil.getAll(classIds,trainIndices);
			List<Integer> testIndices = ExperimentUtil.getTestIndices(allIndices,trainIndices);
			List<Sequence> test = ExperimentUtil.getAll(database,testIndices);
			List<List<Integer>> testClassIds = ExperimentUtil.getAll(classIds,testIndices);
			HashSet<Integer> trainWithoutTest = new HashSet<>(testIndices);
			trainWithoutTest.removeAll(trainIndices);
			assert(trainWithoutTest.size()==testIndices.size());
			/*System.out.println("beginning 1NN");
			MultiLabelIBSM1NN ibsmClassifier = new MultiLabelIBSM1NN(train, trainClassIds, numDimensions, sequenceDuration);
			System.out.println("1NN Training done");
			measureMultiLabel1NNClassificationPerformance(test,testClassIds, ibsmClassifier,ibsmResult);
			System.out.println("beginning 1NN");
			MultiLabelCompressedIBSM1NN classifier = new MultiLabelCompressedIBSM1NN(train, trainClassIds, numDimensions, sequenceDuration);
			System.out.println("1NN Training done");
			measureMultiLabel1NNClassificationPerformance(test,testClassIds, classifier,compressedIBSMResult);*/
			System.out.println("beginning stiferf");
			MultiLabelSTIFERFClassifier classifier2 = new MultiLabelSTIFERFClassifier(random, classifierBuilder, train, trainClassIds, numDimensions, sequenceDuration,epsilon,shapeletFeatureCount,pool);
			System.out.println("training done");
			measureMultiLabelSTIFERFClassificationPerformance(test,testClassIds, classifier2,stifeResult);
			System.out.println("-----------done with fold " +i);
		}
		//save results:
		List<ClassifierResult> resultList = Arrays.asList(ibsmResult,compressedIBSMResult,stifeResult);
		return resultList;
	}

}
