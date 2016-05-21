package experiment;

import java.io.File;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RuntimeComparison {

	public static void main(String[] args) throws Exception {
		//framework variables/parameters:
		ExecutorService pool = Executors.newCachedThreadPool();
		int epsilon = 5;
		int shapeletFeatureCount = 75;
		File singleLabelDatasetPath = new File("data");
		File multiLabelDatasetPath = new File("multiLabelDatasets");
		RealDataExperiment experiment = new RealDataExperiment(pool,epsilon,shapeletFeatureCount,singleLabelDatasetPath,multiLabelDatasetPath,new Random(13),10);
		experiment.runExperiment();
		/*SyntheticDataExperiment syntheticExperiment = new SyntheticDataExperiment(pool,epsilon,shapeletFeatureCount);
		syntheticExperiment.runExperiment();*/
		pool.shutdown();
	}
}
