package org.processmining.plugins.InductiveMiner.dfgOnly.plugins;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UIExportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.graphs.Graph;

@Plugin(name = "Dfg export (directly follows graph)", returnLabels = {}, returnTypes = {}, parameterLabels = {
		"Directly-follows graph", "File" }, userAccessible = true)
@UIExportPlugin(description = "Dfg files", extension = "dfg")
public class DfgExportPlugin {
	@PluginVariant(variantLabel = "Dfg export (Directly follows graph)", requiredParameterLabels = { 0, 1 })
	public void exportDefault(UIPluginContext context, Dfg dfg, File file) throws IOException {
		export(dfg, file);
	}

	public void exportDefault(PluginContext context, Dfg dfg, File file) throws IOException {
		export(dfg, file);
	}

	public static void export(Dfg dfg, File file) throws IOException {
		BufferedWriter result = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
		result.append(dfg.getNumberOfActivities() + "\n");
		for (XEventClass e : dfg.getDirectlyFollowsGraph().getVertices()) {
			result.append(e + "\n");
		}

		result.append(dfg.getNumberOfStartActivitiesAsSet() + "\n");
		for (int activityIndex : dfg.getStartActivityIndices()) {
			result.append(activityIndex + "x" + dfg.getStartActivityCardinality(activityIndex) + "\n");
		}

		result.append(dfg.getNumberOfEndActivitiesAsSet() + "\n");
		for (int activityIndex : dfg.getEndActivityIndices()) {
			result.append(activityIndex + "x" + dfg.getEndActivityCardinality(activityIndex) + "\n");
		}

		Graph<XEventClass> g = dfg.getDirectlyFollowsGraph();
		for (int source : g.getVertexIndices()) {
			for (int target : g.getVertexIndices()) {
				long v = g.getEdgeWeight(source, target);
				if (v > 0) {
					result.append(source + ">");
					result.append(target + "x");
					result.append(v + "\n");
				}
			}
		}
		result.flush();
		result.close();
	}
}
