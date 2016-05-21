package experiment.classifier;

import java.util.ArrayList;
import java.util.List;

import data_structures.EventTable;
import data_structures.Sequence;
import stife.distance.exceptions.InvalidEventTableDimensionException;
import stife.distance.exceptions.TimeScaleException;

public abstract class AbstractIBSM1NN<E> extends AbstractSTI1NN<EventTable,E>{

	private List<EventTable> eventTables;
	
	public AbstractIBSM1NN(List<Sequence> train, List<E> classId, int numDimensions, int maxDuration) {
		super(train, classId, numDimensions, maxDuration);
	}

	@Override
	protected void setTrainingData(List<Sequence> train) {
		eventTables = new ArrayList<>();
		for(Sequence seq : train){
			Sequence resizedSeq = new Sequence(seq);
			resizedSeq.rescaleTimeAxis(1, maxDuration);
			eventTables.add(new EventTable(resizedSeq, numDimensions));
		}
	}

	@Override
	protected double getDistanceToTrainingSequence(EventTable seqRepresentation, int trainIndex) throws TimeScaleException, InvalidEventTableDimensionException {
		return seqRepresentation.euclidianDistance(eventTables.get(trainIndex));
	}

	@Override
	protected EventTable getSequenceRepresentation(Sequence sequence) {
		Sequence resizedSeq = new Sequence(sequence); //TODO: move the copying out of this, we don't want to measure it
		resizedSeq.rescaleTimeAxis(1, maxDuration);
		EventTable table = new EventTable(resizedSeq, numDimensions);
		return table;
	}

	public static String getName() {
		return "IBSM 1-NN";
	}
}
