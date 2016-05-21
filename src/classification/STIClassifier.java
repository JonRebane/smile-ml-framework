package classification;

import java.io.IOException;

import distance.feature.extraction.exceptions.InvalidEventTableDimensionException;
import distance.feature.extraction.exceptions.TimeScaleException;
import representations.Sequence;

/***
 * A classifier for sequences of temporal Intervals
 * @author Leon Bornemann
 *
 */
public interface STIClassifier<E> {
	
	public E classify(Sequence sequences) throws TimeScaleException, InvalidEventTableDimensionException, ClassificationException, IOException, Exception;

}
