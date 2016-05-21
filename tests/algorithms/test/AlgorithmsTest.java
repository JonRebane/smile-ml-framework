package algorithms.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import algorithms.Algorithms;

public class AlgorithmsTest {

	@Test
	public void linearInterpolationTest() {
		//simple double-scale-1:
		assertEquals(19, Algorithms.linearInterpolation(1, 10, 10, 1, 19));
		assertEquals(17, Algorithms.linearInterpolation(1, 10, 9, 1, 19));
		assertEquals(15, Algorithms.linearInterpolation(1, 10, 8, 1, 19));
		assertEquals(13, Algorithms.linearInterpolation(1, 10, 7, 1, 19));
		assertEquals(11, Algorithms.linearInterpolation(1, 10, 6, 1, 19));
		assertEquals(9, Algorithms.linearInterpolation(1, 10, 5, 1, 19));
		assertEquals(7, Algorithms.linearInterpolation(1, 10, 4, 1, 19));
		assertEquals(5, Algorithms.linearInterpolation(1, 10, 3, 1, 19));
		assertEquals(3, Algorithms.linearInterpolation(1, 10, 2, 1, 19));
		assertEquals(1, Algorithms.linearInterpolation(1, 5, 1, 1, 19));
		//not so simple:
		assertEquals(50, Algorithms.linearInterpolation(1, 9, 5, 1, 99));
		assertEquals(432, Algorithms.linearInterpolation(1, 234, 177, 1, 572));
	}

}
