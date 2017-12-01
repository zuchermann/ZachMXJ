package innards;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Dynamic enums. Less sugary and efficient than enums but slightly more flexible. 
 * 
 * Pros:
 * - You can declare constants across different files.
 * - You can extend this class like any Java class.
 * - You can continue adding more constants at runtime through reflection.
 * 
 * Cons: 
 * - It takes a lot more syntax to declare your constants.
 * - You can't leverage special collections like EnumMap and EnumSet. 
 * - If needed, you have to add methods for valueOf(name) and values() when extending this. 
 * 
 * @author cchao
 *
 */
public abstract class DEnum implements Comparable<DEnum>, IEnum {
	
	private static Map<Class<? extends DEnum>, Map<String, DEnum>> allNames = 
		new HashMap<Class<? extends DEnum>, Map<String, DEnum>>();
	
	private final String name;
	private final int ordinal;
	
	public DEnum(String name) {
		Class<? extends DEnum> type = getClass();
		if (! allNames.containsKey(type)) {
			allNames.put(type, new LinkedHashMap<String, DEnum>());
		}
		Map<String, DEnum> names = allNames.get(type);
		if (names.containsKey(name)) 
			throw new IllegalArgumentException(name + " already exists for " + type);
		this.name = name;
		this.ordinal = names.size();
		names.put(name, this);
	}
	
	public final String name() {
		return name;
	}
	
	public final int ordinal() {
		return ordinal;
	}
	
	@Override
    public final int compareTo(DEnum other) {
		if (getClass() != other.getClass() && getDeclaringClass() != other.getDeclaringClass())
		    throw new ClassCastException();
		return ordinal - other.ordinal;
    }
	
	public final Class<?> getDeclaringClass() {
		Class<? extends DEnum> clazz = getClass();
		Class<?> zuper = clazz.getSuperclass();
		return (zuper == DEnum.class) ? clazz : zuper;
	}
	
	protected static <N extends DEnum> int size(Class<N> type) {
		return allNames.get(type).size();
	}
	
	protected static <N extends DEnum> N valueOf(Class<N> type, String name) {
		if (name != null) {
			return type.cast(allNames.get(type).get(name)); 
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	protected static <N extends DEnum> N[] values(Class<N> type) {
    	Collection<DEnum> values = allNames.get(type).values();
    	N[] typedValues = (N[])Array.newInstance(type, values.size());
    	for (DEnum value : values) {
    		Array.set(typedValues, value.ordinal, value);
    	}
    	return typedValues;
    }
	
	public String toString() {
		return name;
	}
}
