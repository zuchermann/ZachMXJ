package innards;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Type token that can be used for generic classes.
 *
 * @author cchao
 *
 * @param <T>
 */
public class TypeRef<T> {

    private final Type type;
    private final Class<T> rawType;
    private volatile Constructor<T> constructor;

    @SuppressWarnings("unchecked")
    protected TypeRef() {
        Type superclass = getClass().getGenericSuperclass();
        if (superclass instanceof Class) {
            throw new RuntimeException("Missing type parameter.");
        }
        this.type = ((ParameterizedType) superclass).getActualTypeArguments()[0];
        this.rawType = type instanceof Class<?>
                ? (Class<T>) type
                : (Class<T>) ((ParameterizedType) type).getRawType();
    }

    public TypeRef(Class<T> type) {
    	this.type = type;
    	this.rawType = type;
    }
    
    /**
     * Instantiates a new instance of {@code T}. 
     */
    public T newInstance(Object... initArgs) throws NoSuchMethodException, IllegalAccessException,
                   InvocationTargetException, InstantiationException {
        if (constructor == null) {
            constructor = rawType.getConstructor(); 
        }
        return rawType.cast(constructor.newInstance());
    }

    /**
     * Gets the referenced type.
     */
    public Class<T> getType() {
        return rawType;
    }
    
    public T cast(Object o) {
    	return rawType.cast(o);
    }

    public int hashCode() {
    	return type.hashCode();
    }

    public boolean equals(Object o) {
    	return (o instanceof TypeRef) ? type.equals(((TypeRef<?>)o).type) : false;
    }
    
    /* For convenience */
    public static final TypeRef<Object> Object = new TypeRef<Object>() {};
    public static final TypeRef<String> String = new TypeRef<String>() {};
    public static final TypeRef<Integer> Integer = new TypeRef<Integer>() {};
    public static final TypeRef<Double> Double = new TypeRef<Double>() {};
    public static final TypeRef<Float> Float = new TypeRef<Float>() {};
    public static final TypeRef<Boolean> Boolean = new TypeRef<Boolean>() {};
    public static final TypeRef<Character> Char = new TypeRef<Character>() {};
    public static final TypeRef<Byte> Byte = new TypeRef<Byte>() {};
    public static final TypeRef<Class<?>> Class = new TypeRef<Class<?>>() {};

    public static void main(String[] args) throws Exception {
    	TypeRef<ArrayList<String>> typeRef = new TypeRef<ArrayList<String>>() {};
        List<String> l1 = typeRef.newInstance();
        System.out.println(l1.size());
        System.out.println(typeRef.getType());
        System.out.println(typeRef.rawType);
    }
}
