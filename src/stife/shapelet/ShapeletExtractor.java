package stife.shapelet;
import java.util.List;

import data_structures.Sequence;

/***
 * Object for a shapelet extraction task, so that many of these can be run in parallel
 * @author leon bornemann
 *
 */
public class ShapeletExtractor implements Runnable {

	private List<Sequence> sequences;
	private ShapeletFeatureMatrix resMatrix;
	private int start;
	private int stop;
	private int epsilon;

	//startIndex inclusive stopIndex exclusive
	/***
	 * Initializes a new task
	 * @param sequences the list of all sequences
	 * @param startIndex the seqeunce index at which this task starts
	 * @param stopIndex the sequence index that this task does not consider anymore
	 * @param shapeletFeatureMatrix The matrix into which to write the results
	 * @param epsilon maximum time span that points of time may differ from each other to still be considered equal
	 */
	public ShapeletExtractor(List<Sequence> sequences,int startIndex, int stopIndex, ShapeletFeatureMatrix shapeletFeatureMatrix, int epsilon){
		assert(startIndex<stopIndex);
		this.sequences = sequences;
		this.start = startIndex;
		this.stop = stopIndex;
		this.resMatrix = shapeletFeatureMatrix;
		this.epsilon = epsilon;
	}
	
	@Override
	public void run() {
		for(int i=start;i<stop;i++){
			Sequence curSeq = sequences.get(i);
			curSeq.countAllShapelets(i,resMatrix, epsilon);
		}
	}

}
