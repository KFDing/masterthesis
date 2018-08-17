package org.processmining.plugins.ding.baseline;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.connections.impl.AbstractStrongReferencingConnection;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.plugins.inductiveminer2.mining.MiningParameters;

public class BaselineConnection extends AbstractStrongReferencingConnection {
	public final static String LABEL = "BaselineConnection"; 
	public final static String TYPE = "Type";
	
	public final static String PN = "Petrinet";
	public final static String LOG = "Log";

	public final static String MINING_PARAMETERS = "FilteringParameters";
	public final static String RESULT = "Filtered result";
	
	public final static String BASELINE_PN = "BaseLine Petrinet";
	

    public BaselineConnection(XLog log, MiningParameters parameters, Petrinet net) {
    	super(LABEL);
    	put(TYPE,BASELINE_PN);
		put(LOG, log);
		put(MINING_PARAMETERS, parameters);
		put(PN, net);
	}
    
}
