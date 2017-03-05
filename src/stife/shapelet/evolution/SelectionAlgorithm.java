package stife.shapelet.evolution;

import java.util.List;
import java.util.Map;

public interface SelectionAlgorithm<T> {
	
	List<T> select(Map<T, Double> evaluatedPopulation);
}
