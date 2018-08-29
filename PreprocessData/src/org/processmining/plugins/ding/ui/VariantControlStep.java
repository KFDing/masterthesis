package org.processmining.plugins.ding.ui;

import java.util.List;

import javax.swing.JComponent;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.util.ui.wizard.ProMWizardStep;
import org.processmining.plugins.ding.preprocess.TraceVariant;
import org.processmining.plugins.ding.util.EventLogUtilities;

public class VariantControlStep implements ProMWizardStep<XLog>{

	private VariantControlView controlPanel; 
	
	public VariantControlStep(XLog log) {
		// parameters = new FilteringParameters();
		List<TraceVariant> variants = EventLogUtilities.getTraceVariants(log);
		this.controlPanel = new VariantControlView(variants, log);
	}
	
	public VariantControlView getFilteringPanel() {
		return controlPanel;
	} 
	
	public XLog apply(XLog model, JComponent component) {
		// TODO Auto-generated method stub
		if(canApply(model, component))
			return model;
		return null;
	}

	public boolean canApply(XLog model, JComponent component) {
		// in which condition it say no
		return true;
	}

	public JComponent getComponent(XLog model) {
		// TODO Auto-generated method stub
		return controlPanel;
	}

	public String getTitle() {
		return "Set Control Parameters";
	}
	
}
