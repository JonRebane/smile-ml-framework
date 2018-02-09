package stife.shapelet.evolution;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import data_structures.Sequence;
import stife.shapelet_size2.ShapeletExtractor;
import stife.shapelet_size2.ShapeletFeatureMatrix;

public class ExhaustiveShapeletSize2FeatureExtractor {

	private ExecutorService pool;

	public ExhaustiveShapeletSize2FeatureExtractor(){
		 pool = Executors.newCachedThreadPool();
	}
	
	public ShapeletFeatureMatrix extract(List<Sequence> train,List<Integer> classIds,int numDimensions,int epsilon,int numFeatures) throws InterruptedException, ExecutionException {
		ShapeletFeatureMatrix shapeletFeatureMatrix = new ShapeletFeatureMatrix(train.size(), numDimensions, Sequence.NUM_RELATIONSHIPS,classIds);
		//create all the jobs:
		int numSequencesPerJob = 10;
		int prev = 0;
		List<ShapeletExtractor> jobs = new LinkedList<>();
		for(int i=0;i<train.size();i+=numSequencesPerJob){
			jobs.add(new ShapeletExtractor(null, train, prev, Math.min(i+numSequencesPerJob, train.size()), shapeletFeatureMatrix, epsilon));
			prev = i+numSequencesPerJob;
		}
		//submit all jobs
		Collection<Future<?>> futures = new LinkedList<Future<?>>();
		for(ShapeletExtractor job : jobs){
			futures.add(pool.submit(job));
		}
		for (Future<?> future:futures) {
			future.get();
		}
		shapeletFeatureMatrix.featureSelection(numFeatures);
		return shapeletFeatureMatrix;
	}

	
	
}
