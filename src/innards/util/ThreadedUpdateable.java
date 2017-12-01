package innards.util;

import innards.iUpdateable;
import sun.misc.Signal;
import sun.misc.SignalHandler;

/**
 * Does this already exist? An iUpdateable that runs in its own thread so it doesn't slow down C6's main loop. 
 * 
 * @author cchao
 *
 */
public abstract class ThreadedUpdateable implements iUpdateable {

	Thread thread;
	SynchronizedBoolean threadSuspended = new SynchronizedBoolean(false);
	
	long lastTime = System.currentTimeMillis();
	long timer;
	
	public ThreadedUpdateable(long interval) {
		this.timer = interval;
		this.thread = new Thread(new Runnable() {
			public void run() {
				Signal.handle(new Signal("INT"), new SignalHandler() {
					@Override
					public void handle(Signal arg0) {
						pause();
						System.exit(1);
					}
				});
				while (true) {
					long now = System.currentTimeMillis();
					if (now - lastTime > timer) {
						try {
							Thread.sleep(1);
							if (threadSuspended.get()) {
								synchronized(this) {
									while (threadSuspended.get())
										wait();
								}
							}
						} catch (InterruptedException e) { }
						if (! threadSuspended.get())
							update();
						lastTime = now;
					}
				}
			}
		});
	}
	
	public ThreadedUpdateable() {
		this(10);
	}
	
	public void start() {
		thread.start();
	}
	
	public void resume() {
		if (threadSuspended.get()) {
			threadSuspended.set(false);
			notify();
		}
	}
	
	public void pause() {
		threadSuspended.set(true);
	}
	
	public Thread getThread() {
		return thread;
	}
}
