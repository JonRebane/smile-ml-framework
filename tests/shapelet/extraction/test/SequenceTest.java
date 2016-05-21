package shapelet.extraction.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import data_structures.Interval;
import data_structures.Sequence;

public class SequenceTest {

	static String path = "testdata/testSequences.txt";
	
	@Test
	public void correctRead() throws IOException {
		List<Sequence> sequences = Sequence.readSequenceData(path);
		assertEquals(3,sequences.size());
		Sequence first = sequences.get(0);
		Sequence second = sequences.get(1);
		assertEquals(10,first.intervalCount());
		assertEquals(12, second.intervalCount());
		assertEquals(36,first.duration());
		assertEquals(40, second.duration());
		//first sequence:
		Interval lastInterval = first.getInterval(9);
		assertEquals(3, lastInterval.getDimension());
		assertEquals(27, lastInterval.getStart());
		assertEquals(30, lastInterval.getEnd());
		//second sequence
		lastInterval = second.getInterval(11);
		assertEquals(3, lastInterval.getDimension());
		assertEquals(25, lastInterval.getStart());
		assertEquals(30, lastInterval.getEnd());
	}

}
