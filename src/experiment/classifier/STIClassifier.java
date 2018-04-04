package experiment.classifier;

import java.io.IOException;
import java.util.List;

import data_structures.Sequence;
import stife.distance.exceptions.InvalidEventTableDimensionException;
import stife.distance.exceptions.TimeScaleException;
import weka.core.FastVector;

/***
 * A classifier for sequences of temporal Intervals
 * @author Leon Bornemann
 *
 */
public interface STIClassifier<E> {

	default List<double[]> predictProba(List<Sequence> sequences) throws TimeScaleException, InvalidEventTableDimensionException, ClassificationException {
		throw new UnsupportedOperationException();
	}
	public E classify(Sequence sequences) throws TimeScaleException, InvalidEventTableDimensionException, ClassificationException, IOException, Exception;

	default int translateClass(int i) {
		throw new UnsupportedOperationException();
	}

}
