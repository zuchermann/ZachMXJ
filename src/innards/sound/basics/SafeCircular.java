package innards.sound.basics;

/**
 * @author marc
 * Created on May 9, 2003
 */
public class SafeCircular
{

	Object[] buffer;
	int writeHead;
	int readHead;


	public SafeCircular(int size)
	{
		buffer = new Object[size];
		writeHead = 0;
		readHead = size/2;
	}
	
}
