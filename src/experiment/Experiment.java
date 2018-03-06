package experiment;

import data_structures.Sequence;
import experiment.classifier.ClassificationException;
import experiment.classifier.MultiLabelSTIFERFClassifier;
import experiment.classifier.STIClassifier;
import stife.distance.exceptions.InvalidEventTableDimensionException;
import stife.distance.exceptions.TimeScaleException;
import weka.classifiers.evaluation.NominalPrediction;
import weka.classifiers.evaluation.ThresholdCurve;
import weka.core.FastVector;
import weka.core.Instances;
import weka.core.Utils;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public abstract class Experiment {

	protected void measureSingleLabelClassificationPerformance(List<Sequence> test, List<Integer> testClassIds, STIClassifier<Integer> classifier,ClassifierResult classifierResult) throws IOException, Exception {
		double correct = 0;
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
		double accuracy = correct / (double) (correct + incorrect);
		classifierResult.addAccuracyValue(accuracy);

		FastVector predictions = new FastVector();
		List<double[]> probas = classifier.predictProba(test);
		correct = 0;
		for (int i = 0; i < testClassIds.size(); i++) {
			double[] proba = probas.get(i);
			int cls = classifier.translateClass(testClassIds.get(i));
			predictions.addElement(new NominalPrediction(cls, proba));
			if (cls == Utils.maxIndex(proba)) {
				correct++;
			}
		}
		ThresholdCurve curve = new ThresholdCurve();
		Instances auc = curve.getCurve(predictions);
        double rocArea = ThresholdCurve.getROCArea(auc);
        if (Double.isNaN(rocArea)) {
            rocArea = 0;
        }
        classifierResult.addAucValue(rocArea);

        assert correct / testClassIds.size() == accuracy;
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
		System.out.println("------------------------------------------------------------------------------------------");
		for(String name : results.keySet()){

			//System.out.println("Results for data-set "+ name);
			//aggregate results:
			List<ClassifierResult> thisDirResults = results.get(name);
			for(ClassifierResult result : thisDirResults){
				System.out.printf("%s,%s,%f,%f,%d\n", name, result.getClassifierName(), result.meanAccuracy(), result.meanClassificationTimeMS(), Sequence.METHOD);
			}
		}
	}
}
