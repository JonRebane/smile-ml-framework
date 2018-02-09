package experiment;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import data_structures.Interval;
import data_structures.Sequence;

/***
 * A Generator that randomly generates artificial sequences, according to parameters.
 * Used to generate large amounts of sequences, for runtime tests.
 * 
 * @author Leon Bornemann
 *
 */
public class SequenceGenerator {

	private int sequenceSize;
	private int dimensionCount;
	private Random random;
	private int sequenceDuration;
	
	
	public SequenceGenerator(int sequenceSize, int sequenceDuration,int dimensionCount, Random random) {
		this.random = random;
		assert(sequenceSize >0);
		assert(sequenceDuration >0);
		assert(dimensionCount > 0);
		this.sequenceSize = sequenceSize;
		this.sequenceDuration = sequenceDuration;
		this.dimensionCount = dimensionCount;
	}
	
	public List<Sequence> generate(int numSequences){
		List<Sequence> seqs = new ArrayList<>(numSequences);
		for(int i=0;i<numSequences;i++){
			seqs.add(generateSequence());
		}
		return seqs;
	}

	private Sequence generateSequence() {
		List<Interval> intervals = new ArrayList<>(sequenceSize);
		for(int i=0;i<sequenceSize-1;i++){
			intervals.add(generateInterval());
		}
		//to ensure sequence duration we add one interval with max duration:
		intervals.add(new Interval(randomDimension(), 1, sequenceDuration));
		return new Sequence(intervals);
	}

	/***
	 * Creates eventId1 new random int in range [lower,upper] (upper is inclusive)
	 * @param lower
	 * @param upper
	 * @return
	 */
	private int randomInt(int lower,int upper) {
		return random.nextInt(upper+1-lower)+lower;
	}

	private int randomDimension() {
		return randomInt(1,dimensionCount);
	}

	private Interval generateInterval() {
		int begin = randomInt(1, sequenceDuration);
		int end = randomInt(begin, sequenceDuration);
		return new Interval(randomDimension(), begin, end);
	}	
}
