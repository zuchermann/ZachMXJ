package innards.provider.network;

import innards.math.linalg.Vec;

/**

 */

public class NetDoubleProviderOutput
{
	static NetworkOutputGroup default_group = null;

	String name;
	NetworkOutputGroup group;

	public NetDoubleProviderOutput(String name)
	{
		this.name = name;
		checkGroup();
		group = default_group;
	}

	public NetDoubleProviderOutput(String name, NetworkOutputGroup group)
	{
		this.group = group;
		this.name = name;
	}

	static public void setDefaultGroup(NetworkOutputGroup group)
	{
		default_group = group;
	}

	protected void checkGroup()
	{
		if (default_group == null)
		{
			default_group = new NetworkOutputGroup();
		}
	}

	public void set(float f)
	{
		group.send(name, f);
	}

	public void set(Vec f)
	{
		group.send(name, f);
	}

	public NetworkOutputGroup getGroup()
	{
		return group;
	}

	/**
	 *
	 */
	public static void main(String[] args)
	{
		NetDoubleProviderOutput fred = new NetDoubleProviderOutput("fred");
		int i = 0;
		while (true)
		{
			fred.set(i++);
			try
			{
				Thread.sleep(10);
			} catch (InterruptedException ex)
			{
			}

		}

	}

}