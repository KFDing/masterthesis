package org.processmining.plugins.gettingstarted;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;

public class HelloWorld2 {
	@Plugin(
		name = "My 2nd Hello World Plug-in", 
		parameterLabels = {}, 
		returnLabels = {"Hello string", "Number", "Worlds string" }, 
		returnTypes = {String.class, Integer.class, String.class }, 
		userAccessible = true, 
		help = "Produces three objects: 'Hello', number, 'world'"
	)
	@UITopiaVariant(
		affiliation = "My company", 
		author = "My name", 
		email = "My e-mail address"
	)
	public static Object[] helloWorlds(PluginContext context) {
		return new Object[] { "Hello", new Integer(6), "Worlds" };
	}
}
