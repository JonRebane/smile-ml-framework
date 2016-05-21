package stife.distance;

/***
 * An ordinary (Key,Value) pair that is ordered by their keys
 * @author Leon Bornemann
 *
 * @param <K>
 * @param <V>
 */
public class OrderedPair<K extends Comparable<K>, V> implements Comparable<OrderedPair<K,V>>{

	private K key;
	private V value;
	
	public OrderedPair(K key, V value) {
		super();
		this.key = key;
		this.value = value;
	}

	public K getKey() {
		return key;
	}

	public V getValue() {
		return value;
	}

	@Override
	public int compareTo(OrderedPair<K,V> o) {
		return key.compareTo(o.getKey());
	}

}
