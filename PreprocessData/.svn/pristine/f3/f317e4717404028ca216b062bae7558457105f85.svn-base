package org.processmining.plugins.gettingstarted;

import javax.swing.JOptionPane;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

@Plugin(
	name = "My Overloaded Hello Many Worlds", 
	parameterLabels = { "First string", "Large Number", "Second string" }, 
	returnLabels = { "Hello world string" }, 
	returnTypes = { String.class }, 
	userAccessible = true, 
	help = "The plugin produces 'hello' concatenated with at least one world. If no world is given, it is requested from the user, provided that a GUI exists."
)
public class HelloWorld11 {

	private String getWorld(UIPluginContext context) {
		// Ask the user for his world
		String w = JOptionPane.showInputDialog(null, "What's the name of your world?",
				"Enter your world", JOptionPane.QUESTION_MESSAGE);
		// change your result label
		context.getFutureResult(0).setLabel("Hello " + w + " string");
		return w;
	}

	@PluginVariant(variantLabel = "My Combine many worlds", requiredParameterLabels = { 0, 1, 2 })
	@UITopiaVariant(uiLabel = "My Combine many worlds", affiliation = "My company", author = "My name", email = "My e-mail address")
	public Object helloWorlds(PluginContext context, String first, Long number, String second) {
		String s = first;
		for (int i = 0; i < number; i++) {
			s += "," + second;
		}
		return s;
	}

	@PluginVariant(variantLabel = "My Combine few unknowns", requiredParameterLabels = { 0, 1 })
	@UITopiaVariant(uiLabel = "My Combine few unknowns", affiliation = "My company", author = "My name", email = "My e-mail address")
	public Object helloWorlds(UIPluginContext context, String first, Integer number) {
		// return the combined string, after asking for the world
		return helloWorlds(context, first, Long.valueOf(number), getWorld(context));
	}

	@PluginVariant(variantLabel = "My Combine many unknowns", requiredParameterLabels = { 0, 1 })
	@UITopiaVariant(uiLabel = "My Combine many unknowns", affiliation = "My company", author = "My name", email = "My e-mail address")
	public Object helloWorlds(UIPluginContext context, String first, Long number) {
		// return the combined string, after asking for the world
		return helloWorlds(context, first, number, getWorld(context));
	}
}

