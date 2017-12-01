package innards;


/**
 * KeyPair couples percept keys with data keys to avoid mismatching problems.
 * 
 * @author crystal
 *
 */
public interface KeyPair {

	public Key dataKey();
	public Key perceptKey();
}
