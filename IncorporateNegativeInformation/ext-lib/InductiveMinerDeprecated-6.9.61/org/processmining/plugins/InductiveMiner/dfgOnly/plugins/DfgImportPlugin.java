package org.processmining.plugins.InductiveMiner.dfgOnly.plugins;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.contexts.uitopia.annotations.UIImportPlugin;
import org.processmining.framework.abstractplugins.AbstractImportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgImpl;

import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVReader;

@Plugin(name = "Import a CSV file and convert it to dfg", parameterLabels = { "Filename" }, returnLabels = { "Directly follows graph" }, returnTypes = { Dfg.class })
@UIImportPlugin(description = "Directly follows graph", extensions = { "dfg", "csv" })
public class DfgImportPlugin extends AbstractImportPlugin {

	private static final int BUFFER_SIZE = 8192 * 4;
	private static final char SEPARATOR = ',';
	private static final String CHARSET = Charset.defaultCharset().name();

	public Dfg importFromStream(PluginContext context, InputStream input, String filename, long fileSizeInBytes)
			throws Exception {

		//read the file
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len;
		while ((len = input.read(buffer)) > -1) {
			baos.write(buffer, 0, len);
		}
		baos.flush();

		//try 2
		Dfg dfg2 = readFile(new ByteArrayInputStream(baos.toByteArray()));

		if (dfg2 != null) {
			return dfg2;
		}

		//try 1
		Dfg dfg1 = readCSV(new ByteArrayInputStream(baos.toByteArray()));

		if (dfg1 != null) {
			return dfg1;
		}

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JOptionPane.showMessageDialog(null, "Invalid directly follows graph file", "Invalid file",
						JOptionPane.ERROR_MESSAGE);
			}
		});
		context.getFutureResult(0).cancel(false);
		return null;
	}

	public static Dfg readCSV(InputStream input) throws IOException {
		CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(input, CHARSET), BUFFER_SIZE),
				SEPARATOR, CSVParser.DEFAULT_QUOTE_CHARACTER, CSVParser.DEFAULT_ESCAPE_CHARACTER, 0, false, false, true);

		Dfg dfg;
		try {
			//add activities
			String[] sActivities = reader.readNext();
			XEventClass[] activities = new XEventClass[sActivities.length];
			for (int a = 0; a < sActivities.length; a++) {
				activities[a] = new XEventClass(sActivities[a], a);
			}

			dfg = new DfgImpl(sActivities.length);
			for (int a = 0; a < sActivities.length; a++) {
				dfg.addActivity(activities[a]);
			}

			//start activities
			String[] sStartActivities = reader.readNext();
			for (int a = 0; a < sActivities.length; a++) {
				long cardinality = Long.valueOf(sStartActivities[a]);
				if (cardinality > 0) {
					dfg.addStartActivity(activities[a], cardinality);
				}
			}

			//end activities
			String[] sEndActivities = reader.readNext();
			for (int a = 0; a < sActivities.length; a++) {
				long cardinality = Long.valueOf(sEndActivities[a]);
				if (cardinality > 0) {
					dfg.addEndActivity(activities[a], cardinality);
				}
			}

			//edges
			for (int a1 = 0; a1 < sActivities.length; a1++) {
				String[] row = reader.readNext();
				for (int a2 = 0; a2 < sActivities.length; a2++) {
					long cardinality = Long.valueOf(row[a2]);
					if (cardinality > 0) {
						dfg.addDirectlyFollowsEdge(activities[a1], activities[a2], cardinality);
					}
				}
			}
		} catch (Exception e) {
			return null;
		} finally {
			reader.close();
		}

		return dfg;
	}

	public static Dfg readFile(InputStream input) throws IOException {
		BufferedReader r = new BufferedReader(new InputStreamReader(input, CHARSET), BUFFER_SIZE);

		Dfg dfg = new DfgImpl();

		//read activity names
		int nrOfActivities = Integer.parseInt(r.readLine());
		for (int i = 0; i < nrOfActivities; i++) {
			String actName = r.readLine();
			dfg.addActivity(new XEventClass(actName, i));
		}

		//read start activities
		{
			int nrOfStartActivities = Integer.parseInt(r.readLine());
			for (int i = 0; i < nrOfStartActivities; i++) {
				String line = r.readLine();
				int xAt = line.indexOf('x');
				int activityIndex = Integer.parseInt(line.substring(0, xAt));
				long cardinality = Long.parseLong(line.substring(xAt + 1, line.length()));

				dfg.addStartActivity(dfg.getDirectlyFollowsGraph().getVertexOfIndex(activityIndex), cardinality);
			}
		}

		//read end activities
		{
			int nrOfEndActivities = Integer.parseInt(r.readLine());
			for (int i = 0; i < nrOfEndActivities; i++) {
				String line = r.readLine();
				int xAt = line.indexOf('x');
				int activityIndex = Integer.parseInt(line.substring(0, xAt));
				long cardinality = Long.parseLong(line.substring(xAt + 1, line.length()));

				dfg.addEndActivity(dfg.getDirectlyFollowsGraph().getVertexOfIndex(activityIndex), cardinality);
			}
		}

		//read edges
		{
			String line;
			while ((line = r.readLine()) != null) {
				int eAt = line.indexOf('>');
				int xAt = line.indexOf('x');
				int source = Integer.parseInt(line.substring(0, eAt));
				int target = Integer.parseInt(line.substring(eAt + 1, xAt));
				long cardinality = Long.parseLong(line.substring(xAt + 1, line.length()));

				dfg.getDirectlyFollowsGraph().addEdge(source, target, cardinality);
			}
		}

		return dfg;
	}
}
