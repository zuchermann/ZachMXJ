package innards.namespace.context;

import java.io.*;
import java.util.*;

/**
	tools for putting parts of boo into and out of disk
	*/
public class ContextTreePersistance
{

	/**
		a list of things that you want to save (or load)
		*/
	static public class BooDirectory
		implements Serializable
	{
		List keys = new ArrayList();
		String root;

		/**
			root can be null, or it can include the : character

			CURRENTLY IGNORED
			*/
		public BooDirectory(String root)
		{
			this.root = root;
		}

		/**
			this can (and usually will be) a string with / in it
			*/
		public void add(Object key)
		{
			keys.add(key);
		}

		public void add(String[] s)
		{
			for(int i=0;i<s.length;i++) add(s[i]);
		}

		/**
			adds everything here downward*/
		public void addAll()
		{
			Object start = ContextTree.where();
			_addAll("");
			ContextTree.go(start);
		}

		protected void _addAll(String s)
		{
			s=s+"/";
			Iterator it = ContextTree.getValueIterator();
			while(it.hasNext())
			{
				Map.Entry e = (Map.Entry)it.next();
				String key = (String)e.getKey();
				add(s+key);
			}
			it = ContextTree.getDirIterator();
			while(it.hasNext())
			{
				ContextTree.Bobj b = (ContextTree.Bobj)it.next();
				_addAll(s+b.getName());
			}
		}

		public void save(ObjectOutputStream oos) throws IOException
		{
			for(int i=0;i<keys.size();i++)
			{
				Object k = keys.get(i);
				Object v = ContextTree.getWithDirectory((String)k);
				oos.writeObject(k);
				oos.writeObject(v);
				System.out.println("saved: "+k+"="+v);
			}
			oos.writeObject(new EODirMarker());
		}

		public boolean skip(ObjectInputStream oos) throws IOException, ClassNotFoundException
		{
			Object o = null;
			while(!(o instanceof EODirMarker))
			{
				if (oos.available()<=0) return false;
				o = oos.readObject();
			}
			return true;
		}

		public void load(ObjectInputStream oos) throws IOException, ClassNotFoundException
		{
			Object o = null;
			while(true)
			{
				System.out.println(" available is <"+oos.available()+">");
				//if (oos.available()<=0) return;
				o = oos.readObject();

				if (!(o instanceof EODirMarker))
				{
					Object v = oos.readObject();
					// now set them
					String key = (String)o;
					// strip off the tail end
					Object restore = ContextTree.where();
					int i = key.lastIndexOf("/");
					String to = "nowhere";
					if (i!=-1)
					{
						to = key.substring(0, i);
						key = key.substring(i+1, key.length());
						ContextTree.cd(to);
					}
					ContextTree.set(key, v);

					if (i!=-1) ContextTree.go(restore);

					System.out.println("loaded: "+to+" "+key+"="+v);
				}
				else
				{
					return;
				}
			}
		}

		// prefix should include a '/' character

		public void load(String prefix, ObjectInputStream oos) throws IOException, ClassNotFoundException
		{
			Object o = null;
			while(true)
			{
				System.out.println(" available is <"+oos.available()+">");
				//if (oos.available()<=0) return;
				o = oos.readObject();

				if (!(o instanceof EODirMarker))
				{
					Object v = oos.readObject();
					// now set them
					String key = (String)o;
					// strip off the tail end
					Object restore = ContextTree.where();
					int i = key.lastIndexOf("/");
					String to = key.substring(0, i);
					key = key.substring(i+1, key.length());
					ContextTree.cd(prefix+to);
					ContextTree.set(key, v);
					ContextTree.go(restore);

					System.out.println("loaded: ("+prefix+")+"+to+" "+key+"="+v);
				}
				else
				{
					return;
				}
			}
		}

		// returns a hashmap of attribute names vs values

		public HashMap capture(ObjectInputStream oos) throws IOException, ClassNotFoundException
		{
			HashMap ret = new HashMap();
			Object o = null;
			while(true)
			{
				//System.out.println(" available is <"+oos.available()+">");
				//if (oos.available()<=0) return;
				o = oos.readObject();

				if (!(o instanceof EODirMarker))
				{
					Object v = oos.readObject();
					// now set them
					String key = (String)o;
					// strip off the tail end
					Object restore = ContextTree.where();
					int i = key.lastIndexOf("/");
					//String to = key.substring(0, i);
					//key = key.substring(i+1, key.length());
					ret.put(key,v);

					/*Boo.cd(to);
					Boo.set(key, v);
					Boo.go(restore);
*/
					//System.out.println("loaded(captured): "+key+"="+v);
				}
				else
				{
					return ret;
				}
			}
		}

		public void save(String file)
		{
			try{
				ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(file)));
				save(oos);
				oos.close();
			}
			catch(Exception ex)
			{
				System.err.println(" problem saving boo tree");
				ex.printStackTrace();
			}
		}

		public void load(String file)
		{
			try{
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(file)));
				load(ois);
				ois.close();
			}
			catch(Exception ex)
			{
				System.err.println(" problem loading boo tree");
				ex.printStackTrace();
			}
		}

	}

	static public class EODirMarker
		implements Serializable
	{}

	// test this sucker

	static public void main(String[] s)
	{
		ContextTreePersistance.BooDirectory dir = new ContextTreePersistance.BooDirectory("");

		dir.add(new String[]
			{
				"one/two/three/value1",
				"one/two/three/value2",
				"one/two/three/value3",
				"one/two/value4"
			});

	    System.out.println(" ba!");
		ContextTree.begin("one");
			ContextTree.begin("two");
				ContextTree.begin("three");
				ContextTree.set("value1", "is1");
				ContextTree.set("value2", "is2");
				ContextTree.set("value3", "is3");
				ContextTree.end("three");
			ContextTree.set("value4", "is4");
			ContextTree.end("two");
		ContextTree.end("one");
		ContextTree.begin("one");
			ContextTree.begin("two");
			System.out.println(" value 4 = "+ContextTree.get("value4"));
				ContextTree.begin("three");
				System.out.println(" pwd = "+ContextTree.pwd());
				System.out.println(" dir = "+ContextTree.dir());
				System.out.println(" value 1 = "+ContextTree.get("value1"));
				System.out.println(" value 2 = "+ContextTree.get("value2"));
				System.out.println(" value 3 = "+ContextTree.get("value3"));
				System.out.println(" value 4 = "+ContextTree.get("value4"));
				ContextTree.end("three");
			System.out.println(" value 4 = "+ContextTree.get("value4"));
			ContextTree.end("two");
		ContextTree.end("one");

		dir.save("c:/temp/testBooPersistance.objects");
		//for(int i=0;i<1000;i++)
		{
			dir.load("c:/temp/testBooPersistance.objects");
		//	System.gc();
		//	System.out.println(Runtime.getRuntime().freeMemory());
		}


		ContextTree.begin("one");
			ContextTree.begin("two");
			System.out.println(" value 4 = "+ContextTree.get("value4"));
				ContextTree.begin("three");
				System.out.println(" pwd = "+ContextTree.pwd());
				System.out.println(" dir = "+ContextTree.dir());
				System.out.println(" value 1 = "+ContextTree.get("value1"));
				System.out.println(" value 2 = "+ContextTree.get("value2"));
				System.out.println(" value 3 = "+ContextTree.get("value3"));
				System.out.println(" value 4 = "+ContextTree.get("value4"));
				ContextTree.end("three");
			System.out.println(" value 4 = "+ContextTree.get("value4"));
			ContextTree.end("two");
		ContextTree.end("one");


	}

}
