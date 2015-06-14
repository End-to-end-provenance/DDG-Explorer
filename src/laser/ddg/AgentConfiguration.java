package laser.ddg;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * The agent responsible for the operations performed on the data The
 * AgentConfiguration object can record information about the software version,
 * configuration parameters, identify a Web service or provide other information
 * about the execution environment.
 * 
 * @author B. Lerner and Sophia Taskova
 */
public class AgentConfiguration {
	// The name of the agent
	private String agentName;

	// The parameters given to the agent when the agent started
	// execution.
	private Map<String, String> configParameters;

	// Version information about the agent
	private String version;

	// Any additional information. This is saved as an attribute
	// name-value pair so anything can be saved. With experience, we may
	// have a better idea of what we need and then can save more in a
	// typed way.
	private Map<String, Object> additionalInfo;

	/**
	 * Counter of agents created so far. Used to assign unique ids.
	 */
	private static int agentCounter = 0;

	/**
	 * The unique id for this agent.
	 */
	private int agentId;

	/**
	 * Create an agent configuration object
	 * 
	 * @param name
	 *            the name of the agent
	 * @param ver
	 *            the version of the agent
	 */
	public AgentConfiguration(String name, String ver) {
		agentName = name;
		version = ver;
		configParameters = new TreeMap<String, String>();
		additionalInfo = new TreeMap<String, Object>();
		agentId = agentCounter;
		agentCounter++;
	}

	/**
	 * Get the name of the agent
	 * 
	 * @return the name of the agent
	 */
	public String getAgentName() {
		return agentName;
	}

	/**
	 * Get the version of the agent
	 * 
	 * @return the name of the agent's version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Add a configuration parameter
	 * 
	 * @param name
	 *            the name of the parameter
	 * @param value
	 *            the value used
	 * @throws ConfigParameterAlreadySetException
	 *             if there is a configuration parameter with that value
	 */
	public void addConfigParameter(String name, String value)
		throws ConfigParameterAlreadySetException {
		if (!configParameters.containsKey(name)) {
			configParameters.put(name, value);
		} else {
			throw new ConfigParameterAlreadySetException(
				"Configuration parameter is already set for this agent:  name "
							+ name + "  value " + value);
		}
	}

	/**
	 * @param name
	 *            the name of the configuration parameter
	 * @return the value associated with a configuration parameter
	 * @throws NoSuchConfigParameterException
	 *             thrown if none have been set
	 */
	public String getConfigValue(String name)
		throws NoSuchConfigParameterException {
		if (configParameters.containsKey(name)) {
			return configParameters.get(name);
		} else {
			throw new NoSuchConfigParameterException(
					"No such configuration parameter:  " + name);
		}
	}

	/**
	 * @return an iterator over the names of the parameters given to the agent
	 */
	public Iterator<String> configParamNames() {

		return configParameters.keySet().iterator();

	}

	/**
	 * @return an iterator over the values of the parameters given to the agent
	 */
	public Iterator<String> configParamValues() {
		return configParameters.values().iterator();

	}

	/**
	 * Add additional information about an agent
	 * 
	 * @param name
	 *            the name to store the information under
	 * @param value
	 *            the value used
	 * @throws InfoAlreadySetException
	 *             if there is information associated with this name
	 */
	public void addInfo(String name, Object value)
		throws InfoAlreadySetException {
		if (!additionalInfo.containsKey(name)) {
			additionalInfo.put(name, value);
		} else {
			throw new InfoAlreadySetException("Additional info already set:  "
					+ name);
		}
	}

	/**
	 * @param name
	 *            the name/key of a key-value pair of additional information
	 * @return the value associated with some information
	 * @throws NoSuchInformationException
	 *             thrown if none have been set
	 */
	public Object getInformation(String name) 
		throws NoSuchInformationException {
		if (additionalInfo.containsKey(name)) {
			return additionalInfo.get(name);
		} else {
			throw new NoSuchInformationException("No such info:  " + name);
		}

	}

	/**
	 * @return an iterator over the names from the name-value pairs in the
	 *         additional info set
	 */
	public Iterator<String> infoNames() {
		return additionalInfo.keySet().iterator();

	}

	/**
	 * @return an iterator over the values from the name-value pairs in the
	 *         additional info set
	 */
	public Iterator<Object> infoValues() {
		return additionalInfo.values().iterator();

	}

	/**
	 * @return the agent's id
	 */
	public int getId() {
		return agentId;
	}

	/**
	 * @return a short description of the agent
	 */
	@Override
	public String toString() {
		return "Agent a" + agentId + " " + agentName;
	}
}
