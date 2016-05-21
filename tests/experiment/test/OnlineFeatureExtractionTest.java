package experiment.test;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;

import classification.AbstractSTIFERFClassifier;
import classification.SingleLabelSTIFERFClassifier;
import distance.feature.extraction.DistanceFeatureExtractor;
import distance.feature.extraction.DistanceFeatureMatrix;
import experiment.ExperimentUtil;
import experiment.SequenceGenerator;
import feature.extraction.framework.IOService;
import representations.Sequence;
import shapelet.extraction.Shapelet;
import shapelet.extraction.ShapeletExtractor;
import shapelet.extraction.ShapeletFeatureMatrix;

public class OnlineFeatureExtractionTest {

	@Test
	public void test() throws Exception {
		double tolerance = 0.00000000000001;
		int n = 1000;
		int m = 500;
		int d = 10;
		int duration = 1000;
		Random random = new Random(13);
		SequenceGenerator gen = new SequenceGenerator(500,duration,d,random);
		//framework:
		int epsilon = 5;
		int numShapeletFeatures = 75;
		List<Sequence> seqs = gen.generate(n);
		for(Sequence seq:seqs){
			seq.sortIntervals();
		}
		List<Integer> trainIndices = new ArrayList<>();
		List<Integer> testIndices = new ArrayList<>();
		List<Integer> classIds = ExperimentUtil.generateRandomBinaryClassIds(1000, new Random(13));
		for(int i=0;i<n;i++){
			if(i<n/2){
				trainIndices.add(i);
			} else{
				testIndices.add(i);
			}
		}
		//shapelet:
		ShapeletFeatureMatrix mat = new ShapeletFeatureMatrix(n, d, Sequence.NUM_RELATIONSHIPS, classIds);
		ShapeletExtractor extractor = new ShapeletExtractor(seqs, 0, 1000, mat, epsilon);
		extractor.run();
		mat.featureSelection(trainIndices, numShapeletFeatures);
		//distance
		DistanceFeatureExtractor dextractor = new DistanceFeatureExtractor(seqs, classIds, d, duration);
		DistanceFeatureMatrix dmat = dextractor.calculateDistanceFeatureMatrix(trainIndices);
		//the other way:
		List<Sequence> train = ExperimentUtil.getAll(seqs,trainIndices);
		List<Sequence> test = ExperimentUtil.getAll(seqs,testIndices);
		ExecutorService pool = Executors.newCachedThreadPool();
		AbstractSTIFERFClassifier stiferf = new SingleLabelSTIFERFClassifier(train, ExperimentUtil.getAll(classIds, trainIndices), d, duration, epsilon, numShapeletFeatures, pool);
		for(Integer i : testIndices){
			Sequence curTestSequence = seqs.get(i);
			assert(test.contains(curTestSequence));
			//shapelet
			short[] onlineShapeletResult = stiferf.onlineShapeletFeatureExtraction(curTestSequence);
			assert(onlineShapeletResult.length==mat.numCols());
			for(int j =0;j<mat.numCols();j++){
				assertEquals(mat.getAt(i, j), onlineShapeletResult[j]);
			}
			//distance
			double[] onlineDistanceResult = stiferf.onlineDistanceFeatureExtraction(curTestSequence);
			for(int j=0;j<dmat.numCols();j++){
				assertEquals(dmat.get(i, j), onlineDistanceResult[j],tolerance);
			}
		}
		pool.shutdown();
	}
	
	@Test
	public void pioneer10_0RealDataTest() throws Exception {
		double tolerance = 0.00000000000001;
		String rootPath = "data/singleLabelDatasets/PIONEER/";
		String specificPath = "data/singleLabelDatasets/PIONEER/10_0/";
		List<Integer> trainIndices = IOService.readTrainIndices(new File(specificPath));
		List<Sequence> database = IOService.readSequenceData(new File(rootPath));
		List<Integer> testIndices = ExperimentUtil.getTestIndices(database.size(), trainIndices);
		int numDimensions = Sequence.getDimensionSet(database).size();
		int sequenceDuration = Sequence.getMaxDuration(database);
		ExecutorService pool = Executors.newCachedThreadPool();
		List<Integer> classIds = IOService.readClassData(new File(rootPath));
		//framework:
		int epsilon = 5;
		int numShapeletFeatures = 75;
		//shapelet:
		ShapeletFeatureMatrix mat = new ShapeletFeatureMatrix(database.size(), numDimensions, Sequence.NUM_RELATIONSHIPS, classIds);
		ShapeletExtractor extractor = new ShapeletExtractor(database, 0, database.size(), mat, epsilon);
		extractor.run();
		mat.featureSelection(trainIndices, numShapeletFeatures);
		//distance
		DistanceFeatureExtractor dextractor = new DistanceFeatureExtractor(database, classIds, numDimensions, sequenceDuration);
		DistanceFeatureMatrix dmat = dextractor.calculateDistanceFeatureMatrix(trainIndices);
		//the other way:
		List<Sequence> train = ExperimentUtil.getAll(database,trainIndices);
		List<Sequence> test = ExperimentUtil.getAll(database,testIndices);
		AbstractSTIFERFClassifier stiferf = new SingleLabelSTIFERFClassifier(train, ExperimentUtil.getAll(classIds, trainIndices), numDimensions, sequenceDuration, epsilon, numShapeletFeatures, pool);
		for(Integer i : testIndices){
			Sequence curTestSequence = database.get(i);
			assert(test.contains(curTestSequence));
			//shapelet
			short[] onlineShapeletResult = stiferf.onlineShapeletFeatureExtraction(curTestSequence);
			assert(onlineShapeletResult.length==mat.numCols());
			for(int j =0;j<mat.numCols();j++){
				assertEquals(mat.getAt(i, j), onlineShapeletResult[j]);
			}
			//distance
			double[] onlineDistanceResult = stiferf.onlineDistanceFeatureExtraction(curTestSequence);
			for(int j=0;j<dmat.numCols();j++){
				assertEquals(dmat.get(i, j), onlineDistanceResult[j],tolerance);
			}
		}
		pool.shutdown();
	}
	
	@Test
	public void testShapeletFeatureMatrixDifference() throws Exception{
		String rootPath = "data/singleLabelDatasets/PIONEER/";
		String specificPath = "data/singleLabelDatasets/PIONEER/10_0/";
		List<Integer> trainIndices = IOService.readTrainIndices(new File(specificPath));
		List<Sequence> database = IOService.readSequenceData(new File(rootPath));
		List<Integer> testIndices = ExperimentUtil.getTestIndices(database.size(), trainIndices);
		int numDimensions = Sequence.getDimensionSet(database).size();
		int sequenceDuration = Sequence.getMaxDuration(database);
		ExecutorService pool = Executors.newCachedThreadPool();
		List<Integer> classIds = IOService.readClassData(new File(rootPath));
		//framework:
		int epsilon = 5;
		int numShapeletFeatures = 75;
		//shapelet:
		ShapeletFeatureMatrix mat = new ShapeletFeatureMatrix(database.size(), numDimensions, Sequence.NUM_RELATIONSHIPS, classIds);
		ShapeletExtractor extractor = new ShapeletExtractor(database, 0, database.size(), mat, epsilon);
		extractor.run();
		mat.featureSelection(trainIndices, numShapeletFeatures);
		//alternative:
		/*ShapeletFeatureMatrix stifeMat = new ShapeletFeatureMatrix(database.size(), numDimensions, Sequence.NUM_RELATIONSHIPS, classIds);
		ShapeletExtractor extractor2 = new ShapeletExtractor(database, 0, database.size(), mat, epsilon);
		extractor.run();*/
		//stiferf:
		List<Sequence> train = ExperimentUtil.getAll(database,trainIndices);
		List<Sequence> test = ExperimentUtil.getAll(database,testIndices);
		AbstractSTIFERFClassifier stiferf = new SingleLabelSTIFERFClassifier(train, ExperimentUtil.getAll(classIds, trainIndices), numDimensions, sequenceDuration, epsilon, numShapeletFeatures, pool);
		ShapeletFeatureMatrix stifeMat = stiferf.getShapeletFeatureMatrix();
		assertTrue(stifeMat.numCols() == mat.numCols());
		for(int col = 0;col<stifeMat.numCols();col++){
			Shapelet stifeColShapelet = stifeMat.getShapeletOfColumn(col);
			Shapelet matColShapelet = mat.getShapeletOfColumn(col);
			assertEquals(matColShapelet.getEventId1(), stifeColShapelet.getEventId1());
			assertEquals(matColShapelet.getEventId2(), stifeColShapelet.getEventId2());
			assertEquals(matColShapelet.getRelationshipId(), stifeColShapelet.getRelationshipId());
		}
	}
}
