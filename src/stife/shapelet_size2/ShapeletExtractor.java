package stife.shapelet_size2;
import java.util.List;
import java.util.Map;

import data_structures.Sequence;
import data_structures.ShapeletKey;
import stife.shapelet_size2_new.ShapeletSize2;

/***
 * Object for eventId1 shapelet extraction task, so that many of these can be run in parallel
 * @author leon bornemann
 *
 */
public class ShapeletExtractor implements Runnable {

	private final Map<ShapeletKey, Shapelet_Size2> database;
	private List<Sequence> sequences;
	private ShapeletFeatureMatrix resMatrix;
		private int start;
	private int stop;
	private int epsilon;

	//startIndex inclusive stopIndex exclusive
	/***
	 * Initializes eventId1 new task
	 * @param sequences the list of all sequences
	 * @param startIndex the seqeunce index at which this task starts
	 * @param stopIndex the sequence index that this task does not consider anymore
	 * @param shapeletFeatureMatrix The matrix into which to write the results
	 * @param epsilon maximum time span that points of time may differ from each other to still be considered equal
	 */
	public ShapeletExtractor(Map<ShapeletKey, Shapelet_Size2> database, List<Sequence> sequences, int startIndex, int stopIndex, ShapeletFeatureMatrix shapeletFeatureMatrix, int epsilon){
		assert(startIndex<stopIndex);
		this.database = database;
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
			curSeq.countAllShapelets(database, i,resMatrix, epsilon);
		}
	}

}
