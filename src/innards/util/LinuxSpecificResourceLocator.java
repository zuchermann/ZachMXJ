package innards.util;

import java.io.File;
import java.util.regex.Pattern;

public class LinuxSpecificResourceLocator extends MacOSSpecificResourceLocator
{
	/**
	 * This is for bootstrapping - finding the user-preferences folder without using InnardsDefaults.
	 *
	 * This is for getting the path to user-specific prefs.  not in source control.
	 * for osx, we will use a directory in ~/Library/Preferences.  if it doesn't exist we will make it.
	 *
	 * the folder name will be based on the application id.  this can be specified on the command line
	 * with -Dapplication.id=blah   If this in not specified, it will be edu.mit.media.robotic.c5m.
	 *
	 * @return path to a directory to store user-specific prefs. or throws error.  (e.g., for innardsdefaults.)
	 */
	public String getPathForUserPreferencesDirectory(){
		String applicationID = System.getProperty(APPLICATION_IDENTIFIER, DEFAULT_APPLICATION_IDENTIFIER);
		String path = "~/"+applicationID+"/";
		Pattern p = Pattern.compile("~");
		String fullPath = p.matcher(path).replaceAll(System.getProperty("user.home"));
		File f = new File(fullPath);
		if(f.exists() && !f.isDirectory()){
			throw new Error("ResourceLocator: Error, user pref dir:"+fullPath+" exists and is not a directory!!");
		}
		if(!f.exists()){
			System.out.println("ResourceLocator: prefs dir: \""+fullPath+" \" does not exist - making it.");
			boolean madeIt = f.mkdir();
			if(!madeIt){
				throw new Error("ResourceLocator: failed to make prefs dir \""+fullPath+"\"");
			}
		}
		return fullPath;
	}
}