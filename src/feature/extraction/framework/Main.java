package feature.extraction.framework;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import distance.feature.extraction.exceptions.InvalidEventTableDimensionException;
import distance.feature.extraction.exceptions.TimeScaleException;

/***
 * This module is part of the feature extraction framework for sequences of temporal intervals.
 * It handles the extraction and selection of the shapelets of size 2. For further information on that we refer
 * to the paper TODO: insert reference.
 * 
 * The module expects the directory of the all data-sets as an input, which must contain each data-set as a subdirectory, each of which must contain 
 * a file named dataSorted.txt containing the sequences of temporal intervals, sorted by sequence-id, then interval start-time, then interval end-time
 * a file named classes.txt, containing the class information
 * 
 * The module outputs the matrix of the selected shapelets, as well as the indices of the training data (starting at zero) into each directory
 * this is done, so the other parts, which are implemented in R can access that. 
 * @author leon bornemann
 *
 */
public class Main {

	/***
	 * 
	 * @param args TODO
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeScaleException
	 * @throws InvalidEventTableDimensionException
	 */
	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException, TimeScaleException, InvalidEventTableDimensionException {
		long begin = System.currentTimeMillis();
		String pathToData = args[0];
		int k = Integer.parseInt(args[1]);
		if(!args[2].equals("_5x2") && ! args[2].equals("kfx")){
			throw new AssertionError("invalid validation type");
		}
		int epsilon = Integer.parseInt(args[3]);
		int shapeletFeatureCount = Integer.parseInt(args[4]);
		Mode mode = Mode.valueOf(args[2]);
		File superDir = new File(pathToData);
		Random random = new Random(10);
		ExecutorService pool = Executors.newCachedThreadPool();
		for(File dir : superDir.listFiles()){
			if(dir.isDirectory()){
				long dirBegin = System.currentTimeMillis();
				System.out.println("-------------------------------------------------------------------------------------------------");
				System.out.println("-------------------------------------------------------------------------------------------------");
				System.out.println("Beginning evaluation of "+dir.getName());
				FeatureExtractor extractor = new FeatureExtractor(dir,k,random,pool,mode, epsilon, shapeletFeatureCount);
				extractor.extractFeatures();
				long dirEnd = System.currentTimeMillis();
				System.out.println("Finished Evaluation of "+dir.getName() + "in " + (dirEnd-dirBegin)/1000 + "s");
			}
		}
		pool.shutdown();
		long end = System.currentTimeMillis();
		System.out.println("Finished all datasets, total time taken: " + (end-begin)/1000 + "s");
	}
	
	

}
