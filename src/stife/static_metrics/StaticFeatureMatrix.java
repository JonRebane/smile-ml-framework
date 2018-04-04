package stife.static_metrics;

public class StaticFeatureMatrix {

	double[][] matrix;
	
	public StaticFeatureMatrix(int rows, int cols) {
		matrix = new double[rows][cols];
	}

	public void set(int row, int col, double val) {
		matrix[row][col] = val;
	}

	public int numCols() {
		return matrix[0].length;
	}

	public double get(int row, int col) {
		return matrix[row][col];
	}

	public double[][] getMatrix(){
		return matrix;
	}

}
