package innards.namespace.context.key;

import innards.Key;
import innards.math.linalg.*;
import innards.namespace.context.*;
import innards.namespace.rhizome.metavariable.MetaList;
import innards.provider.iFloatProvider;
import innards.util.ReflectionTools;

import java.lang.reflect.*;
import java.util.*;

/**
 * the base class for "typed keys". this is abstract, becuase it lacks the type right now, look at OKey if you don't care
 * 
 * todo: caching strategy
 * 
 * @author marc
 */
abstract public class CKey extends Key {

	static boolean debug = false;

	protected MetaList executionStack = new MetaList();

	public CKey(String s) {
		super(s);
	}

	// here are some of the things that one can do to a key, specifically, things one can do to an execution stack

	protected interface StackElement {
		public Object enter(Object o);

		public Object exit(Object o);
	}

	public CKey debug() {
		debug = true;

		System.out.println(" --------------------- key <" + this + "> ----------------- ");
		for (int i = 0; i < executionStack.size(); i++)
			System.out.println(i + " " + executionStack.get(i));
		System.out.println(" ------------ remember, we're in context <" + ContextTree.pwd() + ">");
		return this;
	}

	public CKey lookup(final Object lookup) {
		executionStack.add(new StackElement() {
			public Object enter(Object o) {
				Object ret = ContextTree.get(lookup, o);
				if (debug) System.out.println("lookup inside <" + ContextTree.dir() + "> of <" + lookup + ">got <" + ret + ">");
				return ret;
			}

			public Object exit(Object o) {
				return o;
			}

			public String toString() {
				return "lookup:" + lookup;
			}
		});
		return this;
	}
	
	public CKey key(final CKey key)
	{
		executionStack.add(new StackElement() {
			public Object enter(Object o) {
				Object ret = key.run(o);
				if (debug) System.out.println("lookup(key) inside <" + ContextTree.dir() + "> of <" + key+ ">got <" + ret + ">");
				return ret;
			}

			public Object exit(Object o) {
				return o;
			}

			public String toString() {
				return "lookup(key):" + key;
			}
		});
		return this;
	}

	public CKey then(fu f) {
		executionStack.add(f);
		return this;
	}

	public CKey otherwise(fu f) {
		executionStack.add(new Otherwise(f));
		return this;
	}

	public CKey defaults(fu f) {
		executionStack.add(new Defaults(f));
		return this;
	}

	public CKey with(Key k, Object e) {
		executionStack.add(new With(k, e));
		return this;
	}

	public CKey with(Key k, float e) {
		executionStack.add(new With(k, new Float(e)));
		return this;
	}

	List pushStack = new LinkedList();

	public CKey push(Key k) {
		pushStack.add(ContextTree.where());
		ContextTree.begin(k.toString());
		return this;
	}

	public CKey pop() {
		ContextTree.go(pushStack.remove(pushStack.size() - 1));
		return this;
	}

	public CKey pushRoot() {
		pushStack.add(ContextTree.where());
		ContextTree.go(ContextTreeInternals.root);
		return this;
	}

	protected class Otherwise implements StackElement {
		fu f;

		public Otherwise(fu f) {
			this.f = f;
		}

		public Object enter(Object o) {
			if (o == failure)
				o = f.enter(null);
			return o;
		}

		public Object exit(Object o) {
			return o;
		}
	}

	protected class Defaults implements StackElement {
		fu f;

		public Defaults(fu f) {
			this.f = f;
		}

		public Object enter(Object o) {
			return o;
		}

		public Object exit(Object o) {
			if (o == failure)
				o = f.enter(null);
			return o;
		}

		public String toString() {
			return "(default to <" + f + ">)";
		}
	}

	protected class With implements StackElement {
		Key k;
		Object value;

		public With(Key k, Object value) {
			this.k = k;
			this.value = value;
		}

		Object oldValue;

		public Object enter(Object o) {
			oldValue = ContextTree.get(k, failure);
			return o;
		}

		public Object exit(Object o) {
			if (oldValue != failure)
				ContextTree.set(k, oldValue);
			return o;
		}
	}

	// here are the elements that we need

	static public abstract class fu implements StackElement {
		boolean nullIsFailure = true;

		StackTraceElement[] allocationStackTrace;

		public fu() {
			if (debug) {
				allocationStackTrace = new Exception().getStackTrace();
			}
		}

		Method objectCall;
		Method floatCall;
		Method intCall;
		Method voidCall;
		boolean cached = false;

		public Object base(Object o) {
			// crazy cached reflection party

			// find a method that takes either an object, a float or an int or a void that is in the declaring class
			if (!cached) {
				Class clazz = this.getClass();
				Method[] method = clazz.getDeclaredMethods();

				objectCall = ReflectionTools.findMethodWithParameters(new Class[]{Object.class}, method);
				if (objectCall == null)
					floatCall = ReflectionTools.findMethodWithParameters(new Class[]{Float.TYPE}, method);
				if ((objectCall == null) && (floatCall == null))
					intCall = ReflectionTools.findMethodWithParameters(new Class[]{Integer.TYPE}, method);
				if ((objectCall == null) && (floatCall == null) && (intCall == null))
					voidCall = ReflectionTools.findMethodWithParameters(new Class[]{}, method);

				if ((objectCall == null) && (floatCall == null) && (intCall == null) && (voidCall == null)) {
					throw new oException("lookup inside Basefu failed, couldn't find any declared method with parameter type Object, float, int or void", allocationStackTrace);
				}
				cached = true;
			}

			try {
				if (objectCall != null)
					o = objectCall.invoke(this, new Object[]{o});
				else if (floatCall != null)
					o = floatCall.invoke(this, new Object[]{new Float(toFloat(o))});
				else if (intCall != null)
					o = floatCall.invoke(this, new Object[]{new Integer((int) toFloat(o))});
				else if (voidCall != null)
					o = voidCall.invoke(this, new Object[]{});

				return nullIsFailure ? (o == null ? failure : o) : o;
			} catch (IllegalArgumentException e) {
				oException e2 = new oException("call to Basefu method failed", allocationStackTrace);
				e2.initCause(e);
				throw e2;
			} catch (IllegalAccessException e) {
				oException e2 = new oException("call to Basefu method failed", allocationStackTrace);
				e2.initCause(e);
				throw e2;
			} catch (InvocationTargetException e) {
				oException e2 = new oException("call to Basefu method failed", allocationStackTrace);
				e2.initCause(e);
				throw e2;
			}
		}

		public Object enter(Object o) {
			return base(o);
		}

		public Object exit(Object o) {
			return o;
		}
	}

	static protected float toFloat(Object o) {
		if (o instanceof Number)
			return ((Number) o).floatValue();
		if (o instanceof iFloatProvider)
			return ((iFloatProvider) o).evaluate();
		if (o instanceof CKey)
			return ((CKey) o).asFloat();
		if (o instanceof Vec3)
			return ((Vec3) o).mag();
		if (o instanceof Vec2)
			return ((Vec2) o).mag();
		if (o instanceof Vec)
			return (float) ((Vec) o).mag();
		throw new oException(" toFloat, failed, got class <" + o + "> <" + (o == null ? null : o.getClass()) + ">");
	}

	protected float asFloat() {
		Object o = run(failure);
		return toFloat(o);
	}

	protected Object run(Object o) {
		if (debug)
			System.out.println(" run: stack is <" + executionStack + ">");
		for (int i = 0; i < executionStack.size(); i++) {
			//for (int i = executionStack.size() - 1; i >= 0; i--) {
			StackElement e = (StackElement) executionStack.get(i);
			if (debug)
				System.out.println("##### o <" + o + ">");
			o = e.enter(o);
			if (debug)
				System.out.println("##### becomes o <" + o + "> after <" + e + ">");
		}
		//for (int i = 0; i < executionStack.size(); i++)
		for (int i = executionStack.size() - 1; i >= 0; i--) {
			StackElement e = (StackElement) executionStack.get(i);
			o = e.exit(o);
		}
		return o;
	}

	static public class oException extends RuntimeException {
		public oException(String message) {
			super(message);
		}

		public oException(String message, StackTraceElement[] e) {
			super(message + (e == null ? "\n    turn on asserts for better stacktrace" : "allocation stacktrace is <" + Arrays.asList(e) + ">"));
		}
	}

	static class Failure {
	}

	static protected Failure failure = new Failure();
}