package shapelet.extraction.test;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import experiment.SequenceGenerator;
import representations.Interval;
import representations.Sequence;
import shapelet.extraction.Shapelet;
import shapelet.extraction.ShapeletFeatureMatrix;

public class ShapeletFeatureExtractionTest {
	
	static String path = "testdata/testSequences.txt";
	static String pathLargeDatabase = "testdata/manyTestSequences.txt";
	
	@Test
	public void calcColumnIndexTest() {
		ShapeletFeatureMatrix res = new ShapeletFeatureMatrix(10, 4, 7,Arrays.asList(1,1,1,1,1,0,0,0,0,0));
		assertEquals(0,res.calcColumnIndex(1,1,1));
		assertEquals(1,res.calcColumnIndex(2,1,1));
		assertEquals(3,res.calcColumnIndex(4,1,1));
		assertEquals(4,res.calcColumnIndex(1,2,1));
		assertEquals(5,res.calcColumnIndex(2,2,1));
		assertEquals(7,res.calcColumnIndex(4,2,1));
		assertEquals(16,res.calcColumnIndex(1,1,2));
		assertEquals(17,res.calcColumnIndex(2,1,2));
		assertEquals(20,res.calcColumnIndex(1,2,2));
		assertEquals(21,res.calcColumnIndex(2,2,2));
		assertEquals(32,res.calcColumnIndex(1,1,3));
		assertEquals(36,res.calcColumnIndex(1,2,3));
		assertEquals(60,res.calcColumnIndex(1,4,4));
		assertEquals(111,res.calcColumnIndex(4,4,7));	
	}
	
	@Test
	public void getShapeletTest(){
		int numDistinctEvents = 10;
		int numRelationships = 7;
		ShapeletFeatureMatrix res = new ShapeletFeatureMatrix(10, numDistinctEvents, numRelationships,Arrays.asList(1,1,1,1,1,0,0,0,0,0));
		for(int i=1;i<=numDistinctEvents;i++){
			for(int j=1;j<=numDistinctEvents;j++){
				for(int r=1;r<=numRelationships;r++){
					int colInd = res.calcColumnIndex(i, j, r);
					Shapelet shapelet = res.getShapeletOfColumn(colInd);
					assertEquals(i, shapelet.getEventId1());
					assertEquals(j, shapelet.getEventId2());
					assertEquals(r, shapelet.getRelationshipId());
				}
			}
			
		}
	}
	
	@Test
	public void getRelationshipTest(){
		Sequence seq = new Sequence(Arrays.asList(new Interval(1,1,50)));
		int e = 5;
		int meet = 1; int match = 2; int overlap = 3; int leftContains = 4; int contains = 5;int rightContains = 6; int followedBy = 7;
		assertEquals(match,seq.getRelationship(10, 50, 10, 50, e));
		assertEquals(leftContains,seq.getRelationship(10, 50, 5, 30, e));
		assertEquals(rightContains,seq.getRelationship(10, 50, 20, 53, e));
		assertEquals(contains,seq.getRelationship(10, 50, 20, 30, e));
		assertEquals(meet,seq.getRelationship(10, 50, 45, 70, e));
		assertEquals(leftContains,seq.getRelationship(10, 50, 5, 30, e));
		assertEquals(meet,seq.getRelationship(10, 50, 45, 70, e));
		assertEquals(meet,seq.getRelationship(10, 50, 50, 70, e));
		assertEquals(meet,seq.getRelationship(10, 50, 55, 70, e));
		assertEquals(followedBy,seq.getRelationship(10, 50, 56, 70, e));
		assertEquals(overlap,seq.getRelationship(10, 50, 44, 70, e));
		assertEquals(overlap,seq.getRelationship(10, 50, 16, 70, e));
		assertEquals(overlap,seq.getRelationship(10, 50, 44, 70, e));
	}
	
	@Test
	public void extractAllShapeletsTest() throws IOException{
		Sequence seq = Sequence.readSequenceData(path).get(0);
		ShapeletFeatureMatrix shapeletFeatureMatrix = new ShapeletFeatureMatrix(1, 12, Sequence.NUM_RELATIONSHIPS, Arrays.asList(1));
		seq.countAllShapelets(0, shapeletFeatureMatrix, 5);
		assertEquals(9*8+1,shapeletFeatureMatrix.rowSum(0));
		//test a few single entries:
		//Dimension 3 overlaps with Dimension 1 exactly once (relationship between interval 1 and interval 2 in the data)
		int col = shapeletFeatureMatrix.calcColumnIndex(3, 1, 3);
		assertEquals(1,shapeletFeatureMatrix.getAt(0,col));
		//Dimension 1 left-contains Dimension 3 exactly once (relationship between interval 2 and interval 1 in the data)
		col = shapeletFeatureMatrix.calcColumnIndex(1, 3, 4);
		assertEquals(1,shapeletFeatureMatrix.getAt(0,col));
		//Dimension 1 right-contains Dimension 3 exactly once (relationship between interval 2 and interval 10 in the data)
		col = shapeletFeatureMatrix.calcColumnIndex(1, 3, 6);
		assertEquals(1,shapeletFeatureMatrix.getAt(0,col));
		//second sequence:
		seq = Sequence.readSequenceData(path).get(1);
		shapeletFeatureMatrix = new ShapeletFeatureMatrix(1, 12, Sequence.NUM_RELATIONSHIPS, Arrays.asList(1));
		seq.countAllShapelets(0, shapeletFeatureMatrix, 5);
		assertEquals(11*8 + 3*3,shapeletFeatureMatrix.rowSum(0));
	}
	
	@Test
	public void extractShapeletTest() throws IOException{
		List<Sequence> testSequences = Sequence.readSequenceData(path);
		Sequence seq = testSequences.get(0);
		int meet = 1; int match = 2; int overlap = 3; int leftContains = 4; int contains = 5;int rightContains = 6; int followedBy = 7;
		int e = 5;
		assertEquals(1,seq.countShapeletOccurance(new Shapelet(3, 1, overlap), e));
		assertEquals(1,seq.countShapeletOccurance(new Shapelet(1, 3, leftContains), e));
		assertEquals(1,seq.countShapeletOccurance(new Shapelet(1, 3, rightContains), e));
		seq = testSequences.get(2);
		assertEquals(3,seq.countShapeletOccurance(new Shapelet(1, 3, match), e));
		assertEquals(1,seq.countShapeletOccurance(new Shapelet(1, 3, overlap), e));
	}

	@Test
	public void extractShapeletLargeTest() throws IOException{
		List<Sequence> testSequences = Sequence.readSequenceData(pathLargeDatabase);
		int e=5;
		//classes are irrelevant for this test, generate some:
		List<Integer> classIds = new ArrayList<>();
		for(int i=0;i<testSequences.size();i++){
			classIds.add(1);
		}
		ShapeletFeatureMatrix shapeletFeatureMatrix = new ShapeletFeatureMatrix(testSequences.size(), Sequence.getDimensionSet(testSequences).size(), Sequence.NUM_RELATIONSHIPS, classIds);
		for(int i=0;i<testSequences.size();i++){
			Sequence curSeq = testSequences.get(i);
			curSeq.countAllShapelets(i, shapeletFeatureMatrix, e);
			for(int j=0;j<shapeletFeatureMatrix.numCols();j++){
				Shapelet curShapelet = shapeletFeatureMatrix.getShapeletOfColumn(j);
				short directCountResult = curSeq.countShapeletOccurance(curShapelet, e);
				short matrixCountResult = shapeletFeatureMatrix.getAt(i, j);
				assertEquals(matrixCountResult,directCountResult);
			}
		}
	}
}
