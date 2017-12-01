package innards.provider.network;

import innards.math.linalg.Vec;
import innards.provider.iVectorProvider;

/**
 * @version 	1.0
 * @author
 */
public class NetVecProvider implements NetworkGroup.VecHandler, iVectorProvider
{

	protected NetworkGroup.VecHandler chainDown= null;
	NetworkGroup group;

	String name;

	Vec value;

	public NetVecProvider(String name, Vec def)
	{
		this.name= name;
		checkGroup();
		init(default_group);
		value= def;
	}

	public NetVecProvider(String name, Vec def, NetworkGroup group)
	{
		this.group= group;
		this.name= name;
		init(group);
		value= def;
	}

	public Vec construct()
	{
		return new Vec(value.dim());
	}

	public void get(Vec inplace)
	{
		inplace.set(value);
	}

	public void handle(String name, Vec f)
	{
		value= f;
		if (chainDown != null)
			chainDown.handle(name, f);
	}

	protected void init(NetworkGroup group)
	{
		chainDown= group.register(this, name);
	}
	static public NetworkGroup default_group= null;

	static protected void checkGroup()
	{
		if (default_group == null)
		{
			default_group= new NetworkGroup();
		}
	}

	static public NetworkGroup getDefaultGroup()
	{
		checkGroup();
		return default_group;
	}

	static public void main(String[] s)
	{
		NetVecProvider[] p= new NetVecProvider[s.length];
		for (int i= 0; i < p.length; i++)
			p[i]= new NetVecProvider(s[i], new Vec(1));
		NetVecProvider.getDefaultGroup().registerDefaultHandler(new NetworkGroup.VecHandler()
		{
			/**
			 * @see innards.signal.provider.network.NetworkGroup.VecHandler#handle(String, Vec)
			 */
			public void handle(String name, Vec v)
			{
				System.out.println(" unhandled <" + name + "> <" + v + ">");
			}
		});
		while (true)
		{
			for (int i= 0; i < p.length; i++)
			{
				Vec inplace= p[i].construct();
				p[i].get(inplace);
				System.out.print(s[i] + ":" + inplace + " ");
			}
			System.out.println();
			try
			{
				Thread.sleep(5);
			} catch (InterruptedException ex)
			{
			}

		}

	}

	static public void setDefaultGroup(NetworkGroup group)
	{
		default_group= group;
	}
}
