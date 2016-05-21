package distance.feature.extraction.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import distance.feature.extraction.DistanceFeatureMatrix;
import distance.feature.extraction.DistanceFeatureExtractor;
import distance.feature.extraction.exceptions.InvalidEventTableDimensionException;
import distance.feature.extraction.exceptions.TimeScaleException;
import experiment.SequenceGenerator;
import representations.CompressedEventTable;
import representations.EventTable;
import representations.Interval;
import representations.Sequence;

public class EventTableTest {

	static String path = "testdata/testSequences.txt";
	
	private static double precision = Double.MIN_VALUE;
	
	@Test
	public void tableConstructionTest1() throws IOException {
		Sequence seq1 = Sequence.readSequenceData(path).get(0);
		CompressedEventTable eventTable = new CompressedEventTable(seq1,12);
		assertArrayEquals(new int[]{1,11,27,31,37},eventTable.getTimeAxis());
		byte[][] expectedEventTable = {	{1,1,1,0,0},
										{0,0,0,0,0},
										{1,0,1,0,0},
										{0,1,0,0,0},
										{1,1,1,0,0},
										{0,0,0,0,0},
										{0,0,0,0,0},
										{1,1,1,0,0},
										{1,1,1,0,0},
										{1,1,1,0,0},
										{1,1,1,0,0},
										{1,1,1,1,0}
										};
		assertMatrixEquality(expectedEventTable,eventTable.getEventVectors());
	}

	@Test
	public void tableConstructionTest2() throws IOException {
		Sequence seq2 = Sequence.readSequenceData(path).get(1);
		CompressedEventTable eventTable = new CompressedEventTable(seq2,12);
		assertArrayEquals(new int[]{1,8,9,11,25,31,41},eventTable.getTimeAxis());
		byte[][] expectedEventTable = {	{1,1,0,1,1,1,0},
										{0,0,1,0,0,0,0},
										{1,0,0,0,1,0,0},
										{0,1,1,1,0,0,0},
										{1,1,1,1,1,0,0},
										{0,0,0,0,0,0,0},
										{0,0,0,0,0,0,0},
										{1,1,1,1,1,0,0},
										{1,1,1,1,1,0,0},
										{1,1,1,1,1,0,0},
										{1,1,1,1,1,0,0},
										{1,1,1,1,1,0,0},
										
				};
		assertMatrixEquality(expectedEventTable,eventTable.getEventVectors());
	}
	
	@Test
	public void testEuclidianDistance() throws TimeScaleException, InvalidEventTableDimensionException{
		CompressedEventTable tableA = new CompressedEventTable(new Sequence(Arrays.asList(new Interval(1, 1, 10),new Interval(3,1,20),new Interval(2,5,8))),3);
		CompressedEventTable tableB = new CompressedEventTable(new Sequence(Arrays.asList(new Interval(1, 11, 20))),3);
		CompressedEventTable tableC = new CompressedEventTable(new Sequence(Arrays.asList(new Interval(1, 1, 11),new Interval(2,5,8),new Interval(3,1,20))),3);
		CompressedEventTable tableD = new CompressedEventTable(new Sequence(Arrays.asList(new Interval(2,1,4),new Interval(2,9,20),new Interval(1, 11, 20))),3);
		CompressedEventTable tableE = new CompressedEventTable(new Sequence(Arrays.asList(new Interval(1, 1, 4),new Interval(3, 1, 20),new Interval(1, 8, 12))),3);
		assertEquals(0, tableA.euclidianDistance(tableA),precision);
		assertEquals(Math.sqrt(44), tableA.euclidianDistance(tableB),precision);
		assertEquals(Math.sqrt(1), tableA.euclidianDistance(tableC),precision);
		assertEquals(Math.sqrt(60), tableA.euclidianDistance(tableD),precision);
		assertEquals(Math.sqrt(9), tableA.euclidianDistance(tableE),precision);
	}

	@Test
	public void testKMedoidsFeatureExtractor1() throws TimeScaleException, InvalidEventTableDimensionException{
		List<Integer> classIds = Arrays.asList(1,1,1,2,2,2);
		List<Sequence> sequences = new ArrayList<>();
		//class 1:
		sequences.add(new Sequence(Arrays.asList(new Interval(1,1,5),new Interval(2,11,15),new Interval(5,1,30))));
		sequences.add(new Sequence(Arrays.asList(new Interval(1,1,9),new Interval(2,11,19),new Interval(5,1,30))));
		sequences.add(new Sequence(Arrays.asList(new Interval(1,1,10),new Interval(2,11,20),new Interval(5,1,30))));
		//class 2:
		sequences.add(new Sequence(Arrays.asList(new Interval(2,1,10),new Interval(3,1,20),new Interval(5,1,30))));
		sequences.add(new Sequence(Arrays.asList(new Interval(5,1,30))));
		sequences.add(new Sequence(Arrays.asList(new Interval(2,1,15),new Interval(3,1,10),new Interval(5,1,30))));
		DistanceFeatureExtractor extractor = new DistanceFeatureExtractor(sequences, classIds, 5);
		DistanceFeatureMatrix result = extractor.calculateDistanceFeatureMatrix(Arrays.asList(0,1,2,3,4,5));
		assertMedoidIndicesAre(sequences,Arrays.asList(1,5),result,5);
	}
	
	@Test
	public void testKMedoidsFeatureExtractor2() throws TimeScaleException, InvalidEventTableDimensionException{
		List<Integer> classIds = Arrays.asList(1,1,1,1,2,2,2,2,2);
		List<Sequence> sequences = new ArrayList<>();
		//class 1:
		sequences.add(new Sequence(Arrays.asList(new Interval(4,1,50),new Interval(2,15,20),new Interval(3,20,30),new Interval(4,20,30))));
		sequences.add(new Sequence(Arrays.asList(new Interval(4,1,50),new Interval(2,10,20),new Interval(3,20,30),new Interval(4,20,32))));
		sequences.add(new Sequence(Arrays.asList(new Interval(4,1,50),new Interval(2,10,20),new Interval(3,20,30))));
		sequences.add(new Sequence(Arrays.asList(new Interval(4,1,50),new Interval(3,20,30),new Interval(4,20,30))));
		//class 2:
		sequences.add(new Sequence(Arrays.asList(new Interval(2,1,50),new Interval(1,1,10))));
		sequences.add(new Sequence(Arrays.asList(new Interval(2,1,50),new Interval(1,15,15))));
		sequences.add(new Sequence(Arrays.asList(new Interval(2,1,50),new Interval(1,8,11))));
		sequences.add(new Sequence(Arrays.asList(new Interval(2,1,50),new Interval(1,1,20))));
		sequences.add(new Sequence(Arrays.asList(new Interval(2,1,50),new Interval(1,5,10))));
		DistanceFeatureExtractor extractor = new DistanceFeatureExtractor(sequences, classIds, 4);
		DistanceFeatureMatrix result = extractor.calculateDistanceFeatureMatrix(Arrays.asList(0,1,2,3,4,5,6,7,8));
		assertMedoidIndicesAre(sequences,Arrays.asList(0,8),result,4);
	}
	
	@Test
	public void testIBSMDistanceEquality() throws TimeScaleException, InvalidEventTableDimensionException{
		int n = 200;
		int m = 200;
		int d = 10;
		int duration = 1000;
		Random random = new Random(13);
		SequenceGenerator gen = new SequenceGenerator(m,duration,d,random);
		List<Sequence> testSequences = gen.generate(n);
		List<CompressedEventTable> compressedIBSM = new ArrayList<>();
		List<EventTable> uncompressedIBSM = new ArrayList<>();
		for(Sequence seq : testSequences){
			seq.rescaleTimeAxis(1, duration);
			compressedIBSM.add(new CompressedEventTable(seq, d));
			uncompressedIBSM.add(new EventTable(seq, d));
		}
		for(int i=0;i<testSequences.size();i++){
			for(int j=i;j<testSequences.size();j++){
				double ibsmDistance = uncompressedIBSM.get(i).euclidianDistance(uncompressedIBSM.get(j));
				double compressedIbsmDistance = compressedIBSM.get(i).euclidianDistance(compressedIBSM.get(j));
				assertEquals(ibsmDistance, compressedIbsmDistance,precision);
			}
		}
	}
	
	private void assertMedoidIndicesAre(List<Sequence> sequences, List<Integer> medoidIndices, DistanceFeatureMatrix result, int numDimensions) throws TimeScaleException, InvalidEventTableDimensionException {
		for(int col=0;col<medoidIndices.size();col++){
			for(int row = 0;row<sequences.size();row++){
				Integer medoidIndex = medoidIndices.get(col);
				CompressedEventTable medoid = new CompressedEventTable(sequences.get(medoidIndex), numDimensions);
				CompressedEventTable curSequence = new CompressedEventTable(sequences.get(row), numDimensions);
				double expectedDistance = curSequence.euclidianDistance(medoid);
				assertEquals(expectedDistance,result.get(row,col),precision);
			}
		}
		
	}

	private void assertMatrixEquality(byte[][] expected, byte[][] actual) {
		assertEquals(expected.length, actual.length);
		for(int i=0;i<expected.length;i++){
			assertArrayEquals(expected[i], actual[i]);
		}
	}
}
