package laser.ddg.query;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

/**
 * Manages the listeners for Query classes.  A listener is notified when 
 * a query is complete.
 */
public abstract class AbstractQuery implements Query {

	private List<QueryListener> listeners = new ArrayList<>();
	
	/**
	 * The programming language that the provenance is for
	 */
	protected String language;
	
	@Override
	public void addQueryListener(QueryListener listener) {
		listeners.add(listener);
	}

	/**
	 * Notify the listeners that the query results are ready
	 * @param name title for the results
	 * @param panel the panel the results should be displayed in
	 */
	protected void notifyQueryFinished(String name, JComponent panel) {
    	listeners.stream().forEach((l) -> {
        	l.queryFinished(name, panel);
        });
	}
	
	@Override
	public void setLanguage(String language) {
		this.language = language;
	}

}