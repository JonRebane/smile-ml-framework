package stife.shapelet.evolution;

import java.util.ArrayList;
import java.util.List;

import data_structures.Sequence;
import stife.shapelet_size2.FeatureSelection;

public class NShapeletFitnessEvaluator implements FitnessEvaluator<NShapelet> {

	private List<Sequence> database;
	private List<Integer> classIds;
	private int epsilon;

	public NShapeletFitnessEvaluator(List<Sequence> database,List<Integer> classIds,int epsilon) {
		this.database = database;
		this.classIds = classIds;
		this.epsilon = epsilon;
	}
	
	@Override
	public double getFitness(NShapelet t) {
		int[] occurrenceFeature = new int[database.size()];
		for(int i=0;i<database.size();i++){
			occurrenceFeature[i] = database.get(i).getAllOccurrences(t, epsilon).size();
		}
		return FeatureSelection.calcInfoGain(occurrenceFeature, classIds);
	}

}
