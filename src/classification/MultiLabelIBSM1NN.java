package classification;

import java.util.List;

import representations.Sequence;

public class MultiLabelIBSM1NN extends AbstractIBSM1NN<List<Integer>> {

	public MultiLabelIBSM1NN(List<Sequence> train, List<List<Integer>> classId, int numDimensions, int maxDuration) {
		super(train, classId, numDimensions, maxDuration);
	}

}
