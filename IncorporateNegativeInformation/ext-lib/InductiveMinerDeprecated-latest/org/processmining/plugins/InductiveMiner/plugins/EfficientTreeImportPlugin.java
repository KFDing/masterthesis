package org.processmining.plugins.InductiveMiner.plugins;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.processmining.contexts.uitopia.annotations.UIImportPlugin;
import org.processmining.framework.abstractplugins.AbstractImportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeImpl;
import org.processmining.plugins.InductiveMiner.efficienttree.ProcessTreeParser;

@Plugin(name = "Import an EfficientTree", parameterLabels = { "Filename" }, returnLabels = { "EfficientTree" }, returnTypes = { EfficientTreeImpl.class })
@UIImportPlugin(description = "EfficientTree", extensions = { "tree" })
public class EfficientTreeImportPlugin extends AbstractImportPlugin {

	protected Object importFromStream(PluginContext context, InputStream input, String filename, long fileSizeInBytes)
			throws Exception {
		return importFromStream(input);
	}

	public static EfficientTree importFromStream(InputStream stream) throws Exception {
		//read the file
		StringWriter writer = new StringWriter();
		IOUtils.copy(stream, writer, StandardCharsets.UTF_8);
		String text = writer.toString();

		Triple<EfficientTree, Integer, String> result = ProcessTreeParser.parse(text, 3);
		if (result.getA() == null) {
			//throw error message
			throw new Exception(result.getC());
		} else {
			return result.getA();
		}
	}

	public static EfficientTree importFromFile(File file) throws FileNotFoundException, Exception {
		return importFromStream(new FileInputStream(file));
	}
}
