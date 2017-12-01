package innards.util;

/**

A typical implementation will look like this:
	
        // set to 'true' to ensure initialzation
	private boolean	m_HasChanged = true;
	
	boolean hasChanged()
        {
            return m_HasChanged;
        }
        void setChanged(boolean changed)
        {
            m_HasChanged = changed;
        }

	NOTE:  This file's only purpose is to educate the UI about the nature of objects
	that typically live in innards.  But it's not in the UI folder because we don't
	want anything from innards to have to point to anything in UI.  So this file
	lives in UI.		
*/

public interface iChangePollable
{
    public boolean getChanged();
    public void setChanged(boolean changed);	
}