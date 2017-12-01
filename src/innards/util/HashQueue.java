/* Created on Aug 19, 2003 */
package innards.util;

import innards.util.TaskQueue.Task;

import java.util.HashMap;

/**
 * @author marc
 * created on Aug 19, 2003
 */
public class HashQueue extends TaskQueue {
	HashMap annotations= new HashMap();

	public boolean hasAnnotation(Object o) {
		return annotations.containsKey(o);
	}

	public void update() {
		synchronized (annotations) {
			annotations.clear();
			super.update();
		}
	}

	protected void addTask(Task task) {
		synchronized (annotations) {
			super.addTask(task);
			if (task instanceof HashTask) {
				System.out.println(task+" "+((HashTask) task).hashWith);
				annotations.put(((HashTask) task).hashWith, task);
			}
		}
	}

	abstract public class HashTask extends Task {
		Object hashWith;
		public HashTask(Object hashWith) {
			this.hashWith= hashWith;
			synchronized (annotations) {
				annotations.put(this, ((HashTask) this).hashWith);
			}
		}
	}
	
	/** @see java.lang.Object#toString() */
	public String toString()
	{
		return ""+this.live;
	}

}
