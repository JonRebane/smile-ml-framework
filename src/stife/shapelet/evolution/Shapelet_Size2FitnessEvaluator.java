package stife.shapelet.evolution;

import data_structures.Sequence;
import stife.shapelet_size2.FeatureSelection;
import stife.shapelet_size2.Shapelet_Size2;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class Shapelet_Size2FitnessEvaluator implements FitnessEvaluator<Shapelet_Size2> {

	private List<Sequence> database;
	private List<Integer> classIds;
	private int epsilon;

	public Shapelet_Size2FitnessEvaluator(List<Sequence> database, List<Integer> classIds, int epsilon) {
		this.database = database;
		this.classIds = classIds;
		this.epsilon = epsilon;
	}
	public static Shapelet_Size2FitnessEvaluator create(List<Sequence> train, List<List<Integer>> trainClassIds, int epsilon) {
		return new Shapelet_Size2FitnessEvaluator(modifyTrainSet(train,trainClassIds),modifyClassIds(trainClassIds),epsilon);
	}

	public static List<Integer> modifyClassIds(List<List<Integer>> classIds) {
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
	public static List<Sequence> modifyTrainSet(List<Sequence> train, List<List<Integer>> classIds) {
		assert(train.size() == classIds.size());
		List<Sequence> newTrain = new ArrayList<>();
		for(int i=0;i<classIds.size();i++){
			Sequence curSequence = train.get(i);
			List<Integer> curClassIds = classIds.get(i);
			curClassIds.forEach(e -> newTrain.add(new Sequence(curSequence)));
		}
		return newTrain;
	}
	@Override
	public double getFitness(Shapelet_Size2 t) {
		double[] occurrenceFeature = new double[database.size()];

		IntStream.range(0, database.size()).parallel().forEach(i -> {
			occurrenceFeature[i] = database.get(i).computeShapeletSimilarity(t, epsilon);
		});

		/*for(int i=0;i<database.size();i++){
			occurrenceFeature[i] = database.get(i).computeShapeletSimilarity(t, epsilon);
		}*/

		return FeatureSelection.calcInfoGain(occurrenceFeature, classIds);
	}

}
