package org.processmining.plugins.ding.util;

public class GraphVisualizerParameters {

	private String internalLabelFormat;
	private String externalLabelFormat;
	private String toolTipFormat;
	
	public GraphVisualizerParameters() {
		setInternalLabelFormat("%l"); // Use JGraph internal label as internal label.
		setExternalLabelFormat("%p"); // Use place label as external label.
		setToolTipFormat("%t"); // Use tooltip as tooltip (seems not to show).
	}
	
	public String getInternalLabelFormat() {
		return internalLabelFormat;
	}

	public void setInternalLabelFormat(String internalLabelFormat) {
		this.internalLabelFormat = internalLabelFormat;
	}

	public String getExternalLabelFormat() {
		return externalLabelFormat;
	}

	public void setExternalLabelFormat(String externalLabelFormat) {
		this.externalLabelFormat = externalLabelFormat;
	}

	public String getToolTipFormat() {
		return toolTipFormat;
	}

	public void setToolTipFormat(String toolTipFormat) {
		this.toolTipFormat = toolTipFormat;
	}
}
