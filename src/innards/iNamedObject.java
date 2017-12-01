package innards;


/**
    Base object for the system.
    @author synchar
*/
public interface iNamedObject 
{
	/** Gets the "name" in iNamedObject */
	public String getName();
	public Key getKey();

	public void setParent(iNamedGroup parent);
	public void removeParent(iNamedGroup parent);
	public iNamedGroup getParent();
}
