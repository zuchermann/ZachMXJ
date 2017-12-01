package innards.input.device;

/**
 * Simple interface for getting data from a keyboard.  See
 * <code>KeyboardInputDevice</code> for a specific implementation.
 * 
 * @author synchar
 */
public interface iKeyboard
{
	public static final int UP_ARROW= 100001;
	public static final int DOWN_ARROW= 100002;
	public static final int LEFT_ARROW= 100003;
	public static final int RIGHT_ARROW= 100004;
	public static final int DEL= 100005;
	public static final int HOME= 100006;
	public static final int END= 100007;
	public static final int PAGE_UP= 100008;
	public static final int PAGE_DOWN= 100009;
	public static final int CLEAR= 100010;
	public static final int HELP= 100011;
	
	public static final int CTRL_WITH_SPECIAL = 1<<19; // added to the above;

	public boolean hasNewData();
	public void notifyDataRead();

	public int getKeyID();
	public boolean keyIsDown();
}
