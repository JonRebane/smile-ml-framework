package experiment.test;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Random;

import org.junit.Test;

import data_structures.Sequence;
import experiment.SequenceGenerator;

public class SequenceGeneratorTest {

	@Test
	public void test() {
		int n = 1000;
		int m = 500;
		int d = 10;
		int duration = 1000;
		Random random = new Random(13);
		SequenceGenerator gen = new SequenceGenerator(500,duration,d,random);
		List<Sequence> seqs = gen.generate(n);
		assertEquals(n,seqs.size());
		for(Sequence seq : seqs){
			assertEquals(duration,seq.duration());
			assertEquals(m,seq.intervalCount());
		}
	}

}
