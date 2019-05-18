package org.processmining.plugins.InductiveMiner.mining.fallthrough;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.out.XSerializer;
import org.deckfour.xes.out.XesXmlSerializer;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.Miner;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;

public class FallThroughSaveLog implements FallThrough {

	private final File directory;
	private int number = 0;

	public FallThroughSaveLog(File directory) {
		this.directory = directory;
	}

	public Node fallThrough(IMLog log, IMLogInfo logInfo, ProcessTree tree, MinerState minerState) {

		XLog xLog = log.toXLog();
		XSerializer logSerializer = new XesXmlSerializer();
		try {
			File file = new File(directory, "fall through log " + number + ".xes");

			Miner.debug(" fall through: save log to " + file, minerState);

			number++;
			FileOutputStream out = new FileOutputStream(file);
			logSerializer.serialize(xLog, out);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}
}
