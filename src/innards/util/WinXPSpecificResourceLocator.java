package innards.util;
//
//	WinXPSpecificResourceLocator.java
//	characters
//
//	Created by jg on Wed Feb 19 2003.
//	Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//


import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

//chances are you never need to interact with this.  just use the ResourceLocator in innards.util.
// it will come here as it needs to.

/**
 * <h3>Locating Resources:</h3><br>
 * This class is meant to put all knowledge of paths and finding files in one place.  Most use is to call
 * {@link #getPathForResource(String)} to get the path to a particular file using search paths stored in <code>InnardsDefaults</code>.
 * It can also be used to load native libraries directly.<br><br>
 *
 * For deployment or different installation situations you will also want to override the various search paths
 * used by {@link #getPathForResource(String)}.  These are stored in <code>InnardsDefaults</code>, and can be modified
 * there.  For deployable programs, modify them in the default-defaults, so they will be included in intitial
 * preferences on new machines.

 * <h3>Bootstrapping:</h3><br>
 * For maximum customizability, finding resources depends on values stored in {@link InnardsDefaults InnardsDefaults}. However, <code>InnardsDefaults</code>
 * uses the <code>ResourceLocator</code> to figure out where its preference file is stored, and also the path to "DefaultDefaults" (the
 * initial data used by InnardsDefaults to population new installations).  To solve this, we have 2 bootstrapping functions
 * in the resource locator which do not depend on data from <code>InnardsDefaults</code>: {@link #getPathForDefaultDefaults()} and
 * {@link #getPathForUserPreferencesDirectory()}.<br><br>
 *
 * 2 special properties can be specified as java system properties (set as java args with -D on the command line).  These are
 * the <code>application.id</code> and <code>path.to.defaultdefaults</code>.
 * By default, these will work if either you install into ~/projects/c5m or you launch from your c5m directory.  In other scenarios,
 * or for standalone apps, you will need to modify these. Change the application ID to give yourself prefs separate from regular
 * c5m processes.  Change the default-defaults path if you need to find these elsewhere. Default-defaults allow you to
 * specify <code>InnardsDefaults</code> values within your app bundle or jar that will seed the innards defaults on a new machine,
 * allowing your deployed app to start out with the correct settings.
 *
 */

public class WinXPSpecificResourceLocator implements ResourceLocator.OSSpecificResourceLocator
{
	public static final String RESOURCE_SEARCH_PATH= "ResourceSearchPath";
	public static final String NIB_SEARCH_PATH= "NibSearchPath";
	public static final String ANY_LIBRARY_SEARCH_PATH= "AllLibSearchPath";
	public static final String JNILIB_SEARCH_PATH= "JNILibSearchPath";
	public static final String DYLIB_SEARCH_PATH= "DYLIBSearchPath";
	public static final String BUNDLE_SEARCH_PATH= "BundleSearchPath";
	public static final String CONTENT_ROOT= "ContentRootPath";
	public char getPathSeparator() {return '\\';}

	//******system properties (ie -D on the command line)******
	//bootstrap path finding by setting path to default defaults.  this must be set to find file in jar (start with @!), or if you're putting it nonstandard (see class comment)
	public static final String PATH_TO_DEFAULT_DEFAULTS = "path.to.defaultdefaults";
	//application id.  used for user-prefs folder name, maybe for other things later.  most launchables will just use default, override for deployment
	public static final String APPLICATION_IDENTIFIER = "application.id";
	//*********************************************************

	public static final String DEFAULT_APPLICATION_IDENTIFIER = "edu.mit.media.robotic.c5m";

	//search these paths for default-defaults if no PATH_TO_DEFAULT_DEFAULTS is set.
	public static final String RELATIVE_PATH_TO_DEFAULT_DEFAULTS = "./resources/preferences/defaults-win.txt";
	public static final String ABSOLUTE_PATH_TO_DEFAULT_DEFAULTS = "%USERPROFILE%/projects/c5m/resources/preferences/defaults-win.txt";

//	public static boolean LOOK_IN_MAIN_BUNDLE_FIRST= true;

	//good to set this to true to test if everything is built into the
	//     app properly
//	public static boolean LOOK_ONLY_IN_MAIN_BUNDLE= false;

	protected String[] anyResources, nibs, anyLibs, jniLibs, dylibs, bundles;

	//protected String[] pathsForNib, pathsForJNILib, pathsForDylib, pathsForBundle, pathsForOther;
	protected String[] pathsForDLLs, pathsForOther;
	protected String contentRootPath;

	public WinXPSpecificResourceLocator()
	{
		//copyCVSDefaultsToUserDefaults();
		//lazyInit();
	}

	boolean lazyInitted = false;
	protected void lazyInit()
	{
		if(!lazyInitted){
			lazyInitted = true;
			//copyCVSDefaultsToUserDefaults();
			anyResources= createArrayFromIPKey(RESOURCE_SEARCH_PATH);
			nibs= createArrayFromIPKey(NIB_SEARCH_PATH);
			anyLibs= createArrayFromIPKey(ANY_LIBRARY_SEARCH_PATH);
			jniLibs= createArrayFromIPKey(JNILIB_SEARCH_PATH);
			dylibs= createArrayFromIPKey(DYLIB_SEARCH_PATH);
			bundles= createArrayFromIPKey(BUNDLE_SEARCH_PATH);
			//pathsForNib= concatArrays(nibs, anyResources, null, null);
			//pathsForJNILib= concatArrays(jniLibs, anyLibs, anyResources, null);
			//pathsForDylib= concatArrays(dylibs, anyLibs, anyResources, null);
			pathsForDLLs = concatArrays(dylibs, anyLibs, anyResources, null);
			//pathsForBundle = concatArrays(bundles, anyResources, null, null);
			contentRootPath = resolveEnv(InnardsDefaults.getDirProperty(CONTENT_ROOT, null));
			pathsForOther= concatArrays(anyResources, contentRootPath==null?null:new String[]{contentRootPath}, null, null);
		}
	}

	/**
	 * This is for bootstrapping - finding the default-defaults file without using InnardsDefaults.
	 *
	 * if system property {@link #PATH_TO_DEFAULT_DEFAULTS PATH_TO_DEFAULT_DEFAULTS} (-Dpath.to.defaultdefaults=blah on the command line) is set, we will look only there.
	 *
	 * Otherwise, this function will look in {@link #RELATIVE_PATH_TO_DEFAULT_DEFAULTS RELATIVE_PATH_TO_DEFAULT_DEFAULTS}, (software must be run from c5m directory for this to work).
	 * If this fails, it will look in {@link #ABSOLUTE_PATH_TO_DEFAULT_DEFAULTS ABSOLUTE_PATH_TO_DEFAULT_DEFAULTS}.
	 *
	 * @return the path to the defaultdefaults file, or null if it can't be found.
	 */
	public String getPathForDefaultDefaults(){
		//TODO: make sure this also can load the defaults from inside jar and fix if not.

		String specifiedPathToDefaults = System.getProperty(PATH_TO_DEFAULT_DEFAULTS);

		//try regular files and default paths.
		File fileToLoad;
		if(specifiedPathToDefaults != null){
			fileToLoad = new File(specifiedPathToDefaults);
			if(!fileToLoad.exists()){
				System.out.println("ResourceLocator: Cannot load default-defaults, property specified in \"path.to.defaultdefaults\" ("+specifiedPathToDefaults+") doesn't lead to a file.");
				return null;
			}
		}else{
			fileToLoad = new File(RELATIVE_PATH_TO_DEFAULT_DEFAULTS);
			if(!fileToLoad.exists()){
				System.out.println("ResourceLocator:  relative path to default-defaults ("+RELATIVE_PATH_TO_DEFAULT_DEFAULTS+") does not lead to a file, trying absolute path");
				fileToLoad = new File(resolveEnv(ABSOLUTE_PATH_TO_DEFAULT_DEFAULTS));
				if(!fileToLoad.exists()){
					System.out.println("ResourceLocator:  absolute path to default-defaults ("+ABSOLUTE_PATH_TO_DEFAULT_DEFAULTS+") does not lead to a file, relative and absolute methods failed, will not check default-defaults");
					return null;
				}
			}
		}
		return fileToLoad.getAbsolutePath();
	}

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
		String fullPath = System.getProperty("user.home") + "\\Local Settings\\Application Data" + "\\"+applicationID+"\\";
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

	public String getPathForResource(String resource){
		return getPathForResource(resource, true);
	}

	/**
	 * pass in full name, ie "bob.nib" or "libsarah.dylib", this will help choose which paths to use.
	 * if resource starts with a "\", assumed to be an absolute path. if mustExist is false, this will hallucinate
	 * a valid path if resource not found.  searching is done with paths obtained from InnardsDefaults.
	 *
	 * @param resource name of resource, should be relative to some search path.  can include directories.
	 * @param mustExist
	 * @return
	 */
	public String getPathForResource(String resource, boolean mustExist)
	{
		resource = convertNativeBinaryToWin(resource);
		lazyInit();
		if(resource==null || resource.length() == 0){
			return null;
		}
		String path= null;
		resource = resolveEnv(resource);
		if(resource.charAt(1) == ':')
		{//is absolute
			if(!mustExist || new File(resource).exists())
			{
				path = resource;
			}
			else // mustExist && doesn't exist
			{
				return null;
			}
		}
		if (path == null /*&& !LOOK_ONLY_IN_MAIN_BUNDLE*/)
		{
			/*if (resource.endsWith(".nib"))
			{
				path= findResourceFromPaths(resource, pathsForNib);
			}
			else*/ if (resource.endsWith(".dll") || resource.endsWith(".dylib"))
			{
				path= findResourceFromPaths(resource, pathsForDLLs);
			}/*
			else if (resource.endsWith(".dylib"))
			{
				path= findResourceFromPaths(resource, pathsForDylib);
			}
		    else if (resource.endsWith(".bundle"))
			{
				path= findResourceFromPaths(resource, pathsForBundle);
			}*/
			else
			{
				path= findResourceFromPaths(resource, pathsForOther);
			}
		}
		if(path == null && !mustExist){
			//create path from ContentRoot
			if(contentRootPath == null || contentRootPath.length()==0){
				System.out.println("No Value set for ContentRootPath in innards defaults, but it is needed.  please set it");
			}else{
				System.out.println("Resource \""+resource+"\" not found, creating path in contentroot for it");
				return contentRootPath+resource;
			}
		}
		return path;
	}

	public boolean loadLibrary(String libraryName)
	{
		libraryName = convertNativeBinaryToWin(libraryName);
		String absPathToLib= getPathForResource(System.mapLibraryName(libraryName));
		/*if(absPathToLib == null || absPathToLib.length()==0 && libraryName.indexOf(".")==-1){
			//if it doesn't have a dot, maybe its the platform indep lib name and needs translation
			String sysDepName = System.mapLibraryName(libraryName);
			System.out.println("Resource Locator couldn't find \""+libraryName+"\" as is, trying instead system dep name \""+sysDepName+"\"");
			libraryName = sysDepName;
			absPathToLib = getPathForResource(libraryName);
		}*/
		if (absPathToLib != null && absPathToLib.length() > 0)
		{
			if (libraryName.endsWith(".dll"))
			{
				System.out.println("Loading DLL \"" + absPathToLib + "\"...");
				System.load(absPathToLib);
				System.out.println("Done loading!");
			}
			/*else if (
			{
				System.out.println("Loading dylib \"" + absPathToLib + "\"...");
				System.out.println("DOES THIS WORK??  trying to load \""+absPathToLib+"\" using System.load instead of NSRuntime.loadLibrary");
				//NSRuntime.loadLibrary(absPathToLib);
				System.load(absPathToLib);
				System.out.println("Done loading!");
			}*/
			return true;
		}
		else
		{
			System.out.println("no path found, didn't try to load library.");
			return false;
		}
	}

	//the only one i could think of was nibs.
	//if name is a nib name, params should be the object to be the owner.
	public boolean loadOSSpecificResource(String name, Object params)
	{
		lazyInit();
		return false;
	}
	public String convertNativeBinaryToWin(String oldName) {
		if(oldName.endsWith(".jnilib") || oldName.endsWith(".dylib")) {
			int lastIndex = oldName.lastIndexOf('.');
			
			if(lastIndex >= 0) {
				//System.out.println("Returning: " + oldName.substring(0, lastIndex+1) + "dll");
				return oldName.substring(0, lastIndex+1) + "dll";
			}	
		}
		//System.out.println("Returning: " + oldName);
		return oldName;
	}
	public String findResourceFromPaths(String resourceName, String[] paths)
	{
		//System.out.println("Convert " +resourceName);
		resourceName = convertNativeBinaryToWin(resourceName);
		System.out.println("to " +resourceName);
		if (paths == null)
		{
			System.out.println("WinXPSpecificResourceLocator has no paths to search... try putting some in innards properties.");
			return null;
		}
		
		for (int i= 0; i < paths.length; i++)
		{
			//System.out.println("Looking at path:" + paths[i]);
			File file= new File(paths[i], resourceName);
			if (file.exists())
			{
				return file.getAbsolutePath();
			}
		}
		// try it just by itself
		File file = new File(resourceName);
		if (file.exists())
		{
			return file.getAbsolutePath();
		}
		// See if it's in the JRE path
		file = new File(System.getenv("JRE_HOME") + "\\bin\\" + resourceName);
		if (file.exists())
		{
			return file.getAbsolutePath();
		}
		
		System.out.println("File \"" + resourceName + "\" not found in paths:");
		for (int i= 0; i < paths.length; i++)
		{
			System.out.println("      \"" + paths[i] + "\"");
		}
		return null;
	}

	/////ugly convenience below this point./////

	protected String[] concatArrays(String[] a, String[] b, String[] c, String[] d)
	{
		
		int howMany= ((a == null) ? 0 : a.length) + ((b == null) ? 0 : b.length) + ((c == null) ? 0 : c.length) + ((d == null) ? 0 : d.length);
		String[] ret= new String[howMany];
		int index= 0;
		if (a != null)
		{
			for (int i= 0; i < a.length; i++)
			{
				ret[index++]= a[i];
			}
		}
		if (b != null)
		{
			for (int i= 0; i < b.length; i++)
			{
				ret[index++]= b[i];
			}
		}
		if (c != null)
		{
			for (int i= 0; i < c.length; i++)
			{
				ret[index++]= c[i];
			}
		}
		if (d != null)
		{
			for (int i= 0; i < d.length; i++)
			{
				ret[index++]= d[i];
			}
		}
		return ret;
	}

	protected String[] createArrayFromIPKey(String key)
	{
		String prop= InnardsDefaults.getProperty(key, null);
		if (prop != null && prop.length() != 0)
		{
			//System.out.println("Got property:" + prop);
			StringTokenizer st= new StringTokenizer(prop, ";");
			int howMany= st.countTokens();
			if (howMany > 0)
			{
				String[] ret= new String[howMany];
				for (int i= 0; i < howMany; i++)
				{
					ret[i]= st.nextToken();
					//System.out.println("Token:" + ret[i]);
					ret[i] = resolveEnv(ret[i]);
					if (!ret[i].endsWith("\\"))
					{
						ret[i]= ret[i] + "\\";
					}
				}
				return ret;
			}
		}
		return null;
	}
	
	public String resolveEnv(String path) {
		//Convert all the environment variables in windows to a Path
		Pattern p = Pattern.compile("%[a-zA-Z]+%");
		//System.out.println("Incoming: " + path);
		Matcher mchr = p.matcher(path);
		Map<String, String> variables = System.getenv();
		while(mchr.find()) {
			String key = mchr.group().replace('%', ' ').trim();
			//System.out.println("Resolving var: " + key);
			String value = variables.get(key);
			//System.out.println("Got Value:" + value);
			path = path.substring(0, mchr.start()) + value + path.substring(mchr.end());
			mchr = p.matcher(path);
		}
		//System.out.println("Final Path: " + path);
		return path;
	}

}

