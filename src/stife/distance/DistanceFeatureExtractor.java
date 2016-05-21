package stife.distance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import data_structures.CompressedEventTable;
import data_structures.Sequence;
import stife.distance.exceptions.InvalidEventTableDimensionException;
import stife.distance.exceptions.TimeScaleException;

/***
 * This class realizes the distance based feature extraction part of the framework. For details see TODO paper reference
 * 
 * 
 * @author leon bornemann
 *
 */
public class DistanceFeatureExtractor {

	private List<Integer> classIds;
	private List<CompressedEventTable> resizedSequenceTables;

	public DistanceFeatureExtractor(List<Sequence> sequences, List<Integer> classIds,int numDimensions){
		int maxDuration = Sequence.getMaxDuration(sequences);
		init(sequences, classIds, numDimensions, maxDuration);
	}
	
	/***
	 * Additional Constructor that allows the specification of a maxDuration (necessary if that is bigger than any of the duration of the sequences in the specified list)
	 * @param sequences
	 * @param classIds
	 * @param numDimensions
	 * @param maxDuration
	 */
	public DistanceFeatureExtractor(List<Sequence> sequences, List<Integer> classIds, int numDimensions,int maxDuration) {
		init(sequences, classIds, numDimensions, maxDuration);
	}
	
	private void init(List<Sequence> sequences, List<Integer> classIds, int numDimensions, int maxDuration) {
		//create new, resized sequences and create their event-table representation:
		List<CompressedEventTable> resizedSequenceTables = new ArrayList<>();
		for(Sequence seq : sequences){
			Sequence resizedSeq = new Sequence(seq);
			resizedSeq.rescaleTimeAxis(1, maxDuration);
			resizedSequenceTables.add(new CompressedEventTable(resizedSeq, numDimensions));
		}
		this.resizedSequenceTables = resizedSequenceTables;
		this.classIds = classIds;
	}

	public DistanceFeatureMatrix calculateMedoidDistanceFeatureMatrix(List<Integer> trainIndices) throws TimeScaleException, InvalidEventTableDimensionException{
		Map<Integer,List<Integer>> trainIndicesByClass = buildClassToTrainIndicesMap(trainIndices);
		DistanceFeatureMatrix featureMatrix = new DistanceFeatureMatrix(resizedSequenceTables.size(),trainIndicesByClass.keySet().size());
		int featureMatrixCol = 0;
		for(Integer clazz : trainIndicesByClass.keySet()){
			List<Integer> clusterElementIndices = trainIndicesByClass.get(clazz);
			Collections.sort(clusterElementIndices);
			//k-medoids:
			calculateDistanceToMedoid(featureMatrix, featureMatrixCol, clusterElementIndices);
			featureMatrixCol++;
		}
		return featureMatrix;
	}
	
	/***
	 * analyzes all the sequences, and returns the best distance features
	 * @return
	 * @throws TimeScaleException
	 * @throws InvalidEventTableDimensionException
	 */
	public DistanceFeatureMatrix calculateDistanceFeatureMatrix() throws TimeScaleException, InvalidEventTableDimensionException {
		List<Integer> allIndices = new ArrayList<>();
		for(int i=0;i<resizedSequenceTables.size();i++){
			allIndices.add(i);
		}
		return calculateDistanceFeatureMatrix(allIndices);
	}
	
	/***
	 * Extracts the features for all sequences, but only analyzes the sequences, whose index is in trainIndices.
	 * @param trainIndices
	 * @return
	 * @throws TimeScaleException
	 * @throws InvalidEventTableDimensionException
	 */
	public DistanceFeatureMatrix calculateDistanceFeatureMatrix(List<Integer> trainIndices) throws TimeScaleException, InvalidEventTableDimensionException{
		Map<Integer,List<Integer>> trainIndicesByClass = buildClassToTrainIndicesMap(trainIndices);
		DistanceFeatureMatrix featureMatrix = new DistanceFeatureMatrix(resizedSequenceTables.size(),trainIndicesByClass.keySet().size());
		int featureMatrixCol = 0;
		for(Integer clazz : trainIndicesByClass.keySet()){
			List<Integer> clusterElementIndices = trainIndicesByClass.get(clazz);
			Collections.sort(clusterElementIndices);
			//k-medoids:
			calculateDistanceToMedoid(featureMatrix, featureMatrixCol, clusterElementIndices);
			featureMatrixCol++;
			//k-means:
			//calculateDistanceToCentroid(featureMatrix,featureMatrixCol,clusterElementIndices,clazz);
			//featureMatrixCol++;
		}
		return featureMatrix;
	}

	private void calculateDistanceToCentroid(DistanceFeatureMatrix featureMatrix, int featureMatrixCol,List<Integer> clusterElementIndices,Integer clazz) throws TimeScaleException, InvalidEventTableDimensionException {
		HashSet<CompressedEventTable> cluster = new HashSet<>();
		for(Integer index : clusterElementIndices){
			cluster.add(resizedSequenceTables.get(index));
		}
		MeanEventTable centroid = new MeanEventTable(cluster);
		for(int i=0;i<resizedSequenceTables.size();i++){
			double distToCentroid = centroid.euclidianDistance(resizedSequenceTables.get(i));
			//set in the distance feature matrix
			featureMatrix.set(i,featureMatrixCol,distToCentroid);
		}
		featureMatrix.setColname(featureMatrixCol,"Distance to centroid of class "+clazz);
		
	}

	private CompressedEventTable calculateDistanceToMedoid(DistanceFeatureMatrix featureMatrix, int featureMatrixCol,List<Integer> clusterElementIndices) throws TimeScaleException, InvalidEventTableDimensionException {
		DistanceMatrix distanceMatrix = calculatePairwiseDistances(clusterElementIndices);
		//get cluster medoid:
		int colIndex = distanceMatrix.getLowestColSumIndex();
		//colIndex is the same as in indices:
		int medoidIndex = clusterElementIndices.get(colIndex);
		CompressedEventTable medoid = resizedSequenceTables.get(medoidIndex);
		//calculate distances of all sequences to the medoid:
		for(int i=0;i<resizedSequenceTables.size();i++){
			double distToMedoid = resizedSequenceTables.get(i).euclidianDistance(medoid);
			//set in the distance feature matrix
			featureMatrix.set(i,featureMatrixCol,distToMedoid);
		}
		featureMatrix.setColname(featureMatrixCol,"Distance to sequence "+medoidIndex);
		featureMatrix.setCompressedEventTable(featureMatrixCol,medoid);
		return medoid;
	}

	private DistanceMatrix calculatePairwiseDistances(List<Integer> indices) throws TimeScaleException, InvalidEventTableDimensionException {
		DistanceMatrix pairwiseDistances = new DistanceMatrix(indices.size(), indices.size());
		pairwiseDistances.setColnames(indices);
		for(int i=0;i<indices.size();i++){
			int index1 = indices.get(i);
			for(int j=0;j<indices.size();j++){
				int index2 = indices.get(j);
				double distance = resizedSequenceTables.get(index1).euclidianDistance(resizedSequenceTables.get(index2));
				pairwiseDistances.set(i,j,distance);
			}
		}
		return pairwiseDistances;
		
	}

	private Map<Integer, List<Integer>> buildClassToTrainIndicesMap(List<Integer> trainIndices) {
		Map<Integer,List<Integer>> trainIndicesByClass = new HashMap<>();
		for(Integer index : trainIndices){
			Integer clazz = classIds.get(index);
			if(trainIndicesByClass.containsKey(clazz)){
				trainIndicesByClass.get(clazz).add(index);
			} else{
				List<Integer> newList = new ArrayList<>();
				newList.add(index);
				trainIndicesByClass.put(clazz, newList);
			}
		}
		return trainIndicesByClass;
	}

}
