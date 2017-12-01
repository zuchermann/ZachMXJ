package innards.namespace.context.key;

import innards.math.linalg.Vec3;
import innards.namespace.context.ContextTree;
import innards.provider.iFloatProvider;

/**
 * @author marc
 */
public class Vec3Key extends CKey implements iFloatProvider {
	public Vec3Key(String s) {
		super(s);
		pushRoot().lookup(this).pop();
	}

	public float evaluate() {
		return asFloat();
	}

	public Vec3 evaluate(Vec3 inplace) {
		if (inplace == null)
			inplace = new Vec3();
		Object o = run(failure);
		
		if (o instanceof Vec3) {
			inplace.setValue((Vec3) o);
			return inplace;
		}
		
		

		return null;
	}

	public Vec3Key defaults(final Vec3 f) {
		executionStack.add(new Defaults(new fu() {
			public Vec3 ret() {
				return f;
			}
		}));
		return this;
	}

	public Vec3Key set(Vec3 f) {
		ContextTree.set(this, f);
		return this;
	}

	public Vec3Key rootSet(Vec3 f) {
		pushRoot();
		ContextTree.set(this, f);
		pop();
		return this;
	}
}