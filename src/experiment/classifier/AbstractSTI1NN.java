package experiment.classifier;

import java.util.List;

import data_structures.Sequence;
import data_structures.SequenceRepresentation;
import stife.distance.exceptions.InvalidEventTableDimensionException;
import stife.distance.exceptions.TimeScaleException;

public abstract class AbstractSTI1NN<T extends SequenceRepresentation<T>,E> implements STIClassifier<E>{

	protected int maxDuration;
	protected int numDimensions;
	protected List<E> classIds;
	
	public AbstractSTI1NN(List<Sequence> train,List<E> classId, int numDimensions, int maxDuration){
		assert(train.size()==classId.size());
		assert(maxDuration>0);
		assert(numDimensions>0);
		this.maxDuration = maxDuration;
		this.numDimensions = numDimensions;
		this.classIds = classId;
		setTrainingData(train);
	}

	protected abstract void setTrainingData(List<Sequence> train);
	
	@Override
	public E classify(Sequence sequence) throws TimeScaleException, InvalidEventTableDimensionException {
		int minDistIndex = -1;
		double minDist = Double.MAX_VALUE;
		T seqRepresentation = getSequenceRepresentation(sequence);
		for(int i =0;i<classIds.size();i++){
			double curDist = getDistanceToTrainingSequence(seqRepresentation,i);
			if(minDistIndex==-1 || curDist < minDist){
				minDistIndex = i;
				minDist = curDist;
			}
		}
		return classIds.get(minDistIndex);
	}

	protected abstract double getDistanceToTrainingSequence(T seqRepresentation, int trainIndex) throws TimeScaleException, InvalidEventTableDimensionException;

	protected abstract T getSequenceRepresentation(Sequence sequence);
}
