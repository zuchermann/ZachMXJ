package innards.provider;

import innards.*;
import java.util.*;

/**
	central place for update policies for double providers
	so far: implementsed by the Boo providers
 */
public class ProviderUpdator
	implements iUpdateable
{
	static public final int policy_once_per_tick = 0;
	static public final int policy_every_evaluate = -1;
	static public final int policy_strobed = 1; // through all the integers

	static public final int default_policy = policy_every_evaluate;
	
	Map updatedRecords = new WeakHashMap();
	
	int tick = 0;
	public void update()
	{
		tick++;
	}
	
	public ProviderUpdator()
	{
		single = this;
	}

	static ProviderUpdator single;

	static public void update(iUpdateable provider)
	{
		if (single==null) single = new ProviderUpdator();
		UpdateRecord up = (UpdateRecord)single.updatedRecords.get(provider);
		if (up==null)
		{
			single.updatedRecords.put(provider, up = new UpdateRecord());
		}
		if (up.policy == policy_once_per_tick)
		{
			if (up.lastAt!=single.tick)
			{
				up.lastAt = single.tick;
				provider.update();
			}
		}
		else if (up.policy == policy_every_evaluate)
		{
			provider.update();
		}
		else
		{
			if (up.lastAt!=single.tick)
			{
				up.lastAt = single.tick;
				if (single.tick%up.policy==0) provider.update();
			}
		}
	}
	
	static public class UpdateRecord
	{
		int lastAt = -1;
		int policy = default_policy;
	}
		
}
