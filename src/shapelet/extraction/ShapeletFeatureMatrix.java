package shapelet.extraction;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/***
 * An object that encapsulates the shapelet-feature matrix and provides access and modifying methods. 
 * @author leon bornemann
 * @ Ken wuz here!
 */
public class ShapeletFeatureMatrix {

	private short[][] shapeletFeatureMatrix;
	private int numDistinctEvents;
	private List<Integer> classIds;
	private Shapelet[] shapeletsOfColumns = null;
	
	/***
	 * Initializes a shapelet feature matrix fitting to the parameters, classIds is required to be able to do feature selection
	 * @param numSequences
	 * @param numDistinctEvents
	 * @param numRelationships
	 * @param classIds
	 */
	public ShapeletFeatureMatrix(int numSequences,int numDistinctEvents, int numRelationships, List<Integer> classIds){
		assert(numSequences==classIds.size());
		this.classIds = classIds;
		this.numDistinctEvents = numDistinctEvents;
		shapeletFeatureMatrix = new short[numSequences][numDistinctEvents*numDistinctEvents*numRelationships];
	}
	
	/***
	 * Increments the count of the shapelet (dimensonA,dimensionB,relationshipId) for the sequence seqId by one
	 * @param seqId
	 * @param dimensionA
	 * @param dimensionB
	 * @param relationshipId
	 */
	public void incAt(int seqId,int dimensionA,int dimensionB,int relationshipId){
		int col = calcColumnIndex(dimensionA,dimensionB,relationshipId);
		shapeletFeatureMatrix[seqId][col]++;
	}

	/***
	 * Only public so we can do unit testing!
	 * @param eventId1
	 * @param eventId2
	 * @param relationshipId
	 * @return
	 */
	public int calcColumnIndex(int eventId1, int eventId2, int relationshipId) {
		return eventId1-1 + (eventId2-1)*numDistinctEvents + (relationshipId-1)*numDistinctEvents*numDistinctEvents;
	}
	
	/***
	 * Returns the shapelet, that is assigned to the specified column index
	 * @param colIndex
	 * @return
	 */
	public Shapelet getShapeletOfColumn(int colIndex){
		if(shapeletsOfColumns==null){
			//no feature selection took place yet.
			int eventId1 = colIndex%(numDistinctEvents) + 1;
			int eventId2 = (colIndex/numDistinctEvents)%(numDistinctEvents) +1;
			int relationshipId = colIndex/(numDistinctEvents*numDistinctEvents) +1;
			return new Shapelet(eventId1,eventId2,relationshipId);
		} else{
			return shapeletsOfColumns[colIndex];
		}
	}
	
	/***
	 * selects and keeps the features that have the highest information gain, this will reduce the number of columns to n. This method will look at the information gain for all the seqeunces.
	 * @param n the number of features to keep
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 */
	public void featureSelection(int n) throws InterruptedException, ExecutionException {
		List<Integer> allIndices = new ArrayList<>();
		for(int i=0;i<shapeletFeatureMatrix.length;i++){
			allIndices.add(i);
		}
		featureSelection(allIndices,n);
	}
	
	/***
	 * selects and keeps the features that have the highest information gain, this will reduce the number of columns to n
	 * @param trainIndices the rows (sequence indices) that are to be considered for feature selection
	 * @param n the number of features to keep
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 */
	public void featureSelection(Collection<Integer> trainIndices, int n) throws InterruptedException, ExecutionException{
		//remove useless columns:
		removeAllZeroColumns(trainIndices);
		assert(n <= shapeletFeatureMatrix[0].length);
		if(n>shapeletFeatureMatrix[0].length){
			return;
		}
		Shapelet[] newShapeletsOfColumns = new Shapelet[n];
		TreeSet<IndexGainPair> set = new TreeSet<>();
		for(int i=0;i<shapeletFeatureMatrix[0].length;i++){
			//TODO
			double gain = FeatureSelection.calcInfoGain(getColumn(i,trainIndices),getElementsByIndices(classIds,trainIndices));
			set.add(new IndexGainPair(i,gain));
		}
		int count = 0;
		short[][] newResultMatrix = new short[shapeletFeatureMatrix.length][n];
		Iterator<IndexGainPair> orderedIterator = set.descendingIterator();
		//prev is just used for some assertions that sorting worked
		IndexGainPair prev = null;
		while(count < n){
			assert(orderedIterator.hasNext());
			IndexGainPair curPair = orderedIterator.next();
			if(prev!=null){
				assert(prev.getInformationGain()>=curPair.getInformationGain());
			}
			int colIndex = curPair.getIndex();
			copyColumn(newResultMatrix,count,shapeletFeatureMatrix,colIndex);
			Shapelet shapeletOfColumn = getShapeletOfColumn(colIndex);
			assert(shapeletOfColumn!=null);
			newShapeletsOfColumns[count] = shapeletOfColumn;
			prev = curPair;
			count++;
		}
		shapeletFeatureMatrix = newResultMatrix;
		shapeletsOfColumns = newShapeletsOfColumns;
	}

	private List<Integer> getElementsByIndices(List<Integer> allElements,Collection<Integer> indices) {
		List<Integer> subElements = new ArrayList<>();
		for(int i=0;i<allElements.size();i++){
			if(indices.contains(i)){
				subElements.add(allElements.get(i));
			}
		}
		return subElements;
	}

	private void removeAllZeroColumns(Collection<Integer> trainIndices) throws InterruptedException, ExecutionException {
		ExecutorService pool = Executors.newCachedThreadPool();
		List<Integer> usefulColumns = extractUsefulColumns(trainIndices,pool);
		reduceColumns(usefulColumns,pool);
		pool.shutdown();		
	}

	private void reduceColumns(List<Integer> usefulColumns, ExecutorService pool) throws InterruptedException, ExecutionException {
		Shapelet[] newShapeletsOfColumns = new Shapelet[usefulColumns.size()];
		short[][] newResMatrix = new short[shapeletFeatureMatrix.length][usefulColumns.size()];
		int incCount = 1000;
		List<Future<?>> futures = new ArrayList<>();
		for(int i=0;i<usefulColumns.size();i+=incCount){
			futures.add(pool.submit(new ColumnCopier(this,usefulColumns,shapeletFeatureMatrix,newResMatrix,newShapeletsOfColumns,i,Math.min(i+incCount,usefulColumns.size()))));
		}
		for(Future<?> future :futures){
			future.get();
		}
		shapeletFeatureMatrix = newResMatrix;
		assert(shapeletsOfColumns==null);
		shapeletsOfColumns = newShapeletsOfColumns;
	}

	private List<Integer> extractUsefulColumns(Collection<Integer> trainIndices, ExecutorService pool) throws InterruptedException, ExecutionException {
		int incCount = 1000;
		List<Future<List<Integer>>> futures = new ArrayList<>();
		for(int col = 0;col<shapeletFeatureMatrix[0].length;col += incCount){
			futures.add(pool.submit(new ColumnZeroChecker(trainIndices,shapeletFeatureMatrix,col,Math.min(col+incCount,shapeletFeatureMatrix[0].length))));
		}
		List<Integer> usefullColumns = new ArrayList<>();
		for(Future<List<Integer>> future :futures){
			usefullColumns.addAll(future.get());
		}
		return usefullColumns;
	}

	private short[] getColumn(int colIndex, Collection<Integer> indices) {
		short[] column = new short[indices.size()];
		int index = 0;
		for(int i=0;i<shapeletFeatureMatrix.length;i++){
			if(indices.contains(i)){
				column[index] = shapeletFeatureMatrix[i][colIndex];
				index++;
			}
		}
		return column;
	}

	private void copyColumn(short[][] targetMatrix, int targetColumn, short[][] sourceMatrix, int sourceColumn) {
		for(int i = 0; i< targetMatrix.length;i++){
			targetMatrix[i][targetColumn] = sourceMatrix[i][sourceColumn];
		}
	}

	public void writeToFile(String targetPath) throws IOException {
		PrintWriter pr = new PrintWriter(new FileWriter(new File(targetPath)));
		for(int i=0;i<shapeletFeatureMatrix.length;i++){
			for(int j=0;j<shapeletFeatureMatrix[i].length;j++){
				pr.print(shapeletFeatureMatrix[i][j]);
				if(j!=shapeletFeatureMatrix[i].length-1){
					pr.print(" ");
				}
			}
			if(i!=shapeletFeatureMatrix.length-1){
				pr.println();
			}
		}
		pr.close();
	}

	//useful unit testing methods:
	
	public int rowSum(int row) {
		int sum=0;
		for(int col = 0;col<shapeletFeatureMatrix[0].length;col++){
			sum+=shapeletFeatureMatrix[row][col];
		}
		return sum;
	}

	public short getAt(int row, int col) {
		return shapeletFeatureMatrix[row][col];
	}

	public int numCols() {
		return shapeletFeatureMatrix[0].length;
	}

	public short[][] getMatrix() {
		return shapeletFeatureMatrix;
	}	
}
