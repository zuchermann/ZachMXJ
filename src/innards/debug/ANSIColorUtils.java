package innards.debug;

/**
 * old skhool.
 * @author marc
 * <I>Created on Mar 19, 2003</I>
 */
public class ANSIColorUtils
{

	static public final char esc = (char)0x1b;

	static public String red(String s)
	{
		return esc+"[31m"+s+esc+"[0m";
	}
	static public String blue(String s)
	{
		return esc+"[34m"+s+esc+"[0m";
	}
	static public String green(String s)
	{
		return esc+"[32m"+s+esc+"[0m";
	}
	static public String yellow(String s)
	{
		return esc+"[33m"+s+esc+"[0m";
	}
	
	/**
	 * for use with \r. for example System.out.print(eraseLine()+" status = "+i+" \r");
	 * @return
	 */
	static public String eraseLine()
	{
		return esc+"[K";
	}

}
