package stife.shapelet_size2;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/***
 * Static Class that provides algorithms for feature selection (mainly information gain
 * @author leon bornemann
 *
 */
public class FeatureSelection {

	//TODO: test entropy calculation, use R as reference implementation?
	//just public so we can unit-test
	public static double calcEntropy(List<Integer> classIds) {
		Map<Integer,Integer> classFrequencies = new HashMap<>();
		for(Integer classLabel : classIds){
			if(classFrequencies.containsKey(classLabel)){
				classFrequencies.put(classLabel, classFrequencies.get(classLabel)+1);
			} else{
				classFrequencies.put(classLabel, 1);
			}
		}
		double entropy = 0;
		for(Integer classFrequency : classFrequencies.values()){
			double p = (double)classFrequency/(double)classIds.size();
			entropy += -1*p*Math.log(p)/Math.log(2);
		}
		return entropy;
	}

	/***
	 * Calculates the information gain for eventId1 numeric attribute, given it's classes
	 * @param numericAttribute
	 * @param classIds
	 * @return
	 */
	public static double calcInfoGain(short[] numericAttribute,List<Integer> classIds) {
		assert(numericAttribute.length == classIds.size());
		Integer[] order = getColumnSortedIndices(numericAttribute);
		//find the best splitting point
		double bestGain = 0;
		double entropy = calcEntropy(classIds);
		for(int i=0;i<order.length-1;i++){
			if(numericAttribute[order[i]] != numericAttribute[order[i+1]]){
				double splitVal = (numericAttribute[order[i]] + numericAttribute[order[i+1]])/2.0;
				boolean[] curSplit = columnSmallerOrEqualThan(numericAttribute,splitVal);
				double curGain = entropy - infoAfterSplit(curSplit,classIds);
				if(curGain>bestGain){
					bestGain = curGain;
				}
			}
		}
		return bestGain;
	}

	public static double calcInfoGain(double[] numericAttribute,List<Integer> classIds) {
		assert(numericAttribute.length == classIds.size());
		Integer[] order = getColumnSortedIndices(numericAttribute);
		//find the best splitting point
		double bestGain = 0;
		double entropy = calcEntropy(classIds);
		for(int i=0;i<order.length-1;i++){
			if(numericAttribute[order[i]] != numericAttribute[order[i+1]]){
				double splitVal = (numericAttribute[order[i]] + numericAttribute[order[i+1]])/2.0;
				boolean[] curSplit = columnSmallerOrEqualThan(numericAttribute,splitVal);
				double curGain = entropy - infoAfterSplit(curSplit,classIds);
				if(curGain>bestGain){
					bestGain = curGain;
				}
			}
		}
		return bestGain;
	}
	
	/***
	 * Calculates the information gain for eventId1 numeric attribute, given it's classes
	 * @param numericAttribute
	 * @param classIds
	 * @return
	 */
	public static double calcInfoGain(int[] numericAttribute,List<Integer> classIds) {
		assert(numericAttribute.length == classIds.size());
		Integer[] order = getColumnSortedIndices(numericAttribute);
		//find the best splitting point
		double bestGain = 0;
		double entropy = calcEntropy(classIds);
		for(int i=0;i<order.length-1;i++){
			if(numericAttribute[order[i]] != numericAttribute[order[i+1]]){
				double splitVal = (numericAttribute[order[i]] + numericAttribute[order[i+1]])/2.0;
				boolean[] curSplit = columnSmallerOrEqualThan(numericAttribute,splitVal);
				double curGain = entropy - infoAfterSplit(curSplit,classIds);
				if(curGain>bestGain){
					bestGain = curGain;
				}
			}
		}
		return bestGain;
	}
	
	//just public so we can unit-test
	public static double infoAfterSplit(boolean[] booleanAttribute, List<Integer> classIds) {
		List<Integer> classOfTrue = new LinkedList<>();
		List<Integer> classOfFalse = new LinkedList<>();
		for(int i=0;i<booleanAttribute.length;i++){
			if(booleanAttribute[i]){
				classOfTrue.add(classIds.get(i));
			} else{
				classOfFalse.add(classIds.get(i));
			}
		}
		return calcEntropy(classOfFalse)*classOfFalse.size()/booleanAttribute.length + calcEntropy(classOfTrue)*classOfTrue.size()/booleanAttribute.length;
	}

	private static boolean[] columnSmallerOrEqualThan(double[] numericAttribute, double splitVal) {
		boolean[] out = new boolean[numericAttribute.length];
		for(int i=0;i<numericAttribute.length;i++){
			out[i] = numericAttribute[i] <= splitVal;
		}
		return out;
	}

	private static boolean[] columnSmallerOrEqualThan(short[] numericAttribute, double splitVal) {
		boolean[] out = new boolean[numericAttribute.length];
		for(int i=0;i<numericAttribute.length;i++){
			out[i] = numericAttribute[i] <= splitVal;
		}
		return out;
	}
	
	private static boolean[] columnSmallerOrEqualThan(int[] numericAttribute, double splitVal) {
		boolean[] out = new boolean[numericAttribute.length];
		for(int i=0;i<numericAttribute.length;i++){
			out[i] = numericAttribute[i] <= splitVal;
		}
		return out;
	}

	/***
	 * returns the indices, which represent the order of the input array For example getColumnSortedIndices([20,30,1,54,43]) would return [2,0,1,4,3]
	 * only public so we can unit-test!
	 * @param column
	 * @return
	 */
	public static Integer[] getColumnSortedIndices(short[] column) {
		Integer[] indices = new Integer[column.length];
		for(int i=0;i<indices.length;i++){
			indices[i] = i;
		}
		Arrays.sort(indices, new Comparator<Integer>() {
		    @Override public int compare(final Integer o1, final Integer o2) {
		        return Short.compare(column[o1], column[o2]);
		    }
		});
		return indices;
	}
	
	/***
	 * returns the indices, which represent the order of the input array For example getColumnSortedIndices([20,30,1,54,43]) would return [2,0,1,4,3]
	 * only public so we can unit-test!
	 * @param column
	 * @return
	 */
	public static Integer[] getColumnSortedIndices(int[] column) {
		Integer[] indices = new Integer[column.length];
		for(int i=0;i<indices.length;i++){
			indices[i] = i;
		}
		Arrays.sort(indices, new Comparator<Integer>() {
			@Override public int compare(final Integer o1, final Integer o2) {
				return Integer.compare(column[o1], column[o2]);
			}
		});
		return indices;
	}

	public static Integer[] getColumnSortedIndices(double[] column) {
		Integer[] indices = new Integer[column.length];
		for(int i=0;i<indices.length;i++){
			indices[i] = i;
		}
		Arrays.sort(indices, new Comparator<Integer>() {
			@Override public int compare(final Integer o1, final Integer o2) {
				return Double.compare(column[o1], column[o2]);
			}
		});
		return indices;
	}

		
}
