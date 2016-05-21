package stife.distance;

import java.util.List;

public class DistanceMatrix {
	
	public DistanceMatrix(int rows, int cols) {
		matrix = new double[rows][cols];
		colnames = new String[cols];
	}

	protected String[] colnames;
	
	protected double[][] matrix;

	public void setColnames(List<Integer> indices) {
		for(int i=0;i<indices.size();i++){
			colnames[i] = indices.get(i).toString();
		}
	}

	public void set(int row, int col, double value) {
		matrix[row][col] = value;
		
	}

	public int getLowestColSumIndex() {
		int bestColIndex = -1;
		double bestSum = Double.MAX_VALUE;
		for(int col = 0;col<matrix[0].length;col++){
			double sum = 0;
			for(int row = 0;row<matrix.length;row++){
				sum += matrix[row][col];
			}
			if(sum<bestSum){
				bestSum = sum;
				bestColIndex = col;
			}
		}
		return bestColIndex;
	}
	
	public void setColname(int col, String colname) {
		colnames[col] = colname;
	}
	
	public double get(int row, int col) {
		return matrix[row][col];
	}
}
