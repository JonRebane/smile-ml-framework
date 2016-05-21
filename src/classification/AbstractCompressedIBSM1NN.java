package classification;

import java.util.ArrayList;
import java.util.List;

import distance.feature.extraction.exceptions.InvalidEventTableDimensionException;
import distance.feature.extraction.exceptions.TimeScaleException;
import representations.CompressedEventTable;
import representations.Sequence;

public abstract class AbstractCompressedIBSM1NN<E> extends AbstractSTI1NN<CompressedEventTable,E>{

	private List<CompressedEventTable> eventTables;

	public AbstractCompressedIBSM1NN(List<Sequence> train,List<E> classIds, int numDimensions, int maxDuration){
		super(train,classIds,numDimensions,maxDuration);
	}
	
	protected void setTrainingData(List<Sequence> sequences) {
		eventTables = new ArrayList<>();
		for(Sequence seq : sequences){
			Sequence resizedSeq = new Sequence(seq);
			resizedSeq.rescaleTimeAxis(1, maxDuration);
			eventTables.add(new CompressedEventTable(resizedSeq, numDimensions));
		}
	}

	@Override
	protected double getDistanceToTrainingSequence(CompressedEventTable seqRepresentation, int trainIndex) throws TimeScaleException, InvalidEventTableDimensionException {
		return seqRepresentation.euclidianDistance(eventTables.get(trainIndex));
	}

	@Override
	protected CompressedEventTable getSequenceRepresentation(Sequence sequence) {
		Sequence resizedSeq = new Sequence(sequence); //TODO: move the copying out of this, we don't want to measure it?
		resizedSeq.rescaleTimeAxis(1, maxDuration);
		CompressedEventTable table = new CompressedEventTable(resizedSeq, numDimensions);
		return table;
	}

	public static String getName() {
		return "Compressed IBSM 1-NN";
	}

}
