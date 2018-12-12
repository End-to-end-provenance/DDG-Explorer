package laser.ddg.query;

/**
 * The interface that classes should implement to perform queries on 
 * DDGs
 * 
 * @author Barbara Lerner
 * @version December 12, 2018
 *
 */
public interface Query {
	/**
	 * Add a listener to be notified when a query completes.
	 * @param listener the object to notify
	 */
	public void addQueryListener(QueryListener listener);

	/**
	 * Set the language that the provenance is for
	 * @param language the programming language of the provenance 
	 */
	void setLanguage(String language);
}
