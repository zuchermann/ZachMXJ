package innards.sound.basics;

import innards.util.TaskQueue;

import java.util.*;

/**
 * @author marc
 * Created on May 13, 2003
 */
public class TimedTaskQueue
{
	protected List live= new LinkedList();
	protected List times= new LinkedList();

	protected Object lock= new Object();
	protected float schedulingWindow;
	
	public TimedTaskQueue(float schedulingWindow)
	{
		this.schedulingWindow = schedulingWindow;
	}

	public void update(float timeNow)
	{
		if (live.size() == 0)
			return;
		List todo;
		List todoTimes;
		List addBack;
		List addBackTimes;
		synchronized (lock)
		{
			todo= live;
			todoTimes = times;
			live= new LinkedList();
			times= new LinkedList();
			addBack = new LinkedList();
			addBackTimes = new LinkedList();
		}


		//System.out.println(" updating taskQueue @ <"+timeNow+">");

		for (int i= 0; i < todo.size(); i++)
		{
			Task task= (Task) todo.get(i);
			float time= ((Float) todoTimes.get(i)).floatValue();
			
			//System.out.println(" task at time <"+time+">");
			if (time>timeNow+schedulingWindow)
			{
				addBack.add(task);
				addBackTimes.add(todoTimes.get(i));
			}
			else
			{
				task.run(convertTimeInternalToExternal(time)-convertTimeInternalToExternal(timeNow));
			}
		}
		
		synchronized(lock)
		{
			live.addAll(addBack);
			times.addAll(addBackTimes);
		}
		todo.clear();
	}

	// subclass me 
	public float convertTimeExternalToInternal(float external)
	{
		return external;
	}
	
	public float convertTimeInternalToExternal(float internal)
	{
		return internal;
	}

	abstract public class Task
	{
		public Task(float atTime)
		{
			TimedTaskQueue.this.addTask(this, convertTimeExternalToInternal(atTime));
		}

		abstract public void run(float offsetFromNow);

		/**
		 * run methods can call this if they want to have another crack at it
		 * @param q
		 */
		protected void recur(float atTime)
		{
			if (!halted) addTask(this, atTime);
		}
		
		protected void halt()
		{
			removeTask(this);
			halted = true;
		}
		
		boolean halted = false;
	}
	
	public void addTask(Task t, float atTime)
	{
		synchronized(lock)
		{
			live.add(t);
			times.add(new Float(atTime));
		}
	}
	public void removeTask(Task t)
	{
		synchronized(lock)
		{
			int i = live.indexOf(t);
			if (i!=-1)
			{
				live.remove(i);
				times.remove(i);
			}
		}
	}
}
