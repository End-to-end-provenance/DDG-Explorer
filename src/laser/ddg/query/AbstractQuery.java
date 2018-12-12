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
	
	protected String language;
	
	public AbstractQuery() {
		super();
	}

	@Override
	public void addQueryListener(QueryListener listener) {
		listeners.add(listener);
	}

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