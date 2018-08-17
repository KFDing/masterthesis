package org.processmining.plugins.gettingstarted;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.providedobjects.ProvidedObjectDeletedException;
import org.processmining.framework.providedobjects.ProvidedObjectID;

public class HelloWorld7 {
	@Plugin(
		name = "My 4th Combine Worlds Plug-in", 
		parameterLabels = { "First string", "Number", "Second string" }, 
		returnLabels = { "First string several second strings" }, 
		returnTypes = { String.class }, 
		userAccessible = true, 
		help = "Produces one string consisting of the first and anumber of times the third parameter."
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
		ProvidedObjectID id = context.getProvidedObjectManager()
				.createProvidedObject("intermediate string", s, context);
		for (int i = 0; i < number; i++) {
			context.getFutureResult(0).setLabel("Hello " + i + " worlds string");
			try {
				context.getProvidedObjectManager().changeProvidedObjectObject(id, s);
				Thread.sleep(1000);
			} catch (ProvidedObjectDeletedException e1) {
				// if the user deleted this object,
				// then we create it again
				id = context.getProvidedObjectManager().createProvidedObject("intermediate string", s, context);
			} catch (InterruptedException e) {
				// don't care
			}
			s += "," + second;
			context.getProgress().inc();
		}
		context.getFutureResult(0).setLabel("Hello " + number + " worlds string");
		// The intermediate object is no longer necessary.
		try {
			context.getProvidedObjectManager().deleteProvidedObject(id);
		} catch (ProvidedObjectDeletedException e) {
			// Don't care
		}
		return s;
	}
}

