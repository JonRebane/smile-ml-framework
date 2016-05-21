package representations.test;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;

import experiment.SequenceGenerator;
import representations.Interval;
import representations.Sequence;

public class SequenceTest {

	@Test
	public void testSortIntervals() {
		SequenceGenerator gen = new SequenceGenerator(10000, 3000, 10, new Random(13));
		Sequence seq = gen.generate(1).get(0);
		seq.sortIntervals();
		for(int i=0;i<seq.intervalCount()-1;i++){
			Interval i1 = seq.getInterval(i);
			Interval i2 = seq.getInterval(i+1);
			assertTrue(i1.getStart()<=i2.getStart());
			if(i1.getStart() == i2.getStart()){
				assertTrue(i1.getEnd() <= i2.getEnd());
				if(i1.getEnd() == i2.getEnd()){
					assertTrue(i1.getDimension() <= i2.getDimension());
				}
			}
		}
	}

}
