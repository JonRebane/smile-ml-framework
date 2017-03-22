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
	
	Sequence hepatitisSeq1 = new Sequence(Arrays.asList(
			new Interval(29,1,108),
			new Interval(20,1,122),
			new Interval(37,1,150),
			new Interval(1,1,150),
			new Interval(23,1,150),
			new Interval(4,1,150),
			new Interval(12,1,150),
			new Interval(6,1,150),
			new Interval(11,1,150),
			new Interval(58,1,150),
			new Interval(59,1,150),
			new Interval(16,1,150),
			new Interval(7,1,150),
			new Interval(19,1,150),
			new Interval(3,1,150),
			new Interval(13,1,150),
			new Interval(10,1,150),
			new Interval(17,1,150),
			new Interval(8,1,150),
			new Interval(9,1,150),
			new Interval(22,1,150),
			new Interval(21,1,150),
			new Interval(33,1,150),
			new Interval(34,1,150),
			new Interval(35,1,150),
			new Interval(2,122,150)));
	
	Sequence hepatitisSeq342 = new Sequence(Arrays.asList(
			new Interval(11,1,50),
			new Interval(27,1,50),
			new Interval(2,1,50),
			new Interval(12,1,134),
			new Interval(2,1,169),
			new Interval(33,1,260),
			new Interval(15,1,288),
			new Interval(4,1,316),
			new Interval(3,1,316),
			new Interval(23,1,421),
			new Interval(17,1,421),
			new Interval(35,1,421),
			new Interval(37,1,540),
			new Interval(14,1,540),
			new Interval(6,1,540),
			new Interval(18,1,540),
			new Interval(16,1,540),
			new Interval(19,1,540),
			new Interval(3,1,540),
			new Interval(13,1,540),
			new Interval(10,1,540),
			new Interval(9,1,540),
			new Interval(45,1,540),
			new Interval(21,22,260),
			new Interval(34,22,421),
			new Interval(8,106,260),
			new Interval(6,106,316),
			new Interval(27,134,456),
			new Interval(12,197,260),
			new Interval(2,232,456),
			new Interval(11,288,316),
			new Interval(47,288,393),
			new Interval(51,316,337),
			new Interval(8,316,421),
			new Interval(33,316,421),
			new Interval(9,337,358),
			new Interval(1,337,358),
			new Interval(12,337,540),
			new Interval(4,358,540),
			new Interval(15,358,540),
			new Interval(11,393,456),
			new Interval(3,393,540),
			new Interval(21,421,540),
			new Interval(6,484,540),
			new Interval(36,484,540),
			new Interval(56,484,540)));

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
	public void doubleCountMatchTest(){
		NShapelet shapelet = new NShapelet(Arrays.asList(1,1), Arrays.asList(ShapeletSize2.MATCH));
		Sequence seq = new Sequence(Arrays.asList(
				new Interval(1,1,10),
				new Interval(1,2,12)));
		List<List<Integer>> occurrences = seq.getAllOccurrences(shapelet, epsilon);
		assertEquals(1,occurrences.size());
	}
	
	@Test
	public void doubleCountOverlapTest(){
		NShapelet shapelet = new NShapelet(Arrays.asList(1,1), Arrays.asList(ShapeletSize2.OVERLAP));
		Sequence seq = new Sequence(Arrays.asList(
				new Interval(1,1,30),
				new Interval(1,15,48)));
		List<List<Integer>> occurrences = seq.getAllOccurrences(shapelet, epsilon);
		assertEquals(1,occurrences.size());
	}
	
	@Test
	public void doubleCountMeetTest(){
		NShapelet shapelet = new NShapelet(Arrays.asList(1,1), Arrays.asList(ShapeletSize2.MEET));
		Sequence seq = new Sequence(Arrays.asList(
				new Interval(1,1,30),
				new Interval(1,32,48)));
		List<List<Integer>> occurrences = seq.getAllOccurrences(shapelet, epsilon);
		assertEquals(1,occurrences.size());
	}
	
	
	@Test
	public void simpleHepatitis1Test(){
		NShapelet shapelet = new NShapelet(Arrays.asList(20,29,2), Arrays.asList(ShapeletSize2.LEFTCONTAINS, ShapeletSize2.FOLLOWEDBY));
		List<List<Integer>> occurrences = hepatitisSeq1.getAllOccurrences(shapelet, epsilon);
		assertEquals(1,occurrences.size());
	}
	
	@Test
	public void simpleHepatitis1Test2(){
		NShapelet shapelet = new NShapelet(Arrays.asList(37,6), Arrays.asList(ShapeletSize2.MATCH));
		List<List<Integer>> occurrences = hepatitisSeq1.getAllOccurrences(shapelet, epsilon);
		assertEquals(1,occurrences.size());
	}

	@Test
	public void simpleHepatitis1Test3(){
		NShapelet shapelet = new NShapelet(Arrays.asList(11,2), Arrays.asList(ShapeletSize2.RGHTCONTAINS));
		List<List<Integer>> occurrences = hepatitisSeq1.getAllOccurrences(shapelet, epsilon);
		assertEquals(1,occurrences.size());
	}
	
	@Test
	public void simpleHepatitis342Test1(){
		NShapelet shapelet = new NShapelet(Arrays.asList(6,2,11,36), Arrays.asList(ShapeletSize2.OVERLAP, ShapeletSize2.RGHTCONTAINS, ShapeletSize2.FOLLOWEDBY));
		List<List<Integer>> occurrences = hepatitisSeq342.getAllOccurrences(shapelet, epsilon);
		assertEquals(1,occurrences.size());
	}

	@Test
	public void simpleHepatitis342Test2(){
		NShapelet shapelet = new NShapelet(Arrays.asList(27,6,8), Arrays.asList(ShapeletSize2.FOLLOWEDBY, ShapeletSize2.LEFTCONTAINS));
		List<List<Integer>> occurrences = hepatitisSeq342.getAllOccurrences(shapelet, epsilon);
		assertEquals(1,occurrences.size());
	}
	
	@Test
	public void simpleHepatitis342Test3(){
		NShapelet shapelet = new NShapelet(Arrays.asList(27,2,11), Arrays.asList(ShapeletSize2.MATCH, ShapeletSize2.MATCH));
		List<List<Integer>> occurrences = hepatitisSeq342.getAllOccurrences(shapelet, epsilon);
		assertEquals(1,occurrences.size());
	}
	
	@Test
	public void simpleHepatitis342Test4(){
		// FIXME problem with meet, does not identify the 51 MEETS 9 shapelet 
		NShapelet shapelet = new NShapelet(Arrays.asList(51,9,1), Arrays.asList(ShapeletSize2.MEET, ShapeletSize2.MATCH));
		List<List<Integer>> occurrences = hepatitisSeq342.getAllOccurrences(shapelet, epsilon);
		assertEquals(1,occurrences.size());
	}
	
}
