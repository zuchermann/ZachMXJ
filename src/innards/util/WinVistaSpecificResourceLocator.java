package innards.util;

import java.io.File;

public class WinVistaSpecificResourceLocator extends
		WinXPSpecificResourceLocator {
	public String getPathForUserPreferencesDirectory(){
		String applicationID = System.getProperty(APPLICATION_IDENTIFIER, DEFAULT_APPLICATION_IDENTIFIER);
		String fullPath = System.getProperty("user.home") + "\\AppData\\Local" + "\\"+applicationID+"\\";
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
