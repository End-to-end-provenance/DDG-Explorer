package laser.ddg.persist;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import laser.ddg.DataInstanceNode;
import laser.ddg.ProcedureInstanceNode;
import laser.ddg.ProvenanceData;
import laser.ddg.ProvenanceDataVisitor;
import laser.ddg.ScriptInfo;
import laser.ddg.gui.DDGExplorer;

/**
 * An abstract class containing code to assist with storing DDGs in a database, but excluding
 * any technology-specific code, which must be provided by subclasses.
 * @author Barbara Lerner
 * @version Oct 16, 2013
 *
 */
public abstract class AbstractDBWriter implements DBWriter, ProvenanceDataVisitor {

	private ProvenanceData provData;
	
	// The directory where the saved script and data files go.
	private String saveDir;

	/**
	 * Persists an entire DDG at once.  If information is added to the DDG after this is called, it will 
	 * not be persistent unless this object as added as a listener to the provenance data object.
	 * 
	 * @param provData the in-memory ddg.
	 */
	@Override
	public void persistDDG(ProvenanceData provData) {
		String processPathName = provData.getProcessName();
		String executionTimestamp = provData.getTimestamp();
		
		if (alreadyInDB(processPathName, executionTimestamp, provData.getLanguage())) {
			//Do not put anything into the database, remove the listener
			DDGExplorer.showErrMsg("Already in the database");
			return;
		}
		
		saveDir = FileUtil.getSaveDir(processPathName);

		// Copy all of the scripts used
		for (ScriptInfo script : provData.scripts()) {
			FileUtil.copyScriptFile(script);
		}
		
		this.provData = provData;
		
		// Copy the ddg.txt file (must take place AFTER setting provData)
		// System.out.println("Copying ddg.txt");
		copyFileData(provData.getSourcePath());
		
		// System.out.println("Initializing DDG in DB");
		initializeDDGinDB(provData, executionTimestamp, provData.getLanguage());
		// System.out.println("Saving DDG\n");
		saveDDG(provData);
	}
	

	/**
	 * Return true if a ddg for the given process and timestamp is already stored in the database
	 * @param processName the name of the process
	 * @param executionTimestamp the time at which the process was executed
	 * @param language the language executed to create the ddg
	 * @return true if this ddg is already stored in the database
	 */
	protected abstract boolean alreadyInDB(String processName, String executionTimestamp, String language);

	/**
	 * Creates the header information in the database for this ddg
	 * @param provData the in-memory ddg
	 * @param executionTimestamp the time the ddg was created
	 * @param language the language that the program was written in that was executed to 
	 * 		create the ddg
	 */
	protected abstract void initializeDDGinDB(ProvenanceData provData,
			String executionTimestamp, String language);

	/**
	 * Saves the nodes and edges of the DDG in the DB
	 * @param provData the DDG to save
	 */
	protected abstract void saveDDG(ProvenanceData provData);
	
	/**
	 * Add all the nodes and edges in a DDG to the database
	 * @param ddg the ddg to add
	 */
	protected void addNodesAndEdges(ProvenanceData ddg) {
		ddg.visitPins(this);
		ddg.visitDins(this);
		ddg.visitDataflowEdges(this);
		
	}

	/**
	 * Makes the node and all edges to predecessors persistent
	 */
	@Override
	public void visitPin(ProcedureInstanceNode pin) {
		persistSin(pin);
		provData.visitControlFlowEdges(pin, this);
	}

	/**
	 * Makes the node psersistent
	 */
	@Override
	public void visitDin(DataInstanceNode din) {
		String dinType = din.getType();
		
		// Copy the file to a permanent place and update the file location,
		// which is saved in the value before adding the node to the database.
		if (dinType.equals("Snapshot") || dinType.equals("File") || dinType.equals("CheckpointFile")) {
			String copyFileName = copyNodeData(din);
			din.setValue(copyFileName);
		}
		persistDin(din);
	}

	private String copyNodeData(DataInstanceNode din) {
		
		String filename = (String) din.getValue();
		return copyFileData(filename);
	}
	
	private String copyFileData(String filename){
		String executionTimestamp = provData.getTimestamp();
		File theFile = new File(filename);

		try {
			File savedFile = FileUtil.createSavedFile(saveDir, executionTimestamp, theFile);
			//ErrorLog.showErrMsg("Copying " + theFile.toPath() + " to " + savedFile.toPath() + "\n");
			Files.copy(theFile.toPath(), savedFile.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
			if (!savedFile.exists()) {
				DDGExplorer.showErrMsg("Copy failed!!!\n\n");
			}
			savedFile.setReadOnly();
			return savedFile.getAbsolutePath();
			
			//System.out.println(fileContents.toString());
		} catch (FileNotFoundException e) {
			DDGExplorer.showErrMsg("Cannot find script data file: " + filename + "\n\n");
			return null;
		} catch (IOException e) {
			DDGExplorer.showErrMsg("Exception copying script data file " + filename + " to database: " + e);
			return null;
		}
	}

	/**
	 * Makes the edge persistent.
	 */
	@Override
	public void visitControlFlowEdge(ProcedureInstanceNode predecessor,
			ProcedureInstanceNode successor) {
		persistSuccessorEdge(predecessor, successor);
	}

	/**
	 * Makes the edge persistent.
	 */
	@Override
	public void visitInputEdge(DataInstanceNode input,
			ProcedureInstanceNode consumer) {
		persistInputEdge(consumer, input);
	}

	/**
	 * Makes the edge persistent.
	 */
	@Override
	public void visitOutputEdge(ProcedureInstanceNode producer,
			DataInstanceNode output) {
		persistOutputEdge(producer, output);
	}

	/**
	 * @return the internal representation of the DDG being stored
	 */
	protected ProvenanceData getProvData() {
		return provData;
	}

}
