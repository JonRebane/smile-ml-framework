package experiment;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import classification.ClassificationException;
import classification.MultiLabelSTIFERFClassifier;
import classification.STIClassifier;
import distance.feature.extraction.exceptions.InvalidEventTableDimensionException;
import distance.feature.extraction.exceptions.TimeScaleException;
import representations.Sequence;

public abstract class Experiment {

	protected void measureSingleLabelClassificationPerformance(List<Sequence> test, List<Integer> testClassIds, STIClassifier<Integer> classifier,ClassifierResult classifierResult) throws IOException, Exception {
		int correct = 0;
		int incorrect = 0;
		for(int i=0;i<test.size();i++){
			long before = ExperimentUtil.getCpuTime();
			int predicted = classifier.classify(test.get(i));
			long after = ExperimentUtil.getCpuTime();
			classifierResult.addClassificationTime(after-before);
			if(predicted == testClassIds.get(i)){
				correct++;
			} else{
				incorrect++;
			}
		}
		classifierResult.addAccuracyValue(correct / (double) (correct+incorrect));	
	}
	
	protected void measureMultiLabel1NNClassificationPerformance(List<Sequence> test, List<List<Integer>> testClassIds, STIClassifier<List<Integer>> classifier,ClassifierResult classifierResult) throws TimeScaleException, InvalidEventTableDimensionException, ClassificationException, IOException, Exception{
		int correct = 0;
		int incorrect = 0;
		for(int i=0;i<test.size();i++){
			long before = ExperimentUtil.getCpuTime();
			List<Integer> predicted = classifier.classify(test.get(i));
			long after = ExperimentUtil.getCpuTime();
			classifierResult.addClassificationTime(after-before);
			List<Integer> actual = testClassIds.get(i);
			HashSet<Integer> intersection = new HashSet<>(actual);
			intersection.retainAll(predicted);
			if(intersection.size() !=0){
				correct++;
			} else{
				incorrect++;
			}
		}
		classifierResult.addAccuracyValue(correct / (double) (correct+incorrect));	
	}
	
	protected void measureMultiLabelSTIFERFClassificationPerformance(List<Sequence> test, List<List<Integer>> testClassIds, MultiLabelSTIFERFClassifier classifier,ClassifierResult classifierResult) throws TimeScaleException, InvalidEventTableDimensionException, ClassificationException, IOException, Exception{
		int correct = 0;
		int incorrect = 0;
		for(int i=0;i<test.size();i++){
			long before = ExperimentUtil.getCpuTime();
			Integer predicted = classifier.classify(test.get(i));
			long after = ExperimentUtil.getCpuTime();
			classifierResult.addClassificationTime(after-before);
			List<Integer> actual = testClassIds.get(i);
			if(actual.contains(predicted)){
				correct++;
			} else{
				incorrect++;
			}
		}
		classifierResult.addAccuracyValue(correct / (double) (correct+incorrect));	
	}

	protected void printExperimentResults(Map<String, List<ClassifierResult>> results) {
		for(String name : results.keySet()){
			System.out.println("------------------------------------------------------------------------------------------");
			System.out.println("Results for data-set "+ name);
			//aggregate results:
			List<ClassifierResult> thisDirResults = results.get(name);
			for(ClassifierResult result : thisDirResults){
				System.out.println(result.getClassifierName() + "average Accuracy: \t" + result.meanAccuracy());
				System.out.println(result.getClassifierName() + "  average Classification Time: " + result.meanClassificationTimeMS() + "ms");
				
			}
		}
	}
}
