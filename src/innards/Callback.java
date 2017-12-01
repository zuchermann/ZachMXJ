package innards;

import java.lang.reflect.Method;

/**
 * This is a general form for a callback using Java's reflection API. 
 *  
 * @author cchao
 *
 */
public class Callback {

	public final Object target;
	public final String methodName;
	public final Class<?>[] argClasses;
	public final Object[] defaultArgs;
	
	Method method;
	
	/**
	 * This is a convenience constructor for methods that don't take any arguments. 
	 */
	public Callback(Object target, String methodName) {
		this(target, methodName, new Class<?>[] {}, new Object[] {});
	}
	
	/**
	 * Create a callback to run later.
	 * 
	 * @param target		the object that contains the method to be called
	 * @param methodName	the name of the method to be called
	 * @param argClasses	the classes of the arguments of the method
	 * @param defaultArgs	the default arguments for the method
	 */
	public Callback(Object target, String methodName, Class<?>[] argClasses, Object[] defaultArgs) {
		this.target = target;
		this.methodName = methodName;
		this.argClasses = argClasses;
		this.defaultArgs = defaultArgs;
		setup();
	}
	
	void setup() {
		try {
			method = target.getClass().getMethod(methodName, argClasses);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Run the attached method with the default arguments. 
	 */
	public void run() {
        if (method == null) {
        	System.out.println("Warning: no method");
		} else {
			try {
				method.invoke(target, defaultArgs);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	/**
	 * Run the attached method with the specified arguments. 
	 * 
	 * @param args
	 */
	public void run(Object[] args) {
        if (method == null) {
        	System.out.println("Warning: no method");
		} else {
			try {
				method.invoke(target, args);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	/**
	 * Get the attached method.
	 * 
	 * @return
	 */
	public Method getMethod() {
		return method;
	}
}
