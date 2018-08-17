package org.processmining.plugins.gettingstarted;

import javax.swing.JOptionPane;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

@Plugin(
	name = "My Overloaded Hello World Plugin", 
	parameterLabels = { "First string", "Number", "Second string" }, 
	returnLabels = { "Hello world string" }, 
	returnTypes = { String.class }, 
	userAccessible = true, 
	help = "The plugin produces 'hello' concatenated with at least one world. If no world is given, it is requested from the user, provided that a GUI exists."
)
public class HelloWorld10 {

	private String getWorld(UIPluginContext context) {
		// Ask the user for his world
		String w = JOptionPane.showInputDialog(null, "What's the name of your world?",
				"Enter your world", JOptionPane.QUESTION_MESSAGE);
		// change your result label
		context.getFutureResult(0).setLabel("Hello " + w + " string");
		return w;
	}

	@PluginVariant(variantLabel = "My original hello world", requiredParameterLabels = {})
	@UITopiaVariant(uiLabel = "My original hello world", affiliation = "My company", author = "My name", email = "My e-mail address")
	public String helloWorld(PluginContext context) {
		return "Hello World";
	}

	@PluginVariant(variantLabel = "My Hello unknown", requiredParameterLabels = {})
	@UITopiaVariant(uiLabel = "My Hello unknown", affiliation = "My company", author = "My name", email = "My e-mail address")
	public String helloUnknown(UIPluginContext context) {
		return "Hello " + getWorld(context);
	}

	@PluginVariant(variantLabel = "My Combine worlds", requiredParameterLabels = { 0, 1, 2 })
	@UITopiaVariant(uiLabel = "My Combine worlds", affiliation = "My company", author = "My name", email = "My e-mail address")
	public Object helloWorlds(PluginContext context, String first, Integer number, String second) {
		String s = first;
		for (int i = 0; i < number; i++) {
			s += "," + second;
		}
		return s;
	}

	@PluginVariant(variantLabel = "My Combine unknowns", requiredParameterLabels = { 0, 1 })
	@UITopiaVariant(uiLabel = "My Combine unknowns", affiliation = "My company", author = "My name", email = "My e-mail address")
	public Object helloWorlds(UIPluginContext context, String first, Integer number) {
		// return the combined string, after asking for the world
		return helloWorlds(context, first, number, getWorld(context));
	}
}

