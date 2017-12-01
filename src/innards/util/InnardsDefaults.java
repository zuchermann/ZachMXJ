package innards.util;

import java.util.*;
import java.util.regex.Pattern;
import java.util.Map.Entry;
import java.io.*;

import innards.math.linalg.Vec3;
import innards.math.linalg.Vec2;

/**
 * A mechanism to store simple preferences or pass arguments to your software.  These can be entered directly into the
 * persistent preference file, entered as command line arguments, and even set from inside the code.  Command line
 * arguments affect the current run but do not persist.  Setting from within the code persists between runs unless
 * the optional "temporary" argument is provided.<br><br>
 *
 * This class gets the location of the file to load/store preferences from the {@link ResourceLocator#getPathForUserPreferencesDirectory()}.<br><br>
 *
 * Apart from standard gettings/setting properties, there are 2 special functions.  One is to copy data from the
 * default-defaults file to the users' preferences if they are absent from there (usually this happens during
 * the first run on a new computer, see {@link #copyDefaultDefaultsToUserDefaults()}).  The other is to log all requests
 * made for particular key/value pairs to a text file (the name of which is store in InnardsDefaults.saveKeysFile) to
 * allow command line completion of possible command line arguments.
 *
 *
 */
public class InnardsDefaults
{

	static private HashMap requestedKeys;
	static private Properties userProps;
	static private Properties tempProps = new Properties();

	protected static void setupLogging(){
		Runtime.getRuntime().addShutdownHook(new ShutdownHook(){
			public void safeRun(){
				System.out.println("trying to save out used key data for InnardsDefaults.");

                //output all keys that have been requested, if "InnardsDefaults.saveKeysFile" is set
                //this is useful to collect all these parameters in one place.
                try{
                    String fileName = ResourceLocator.getPathForResource(InnardsDefaults.getProperty("InnardsDefaults.saveKeysFile"), false);
                    if(fileName != null && fileName.length() > 0){
                        int historyLength = InnardsDefaults.getIntProperty("InnardsDefaults.historyLength", 6);

                        //convert hashmap values to one element linked lists, for adding more from file.
                        HashMap requestedKeysAsLists = new HashMap();
                        {
                            Iterator ii = requestedKeys.keySet().iterator();
                            while(ii.hasNext()){
                                Object o = ii.next();
                                LinkedList ll = new LinkedList();
                                String val = (String)requestedKeys.get(o);
                                if(val != null && val.length() > 0){
                                    ll.add(requestedKeys.get(o));
                                }
                                requestedKeysAsLists.put(o, ll);
                            }
                        }

                        //read in old keys.
                        try{
                            BufferedReader br = new BufferedReader(new FileReader(fileName));
                            String s = br.readLine();
                            while(s!=null){
                                StringTokenizer st = new StringTokenizer(s, " ");
                                if(!st.hasMoreTokens())continue;
                                String key = st.nextToken();
                                LinkedList ll;
                                if(requestedKeysAsLists.containsKey(key)){
                                    ll = (LinkedList)requestedKeysAsLists.get(key);
                                }else{
                                    ll = new LinkedList();
                                    requestedKeysAsLists.put(key, ll);
                                }
                                while(st.hasMoreTokens()){
                                    String next = st.nextToken();
                                    if(!ll.contains(next)){
                                        ll.addLast(next);
                                    }
                                }
                                s = br.readLine();
                            }
                            br.close();
                        }catch(Exception ex){
                            System.out.println("Could not read old prefs file.  maybe there was none.");
                        }

                        int numKeys = requestedKeysAsLists.size();
                        String[] keys = new String[numKeys];
                        Iterator ii = requestedKeysAsLists.keySet().iterator();
                        int i = 0;
                        while(i < numKeys){
                            keys[i++] = (String)ii.next();
                        }
                        Arrays.sort(keys);
                        BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
                        for(i = 0; i < keys.length; i++){
                            bw.write(keys[i]);
                            LinkedList ll = (LinkedList)requestedKeysAsLists.get(keys[i]);
                            Iterator lli = ll.iterator();
                            int countWritten = 0;
                            while(lli.hasNext() && countWritten < historyLength){
                                String s = (String)lli.next();
                                bw.write(" "+s);
                                countWritten++;
                            }
                            bw.write("\n");
                        }
                        bw.flush();
                        bw.close();
                    }
                }catch(Exception ex){
                    System.out.println("Failed to write out requestedKeys.");
                    ex.printStackTrace();
                }

                //Now write out the preferences file itself.
                try
                {
	                if(userProps != null) {
						String prefsDir = ResourceLocator.getPathForUserPreferencesDirectory();
						if(prefsDir!=null){
							/* // easy way, unsorted:
							FileOutputStream out = new FileOutputStream(prefsDir +"PrefsList.txt");
		                    userProps.store(out, null);
	    	                out.close();
	    	                */
							
							// hard way, but with preference lines sorted in alphabetical order!
	    	                ByteArrayOutputStream out2 = new ByteArrayOutputStream();
	    	                userProps.store(out2, null);
	    	                out2.close();
	    	                
	    	                byte[] theBytes = out2.toByteArray();
	    	                ByteArrayInputStream in = new ByteArrayInputStream(theBytes);
	    	                BufferedReader reader = new BufferedReader(new InputStreamReader(in, "8859_1"));
	    	                
	    	                ArrayList<String> commentLines = new ArrayList<String>();
	    	                ArrayList<String> propertyLines = new ArrayList<String>();
	    	                String nextLine;
	    	                while ((nextLine = reader.readLine()) != null)
	    	                {
	    	                	if (nextLine.startsWith("#"))
	    	                	{
	    	                		commentLines.add(nextLine);
	    	                	}
	    	                	else
	    	                	{
	    	                		propertyLines.add(nextLine);
	    	                	}
	    	                }
	    	                reader.close();
	    	                
	    	                Collections.sort(propertyLines, String.CASE_INSENSITIVE_ORDER);

							FileOutputStream out3 = new FileOutputStream(prefsDir +"PrefsList.txt");
							BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out3, "8859_1"));
							for (String commentLine : commentLines)
							{
								writer.write(commentLine);
								writer.newLine();
							}
							for (String propertyLine : propertyLines)
							{
								writer.write(propertyLine);
								writer.newLine();
							}
							writer.flush();
							writer.close();
						}
					}
                }catch(Exception ex){
                    System.out.println("Failed to write out InnardsDefaults preferences.");
                    ex.printStackTrace();
                }
            }
		});
		//ok now we need to do none of this?
		//init the resource locator to get defaults copied over, if this is our first launch ever.
		//ResourceLocator.lazyInit();
		//ResourceLocator.getPathForResource("/whatever"); //ok this is kinda messy - cannot just have everything happen in lazyinit because init must complete for InnardsDefaults to get path to property file before we can copy in defaults, which happens in the lazy init of the os-specific resource locator.
		//maybe it would be cleaner to copy default-defaults into innards defaults with a function here, in innards defaults?

		String fn = ResourceLocator.getPathForResource(InnardsDefaults.getProperty("InnardsDefaults.saveKeysFile"), false);
        if(fn!=null && !fn.equals("")){
            requestedKeys = new HashMap();
        }

	}

	/**
	 * This mechanism is designed to copy preset defaults to InnardsDefaults the first time the software is run on a new machine.
	 * This allows standalone apps to deploy on random machines without needing to either
	 * copy in a preference file manually or modify all sorts of InnardsDefaults calls to
	 * specify appropriate default values for that application.
	 *
	 * See {@link ResourceLocator#getPathForDefaultDefaults()} to see how default defaults are located.
	 *
	 */
	protected static void copyDefaultDefaultsToUserDefaults(){
		String defaultDefaultsPath = ResourceLocator.getPathForDefaultDefaults();
		if(defaultDefaultsPath==null){
			System.out.println("InnardsDefaults: will not copy DefaultDefaults to InnardsDefaults since ResourceLocator couldn't find it");
			return;
		}
		FileInputStream fileStream;
		try{
			fileStream = new FileInputStream(defaultDefaultsPath);
		}catch(FileNotFoundException e){
			System.out.println("InnardsDefaults: Default-defaults file not found at \""+defaultDefaultsPath+"\".  This shouldn't happen since we just checked the path");
			e.printStackTrace();
			return;
		}

		Properties defaultDefaults = new Properties();
		try{
			defaultDefaults.load(fileStream);
		} catch(IOException e){
			System.out.println("InnardsDefaults: Error loading in default-defaults from existing file path:"+defaultDefaultsPath+", mabye it is not formatted correctly:");
			e.printStackTrace();
			defaultDefaults = null;
			return;
		}

		System.out.println("InnardsDefaults: loading default-defaults from "+defaultDefaultsPath);

		for(Entry defaultDefaultPair : defaultDefaults.entrySet()){
			String key = defaultDefaultPair.getKey().toString();
			if(InnardsDefaults.getProperty(key, null) == null){
				System.out.println("Adding User Default "+key+" from defaults since it does not exist!");
				String value = defaultDefaultPair.getValue().toString();
			
				value = ResourceLocator.osSpecificResourceLocator.resolveEnv(value);
				System.out.println("\t"+key+"->");
				System.out.println("\t\t"+value+"\n");
				InnardsDefaults.setProperty(key, value);
			}
		}
	}



	static public void initProps() {
		userProps = new Properties();
        try {
			String userPrefsFolder = ResourceLocator.getPathForUserPreferencesDirectory();
			if(userPrefsFolder!=null){
				File f = new File(userPrefsFolder+"PrefsList.txt");
				if(!f.exists()){
					boolean createdFile = f.createNewFile();
					if(!createdFile){
						throw new Error("Innards Defaults Failed to maike PrefsList.txt at \""+userPrefsFolder+"\"");
					}
				}
				FileInputStream in = new FileInputStream(f);
				userProps.load(in);
			}else{
				throw new Error("Innards Defaults Failed to maike PrefsList.txt, got null from ResourceLocator");
			}
        } catch (IOException e) {
            e.printStackTrace();
			throw new Error("Failed to init InnardsDefaults"); //pass it on?
		}
		copyDefaultDefaultsToUserDefaults();
		setupLogging();
	}

    static public String getProperty(String key, String def)
	{
        if(userProps == null) initProps();
        
		String value;
        if(tempProps.containsKey(key)) {
            value = tempProps.getProperty(key);
        }else{
        	value = userProps.getProperty(key, def);        	
		}

		if(requestedKeys != null && !requestedKeys.containsKey(key)){
			requestedKeys.put(key, value);
		}

        return value;
	}

	static public String getProperty(String key)
	{
		return getProperty(key, "");
	}

	/**
	 * setting null value removes the key, as properties cannot have null values
	 * setting null with setTemporarily==true will not behave as expected, as later gets will then skip temp and get from perm
	 * @param property
	 * @param value
	 * @param setTemporarily
	 */
    static public void setProperty(String property, String value, Boolean setTemporarily) {
        if(userProps == null) initProps();

        if(!setTemporarily){
			if(value != null){
				userProps.setProperty(property, value);
			}else{
				userProps.remove(property);
			}
		}

		if(value!=null){
			tempProps.setProperty(property, value);
		}else{
			tempProps.remove(property);
		}
	}

    static public void setProperty(String property, String value)
	{
        setProperty(property, value, false);
    }

	static public void removeProperty(String property)
	{
        if(userProps == null) initProps();

        userProps.remove(property);
        tempProps.remove(property);
    }

	/**
	    exactly the same as getProperty except this makes sure that the property it
	    returns has a trailing '/'. great for directory names, hence the name */

	static public String getDirProperty(String s)
	{
		String p= getProperty(s);

		if (p == null)
			return null;

		if (!p.endsWith("" + ResourceLocator.osSpecificResourceLocator.getPathSeparator()))
			return p + ResourceLocator.osSpecificResourceLocator.getPathSeparator();
		return p;
	}
	static public String getDirProperty(String s, String def)
	{
		String p= getProperty(s,def);

		if (p == null)
			return null;

		if (!p.endsWith("" + ResourceLocator.osSpecificResourceLocator.getPathSeparator()))
			return p + ResourceLocator.osSpecificResourceLocator.getPathSeparator();
		return p;
	}
	static public void setIntProperty(String s, int def)
	{
		setProperty(s, ""+def);
	}
	static public int getIntProperty(String s, int def)
	{
		String p= getProperty(s,""+def);

		int ret= def;
		try
		{
			ret= Integer.parseInt(p);
		}
		catch (Exception ex)
		{
		}
		return ret;
	}
	static public double getDoubleProperty(String s, double def)
	{
		String p= getProperty(s,""+def);

		double ret= def;
		try
		{
			ret= Double.parseDouble(p);
		}
		catch (Exception ex)
		{
		}
		return ret;
	}
	static public void setDoubleProperty(String s, double d){
		setProperty(s, ""+d);
	}

	static public void setBooleanProperty(String s, boolean def)
	{
		setProperty(s, "" + def);
	}
	static public boolean getBooleanProperty(String s, boolean def)
	{
		String p= getProperty(s, def?"true":"false");
		//if (p == null)
		//	return def;
		return p.equalsIgnoreCase("true");
	}

    static public void setVec3Property(String s, Vec3 v) {
        setProperty(s, "("+v.x()+","+v.y()+","+v.z()+")");
    }

    static public Vec3 getVec3Property(String s, Vec3 def) {

        String p = getProperty(s);
        if (p.equals(""))
            return def;

        Vec3 rv = new Vec3();

        StringTokenizer tok = new StringTokenizer(p, "(,) ");
        if(tok.countTokens()!=3){
            System.out.println("Vec3 property should be formatted like (0,0,40)");
        }else{
            for(int i = 0; i < 3; i++){
                String t = null;
                try{
                    t = tok.nextToken();
                    rv.set(i, Float.parseFloat(t));
                }catch(Exception ex){
                    System.out.println("could not parse lookat to ints.  tok:<"+t+">  ex:"+ex);
                    return def;
                }
            }
        }

        return rv;
    }

    static public void setVec2Property(String s, Vec2 v) {
        setProperty(s, "("+v.x()+","+v.y()+")");
    }

    static public Vec2 getVec2Property(String s, Vec2 def) {

        String p = getProperty(s);
        if (p.equals(""))
            return def;

        Vec2 rv = new Vec2();

        StringTokenizer tok = new StringTokenizer(p, "(,) ");
        if(tok.countTokens()!=2){
            System.out.println("Vec2 property should be formatted like (0,40)");
        }else{
            for(int i = 0; i < 2; i++){
                String t = null;
                try{
                    t = tok.nextToken();
                    rv.set(i, Float.parseFloat(t));
                }catch(Exception ex){
                    System.out.println("could not parse lookat to ints.  tok:<"+t+">  ex:"+ex);
                    return def;
                }
            }
        }

        return rv;
    }

}
