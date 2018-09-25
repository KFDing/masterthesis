package org.processmining.plugins.gettingstarted;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;

public class HelloWorld5 {
	@Plugin(
		name = "My 2nd Combine Worlds Plug-in", 
		parameterLabels = { "First string", "Number", "Second string" }, 
		returnLabels = { "First string several second strings" }, 
		returnTypes = { String.class }, 
		userAccessible = true, 
		help = "Produces one string consisting of the first and a number of times the third parameter."
	)
	@UITopiaVariant(
		affiliation = "My company", 
		author = "My name", 
		email = "My e-mail address"
	)
	public static Object helloWorlds(PluginContext context, String first, Integer number, String second) {
		context.getProgress().setMinimum(0);
		context.getProgress().setMaximum(number);
		context.getProgress().setCaption("Constructing hello worlds string");
		context.getProgress().setIndeterminate(false);
		String s = first;
		for (int i = 0; i < number; i++) {
			s += "," + second;
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// don't care
			}
			context.getProgress().inc();
		}
		return s;
	}
}

