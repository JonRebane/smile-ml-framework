package shapelet.evolution.test;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import data_structures.Interval;
import data_structures.Sequence;
import stife.shapelet.evolution.NShapelet;
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
	
	@Test
	public void occurrenceTestPositive(){
		NShapelet shapelet = new NShapelet(Arrays.asList(1,1,2), Arrays.asList(ShapeletSize2.FOLLOWEDBY,ShapeletSize2.MATCH));
		Sequence seq = new Sequence(Arrays.asList(
				new Interval(1,1,10),
				new Interval(1,5,20),
				new Interval(1,30,40),
				new Interval(1,30,42),
				new Interval(2,30,45),
				new Interval(2,31,40)));
		List<List<Integer>> occurrences = seq.getAllOccurrences(shapelet, epsilon);
		assertEquals(8,occurrences.size());
		assertEquals(Arrays.asList(0,2,4),occurrences.get(0));
		assertEquals(Arrays.asList(0,2,5),occurrences.get(1));
		assertEquals(Arrays.asList(0,3,4),occurrences.get(2));
		assertEquals(Arrays.asList(0,3,5),occurrences.get(3));
		assertEquals(Arrays.asList(1,2,4),occurrences.get(4));
		assertEquals(Arrays.asList(1,2,5),occurrences.get(5));
		assertEquals(Arrays.asList(1,3,4),occurrences.get(6));
		assertEquals(Arrays.asList(1,3,5),occurrences.get(7));
	}
	
	@Test
	public void occurrenceTestNegative(){
		NShapelet shapelet = new NShapelet(Arrays.asList(1,1,2), Arrays.asList(ShapeletSize2.FOLLOWEDBY,ShapeletSize2.MATCH));
		Sequence seq = new Sequence(Arrays.asList(
				new Interval(1,1,10),
				new Interval(1,5,20),
				new Interval(1,30,40),
				new Interval(1,30,42),
				new Interval(2,50,60)));
		List<List<Integer>> occurrences = seq.getAllOccurrences(shapelet, epsilon);
		assertEquals(0,occurrences.size());
	}
	
	@Test
	public void doubleCountTest(){
		NShapelet shapelet = new NShapelet(Arrays.asList(1,1,1), Arrays.asList(ShapeletSize2.MATCH,ShapeletSize2.MATCH));
		Sequence seq = new Sequence(Arrays.asList(
				new Interval(1,1,10),
				new Interval(1,5,10)));
		List<List<Integer>> occurrences = seq.getAllOccurrences(shapelet, epsilon);
		assertEquals(0,occurrences.size());
		//FIXME: this does not work yet!!
	}
	
	@Test
	public void doubleCountTest2(){
		NShapelet shapelet = new NShapelet(Arrays.asList(1,1), Arrays.asList(ShapeletSize2.MATCH));
		Sequence seq = new Sequence(Arrays.asList(
				new Interval(1,1,10)));
		List<List<Integer>> occurrences = seq.getAllOccurrences(shapelet, epsilon);
		assertEquals(0,occurrences.size());
		//TODO: make such a test for all two shapelets! (maybe for jason?)
	}

}
