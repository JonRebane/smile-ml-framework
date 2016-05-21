package distance.feature.extraction.test;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

import data_structures.CompressedEventTable;
import data_structures.Interval;
import data_structures.Sequence;
import stife.distance.MeanEventTable;
import stife.distance.exceptions.InvalidEventTableDimensionException;
import stife.distance.exceptions.TimeScaleException;

public class MeanEventTableTest {

	private static double precision = Double.MIN_VALUE;
	
	@Test
	public void testMean1() throws TimeScaleException, InvalidEventTableDimensionException {
		CompressedEventTable tableA = new CompressedEventTable(new Sequence(Arrays.asList(new Interval(1, 1, 10),new Interval(3,1,20),new Interval(2,15,20))),3);
		CompressedEventTable tableB = new CompressedEventTable(new Sequence(Arrays.asList(new Interval(1, 1, 10),new Interval(3,1,20),new Interval(2,13,17))),3);
		CompressedEventTable tableC = new CompressedEventTable(new Sequence(Arrays.asList(new Interval(1, 1, 5),new Interval(3,1,20),new Interval(2,11,17))),3);
		MeanEventTable mean = new MeanEventTable(Arrays.asList(tableA,tableB,tableC));
		//time axis:
		assertArrayEquals(new int[]{1,6,11,13,15,18,21}, mean.getTimeAxis());
		double[][] expectedEventTable = {	{1.0, 2.0/3.0, 0.0    , 0.0    , 0.0, 0.0    , 0.0},
											{0.0, 0.0    , 1.0/3.0, 2.0/3.0, 1.0, 1.0/3.0, 0.0},
											{1.0, 1.0    , 1.0    , 1.0    , 1.0, 1.0    , 0.0},
		};
		assertMatrixEquality(expectedEventTable, mean.getEventVectors());
		double expectedDistToMean = Math.sqrt(	5*Math.pow(1.0/3.0,2) +  //dimension 1
												2*Math.pow(1.0/3.0,2) + 2*Math.pow(2.0/3.0,2) + 3*Math.pow(2.0/3.0,2) //dimension 2
												+0.0 //dimension 3
											);
		assertEquals(expectedDistToMean,mean.euclidianDistance(tableA),precision);
	}
	
	@Test
	public void testMean2() throws TimeScaleException{
		CompressedEventTable tableA = new CompressedEventTable(new Sequence(Arrays.asList(new Interval(1, 1, 50),new Interval(2,1,5),new Interval(3,10,20))),3);
		CompressedEventTable tableB = new CompressedEventTable(new Sequence(Arrays.asList(new Interval(1, 1, 50),new Interval(2,1,5),new Interval(3,10,20))),3);
		CompressedEventTable tableC = new CompressedEventTable(new Sequence(Arrays.asList(new Interval(1, 1, 50),new Interval(2,1,5),new Interval(3,10,19))),3);
		MeanEventTable a = new MeanEventTable(Arrays.asList(tableA,tableB,tableC));
		//time axis:
		assertArrayEquals(new int[]{1,6,10,20,21,51}, a.getTimeAxis());
		double[][] expectedEventTable = {	{1.0, 1.0, 1.0, 1.0,     1.0, 0.0},
											{1.0, 0.0, 0.0, 0.0,     0.0, 0.0},
											{0.0, 0.0, 1.0, 2.0/3.0, 0.0, 0.0},
		};
		assertMatrixEquality(expectedEventTable, a.getEventVectors());
	}
	
	private void assertMatrixEquality(double[][] expected, double[][] actual) {
		assertEquals(expected.length, actual.length);
		for(int i=0;i<expected.length;i++){
			assertArrayEquals(expected[i], actual[i],precision);
		}
	}

}
