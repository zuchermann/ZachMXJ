package innards.data;

import innards.Key;
import innards.iNamedObject;

import java.io.Serializable;
import java.util.Set;

/**
 * Interface for a read-only DataRecord.  Note that each iDataRecord is required
 * to be implemented as a read-only view onto a 'parent'
 * <code>iWritableDataRecord</code>.
 * <p>
 * In addition to being NamedObjects, iDataRecords have an ID
 * <code>Key</code>.
 * <p>
 * The <i>name</i> is immutable, and should be used to give the
 * <code>iDataRecord</code> a permanent designation.  For example, a device
 * might wish to give the <code>iDataRecords</code> it produces a name that
 * indicates their origin.
 * <p>
 * The <i>ID</i> is changeable, and should be used to give transient labels to
 * the <code>iDataRecords</code>.  Different objects in the perceptual pipeline
 * might want to change the ID to reflect the <code>iDataRecords</code> stage of
 * processing.  For example, a sensor might want to change the ID of the
 * <code>iDataRecords</code> it handles to a key such as
 * POST_SENSOR_DATA_RECORD.
 * 
 * @see innards.iWritableDataRecord
 */
public interface iDataRecord extends iNamedObject, Serializable
{	

	/**
		Get the data associated with a given key.
	*/
	public Object getData(Key key);
	
	/**
	 * convenience method for getting data you know is a float, so you don't 
	 * need to cast and unwrap
	 */
	public float getFloat(Key key);
	
	/**
	 * convenience method for getting data you know is an int, so you don't 
	 * need to cast and unwrap
	 */
	public int getInt(Key key);

	/**
	    Determine whether the <code>iDataRecord</code> has any data for a given key.
	 */
	public boolean hasKey(Key key);

	/**
	    Return the set of keys for which the <code>iDataRecord</code> has data.
	 */
	public Set<Key> getKeySet();

	/**
	    Get a copy of the <code>iWritableDataRecord</code> for which this <code>iDataRecord</code> is a read-only view.
	 */
	public iWritableDataRecord getWritableCopy();
	
	/**
	 * Get a copy of the <code>iWritableDataRecord</code>, WITH KEY SET TO k,
	 * for which this <code>iDataRecord</code> is a read-only view.
	 * <p>
	 * DO NOT FORGET TO SET THE KEY OF THE RETURN DATA RECORD TO THE ARGUMENT
	 * KEY!!
	 */
	public iWritableDataRecord getWritableCopy(Key k);
	
	/**
	 * Get a read-only copy of this data record (if the data record is already
	 * read-only, it may return a pointer to itself)
	 * @return iDataRecord
	 */
	public iDataRecord getReadOnlyView();
}
