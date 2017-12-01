package innards;

/**
 * The point of this is to give you a choice to use static Java enums or dynamic enums.
 * Just have whichever enumeration you choose implement this interface.
 * 
 * @author cchao
 *
 */
public interface IEnum {

	public String name();
	public int ordinal();
}
