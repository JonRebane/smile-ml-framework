package experiment;

import java.io.File;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RuntimeComparisonRealDatasets {

	public static void main(String[] args) throws Exception {
		ExecutorService pool = Executors.newCachedThreadPool();
		int epsilon = 5;
		int shapeletFeatureCount = 75;
		File singleLabelDatasetPath = new File("data/singleLabelDatasets");
		File multiLabelDatasetPath = new File("data/multiLabelDatasets");
		RealDataExperiment experiment = new RealDataExperiment(pool,epsilon,shapeletFeatureCount,singleLabelDatasetPath,multiLabelDatasetPath,new Random(13),10);
		experiment.runExperiment();
		pool.shutdown();
	}
}
