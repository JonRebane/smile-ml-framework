package representations;

import distance.feature.extraction.exceptions.InvalidEventTableDimensionException;
import distance.feature.extraction.exceptions.TimeScaleException;

public interface SequenceRepresentation<T> {

	public double euclidianDistance(T other) throws TimeScaleException, InvalidEventTableDimensionException;
}
