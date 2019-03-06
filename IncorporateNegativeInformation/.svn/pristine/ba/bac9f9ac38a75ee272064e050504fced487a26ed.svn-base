package org.processmining.incorporatenegativeinformation.plugins;

import java.util.List;

import javax.swing.JComponent;

import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.incorporatenegativeinformation.dialogs.ui.VariantWholeView;
import org.processmining.incorporatenegativeinformation.models.TraceVariant;

public class VariantViewPlugin {

	@Plugin(name = "Explore and Label Trace Variants", level = PluginLevel.PeerReviewed, //
			returnLabels = { "Log Variants Visualization" }, returnTypes = { JComponent.class }, //
			userAccessible = true, parameterLabels = { "Trace Variants" ,"XLog"})
	@Visualizer
	@PluginVariant(requiredParameterLabels = { 0, 1 })
	@UITopiaVariant(pack = "Preprocess", affiliation = UITopiaVariant.EHV, author = "kefang", email = "@kefang")
	public JComponent visualise(final PluginContext context, List<TraceVariant> variants, XLog log) {
		XLogInfo info = XLogInfoFactory.createLogInfo(log);
		return new VariantWholeView( variants, info); //PluginContext context, 
	}

}
