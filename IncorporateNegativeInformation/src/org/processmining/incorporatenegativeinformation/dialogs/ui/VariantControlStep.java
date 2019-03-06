package org.processmining.incorporatenegativeinformation.dialogs.ui;

import java.util.List;

import javax.swing.JComponent;

import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.util.ui.wizard.ProMWizardStep;
import org.processmining.incorporatenegativeinformation.help.EventLogUtilities;
import org.processmining.incorporatenegativeinformation.models.TraceVariant;

public class VariantControlStep implements ProMWizardStep<XLog>{

	private VariantWholeView controlPanel; 
	
	public VariantControlStep(XLog log) {
		// parameters = new FilteringParameters();
		List<TraceVariant> variants = EventLogUtilities.getTraceVariants(log);
		XLogInfo info = XLogInfoFactory.createLogInfo(log);
		this.controlPanel = new VariantWholeView(variants, info);
	}
	
	public VariantWholeView getFilteringPanel() {
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
