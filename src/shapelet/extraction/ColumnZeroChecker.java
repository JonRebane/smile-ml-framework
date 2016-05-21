package shapelet.extraction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

public class ColumnZeroChecker implements Callable<List<Integer>> {

	private short[][] shapeletFeatureMatrix;
	private Collection<Integer> trainIndices;
	private int upper;
	private int lower;

	public ColumnZeroChecker(Collection<Integer> trainIndices,short[][] shapeletFeatureMatrix, int lower, int upper) {
		this.trainIndices = trainIndices;
		this.shapeletFeatureMatrix = shapeletFeatureMatrix;
		this.lower = lower;
		this.upper = upper;
	}

	private boolean columnAllZero(int col) {
		boolean allZero = true;
		for(int row = 0;row<shapeletFeatureMatrix.length;row++){
			if(trainIndices.contains(row) && shapeletFeatureMatrix[row][col]!=0){
				allZero = false;
				break;
			}
		}
		return allZero;
	}

	@Override
	public List<Integer> call() throws Exception {
		List<Integer> usefulColumns = new ArrayList<Integer>();
		for(int i=lower;i<upper;i++){
			if(!columnAllZero(i)){
				usefulColumns.add(i);
			}
		}
		return usefulColumns;
	}

}
