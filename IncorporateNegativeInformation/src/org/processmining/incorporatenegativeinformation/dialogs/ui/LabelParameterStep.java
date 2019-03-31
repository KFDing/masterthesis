package org.processmining.incorporatenegativeinformation.dialogs.ui;

import javax.swing.JComponent;

import org.processmining.framework.util.ui.wizard.ProMWizardStep;
import org.processmining.incorporatenegativeinformation.parameters.LabelParameters;

public class LabelParameterStep implements ProMWizardStep<LabelParameters> {

	private LabelParameterUI controlPanel;

	public LabelParameterStep(LabelParameters parameters) {
		// parameters = new FilteringParameters();
		this.controlPanel = new LabelParameterUI(parameters);
	}

	public LabelParameterUI getFilteringPanel() {
		return controlPanel;
	}

	public LabelParameters apply(LabelParameters model, JComponent component) {
		// TODO Auto-generated method stub
		if (canApply(model, component))
			return model;
		return null;
	}

	public boolean canApply(LabelParameters model, JComponent component) {
		// in which condition it say applyable?? 
		return true;
	}

	public JComponent getComponent(LabelParameters model) {
		// TODO Auto-generated method stub
		return controlPanel;
	}

	public String getTitle() {
		return "Set Control Parameters";
	}

}
