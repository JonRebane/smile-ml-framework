package stife.shapelet;

public interface MutationStrategy<T> {

	public T mutate(T t);
}
