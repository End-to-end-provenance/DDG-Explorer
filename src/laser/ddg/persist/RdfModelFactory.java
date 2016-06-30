package laser.ddg.persist;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.tdb.TDBFactory;

/**
 * sets up the unique database directory for the ddg about to be made
 * persistent
 *
 * @author Sophia.
 *         Created Jan 4, 2012.
 */
public class RdfModelFactory {
	/** The default location for all the Jena databases. */
	public static final String JENA_DIRECTORY
		= FileUtil.DDG_DIRECTORY + "jena" + File.separatorChar;
	
	private static Map<String,Dataset> datasetMap = new HashMap<>();
	
	// Nobody should try to create an instance of this class.
	private RdfModelFactory() {
		
	}
	
	/**
     * Gets the default dataset from the given directory.  Creates the database if it does not exist yet.
     * @param directory The directory where the DDG should be stored
     * @return The Jena dataset to add data to.  Returns null if the database 
     *   cannot be created.
     */
    public static Dataset getDataset(String directory){
    	Dataset dataset = datasetMap.get(directory);
    	if (dataset != null) {
    		return dataset;
    	}
    	
    	File jenaDir = new File(JENA_DIRECTORY);
    	if (!jenaDir.exists()) {
    		File ddgDir = new File(FileUtil.DDG_DIRECTORY);
    		if (!ddgDir.exists()) {
    			if (!ddgDir.mkdir()) {
        			System.out.println ("Unable to create ddg directory");
        			return null;
    			}
    		}
    		if (!jenaDir.mkdir()) {
    			System.out.println ("Unable to create jena directory");
    			return null;
    		}
    	}
    	else if (!jenaDir.isDirectory()) {
    		System.out.println ("Unable to create jena directory");
    		return null;
    	}
    	else if (!jenaDir.canWrite()) {
    		System.out.println ("Unable to write to jena directory");
    		return null;
    	}
    	
    	System.out.println("Creating model in directory " + directory);
    	
    	System.out.println("Creating Jena directory " + directory);
    	dataset = TDBFactory.createDataset(directory);
    	datasetMap.put(directory, dataset);
    	return dataset;
    }

    /**
     * Connects to a TDB database.  If the database does not exist, it is created.
     * 
     * @return The Jena dataset to add data to.  Returns null if the database 
     *   cannot be created.
     */
    public static Dataset getDataset() {
        return getDataset(JENA_DIRECTORY);
        
    }

}
