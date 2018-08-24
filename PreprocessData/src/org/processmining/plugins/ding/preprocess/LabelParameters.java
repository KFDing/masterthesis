package org.processmining.plugins.ding.preprocess;

public class LabelParameters {
	private double fit_overlap_rate; 
	private double fit_pos_rate; // fit_rate
	private double unfit_overlap_rate;
	private double unfit_pos_rate;
	public double getFit_overlap_rate() {
		return fit_overlap_rate;
	}
	public void setFit_overlap_rate(double fit_overlap_rate) {
		this.fit_overlap_rate = fit_overlap_rate;
	}
	public double getFit_pos_rate() {
		return fit_pos_rate;
	}
	public void setFit_pos_rate(double fit_pos_rate) {
		this.fit_pos_rate = fit_pos_rate;
	}
	public double getUnfit_overlap_rate() {
		return unfit_overlap_rate;
	}
	public void setUnfit_overlap_rate(double unfit_overlap_rate) {
		this.unfit_overlap_rate = unfit_overlap_rate;
	}
	public double getUnfit_pos_rate() {
		return unfit_pos_rate;
	}
	public void setUnfit_pos_rate(double unfit_pos_rate) {
		this.unfit_pos_rate = unfit_pos_rate;
	}
	
}
