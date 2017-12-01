package innards.data;

import innards.Key;
import innards.TKey;

/**
 * An Interface which extends <code> iDataRecord </code> to provide methods for
 * changing the underlying data.
 *
 * @see innards.iDataRecord
 * @author synchar
 */
public interface iWritableDataRecord extends iDataRecord
{
	/**
	 * stores an Object, associated with a Key, in the DataRecord
	 * @param key a Key identifying the piece of data being added to the
	 * DataRecord
	 * @param data the Object to be added to the DataRecord
	 * @return Object the previous Object associated with this key, or
	 * <code> null </code> if there was no previous object.
	 */
	public Object putData(Key key, Object data);
	
	public <T> T putData(TKey<T> key, T data);
	
	/**
	 * removes an Object previously stored in the DataRecord.
	 * @param key the Key associated with the Object to be removed.
	 * @return Object the Object that was removed, or null if the Object was not
	 * found.
	 */
	public Object removeEntry(Key key);
	
	public <T> T removeEntry(TKey<T> key);
	
	public void dumpContentsInotMe(iDataRecord dr);
}
