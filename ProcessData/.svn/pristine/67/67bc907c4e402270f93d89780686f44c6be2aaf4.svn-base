package org.processmining.plugins.gettingstarted;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;

public class HelloWorld9 {
	@Plugin(
		name = "My 5th Hello World Plug-in", 
		parameterLabels = {}, 
		returnLabels = { "Hello world string" }, 
		returnTypes = { String.class }, 
		userAccessible = true, 
		help = "Produces the string: 'Hello world'"
	)
	@UITopiaVariant(
		affiliation = "My company", 
		author = "My name", 
		email = "My e-mail address"
	)
	public String helloWorld(PluginContext context) {
		return "Hello World";
	}
}

