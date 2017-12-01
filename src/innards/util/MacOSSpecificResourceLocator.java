package innards.util;
//
//	MacOSSpecificResourceLocator.java
//	characters
//
//	Created by jg on Wed Feb 19 2003.
//	Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//


import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

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

public class MacOSSpecificResourceLocator implements ResourceLocator.OSSpecificResourceLocator
{
	public static final String RESOURCE_SEARCH_PATH= "ResourceSearchPath";
	public static final String NIB_SEARCH_PATH= "NibSearchPath";
	public static final String ANY_LIBRARY_SEARCH_PATH= "AllLibSearchPath";
	public static final String JNILIB_SEARCH_PATH= "JNILibSearchPath";
	public static final String DYLIB_SEARCH_PATH= "DYLIBSearchPath";
	public static final String BUNDLE_SEARCH_PATH= "BundleSearchPath";
	public static final String CONTENT_ROOT= "ContentRootPath";
	public char getPathSeparator() {return '/';}
	//******system properties (ie -D on the command line)******
	//bootstrap path finding by setting path to default defaults.  this must be set to find file in jar (start with @!), or if you're putting it nonstandard (see class comment)
	public static final String PATH_TO_DEFAULT_DEFAULTS = "path.to.defaultdefaults";
	//application id.  used for user-prefs folder name, maybe for other things later.  most launchables will just use default, override for deployment
	public static final String APPLICATION_IDENTIFIER = "application.id";
	//*********************************************************

	public static final String DEFAULT_APPLICATION_IDENTIFIER = "edu.mit.media.robotic.c5m";

	//search these paths for default-defaults if no PATH_TO_DEFAULT_DEFAULTS is set.
	public static final String RELATIVE_PATH_TO_DEFAULT_DEFAULTS = "./resources/preferences/defaults.txt";
	public static final String ABSOLUTE_PATH_TO_DEFAULT_DEFAULTS = "~/projects/c5m/resources/preferences/defaults.txt";

//	public static boolean LOOK_IN_MAIN_BUNDLE_FIRST= true;

	//good to set this to true to test if everything is built into the
	//     app properly
//	public static boolean LOOK_ONLY_IN_MAIN_BUNDLE= false;

	protected String[] anyResources, nibs, anyLibs, jniLibs, dylibs, bundles;

	protected String[] pathsForNib, pathsForJNILib, pathsForDylib, pathsForBundle, pathsForOther;

	protected String contentRootPath;

	public MacOSSpecificResourceLocator()
	{
		//copyCVSDefaultsToUserDefaults();
		//lazyInit();
	}


/*
	*/
/**
	 * This mechanism is designed to copy preset defaults to the userdefaults the first time the software is run on a new machine.
	 * This allows standalone apps to deploy on random machines without needing to either
	 * copy in a preference file or modify all sorts of InnardsDefaults calls to
	 * specify appropriate default values for that application.
	 *
	 * if system property {@link #PATH_TO_DEFAULT_DEFAULTS PATH_TO_DEFAULT_DEFAULTS} (-Dpath.to.defaultdefaults=blah on the command line) is set, we will look only there.
	 *
	 * Otherwise, this function will look in {@link #RELATIVE_PATH_TO_DEFAULT_DEFAULTS RELATIVE_PATH_TO_DEFAULT_DEFAULTS}, (software must be run from c5m directory for this to work).
	 * If this fails, it will look in {@link #ABSOLUTE_PATH_TO_DEFAULT_DEFAULTS ABSOLUTE_PATH_TO_DEFAULT_DEFAULTS}.
	 *
	 * for jar based deployments, you must set the path.to.defaults to the path within the jar, starting with "@" meaning root of jar
	 */
/*
	protected void copyCVSDefaultsToUserDefaults(){
		//TODO: make sure this also can load the defaults from inside jar and fix if not.
		//this mechanism will help with building standalone apps without modifying defaults code.

		Properties defaultDefaults = null;

		String specifiedPathToDefaults = System.getProperty(PATH_TO_DEFAULT_DEFAULTS);

		InputStream fileStream = null;

		//try loading relatively, as from jar:
		if(specifiedPathToDefaults!=null && specifiedPathToDefaults.startsWith("@")){
			specifiedPathToDefaults = "/"+specifiedPathToDefaults.substring(1, specifiedPathToDefaults.length());
			fileStream = this.getClass().getResourceAsStream(specifiedPathToDefaults);
			if(fileStream == null){
				System.out.println("ResourceLocator: Cannot load default-defaults resource from specified path \""+specifiedPathToDefaults+"\" in jar, maybe we're not in one.");
				return;
			}
		}

		if(fileStream == null){
			//try regular files and default paths.
			File fileToLoad = null;

			if(specifiedPathToDefaults != null){
				fileToLoad = new File(specifiedPathToDefaults);
				if(!fileToLoad.exists()){
					System.out.println("ResourceLocator: Cannot load default-defaults, property specified in \"path.to.defaultdefaults\" ("+specifiedPathToDefaults+") doesn't lead to a file.");
					fileToLoad = null;
					return;
				}
			}else{
				fileToLoad = new File(RELATIVE_PATH_TO_DEFAULT_DEFAULTS);
				if(!fileToLoad.exists()){
					System.out.println("ResourceLocator:  relative path to default-defaults ("+RELATIVE_PATH_TO_DEFAULT_DEFAULTS+") does not lead to a file, trying absolute path");
					fileToLoad = null;
					fileToLoad = new File(expandPath(ABSOLUTE_PATH_TO_DEFAULT_DEFAULTS));
					if(!fileToLoad.exists()){
						System.out.println("ResourceLocator:  absolute path to default-defaults ("+ABSOLUTE_PATH_TO_DEFAULT_DEFAULTS+") does not lead to a file, relative and absolute methods failed, will not check default-defaults");
						fileToLoad = null;
						return;
					}
				}
			}

			try{
				fileStream = new FileInputStream(fileToLoad);
			}catch(FileNotFoundException e){
				System.out.println("ResourceLocator: Default-defaults file not found at \""+fileToLoad.getAbsoluteFile()+"\".  This shouldn't happen since we just checked the path");
				e.printStackTrace();
				return;
			}
		}


		defaultDefaults = new Properties();
		try{
			defaultDefaults.load(fileStream);
		} catch(IOException e){
			System.out.println("ResourceLocator: Error loading in default-defaults from existing file stream:"+fileStream.toString()+", mabye it is not formatted correctly:");
			e.printStackTrace();
			defaultDefaults = null;
			return;
		}

		System.out.println("ResourceLocator: loading default-defaults from "+fileStream);

		for(Entry defaultDefaultPair : defaultDefaults.entrySet()){
			String key = defaultDefaultPair.getKey().toString();
			if(InnardsDefaults.getProperty(key, null) == null){
				System.out.println("Adding User Default "+key+" from defaults since it does not exist!");
				String value = defaultDefaultPair.getValue().toString();
				//~'s to `pwd`
				Pattern p = Pattern.compile("~");
				value = p.matcher(value).replaceAll(System.getProperty("user.home"));
				System.out.println("\t"+key+"->");
				System.out.println("\t\t"+value+"\n");
				InnardsDefaults.setProperty(key, value);
			}
		}
	}
*/

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
			pathsForNib= concatArrays(nibs, anyResources, null, null);
			pathsForJNILib= concatArrays(jniLibs, anyLibs, anyResources, null);
			pathsForDylib= concatArrays(dylibs, anyLibs, anyResources, null);
			pathsForBundle = concatArrays(bundles, anyResources, null, null);
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

		//try loading relatively, as from jar:
//		if(specifiedPathToDefaults!=null && specifiedPathToDefaults.startsWith("@")){
//			specifiedPathToDefaults = "/"+specifiedPathToDefaults.substring(1, specifiedPathToDefaults.length());
//			URL url = this.getClass().getResource(specifiedPathToDefaults);
//			if(url == null){
//				System.out.println("ResourceLocator: Cannot find default-defaults resource from specified path \""+specifiedPathToDefaults+"\" in jar, maybe we're not in one.");
//				return null;
//			}
//		}

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
		String path = "~/Library/Preferences/"+applicationID+"/";
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

	public String getPathForResource(String resource){
		return getPathForResource(resource, true);
	}

	/**
	 * pass in full name, ie "bob.nib" or "libsarah.dylib", this will help choose which paths to use.
	 * if resource starts with a "/", assumed to be an absolute path. if mustExist is false, this will hallucinate
	 * a valid path if resource not found.  searching is done with paths obtained from InnardsDefaults.
	 *
	 * @param resource name of resource, should be relative to some search path.  can include directories.
	 * @param mustExist
	 * @return
	 */
	public String getPathForResource(String resource, boolean mustExist)
	{
		lazyInit();
		if(resource==null || resource.length() == 0){
			return null;
		}
		String path= null;
		resource = resolveEnv(resource);
		if(resource.startsWith("/"))
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
//		if (LOOK_IN_JAR_FIRST && path == null) //look in main bundle doesn't make sense anymore.  to look in jar, use search paths that start with @
//		{
			//TODO: cannot return real path to jarred resources - only url's or streams.  for now we will return the url path but that is not the right solution
			//path= NSBundle.mainBundle().pathForResource(resource, null);
//		}
		if (path == null /*&& !LOOK_ONLY_IN_MAIN_BUNDLE*/)
		{
			if (resource.endsWith(".nib"))
			{
				path= findResourceFromPaths(resource, pathsForNib);
			}
			else if (resource.endsWith(".jnilib"))
			{
				path= findResourceFromPaths(resource, pathsForJNILib);
			}
			else if (resource.endsWith(".dylib"))
			{
				path= findResourceFromPaths(resource, pathsForDylib);
			}
			else if (resource.endsWith(".so"))
			{
				System.out.println("Using Anylibs");
				path= findResourceFromPaths(resource, anyLibs);
			}
		    else if (resource.endsWith(".bundle"))
			{
				path= findResourceFromPaths(resource, pathsForBundle);
			}
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
		String absPathToLib= getPathForResource(libraryName);
		if(absPathToLib == null || absPathToLib.length()==0 && libraryName.indexOf(".")==-1){
			//if it doesn't have a dot, maybe its the platform indep lib name and needs translation
			String sysDepName = System.mapLibraryName(libraryName);
			System.out.println("Resource Locator couldn't find \""+libraryName+"\" as is, trying instead system dep name \""+sysDepName+"\"");
			libraryName = sysDepName;
			absPathToLib = getPathForResource(libraryName);
		}
		if (absPathToLib != null && absPathToLib.length() > 0)
		{
			if (libraryName.endsWith(".jnilib"))
			{
				System.out.println("Loading jnilib \"" + absPathToLib + "\"...");
				System.load(absPathToLib);
				System.out.println("Done loading!");
			}
			else if (libraryName.endsWith(".dylib"))
			{
				System.out.println("Loading dylib \"" + absPathToLib + "\"...");
				System.out.println("DOES THIS WORK??  trying to load \""+absPathToLib+"\" using System.load instead of NSRuntime.loadLibrary");
				//NSRuntime.loadLibrary(absPathToLib);
				System.load(absPathToLib);
				System.out.println("Done loading!");
			}
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
		/*
		boolean success= false;

		String bundleName= null;
		Object owner= null;

		if (params instanceof Object[])
		{
			bundleName= (String) ((Object[]) params)[0];
			owner= ((Object[]) params)[1];
		}
		else
		{
			owner= params;
		}

		if (LOOK_IN_MAIN_BUNDLE_FIRST)
		{
			if (bundleName != null)
			{
				String foundBundle= NSBundle.mainBundle().pathForResource(bundleName, null);
				if (foundBundle != null)
				{
					// loads native code assosiated with bundle
					NSBundle.bundleWithPath(foundBundle).load();
					
					// loads what it was you were looking for
					success= NSApplication.loadNibFromBundle(NSBundle.bundleWithPath(foundBundle), name, owner);
					
					if (success)
					{
						return true;
					}
					else
					{
						//do nothing.
					}
				}
			}
			else
			{
				success= NSApplication.loadNibFromBundle(NSBundle.mainBundle(), name, owner);
				if (success)
				{
					return true;
				}
				else
				{
					//do nothing.
				}
			}
		}

		if (bundleName == null)
		{
			String absPathToLib= getPathForResource(name);
			if(absPathToLib == null){
				if(!name.endsWith(".nib")){
					//maybe it should!
					absPathToLib= getPathForResource(name+".nib");
				}
			}
			File file= new File(absPathToLib);
			bundleName= file.getParent();
		}
		else
		{
			bundleName= getPathForResource(bundleName);
		}

		if (!success && !LOOK_ONLY_IN_MAIN_BUNDLE)
		{
			if (bundleName != null && bundleName.length() > 0)
			{
				//if (name.endsWith(".nib"))
				{
					NSBundle bundle= NSBundle.bundleWithPath(bundleName);
					if (bundle == null)
					{
						System.out.println("unable to create bundle from path \"" + bundleName + "\"...");
						return false;
					}
					System.out.println("Trying to load nib \"" + name + "\"");
					System.out.println("    from NSBundle: \"" + bundle.bundlePath() + "\"");
					System.out.println("    with owner: \"" + owner + "\"...");
					bundle.load();
					success= NSApplication.loadNibFromBundle(bundle, name, owner);
					if (success)
					{
						System.out.println("Done!");
						return true;
					}
					else
					{
						System.out.println("Failed!!");
						return false;
					}
				}
				//else
				//{
				//	System.out.println("I don't know any things to do with \"" + bundleName + "\"");
				//	return false;
				//}
			}
			else
			{
				System.out.println("Resource not found; no loading attempted");
				return false;
			}
		}*/
		return false;
	}

	public String findResourceFromPaths(String resourceName, String[] paths)
	{
		if (paths == null)
		{
			System.out.println("MacOSSpecificResourceLocator has no paths to search... try putting some in innards properties.");
			return null;
		}
		
		for (int i= 0; i < paths.length; i++)
		{
//			if(paths[i].startsWith("@")){
//				//look in the jar
//				String p = "/"+paths[i].substring(1, paths[i].length());
//				if(!p.endsWith("/")){
//					p = p + "/";
//				}
//				URL url = this.getClass().getResource(p+resourceName);
//				return url.toString(); //TODO: this is just mean - they cannot load the file from this path.
//			}else{
				File file= new File(paths[i], resourceName);
				if (file.exists())
				{
					return file.getAbsolutePath();
				}
//			}
		}
		// try it just by itself
		File file = new File(resourceName);
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
			StringTokenizer st= new StringTokenizer(prop, ": ");
			int howMany= st.countTokens();
			if (howMany > 0)
			{
				String[] ret= new String[howMany];
				for (int i= 0; i < howMany; i++)
				{
					ret[i]= st.nextToken();

					ret[i] = resolveEnv(ret[i]);
					if (!ret[i].endsWith("/"))
					{
						ret[i]= ret[i] + "/";
					}
				}
				return ret;
			}
		}
		return null;
	}

	public String resolveEnv(String path) {
		
		if(path == null)return null;
//		if (path.startsWith("@"))
//		{ //its meant to be mainBundle local.
//			path= path.substring(1, path.length());
//			File file= new File(NSBundle.mainBundle().bundlePath(), path);
//			path= file.getAbsolutePath();
//		}
		//~'s to `pwd`
		if (path.contains("~"))
		{			
			Pattern p = Pattern.compile("~");
			path = p.matcher(path).replaceAll(System.getProperty("user.home"));
		}
		return path;
	}
}
