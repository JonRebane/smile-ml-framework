package experiment;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RuntimeComparisonSyntheticDataSets {

	public static void main(String[] args) throws Exception {
		ExecutorService pool = Executors.newCachedThreadPool();
		int epsilon = 5;
		int shapeletFeatureCount = 75;
		SyntheticDataExperiment syntheticExperiment = new SyntheticDataExperiment(pool,epsilon,shapeletFeatureCount);
		syntheticExperiment.runExperiment();
		pool.shutdown();
	}
}
