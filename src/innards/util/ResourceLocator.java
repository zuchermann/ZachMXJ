package innards.util;

public class ResourceLocator
{
	public interface OSSpecificResourceLocator
	{
		public char getPathSeparator();
		public String getPathForUserPreferencesDirectory();
		public String getPathForDefaultDefaults();
		public String getPathForResource(String resource, boolean mustExist);
		public boolean loadLibrary(String libraryName);
		public boolean loadOSSpecificResource(String resourceName, Object params);
		public String resolveEnv(String value);
	}

	public static String getPathForDefaultDefaults(){
		if (lazyInit())
		{
			return osSpecificResourceLocator.getPathForDefaultDefaults();
		}
		return null;
	}

	public static String getPathForUserPreferencesDirectory(){
		if (lazyInit())
		{
			return osSpecificResourceLocator.getPathForUserPreferencesDirectory();
		}
		return null;
	}

	public static String getPathForResource(String resource, boolean mustExist)
	{
		if (lazyInit())
		{
			return osSpecificResourceLocator.getPathForResource(resource, mustExist);
		}
		return null;
	}

	public static String getPathForResource(String resource)
	{
		if (lazyInit())
		{
			return osSpecificResourceLocator.getPathForResource(resource, true);
		}
		return null;
	}

	public static boolean loadLibrary(String libraryName)
	{
		if (lazyInit())
		{
			return osSpecificResourceLocator.loadLibrary(libraryName);
		}
		return false;
	}

	public static boolean loadOSSpecificResource(String resourceName, Object params)
	{
		if (lazyInit())
		{
			return osSpecificResourceLocator.loadOSSpecificResource(resourceName, params);
		}
		return false;
	}

	protected static OSSpecificResourceLocator osSpecificResourceLocator= null;
	protected static boolean initialized= false;


	//return true if we're initialized and good to go.
	public static boolean lazyInit()
	{
		if (!initialized)
		{
			initialized= true;
			String osName= System.getProperty("os.name");

			String className= null;
			if ("Mac OS X".equals(osName))
			{
				System.out.println("ResourceLocator has determined we're on Mac OS X");
				className= "innards.util.MacOSSpecificResourceLocator";
			}
			//            else if("WindowsBLAH".equals(osName)){
			//                System.out.println("we are on windows!");
			//                className = "blah";
			//            } //....  etc.
			else if("Windows XP".equals(osName)) {
				System.out.println("ResourceLocator has determined we're on Windows XP");
				className= "innards.util.WinXPSpecificResourceLocator";
			}
			else if("Windows Vista".equals(osName)) {
				System.out.println("ResourceLocator has determined we're on Windows Vista");
				className= "innards.util.WinVistaSpecificResourceLocator";	
			}
			else if("Windows 7".equals(osName)) {
				System.out.println("ResourceLocator has determined we're on Windows 7");
				className= "innards.util.WinVistaSpecificResourceLocator";//same as vista	
			}
			else if("Windows 8".equals(osName)) {
				System.out.println("ResourceLocator has determined we're on Windows 8");
				System.out.println("However, we do not know what to do about it yet. We'll just try what works on Windows Vista and 7.");
				className= "innards.util.WinVistaSpecificResourceLocator";//same as vista
			}
			else if ("Linux".equals(osName)) {
				System.out.println("ResourceLocator has determined we're on Linux");
				className= "innards.util.LinuxSpecificResourceLocator";				
			}
			else
			{
				System.out.println("ResourceLocator has no known class for OS named: \"" + osName + "\"");
			}

			Class osSpecificResourceLocatorClass= null;
			if (className != null)
			{
				try
				{
					osSpecificResourceLocatorClass= Class.forName(className);
				}
				catch (Exception ex)
				{
					System.out.println("Classloader could not get class for name:\"" + className + "\"");
					ex.printStackTrace();
				}
			}

			if (osSpecificResourceLocatorClass != null)
			{
				try
				{
					osSpecificResourceLocator= (OSSpecificResourceLocator) osSpecificResourceLocatorClass.newInstance();
				}
				catch (Exception ex)
				{
					System.out.println(
						"Class \""
							+ osSpecificResourceLocatorClass.getName()
							+ "\" probably has no void constuctor, or cannot be cast to type OSSpecificResourceLocator:");
					ex.printStackTrace();
				}
			}

		}
		if (osSpecificResourceLocator == null)
		{
			System.out.println("Resource Locator cannot initialize for this platform; it will most likely return null now.");
		}
		return osSpecificResourceLocator != null;
	}

}
