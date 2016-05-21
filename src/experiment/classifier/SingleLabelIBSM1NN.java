package experiment.classifier;

import java.util.List;

import data_structures.Sequence;

public class SingleLabelIBSM1NN extends AbstractIBSM1NN<Integer> {

	public SingleLabelIBSM1NN(List<Sequence> train, List<Integer> classId, int numDimensions, int maxDuration) {
		super(train, classId, numDimensions, maxDuration);
	}

}
