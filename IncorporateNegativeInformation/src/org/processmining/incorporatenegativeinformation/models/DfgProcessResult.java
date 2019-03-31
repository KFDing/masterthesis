package org.processmining.incorporatenegativeinformation.models;
/**
 * this class is used to take the result from the first phrase to generate the
 * DfMatrix, we need to give result to visualizer, so it can accept it and
 * process it
 * 
 * it has log and dfmatrix, but we still lack at the threshold on the counting
 * of long-term dependency how we decide it ?? We have existing, event log, new
 * created threshold, how should we depends on?? also, we need to check if there
 * is long-term dependency in the existing model, and how to remove or change it
 * if there is some weights on it ?? But actually, if we transfer from process
 * tree into the petri net we have lost the information of old graph.. -- if
 * consider the old long-term dependency on model, we need to get it at first
 * from the Petri net ++ but how to get them??? -- we have the thresholds from
 * both sides, how to let them decide the long-term dependency?? ++ if pos and
 * neg situations on them, still 6 numbers, existing, pos and neg;;; ++
 * existing, we still need to assign one number on them, but at first to let
 * them connect
 * 
 * @author dkf
 *
 */

import org.deckfour.xes.model.XLog;

public class DfgProcessResult {

	XLog log;
	DfMatrix dfMatrix;

	public DfgProcessResult(XLog xlog, DfMatrix matrix) {
		log = xlog;
		dfMatrix = matrix;
	}

	public XLog getLog() {
		return log;
	}

	public void setLog(XLog log) {
		this.log = log;
	}

	public DfMatrix getDfMatrix() {
		return dfMatrix;
	}

	public void setDfMatrix(DfMatrix dfMatrix) {
		this.dfMatrix = dfMatrix;
	}

}
