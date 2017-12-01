package innards.util;

/**
 * @author marc
 * Created on May 6, 2003
 */
public interface iChangable
{

	public iModCount getModCount(Object withRespectTo);
	
	public interface iModCount
	{
		public Object data();
		public Object data(iRecompute recompute);
		public boolean hasChanged();
		public iModCount clear(Object newData);
	}
	
	public interface iRecompute
	{
		public Object recompute();
	}

}
