package stife.static_metrics;

import java.util.List;
import java.util.Map;

import data_structures.Sequence;

public class StaticMetricExtractor {

	private static final int NUM_STATIC_FEATURES = 14; //TODO

	public StaticFeatureMatrix extractAll(List<Sequence> train) {
		StaticFeatureMatrix mat = new StaticFeatureMatrix(train.size(),NUM_STATIC_FEATURES);
		for(int i=0;i<train.size();i++){
			double[] row = extract(train.get(i));
			for(int j =0;j<row.length;j++){
				mat.set(i,j,row[j]);				
			}
		}
		return mat;
	}
	
	public double[] extract(Sequence seq){
		double[] features = new double[NUM_STATIC_FEATURES];
		features[0] = seq.duration();
		features[1] = seq.earliestStart();
		Map<Integer,Integer> dimMap = seq.getDimensionOccurances();
		features[2] = getKeyWithHighestEntry(dimMap); //nominal
		features[3] = seq.intervalCount();
		features[4] = dimMap.keySet().size();
		features[5] = seq.getDensity();
		features[6] = seq.getDensity()/features[0];
		features[7] = seq.getMaxConcurrentIntervalCount();
		features[8] = seq.getConcurrentIntervalDuration((int)features[7]);
		features[9] = features[8] / features[0];
		features[10] = seq.getSummedPauseTime();
		features[11] = features[10] / features[0];
		features[12] = features[0] - features[10];
		features[13] = (features[0] - features[10]) / features[0];
		return features;
	}

	private Integer getKeyWithHighestEntry(Map<Integer, Integer> dimMap) {
		int max = -1;
		Integer maxDimension = -1;
		for(Integer curDim : dimMap.keySet()){
			Integer curCount = dimMap.get(curDim);
			if(max == -1 || curCount>max || (curCount == max && curDim < maxDimension)){
				max =curCount;
				maxDimension = curDim;
			}
		}
		return maxDimension;
	}

}
