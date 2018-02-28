package experiment.classifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;

import data_structures.Sequence;
import weka.classifiers.Classifier;
import weka.core.Instances;

public class MultiLabelSTIFERFClassifier extends AbstractSTIFERFClassifier {

	public MultiLabelSTIFERFClassifier(Random random, Function<Instances, Classifier> supplier, List<Sequence> train, List<List<Integer>> classIds, int numDimensions, int sequenceDuration, int epsilon, int shapeletFeatureCount, ExecutorService pool) throws Exception {
		super(random, supplier, modifyTrainSet(train,classIds), modifyClassIds(classIds), numDimensions, sequenceDuration, epsilon, shapeletFeatureCount, pool);
		// TODO Auto-generated constructor stub
	}

	private static List<Integer> modifyClassIds(List<List<Integer>> classIds) {
		List<Integer> newClassIds = new ArrayList<>();
		for(int i=0;i<classIds.size();i++){
			List<Integer> curClassIds = classIds.get(i);
			for(Integer classLabel : curClassIds){
				newClassIds.add(classLabel);
			}
		}
		return newClassIds;
	}

	/***
	 * Adds each Sequence to the new Training set x-times, where x is the number of different class labels assigned to that sequence
	 * @param train
	 * @param classIds
	 * @return
	 */
	private static List<Sequence> modifyTrainSet(List<Sequence> train, List<List<Integer>> classIds) {
		assert(train.size() == classIds.size());
		List<Sequence> newTrain = new ArrayList<>();
		for(int i=0;i<classIds.size();i++){
			Sequence curSequence = train.get(i);
			List<Integer> curClassIds = classIds.get(i);
			curClassIds.forEach(e -> newTrain.add(new Sequence(curSequence)));
		}
		return newTrain;
	}

}
