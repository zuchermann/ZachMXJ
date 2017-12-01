package innards.provider.network;

import innards.provider.iFloatProvider;

public class NetDoubleProvider implements iFloatProvider, NetworkGroup.Handler
{

	static public NetworkGroup default_group= null;

	String name;
	NetworkGroup group;

	public NetDoubleProvider(String name, float def)
	{
		this.name= name;
		checkGroup();
		init(default_group);
		value= def;
	}

	public NetDoubleProvider(String name, float def, NetworkGroup group)
	{
		this.group= group;
		this.name= name;
		init(group);
		value= def;
	}

	static public void setDefaultGroup(NetworkGroup group)
	{
		default_group= group;
	}

	static public NetworkGroup getDefaultGroup()
	{
		checkGroup();
		return default_group;
	}

	protected NetworkGroup.Handler chainDown= null;

	protected void init(NetworkGroup group)
	{
		chainDown= group.register(this, name);
	}

	static protected void checkGroup()
	{
		if (default_group == null)
		{
			default_group= new NetworkGroup();
		}
	}

	float value;

	public void handle(String name, float f)
	{
		value= f;
		if (chainDown != null)
			chainDown.handle(name, f);
	}

	public float evaluate()
	{
		return value;
	}

	static public void main(String[] s)
	{
		NetDoubleProvider[] p= new NetDoubleProvider[s.length];
		for (int i= 0; i < p.length; i++)
			p[i]= new NetDoubleProvider(s[i], -1);
		while (true)
		{
			for (int i= 0; i < p.length; i++)
				System.out.print(s[i] + ":" + p[i].evaluate() + " ");
			System.out.println();
			try
			{
				Thread.sleep(5);
			} catch (InterruptedException ex)
			{
			}

		}

	}
}