package innards.data;

import java.util.*;

import innards.*;

/**
 * The base class for a writable data record.
 * @see innards.iWritableDataRecord
 * @author synchar
 */
public class WritableDataRecord extends NamedObject implements iWritableDataRecord
{
	protected HashMap<Key, Object> hashMap = null;
	protected Object firstData = null;
	protected Key firstKey = null;
	protected boolean hasFirstData = false;

	/**
	 * 
	 * @param name A String that is the permanent name of the DataRecord.
	 */
	public WritableDataRecord(String name)
	{
		super(name);
	}
	
	/**
	 * Allows you to create a WritableDataRecord using an existing map.
	 * @param name
	 * @param m - map, copied on entry
	 */
	public WritableDataRecord(String name, Map<Key, Object> m)
	{
		super(name);
		if(m!=null){
			setMap(m);
			hasFirstData = true;
		}
	}
	
	/**
	* 
	* @param name A Key that is the permanent name of the DataRecord.
	*/
	public WritableDataRecord(Key key)
	{
		super(key);
	}
	
	/**
	* Allows you to create a WritableDataRecord using an existing map.
	 * @param name
	 * @param m - map, copied on entry
	 */
	public WritableDataRecord(Key key, Map<Key, Object> m)
	{
		super(key);
		if(m == null){
			hasFirstData = true;
			setMap(m);
		}
		
	}


	public void setMap(Map<Key, Object> m)
	{
		if(m!=null){
			hasFirstData = true;
			hashMap = new HashMap<Key, Object>(m);
		}
	}

	/**
	 * @see innards.iWritableDataRecord#putData(innards.Key, java.lang.Object)
	 */
	public Object putData(Key key, Object data)
	{
		if (!hasFirstData)
		{
			firstData = data;
			firstKey = key;
			hasFirstData = true;
			return data;
		}
		
		else if(hashMap == null)
		{
			hashMap = new HashMap<Key, Object>();
			if (firstData != null)
			{
				hashMap.put(firstKey, firstData);
			}
		}
		
		return hashMap.put(key, data);
		
	}
	
	@SuppressWarnings("unchecked")
	public <T> T putData(TKey<T> key, T data) {
		return (T)putData((Key)key, data);
	}
	
	/**
	* @see innards.iDataRecord#getData(innards.Key)
	*/
	public Object getData(Key key)
	{
		if (hashMap == null)
		{
			if (firstKey == key)
			{
				return firstData;
			}
			
			else
			{
				return null;
			}
		}
		
		else
		{
			return hashMap.get(key);
		}	
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getData(TKey<T> key) {
		return (T)getData((Key)key);
	}
	
	/**
	 * 
	 * @see innards.data.iDataRecord#getFloat(Key)
	 */
	public float getFloat(Key key)
	{
		return ((Float) getData(key)).floatValue();
	}
	
	public int getInt(Key key)
	{
		return ((Integer) getData(key)).intValue();
	}
	
	public Key getKey(Key key)
	{
		return ((Key) getData(key));
	}

	/**
	 * @see innards.iWritableDataRecord#removeEntry(innards.Key)
	 */
	public Object removeEntry(Key key)
	{
		if (hashMap == null)
		{
			if (firstKey == key)
			{
				Object obj = firstData;
				firstData = null;
				firstKey = null;
				hasFirstData = false;
				return obj;
				
			}
			
			else 
			{
				return null;
			}
		}
		
		else
		{
			return hashMap.remove(key);
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T removeEntry(TKey<T> key) {
		return (T)removeEntry((Key)key);
	}
	
	/**
	 * @see innards.iDataRecord#hasKey(innards.Key)
	 */
	public boolean hasKey(Key key) {
		if (hashMap == null)
		{
			return key == firstKey;
		}
		else
		{
			return hashMap.containsKey(key);
		}
		
	}

	/**
	 * @see innards.iDataRecord#getKeySet()
	 */
	public Set<Key> getKeySet() {
		if (hashMap == null)
		{
			Set<Key> set = new HashSet<Key>();
			if(hasFirstData){
				set.add(firstKey);
			}
			return set;
		}
		return hashMap.keySet();
	}

	public void dumpContentsInotMe(iDataRecord dr){
		Set<Key> otherKeySet = dr.getKeySet();
		Iterator<Key> i = otherKeySet.iterator();
		while(i.hasNext()){
			Key keyToAdd = (Key)i.next();
			putData(keyToAdd,dr.getData(keyToAdd));
		}
	}

	/**
	 * @see innards.iWritableDataRecord#getReadOnlyView()
	 */
	public iDataRecord getReadOnlyView() 
	{
		class ReadOnlyView extends NamedObject implements iDataRecord {
			public ReadOnlyView(Key key) {
				super(key);
			}

			public Object getData(Key key) {
				return WritableDataRecord.this.getData(key);
			}
			
			public float getFloat(Key key)
			{
				return WritableDataRecord.this.getFloat(key);
			}
			
			public int getInt(Key key)
			{
				return WritableDataRecord.this.getInt(key);
			}
			
			public Key getKey(Key key)
			{
				return WritableDataRecord.this.getKey(key);
			}

			public boolean hasKey(Key key) {
				return WritableDataRecord.this.hasKey(key);
			}

			public Set getKeySet() {
				return WritableDataRecord.this.getKeySet();
			}

			public iWritableDataRecord getWritableCopy() {
				return WritableDataRecord.this.getWritableCopy();
			}
			
			public iWritableDataRecord getWritableCopy(Key k)
			{
				return WritableDataRecord.this.getWritableCopy(k);
			}
			
			public iDataRecord getReadOnlyView() {
				return this;
			}
		};

		return new ReadOnlyView(this.getKey());
	}


	/**
		@see innards.iDataRecord#getWritableCopy()
		*/
	public iWritableDataRecord getWritableCopy()
	{
		return getWritableCopy(this.getKey());
	}
	
	public iWritableDataRecord getWritableCopy(Key k)
	{
		return getWritableCopy(new WritableDataRecord(k));
	}
	
	protected iWritableDataRecord getWritableCopy(WritableDataRecord copy)
	{
		if (hashMap == null)
		{
			if (hasFirstData)
			{
				copy.putData(firstKey, firstData);
			}
		}

		else
		{
			copy.setMap(hashMap);
		}

		return copy;
	}

    public String toString() {

        String rv = "";
        Set<Key> s = getKeySet();
        for (Iterator<Key> it = s.iterator(); it.hasNext();) {
            Key k = (Key) it.next();
            rv += (k + " => " + getData(k) + "\n");
        }
        
        return rv;
    }
}
