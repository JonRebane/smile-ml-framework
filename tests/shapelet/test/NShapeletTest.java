package shapelet.test;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

import data_structures.Interval;
import data_structures.Sequence;
import stife.shapelet.NShapelet;
import stife.shapelet_size2_new.ShapeletSize2;

public class NShapeletTest {

	private static int epsilon = 5;

	@Test
	public void simpleTestNegative() {
		NShapelet shapelet = new NShapelet(Arrays.asList(1,1,2), Arrays.asList(ShapeletSize2.FOLLOWEDBY,ShapeletSize2.MATCH));
		Sequence seq = new Sequence(Arrays.asList(
				new Interval(1,1,10),
				new Interval(1,1,20),
				new Interval(2,1,20)));
		assertFalse(seq.containsNSHapelet(shapelet, epsilon));
	}
	
	@Test
	public void simpleTestPositive(){
		NShapelet shapelet = new NShapelet(Arrays.asList(1,1,2), Arrays.asList(ShapeletSize2.FOLLOWEDBY,ShapeletSize2.MATCH));
		Sequence seq = new Sequence(Arrays.asList(
				new Interval(1,1,10),
				new Interval(1,5,20),
				new Interval(1,30,40),
				new Interval(2,30,40)));
		assertTrue(seq.containsNSHapelet(shapelet, epsilon));
	}

}
