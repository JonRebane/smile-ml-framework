package classification;

import java.util.List;

import representations.Sequence;

public class MultiLabelCompressedIBSM1NN extends AbstractCompressedIBSM1NN<List<Integer>> {

	public MultiLabelCompressedIBSM1NN(List<Sequence> train, List<List<Integer>> classIds, int numDimensions,int maxDuration) {
		super(train, classIds, numDimensions, maxDuration);
		// TODO Auto-generated constructor stub
	}

}
