/*
 * Created on Oct 10, 2003
 */
package innards.provider.network;

import innards.*;
import innards.iLaunchable;
import innards.util.InnardsDefaults;

/**
 * @author marc
 */
public class T_NIONetwork implements iLaunchable {

	public void launch() {
		if (InnardsDefaults.getIntProperty("in", 0) == 1) {
			launchIn();
		} else {
			launchOut();
		}
	}

	private void launchOut() {
		
		final NIONetworkOutputGroup outputGroup = new NIONetworkOutputGroup("255.255.255.255", 5000);
		Launcher.getLauncher().registerUpdateable(new iUpdateable() {
			int t;
			public void update() {
				//System.out.println(t);
				outputGroup.send("boo", t++);
			}
		});
		
	}

	private void launchIn() 
	{
		final NIONetworkGroup outputGroup = new NIONetworkGroup(5000);
		outputGroup.register(new NIONetworkGroup.Handler() {
			float lastt = 0;
			public void handle(String name, float f) {
				if (lastt+1!=f) System.out.println(" missed <"+(f-lastt-1)+">");
				lastt=f;
			}
		}, "boo");
	}
}
