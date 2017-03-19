package shapelet.evolution.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import data_structures.Interval;
import data_structures.Pair;
import data_structures.Sequence;
import stife.shapelet_size2_new.Contains;
import stife.shapelet_size2_new.FollowedBy;
import stife.shapelet_size2_new.LeftContains;
import stife.shapelet_size2_new.Matches;
import stife.shapelet_size2_new.Meets;
import stife.shapelet_size2_new.Overlaps;
import stife.shapelet_size2_new.RightContains;

public class ShapeletSize2Test {

	private static int epsilon = 5;
	
	@Test
	public void noSelfMatch(){
		Matches shapelet = new Matches(1, 1);
		Sequence seq = new Sequence(Arrays.asList(
				new Interval(1,1,10),
				new Interval(1,5,10)));
		List<Pair<Integer, Integer>> allOccurrences = shapelet.getAllOccurrences(seq, epsilon);
		assertEquals(1,allOccurrences.size());
		assertTrue(containsPair(allOccurrences,new Pair<>(0,1)));
	}
	
	@Test
	public void meets(){
		Meets shapelet = new Meets(1, 3);
		Sequence seq = new Sequence(Arrays.asList(
				new Interval(1,1,10),
				new Interval(1,5,10),
				new Interval(3,12,20),
				new Interval(3,15,20),
				new Interval(3,16,20),
				new Interval(3,25,29),
				new Interval(1,27,30),
				new Interval(1,30,40),
				new Interval(2,40,50),
				new Interval(3,40,50)));
		List<Pair<Integer, Integer>> allOccurrences = shapelet.getAllOccurrences(seq, epsilon);
		assertEquals(6,allOccurrences.size());
		assertTrue(containsPair(allOccurrences,new Pair<>(0,2)));
		assertTrue(containsPair(allOccurrences,new Pair<>(0,3)));
		assertTrue(containsPair(allOccurrences,new Pair<>(1,2)));
		assertTrue(containsPair(allOccurrences,new Pair<>(1,3)));
		assertTrue(containsPair(allOccurrences,new Pair<>(6,5)));
		assertTrue(containsPair(allOccurrences,new Pair<>(7,9)));
	}
	
	@Test
	public void contains(){
		Contains shapelet = new Contains(1, 3);
		Sequence seq = new Sequence(Arrays.asList(
				new Interval(3,1,5),
				new Interval(1,1,30),
				new Interval(3,6,10),
				new Interval(3,7,24),
				new Interval(3,7,25),
				new Interval(1,60,80),
				new Interval(3,70,70),
				new Interval(2,70,71)));
		List<Pair<Integer, Integer>> allOccurrences = shapelet.getAllOccurrences(seq, epsilon);
		assertEquals(2,allOccurrences.size());
		assertTrue(containsPair(allOccurrences,new Pair<>(1,3)));
		assertTrue(containsPair(allOccurrences,new Pair<>(5,6)));
	}
	
	@Test
	public void overlap(){
		Overlaps shapelet = new Overlaps(1, 3);
		Sequence seq = new Sequence(Arrays.asList(
				new Interval(1,1,30),
				new Interval(3,1,40),
				new Interval(3,6,35),
				new Interval(3,7,36),
				new Interval(2,7,40),
				new Interval(3,100,140),
				new Interval(1,110,130),
				new Interval(3,120,140)));
		List<Pair<Integer, Integer>> allOccurrences = shapelet.getAllOccurrences(seq, epsilon);
		assertEquals(2,allOccurrences.size());
		assertTrue(containsPair(allOccurrences,new Pair<>(0,3)));
		assertTrue(containsPair(allOccurrences,new Pair<>(6,7)));
	}
	
	@Test
	public void leftContains(){
		LeftContains shapelet = new LeftContains(1, 3);
		Sequence seq = new Sequence(Arrays.asList(
				new Interval(3,1,5),
				new Interval(1,1,30),
				new Interval(3,5,10),
				new Interval(2,6,10),
				new Interval(3,6,30),
				new Interval(3,7,24),
				new Interval(3,7,25),
				new Interval(3,55,70),
				new Interval(1,60,80),
				new Interval(2,60,81)));
		List<Pair<Integer, Integer>> allOccurrences = shapelet.getAllOccurrences(seq, epsilon);
		assertEquals(3,allOccurrences.size());
		assertTrue(containsPair(allOccurrences,new Pair<>(1,0)));
		assertTrue(containsPair(allOccurrences,new Pair<>(1,2)));
		assertTrue(containsPair(allOccurrences,new Pair<>(8,7)));
	}
	

	@Test
	public void rightContains(){
		RightContains shapelet = new RightContains(1, 3);
		Sequence seq = new Sequence(Arrays.asList(
				new Interval(1,1,30),
				new Interval(3,1,30),
				new Interval(2,6,30),
				new Interval(3,7,25),
				new Interval(3,7,35),
				new Interval(3,7,36),
				new Interval(3,55,80),
				new Interval(1,60,80),
				new Interval(3,75,85)));
		List<Pair<Integer, Integer>> allOccurrences = shapelet.getAllOccurrences(seq, epsilon);
		assertEquals(3,allOccurrences.size());
		assertTrue(containsPair(allOccurrences,new Pair<>(0,3)));
		assertTrue(containsPair(allOccurrences,new Pair<>(0,4)));
		assertTrue(containsPair(allOccurrences,new Pair<>(7,8)));
	}
	
	@Test
	public void matches(){
		Matches shapelet = new Matches(1, 3);
		Sequence seq = new Sequence(Arrays.asList(
				new Interval(1,1,10),
				new Interval(3,5,5),
				new Interval(1,5,10),
				new Interval(2,6,10),
				new Interval(3,7,10),
				new Interval(3,34,60),
				new Interval(3,35,65),
				new Interval(1,40,60),
				new Interval(3,45,55)));
		List<Pair<Integer, Integer>> allOccurrences = shapelet.getAllOccurrences(seq, epsilon);
		assertEquals(5,allOccurrences.size());
		assertTrue(containsPair(allOccurrences,new Pair<>(0,1)));
		assertTrue(containsPair(allOccurrences,new Pair<>(2,1)));
		assertTrue(containsPair(allOccurrences,new Pair<>(2,4)));
		assertTrue(containsPair(allOccurrences,new Pair<>(7,6)));
		assertTrue(containsPair(allOccurrences,new Pair<>(7,8)));
	}
	
	
	
	@Test
	public void followedBy() {
		FollowedBy shapelet = new FollowedBy(1, 3);
		Sequence seq = new Sequence(Arrays.asList(
				new Interval(1,1,10),
				new Interval(1,5,20),
				new Interval(3,12,20),
				new Interval(3,15,20),
				new Interval(3,16,20),
				new Interval(3,26,30),
				new Interval(1,30,40),
				new Interval(2,30,40),
				new Interval(3,60,70)));
		List<Pair<Integer, Integer>> allOccurrences = shapelet.getAllOccurrences(seq, epsilon);
		assertEquals(6,allOccurrences.size());
		assertTrue(containsPair(allOccurrences,new Pair<>(0,4)));
		assertTrue(containsPair(allOccurrences,new Pair<>(0,5)));
		assertTrue(containsPair(allOccurrences,new Pair<>(0,8)));
		assertTrue(containsPair(allOccurrences,new Pair<>(1,5)));
		assertTrue(containsPair(allOccurrences,new Pair<>(1,8)));
		assertTrue(containsPair(allOccurrences,new Pair<>(6,8)));
	}

	private boolean containsPair(List<Pair<Integer, Integer>> allOccurrences, Pair<Integer,Integer> pair) {
		for(Pair<Integer, Integer> p : allOccurrences){
			if(p.getFirst()==pair.getFirst() && p.getSecond() == pair.getSecond()){
				return true;
			}
		}
		return false;
	}

}
