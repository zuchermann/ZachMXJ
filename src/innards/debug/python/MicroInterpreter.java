package innards.debug.python;

/**
 * an interface for very simple (one line long) languages. installed into PythonInterpreterBridges
 * languages 'run' in their own thread
 * @author marc
 *
 */
public interface MicroInterpreter
{
	/**
	 * returns true if this MicroInterpreter handled the command (thus stopping the delegation chain)
	 * @param command
	 * @return boolean
	 */
	public boolean handle(String command);
	/**
	 * returns the 'result' string (to show to the user) of this command (since handle(...) returned true)
	 * @return String
	 */
	public String getResult();
}
