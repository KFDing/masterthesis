package org.processmining.incorporatenegativeinformation.plugins;
/**
 * This class is used to preprocess event log before it is passed to other application. 
 * Preprocess includes: 
 * == throughtime of each trace
   == whole costs of each trace
*  == empty but just add the attribute to it..
*  In the end it could divide event log into different sets w.r.t. performance
*  
*  First Task of it is to add additional attributes into the event log
*  
*  
 * @author dkf
 * date: 14. August 2018 
 */

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XAttributeBoolean;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.ui.wizard.ListWizard;
import org.processmining.framework.util.ui.wizard.ProMWizardDisplay;
import org.processmining.incorporatenegativeinformation.dialogs.ui.LabelParameterStep;
import org.processmining.incorporatenegativeinformation.dialogs.ui.VariantWholeView;
import org.processmining.incorporatenegativeinformation.help.Configuration;
import org.processmining.incorporatenegativeinformation.help.EventLogUtilities;
import org.processmining.incorporatenegativeinformation.models.TraceVariant;
import org.processmining.incorporatenegativeinformation.parameters.LabelParameters;


@Plugin(
		name = "Label Event Log",
		parameterLabels = {"Event log"}, 
		returnLabels = { "Labeled Log"},
		returnTypes = {XLog.class}, 
		userAccessible = true,
		help = "add additional label information into the event log "
		)

public class PreprocessPlugin { 
	
	/**
	 * After we randomly assign labels on the traces, we need to have control on it. 
	 * <1> overlap or not overlap
	 * <2> pos and neg distribution
	 * <3> fit and unfit traces... If we know the traces is fit or not fit, by label or by programs??
	 *   ---- By label,
	 *   == after we generate event log, we assign one attribute to it if they are fit or not
	 *   == We could control the parameters in a dialog
	 *   ---- no overlap:: 
	 *   0.3:0.7 pos:neg, different variants [ variant has A. same frequence B. different frequence] 
	 *   in fit(0.3:0.7) and unfit (0.3:0.7)
	 *   ---- overlap:: 0.3 and 0.7 
	 *   0.3:0.7, pos:neg, same variant, and according to the distribution. 
	 * How to achieve them?? 
	 *   4 parameter to assign labels for fit and unfit specially 
	 */
	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Kefang", email = "***@gmail.com", uiLabel = UITopiaVariant.USEVARIANT)
	@PluginVariant(variantLabel = "Assign Controlled Label",  requiredParameterLabels = { 0})
	public XLog assignControlledLabel(UIPluginContext context, XLog log) {
		
		XFactory factory = XFactoryRegistry.instance().currentDefault();
		XLog label_log = (XLog)log.clone();
		// how to decide the throughputtime of each trace?? 
		label_log.getGlobalTraceAttributes().add(factory.createAttributeBoolean(Configuration.POS_LABEL, false, null));
		// we need to create a dislogue for setting parameters
		LabelParameters parameters = new LabelParameters();
		LabelParameterStep lp_step = new LabelParameterStep(parameters);
		
		ListWizard<LabelParameters> wizard = new ListWizard<LabelParameters>(lp_step);
		parameters = ProMWizardDisplay.show(context, wizard, parameters);
		//System.out.println(parameters.getFit_overlap_rate());
    	//System.out.println(parameters.getFit_pos_rate());
		
		List<TraceVariant> variants = EventLogUtilities.getTraceVariants(label_log); // for all variants 
		// fit and not fit for variants 
		// assignLabel to fit and not fit for also the variants.
		List<TraceVariant> fit_variants = new ArrayList<TraceVariant>();
		List<TraceVariant> unfit_variants = new ArrayList<TraceVariant>();
	
		for(TraceVariant var : variants) {
			if(var.getFitLabel()==null || var.getFitLabel())
				fit_variants.add(var);
			else // if(var.getFitLabel() == false)
				unfit_variants.add(var);
		} // variants size ==0, we don't need to do it??? 
		if(fit_variants.size()>0) 
			EventLogUtilities.assignVariantListLabel(fit_variants, parameters.getFit_overlap_rate(), parameters.getFit_pos_rate());
		// for unfit variants // if variants.size == 0, we don't need to do it
		if(unfit_variants.size()>0)
			EventLogUtilities.assignVariantListLabel(unfit_variants, parameters.getUnfit_overlap_rate(), parameters.getUnfit_pos_rate());
		
		return label_log;
	}
	
	
	/**
	 * One more plugin to assign label to specific log variants 
	 * -- to show summary, variants and its number 
	 * -- also the attributes when its traces have, then also show summary
	 * -- given the label, choose to assign it to specific variants 
	 * -- only for situation, not overlap ones. 
	 * -- generate the labeled log
	 */
	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Kefang", email = "***@gmail.com", uiLabel = UITopiaVariant.USEVARIANT)
	@PluginVariant(variantLabel = "Assign Label to Specific Variant",  requiredParameterLabels = { 0})
	public XLog assignSpecificLabel(UIPluginContext context, XLog log) {
		// get variants with summary
		XFactory factory = XFactoryRegistry.instance().currentDefault();
		XLog label_log = (XLog) log.clone();
		XLogInfo info = XLogInfoFactory.createLogInfo(label_log);
		
		label_log.getGlobalTraceAttributes().add(factory.createAttributeBoolean(Configuration.POS_LABEL, false, null));
		label_log.getGlobalTraceAttributes().add(factory.createAttributeBoolean(Configuration.FIT_LABEL, false, null));
		// so here we just set one interactive parameter setting window
		// after the process, we get labeled_log ??? No, here are the variants changed
		// but it refers to data in log, so change variant changed the log
		// VariantControlStep c_step = new VariantControlStep(label_log);

		List<TraceVariant> variants = EventLogUtilities.getTraceVariants(label_log);
		VariantWholeView view =  new VariantWholeView(variants, info);
		InteractionResult result = context.showWizard("Setting Variant", true, true, view);
		if (result != InteractionResult.FINISHED) {
	    		return null;
		}
		return label_log;
		// ListWizard<XLog> wizard = new ListWizard<XLog>(c_step);
		// it doesn't retun value, just the change on variants, then actually, I could get it here.
		// return  ProMWizardDisplay.show(context, wizard, label_log);
		// if we update the view, how should we do it ??? Or actually we could do it in trace
				
	}
	
	/**
	 * create a plugin to group data w.r.t. given criteria
	 * like extract traces with pos, or neg
	 * get the complement of variant only with pure pos or neg
	 */
	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Kefang", email = "***@gmail.com", uiLabel = UITopiaVariant.USEVARIANT)
	@PluginVariant(variantLabel = "Extract Pos Data from Log",  requiredParameterLabels = { 0})
	public XLog extractPosData(UIPluginContext context, XLog log) {
		// but we need to set parameters for it.. Then we can use it
		// -- create an interface to choose the data only from positive 
		// or from the complement of positive
		// extract it but we could do it before create the model
		XLog pos_log = (XLog) log.clone();
		
		boolean only_pos = true;
		int neg_count = 0, pos_count=0;
		if(only_pos) {
			Iterator liter = pos_log.iterator();
			while(liter.hasNext()) {
				XTrace trace = (XTrace) liter.next();
				if(trace.getAttributes().containsKey(Configuration.POS_LABEL)) {
					XAttributeBoolean attr = (XAttributeBoolean) trace.getAttributes().get(Configuration.POS_LABEL);
					if(!attr.getValue()) {
						liter.remove();
						neg_count++;
					}else
						pos_count++;
				}
			}
			JOptionPane.showMessageDialog(null,
				    "The event log has "+ pos_count + " positive traces and "+ neg_count + " negative traces",
				    "Inane information",
				    JOptionPane.INFORMATION_MESSAGE);
		}
		return pos_log;
	}
		
	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Kefang", email = "***@gmail.com", uiLabel = UITopiaVariant.USEVARIANT)
	@PluginVariant(variantLabel = "Extract Pos Complement Data from Log",  requiredParameterLabels = { 0})
	public XLog extractPosComplementData(UIPluginContext context, XLog log) {
		// but we need to set parameters for it.. Then we can use it
		// -- create an interface to choose the data only from positive 
		// or from the complement of positive
		// extract it but we could do it before create the model
		XLog pos_log = (XLog) log.clone();
		//  complement means we need to get the variants and summary of them
		//  if summary of them is ?:0, we accept it else, not!!! 
		List<TraceVariant> variants = EventLogUtilities.getTraceVariants(pos_log);
		for(TraceVariant var: variants) {
			// if they have overlap  var.getSummary().get(Configuration.POS_IDX) < 1 ||
			if( var.getSummary().get(Configuration.NEG_IDX) > 0)
				EventLogUtilities.deleteVariantFromLog(var, pos_log);
		}
		JOptionPane.showMessageDialog(null,
			    "The event log has "+ pos_log.size() + " traces in positive complement",
			    "Inane information",
			    JOptionPane.INFORMATION_MESSAGE);
		return pos_log;
	}
	
}
