package org.processmining.plugins.InductiveMiner.plugins;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UIExportPlugin;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree2HumanReadableString;

@Plugin(name = "EfficientTree export", returnLabels = {}, returnTypes = {}, parameterLabels = { "EfficientTree", "File" }, userAccessible = true)
@UIExportPlugin(description = "EfficientTree files", extension = "tree")
public class EfficientTreeExportPlugin {
	
	@PluginVariant(variantLabel = "EfficientTree export", requiredParameterLabels = { 0, 1 })
	public void exportDefault(UIPluginContext context, EfficientTree tree, File file) throws IOException {
		export(tree, file);
	}
	
	public static void export(EfficientTree tree, File file) throws IOException {
		BufferedWriter result = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
		result.append(EfficientTree2HumanReadableString.toMachineString(tree));
		result.flush();
		result.close();
	}
}
