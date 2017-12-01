package innards.namespace.loaders;

import java.io.*;
import java.lang.ref.SoftReference;
import java.util.*;

import innards.debug.Debug;

/**
   LoaderRegistry
   <p>
   this is a complete rip-off by marc of the AnimationRegistry by Aries.
   It turns out that the animationRegistry stuff has a more general and
   abstract use, therefore it gets removed for the greater good.
   <p>
   also, changed to use soft references - so that cached things can be gc'd
   <p>
   todo - .py loader that loads things. change the verbs to use this. introduce
   some global factories for this purpose.
   <p>
   write a perl script that the goes through and replaces blank lines in 
   javadoc comments with p's ...
   <p>
   below is the original comment:
   <p>
   AnimationRegistry is the central location where animation data is
   kept and loaded.  It serves as a repository and is smart enough to
   hand back a handle to a previously loaded animation if one already
   has been loaded to avoid reloading many animations and keeping
   memory lower.  
   <p>
   Ideally, it will know about various animation file types and will
   always return the animation in our internal form.  Also, it will
   know how to serialize out loaded animations so that they can be
   loaded much more quickly after they are parsed from ascii once.
   <p>
   One issue with this class is that it could get GC'd, since it is
   purely static, which would suck serious ass.  Don't know how to
   deal with that.
   <p>
   @author marc, Michael Patrick Johnson <aries@media.mit.edu>
  */
public class LoaderRegistry
{

	private static final String serialize_suffix= ".banim";
	// here we create some loaders and shove em in

	private static Map registry;
	private static Map loaders;

	static {
		registry= new HashMap();
		loaders= new HashMap();
		registerLoader(new SerializeLoader(), serialize_suffix);
	}

	/** A simple loader for the inner class. */
	public static class SerializeLoader implements Loader
	{
		//    public SerializeLoader() { }

		public Object load(String filename) throws FileNotFoundException, IOException, UnknownFileFormatException
		{
			ObjectInputStream is= new ObjectInputStream(new BufferedInputStream(new FileInputStream(filename)));
			try
			{
				Object anim= is.readObject();
				return anim;
			}
			catch (ClassNotFoundException e)
			{
				Debug.doReport("loader", "Class not found error loading:" + filename);
				return null;
			}
			finally
			{
				is.close();
			}
		}

		public void save(Object anim, String filename) throws IOException
		{
			if (!filename.endsWith(serialize_suffix))
			{
				filename= filename + serialize_suffix;
				ObjectOutputStream os= new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(filename)));
				os.writeObject(anim);
				os.close();
			}
		}
	}

	/** Simple inner class for unknown formats */
	public static class UnknownFileFormatException extends Exception
	{
		public UnknownFileFormatException(String msg)
		{
			super(msg);
		}
	}

	public interface Loader
	{
		public Object load(String filename) throws FileNotFoundException, IOException, UnknownFileFormatException;
	}

	/**
	  equivilent to load(filename,false,false)
	  */
	public static Object load(String filename) throws UnknownFileFormatException, FileNotFoundException, IOException
	{
		return load(filename, false, false);
	}
	/** 
	    Load the Object from a file with the given name and return
	    it.  Will return a cached copy if it was already loaded for
	    efficiency.  
	          
	    @exception UnknownFileFormatException if the file type is
	    unknown.
	    @param returnCachedCopy - true if it is ok to return a previously cached (and previously returned) object
	    @param saveCached - true if we should cache away a reference to it
	
	
	      // fixme somebody has to call setImmutable recursive on loaded animations
	*/

	public static Object load(String filename, boolean returnCached, boolean saveCached)
		throws UnknownFileFormatException, FileNotFoundException, IOException
	{
		Debug.doReport("loader", "loader registry trying to load <" + filename + ">");
		if (returnCached)
		{
			Object o= getPreviouslyLoaded(filename);
			if (o != null)
			{
				Debug.doReport("loader", " previously loaded <" + o + ">");
				return o;
			}

		}

		String suffix= getSuffix(filename);
		Loader loader= getLoaderForFileType(suffix);
		if (loader == null)
			throw new UnknownFileFormatException("Don't have a registered loader with file type (suffix) of " + suffix);

		Object anim= loader.load(filename);
		//anim.setImmutableRecursive();
		if (saveCached)
			remember(filename, anim);

		Debug.doReport("loader", " returning <" + anim + ">");
		return anim;
	}

	/** 
	    register the loader for the specified suffix (with . please)
	    For example, registerLoader(MyDirectXLoader, ".x");
	 */
	public static void registerLoader(Loader loader, String suffix)
	{
		loaders.put(suffix, loader);
	}

	/** 
			register the loader for the specified suffix (with . please)
			For example, registerLoader(MyDirectXLoader, ".x");
		 */
	public static void registerLoader(Loader loader, String[] suffix)
	{
		for (int i= 0; i < suffix.length; i++)
			loaders.put(suffix[i], loader);
	}

	public static boolean alreadyLoaded(String filename)
	{
		try
		{
			File file= new File(filename);
			String unique_filename= file.getCanonicalPath();
			return (registry.containsKey(unique_filename));
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return false;
		}
	}

	public static Object getPreviouslyLoaded(String filename)
	{
		try
		{
			File file= new File(filename);
			String unique_filename= file.getCanonicalPath();
			Object o= registry.get(unique_filename);
			if (o == null)
				return null;
			return ((SoftReference) o).get();
		}
		catch (IOException e)
		{
			return null;
		}
	}

	/** 
	    return the registered loader for the file type, or null if
	    none. 
	*/
	private static Loader getLoaderForFileType(String suffix)
	{
		Loader loader= (Loader) loaders.get(suffix);
		return loader;
	}

	/** get the suffix including the dot. */
	private static String getSuffix(String s)
	{
		return s.substring(s.lastIndexOf('.'));
	}

	private static void remember(String filename, Object anim)
	{
		try
		{
			File file= new File(filename);
			String key= file.getCanonicalPath();
			registry.put(key, new SoftReference(anim));
		}
		catch (IOException e)
		{
			Debug.doAssert(false, "This should never happen." + e);
		}
	}

	public static void main(String[] argh)
	{

	}
}
