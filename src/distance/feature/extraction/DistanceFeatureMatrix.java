package distance.feature.extraction;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import representations.CompressedEventTable;

public class DistanceFeatureMatrix extends DistanceMatrix{
	
	private List<CompressedEventTable> columnRepresentation;

	public DistanceFeatureMatrix(int rows, int cols) {
		super(rows,cols);
		this.columnRepresentation = new ArrayList<>(cols);
		for(int i=0;i<cols;i++){
			columnRepresentation.add(null);
		}
		assert(columnRepresentation.size()==cols);
	}

	public String[] getColnames() {
		return colnames;
	}

	public void writeToFile(String path) throws IOException {
		PrintWriter writer = new PrintWriter(new FileWriter(new File(path)));
		for(int row = 0;row<matrix.length;row++){
			for(int col = 0;col<matrix[0].length;col++){
				writer.print(matrix[row][col]);
				if(col!=matrix[0].length-1){
					writer.print(" ");
				}
			}
			if(row!= matrix.length-1){
				writer.println();
			}
		}
		writer.close();
	}

	/***
	 * Sets the CompressedEventTable (sequence representation) whose distance is the feature (column). It is necessary to set this for all columns if online feature extraction needs to be performed ( we need to remember which sequence belongs to which column)
	 * @param col index of the feature
	 * @param table the sequence representation to which the distance of another sequence is the feature in column @see col
	 */
	public void setCompressedEventTable(int col, CompressedEventTable table) {
		assert(col < columnRepresentation.size());
		columnRepresentation.set(col, table);
	}
	
	/***
	 * Returns the sequence representation to which the distance of another sequence is the feature in the specified column
	 * @param col
	 * @return
	 */
	public CompressedEventTable getCompressedEventTable(int col){
		return columnRepresentation.get(col);
	}

	public int numCols() {
		return matrix[0].length;
	}

	public int nrows() {
		return matrix.length;
	}

	public double[][] getMatrix() {
		return matrix;
	}
}
