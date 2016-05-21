package feature.extraction.framework;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import distance.feature.extraction.DistanceFeatureExtractor;
import distance.feature.extraction.DistanceFeatureMatrix;
import distance.feature.extraction.exceptions.InvalidEventTableDimensionException;
import distance.feature.extraction.exceptions.TimeScaleException;
import representations.Sequence;
import shapelet.extraction.ShapeletExtractor;
import shapelet.extraction.ShapeletFeatureMatrix;

public class FeatureExtractor {

	private int k;
	private Random random;
	private List<Integer> classIds;
	private List<Sequence> sequences;
	private TreeSet<Integer> dimensions;
	private File dataDir;
	ExecutorService pool;
	private Mode mode;
	private int epsilon;
	private int shapeletFeatureCount;


	public FeatureExtractor(File dataDir, int k, Random random,	ExecutorService pool, Mode mode, int epsilon, int shapeletFeatureCount) throws IOException {
		this.k = k;
		this.random = random;
		this.dataDir = dataDir;
		classIds = IOService.readClassData(dataDir);
		sequences = IOService.readSequenceData(dataDir);
		dimensions = Sequence.getDimensionSet(sequences);
		this.pool = pool; Executors.newCachedThreadPool();
		this.mode = mode;
		this.epsilon = epsilon;
		this.shapeletFeatureCount = shapeletFeatureCount;
	}

	public void extractFeatures() throws IOException, InterruptedException,ExecutionException, TimeScaleException, InvalidEventTableDimensionException {
		DistanceFeatureExtractor distanceFeatureExtractor = new DistanceFeatureExtractor(sequences, classIds, dimensions.size());
		System.out.println("Identified " + sequences.size() + " sequences with " + dimensions.size() +" event dimensions");
		if(mode == Mode.kfx){
			System.out.println("Beginning " + k + "-fold cross-validation");
			List<Integer> allIndices = getShuffledIndices();
			for(int i=0; i<k; ++i)
			{
				String ifold_dir_name = dataDir.getAbsolutePath() + File.separator + k + "_" + i;
				IOService.createIfNotExists(ifold_dir_name);
				List<Integer> trainIndices = getTrainingIndices(allIndices, i);
				extractFeatures(distanceFeatureExtractor, i,k, ifold_dir_name, trainIndices);
			}
		} else{
			System.out.println("Beginning " + k + "x2 validation");
			List<List<Integer>> allTrainIndices = new ArrayList<>();
			for(int i=0;i<k;i++){
				List<Integer> curShuffling = getShuffledIndices();
				//train1:
				allTrainIndices.add(curShuffling.subList(0, curShuffling.size()/2));
				//train2:
				allTrainIndices.add(curShuffling.subList(curShuffling.size()/2, curShuffling.size()));
			}
			for(int i=0; i<allTrainIndices.size();i++){
				String ifold_dir_name = dataDir.getAbsolutePath() + File.separator + k + "_" + i;
				IOService.createIfNotExists(ifold_dir_name);
				List<Integer> trainIndices = allTrainIndices.get(i);
				extractFeatures(distanceFeatureExtractor, i,allTrainIndices.size(), ifold_dir_name, trainIndices);
			}
		}
		
	}

	private void extractFeatures(DistanceFeatureExtractor distanceFeatureExtractor, int curFold,int max, String ifold_dir_name,List<Integer> trainIndices) throws IOException, InterruptedException, ExecutionException,TimeScaleException, InvalidEventTableDimensionException {
		IOService.writeTrainIndices(trainIndices,ifold_dir_name + File.separator + "trainIndices.txt");
		System.out.println("Beginning shapelet extraction " + curFold +" out of " + max);	
		extractShapeletFeatures(trainIndices,ifold_dir_name + File.separator + "shapeletFeatures.txt");
		System.out.println("Finished shapelet extraction " + curFold +" out of " + max);
		System.out.println("Beginning distance feature extraction  " + curFold +" out of " + max);
		extractDistanceFeatures(distanceFeatureExtractor, ifold_dir_name, trainIndices);
		System.out.println("Finished distance feature extraction  " + curFold +" out of " + max);
	}

	private void extractDistanceFeatures(DistanceFeatureExtractor distanceFeatureExtractor, String ifold_dir_name,
			List<Integer> trainIndices) throws TimeScaleException, InvalidEventTableDimensionException, IOException {
		DistanceFeatureMatrix distanceFeatureMatrix = distanceFeatureExtractor.calculateDistanceFeatureMatrix(trainIndices);
		distanceFeatureMatrix.writeToFile(ifold_dir_name + File.separator + "distanceFeatures.txt");
	}

	private List<Integer> getTrainingIndices(List<Integer> allIndices, int i) {
		assert(allIndices.size()==sequences.size());
		List<Integer> trainIndices = new ArrayList<>();
		trainIndices.addAll(allIndices.subList(0, i*allIndices.size()/k));
		trainIndices.addAll(allIndices.subList((i+1)*allIndices.size()/k, allIndices.size()));
		trainIndices = Collections.unmodifiableList(trainIndices);
		return trainIndices;
	}
	
	private List<Integer> getShuffledIndices() {
		List<Integer> shuffledIndices = new ArrayList<>();
		for(int i = 0;i<sequences.size();i++){
			shuffledIndices.add(i);
		}
		Collections.shuffle(shuffledIndices, random);
		assert(shuffledIndices.size()==sequences.size());
		return shuffledIndices;
	}

	private void extractShapeletFeatures(Collection<Integer> trainIndices, String targetPath) throws InterruptedException, ExecutionException, IOException {
		assert(sequences.size() == classIds.size());
		ShapeletFeatureMatrix res = new ShapeletFeatureMatrix(sequences.size(), dimensions.size(), Sequence.NUM_RELATIONSHIPS,classIds);
		//create all the jobs:
		int numSequencesPerJob = 10;
		int prev = 0;
		List<ShapeletExtractor> jobs = new LinkedList<>();
		for(int i=0;i<sequences.size();i+=numSequencesPerJob){
			jobs.add(new ShapeletExtractor(sequences, prev, Math.min(i+numSequencesPerJob, sequences.size()), res, epsilon));
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
		res.featureSelection(trainIndices,shapeletFeatureCount);
		res.writeToFile(targetPath);
	}

}
