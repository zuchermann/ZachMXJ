package innards.namespace.factory;

import java.io.*;

import innards.NamedGroup;
import innards.debug.Debug;
import innards.namespace.loaders.LoaderRegistry;
import innards.util.InnardsDefaults;

/**
    a Factory that attempts to find things on disk
    and load them in.
    <p>
    it can 'load them in' either by executing a 
    python script mechanism (filename ends with .py) 
    or by deserialization (filename ends with .object)
    or perhaps, in the future, by some other mechanisms
    - ideas include querying the animation registry to see
    if it can perhaps load it.
    <p>
    this can be augmented with a simplefactoryproduction as 
    a parent that can provide a new object from scratch
    if this fails
    <p>
    one trick is that all parameter objects that might
    use this must be of type NamedObject and the
    name of the parameter must be the name of the object
    to be created
    <p>
    one trick we need to think about - how to _not_ run 
    some preliminary setup code if this executes.
    <p>
    something like:
    <pre>
    factory.createSomething("boo","fred", new DefaultSetup()
    {
        public setup(Object o)
        {
            ...
        }
    }
    </pre>
    the default setup should only run if a _new_ object is created
    (rather than loaded from disk like this)
    <p>
    this class fails pretty quietly (only warnings printed to err. )
    <p>
    this class ought to take a builder context of some description
    to further uniqueify the names (e.g. to keep things on a 
    character basis). perhaps we can implement a widening scope
    of uniqification - regardless this should be implemented through
    the obtainFilename thing
    <p>
    see also the 'FactoryAuditor' class that can keep track of 
    what objects were created from what FactoryHelpers, under
    which identifiers, etc - this
    lets you know how to put, potentially new things, (back) into 
    an archive.
    <p>
    this class implements the tag interface ArchiveFactoryProducer
    this facilitates the magic above (if something comes from a 
    archive factory producer then you know it was loaded from disk)
    <p>
    @author marc
    */         
public class ArchiveFactoryProducer extends Factory
{
    File databaseDir;
    
    public ArchiveFactoryProducer() {
    	super("An ArchiveFactoryProducer");
    }

    public ArchiveFactoryProducer(String name) {
    	super(name);
    }
    
    /**

        defines whether or not we should cache things that we load and return
        the same one later on, or keep reloading things from scratch.
        <p>
        call setCachePolicy(true) to turn caching on
        */
    
    boolean cachingIsGood = false;
    /**
        (relative to innards properties persistance.database.root)
        looks in persistance.database.root+helper.getName();
        
        */
    
    public ArchiveFactoryProducer(Factory helper)
    {
    	this();
    	
        try{
            databaseDir = new File(InnardsDefaults.getDirProperty("persistance.database.root")+helper.getName()+"/");
        }
        catch(Exception ex)
        {
            Debug.doReport("factory", " problem accessing database directorty <"+databaseDir+">");
        }
    }
    
    public void setCachingPolicy(boolean toCacheOrNot)
    {
        cachingIsGood = toCacheOrNot;
    }
        
    public Object produce(Object identifier, Object parameters)
    {
        // munge the identifier down to a string
        final String ident = obtainFilename((NamedGroup)parameters,identifier);
        
        // todo - only accept new string that point to files with
        // later creation dates
        String[] beginsWith = databaseDir.list(new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                return name.startsWith(ident); // not exact for names with dot's in them
            }
        });
        
        if (beginsWith.length == 0) return null;
        
        for(int i=0;i<beginsWith.length;i++)
        {
            // attempt to a) work out what this file is and b) load it
            
            // this uses the LoaderRegistry
            
            Object o = null;
            
            try{
                o = LoaderRegistry.load(beginsWith[i],cachingIsGood,cachingIsGood);
            }
            catch(Exception ex){}
            
            if (o!=null) return o;            
        }
        return null;
    }

    /**
        called by factory helpers (thats why it is package scope) 
        that want to support archiveing.
        for example, if you wanted to serialize out somethign someplace
        into this archive then this will tell you under which filename
        to store it so that it will be loaded in by this thing
        <p>
        typically you can recover the identifier that created an object
        by asking the FactoryAuditor
        <p>
        you must post-pend a file suffix that uniquely defines the type
        to this.
        */
    String obtainFilename(NamedGroup theObjectToWrite, Object identifier)
    {
        return identifier.toString()+"."+((NamedGroup)theObjectToWrite).getName();
    }
    
}
