package innards;

/**
 * Typed property that you can modify.
 * 
 * @author cchao
 *
 * @param <V>
 */
public class Param<V> {

	protected V value;
	
	public Param(V defaultValue) {
		this.value = defaultValue;
	}
	
	public void set(V value) {
		this.value = value;
	}
	
	public V get() {
		return value;
	}
}
