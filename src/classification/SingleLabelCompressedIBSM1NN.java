package classification;

import java.util.List;

import representations.Sequence;

public class SingleLabelCompressedIBSM1NN extends AbstractCompressedIBSM1NN<Integer> {

	public SingleLabelCompressedIBSM1NN(List<Sequence> train, List<Integer> classIds, int numDimensions, int maxDuration) {
		super(train, classIds, numDimensions, maxDuration);
	}

}
