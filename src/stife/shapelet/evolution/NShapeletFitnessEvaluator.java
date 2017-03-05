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
		short[] occurrenceFeature = new short[database.size()];
		for(int i=0;i<database.size();i++){
			if(database.get(i).containsNSHapelet(t,epsilon)){
				occurrenceFeature[i] = 1;
			} else{
				occurrenceFeature[i] = 0;
			}
		}
		return FeatureSelection.calcInfoGain(occurrenceFeature, classIds);
	}

}
