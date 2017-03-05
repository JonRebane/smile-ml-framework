package stife.shapelet.evolution.evolution.alterers;

public interface MutationStrategy<T> {

	public T mutate(T t);
}
