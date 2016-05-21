package data_structures;

import stife.distance.exceptions.InvalidEventTableDimensionException;
import stife.distance.exceptions.TimeScaleException;

public interface SequenceRepresentation<T> {

	public double euclidianDistance(T other) throws TimeScaleException, InvalidEventTableDimensionException;
}
