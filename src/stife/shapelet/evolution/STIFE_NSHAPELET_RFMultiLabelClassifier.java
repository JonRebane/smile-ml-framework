package stife.shapelet.evolution;

import java.util.ArrayList;
import java.util.List;

import data_structures.Sequence;

public class STIFE_NSHAPELET_RFMultiLabelClassifier extends STIFE_NSHAPELET_RFSingleLabelClassifier {

	public STIFE_NSHAPELET_RFMultiLabelClassifier(List<NShapelet> nShapelets, List<Sequence> train,
												  List<List<Integer>> classIds, int numDimensions, int sequenceDuration, int epsilon) throws Exception {
		super(nShapelets, modifyTrainSet(train,classIds), modifyClassIds(classIds), numDimensions, sequenceDuration, epsilon);
		// TODO Auto-generated constructor stub
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


}
