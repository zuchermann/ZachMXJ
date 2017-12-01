package innards;

public interface iClock_Keys extends iKeyInterfaces
{
	public interface Write extends iKeyInterfaces.Write
	{
		/**
		 * defines the location of the current time, a <code>float</code> value
		 * which will typically be monotonically increasing.
		 */
		public static final Key TIME_KEY = new Key("time key");
		
		/**
		 * defines the location of the current time step between ticks, a
		 * <code>float</code> value which will typically stay constant.
		 */
		public static final Key TIMESTEP_KEY = new Key("timestep key");
	}
}
