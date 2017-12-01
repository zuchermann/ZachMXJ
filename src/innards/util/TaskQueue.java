package innards.util;

import innards.iUpdateable;

import java.util.*;

/**
* 	thread safe task queue
 */
public class TaskQueue implements iUpdateable
{

	protected List live= new LinkedList();

	Object lock= new Object();

	public void update()
	{
		if (live.size() == 0)
			return;
		List todo;
		synchronized (lock)
		{
			todo= live;
			live= new LinkedList();
		}
		for (int i= 0; i < todo.size(); i++)
		{
			Task task= (Task) todo.get(i);
			task.run();
		}
		todo.clear();
	}

	protected void addTask(Task task)
	{
		synchronized (lock)
		{
			live.add(task);
		}
	}

	abstract public class Task
	{
		public Task()
		{
			TaskQueue.this.addTask(this);		
		}
		

		abstract public void run();

		/**
		 * run methods can call this if they want to have another crack at it
		 * @param q
		 */
		protected void recur()
		{
			addTask(this);
		}
	}

	public class Updateable extends Task
	{
		iUpdateable u;
		public Updateable(iUpdateable u)
		{
			this.u= u;
		}
		public void run()
		{
			u.update();
			recur();
		}
	}
}