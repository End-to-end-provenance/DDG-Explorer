package laser.ddg.persist;

import laser.ddg.DataInstanceNode;
import laser.ddg.ProcedureInstanceNode;
import laser.ddg.ProvenanceData;

/** 
 * The interface that all classes that implement a specific DB technology to write to a database 
 * should implement.
 * @author Barbara Lerner
 * @version Oct 16, 2013
 *
 */
public interface DBWriter {
	/**
	 * Output the contents of the database to standard output.
	 */
	public abstract void printDBContents();

	/**
	 * Persist an entire DDG.
	 * @param provData the ddg to persist
	 */
	public abstract void persistDDG(ProvenanceData provData);

	/**
	 * Persist an output edge
	 * @param pin the producer of the data
	 * @param din the data produced
	 */
	public abstract void persistOutputEdge(ProcedureInstanceNode pin, DataInstanceNode din);

	/**
	 * Persists an input edge
	 * @param pin the consumer of the data
	 * @param din the data consumed
	 */
	public abstract void persistInputEdge(ProcedureInstanceNode pin, DataInstanceNode din);

	/**
	 * Persist a control flow edge
	 * @param predecessor the the first node to execute
	 * @param successor the second node to execute
	 */
	public abstract void persistSuccessorEdge(ProcedureInstanceNode predecessor, ProcedureInstanceNode successor);

	/**
	 * Persist a procedure node
	 * @param sin the node
	 */
	public abstract void persistSin(ProcedureInstanceNode sin);

	/**
	 * Persist a data node
	 * @param din the node
	 */
	public abstract void persistDin(DataInstanceNode din);

}