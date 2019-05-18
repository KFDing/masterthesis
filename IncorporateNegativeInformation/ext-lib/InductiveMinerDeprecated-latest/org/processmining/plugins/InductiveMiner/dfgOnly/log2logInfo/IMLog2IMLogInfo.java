package org.processmining.plugins.InductiveMiner.dfgOnly.log2logInfo;

import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;

public interface IMLog2IMLogInfo {

	/**
	 * Create an IMLogInfo from an IMLog
	 * 
	 * @param log
	 * @return the IMLogInfo
	 */
	public IMLogInfo createLogInfo(IMLog log);
	
	public boolean useLifeCycle();

}
