package laser.ddg.query;

import java.awt.Component;

import laser.ddg.persist.JenaLoader;

/**
 * The interface that classes should implement to perform queries on 
 * persistent DDGs
 * 
 * @author Barbara Lerner
 * @version Jul 30, 2012
 *
 */
public interface Query {
	/**
	 * Return the string that should appear in the query menu
	 * @return  the string that should appear in the query menu
	 */
	public String getMenuItem();
	
	/**
	 * Execute the query
	 * @param dbLoader the object that can read from the database
	 * @param processName the name of the process being queried
	 * @param timestamp the timestamp of the ddg being queried
	 * @param visualization
	 */
	public void performQuery(JenaLoader dbLoader, String processName, String timestamp, Component visualization);

	/**
	 * Add a listener to be notified when a query completes.
	 * @param listener the object to notify
	 */
	public void addQueryListener(QueryListener listener);
}
