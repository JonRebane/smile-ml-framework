package stife.shapelet_size2;

import java.util.List;

public class ColumnCopier implements Runnable {

	private double[][] oldShapeletFeatureMatrix;
	private double[][] newShapeletFeatureMatrix;
	private Shapelet_Size2[] newShapeletsOfColumns;
	private int upper;
	private int lower;
	private List<Integer> usefulColumns;
	private ShapeletFeatureMatrix matrixObject;

	public ColumnCopier(ShapeletFeatureMatrix matrixObject, List<Integer> usefulColumns, double[][] oldShapeletFeatureMatrix, double[][] newShapeletFeatureMatrix, Shapelet_Size2[] newShapeletsOfColumns,int lower, int upper) {
		this.matrixObject = matrixObject;
		this.usefulColumns = usefulColumns;
		this.oldShapeletFeatureMatrix = oldShapeletFeatureMatrix;
		this.newShapeletFeatureMatrix = newShapeletFeatureMatrix;
		this.newShapeletsOfColumns = newShapeletsOfColumns;
		this.lower = lower;
		this.upper = upper;
	}

	@Override
	public void run() {
		for(int i=lower;i<upper;i++){
			newShapeletsOfColumns[i] = matrixObject.getShapeletOfColumn(usefulColumns.get(i));
			copyColumn(newShapeletFeatureMatrix,i,oldShapeletFeatureMatrix,usefulColumns.get(i));
		}
	}
	
	private void copyColumn(double[][] targetMatrix, int targetColumn, double[][] sourceMatrix, int sourceColumn) {
		for(int i = 0; i< targetMatrix.length;i++){
			targetMatrix[i][targetColumn] = sourceMatrix[i][sourceColumn];
		}
	}

}
