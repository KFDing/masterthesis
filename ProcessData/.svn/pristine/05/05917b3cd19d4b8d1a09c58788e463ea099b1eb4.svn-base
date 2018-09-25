package org.processmining.plugins.gettingstarted;

import javax.swing.JOptionPane;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;

public class HelloWorld8 {
	@Plugin(
		name = "My 5th Combine Worlds Plug-in", 
		parameterLabels = { "First string", "Number" }, 
		returnLabels = { "First string several second strings" }, 
		returnTypes = { String.class }, 
		userAccessible = true, 
		help = "Produces one string consisting of the first and a number of times a string given as input in a dialog."
	)
	@UITopiaVariant(
		affiliation = "My company", 
		author = "My name", 
		email = "My e-mail address"
	)
	public static Object helloWorlds(UIPluginContext context, String first, Integer number) {
		// Ask the user for his world
		String w = JOptionPane.showInputDialog(null, "What's the name of your world?",
				"Enter your world", JOptionPane.QUESTION_MESSAGE);
		// change your result label
		context.getFutureResult(0).setLabel("Hello " + w + " string");
		// return the combined string
		return helloWorlds(context, first, number, w);
	}

	@Plugin(
		name = "My 4th Hello World Plug-in", 
		parameterLabels = {}, 
		returnLabels = { "Hello string", "Number", "Worlds string" }, 
		returnTypes = { String.class, Integer.class, String.class }, 
		userAccessible = true, 
		help = "Produces three objects: 'Hello', number, 'world'"
	)
	@UITopiaVariant(
		affiliation = "My company", 
		author = "My name", 
		email = "My e-mail address"
	)
	public static Object helloWorlds(PluginContext context, String first, Integer number, String second) {
		String s = first;
		for (int i = 0; i < number; i++) {
			s += "," + second;
		}
		return s;
	}
}

