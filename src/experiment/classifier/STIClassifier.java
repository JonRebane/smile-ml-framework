package experiment.classifier;

import java.io.IOException;

import data_structures.Sequence;
import stife.distance.exceptions.InvalidEventTableDimensionException;
import stife.distance.exceptions.TimeScaleException;

/***
 * A classifier for sequences of temporal Intervals
 * @author Leon Bornemann
 *
 */
public interface STIClassifier<E> {
	
	public E classify(Sequence sequences) throws TimeScaleException, InvalidEventTableDimensionException, ClassificationException, IOException, Exception;

}
