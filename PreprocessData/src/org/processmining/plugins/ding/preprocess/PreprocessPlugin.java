package org.processmining.plugins.ding.preprocess;
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
import java.util.Map;
import java.util.Random;

import javax.swing.JOptionPane;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XAttributeBoolean;
import org.deckfour.xes.model.XAttributeDiscrete;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.ui.wizard.ListWizard;
import org.processmining.framework.util.ui.wizard.ProMWizardDisplay;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.ding.preprocess.ui.LabelParameterStep;
import org.processmining.plugins.ding.preprocess.ui.VariantWholeView;
import org.processmining.plugins.ding.preprocess.util.Configuration;
import org.processmining.plugins.ding.preprocess.util.EventLogUtilities;
import org.processmining.plugins.ding.preprocess.util.NetUtilities;


@Plugin(
		name = "Label Event Log",
		parameterLabels = {"Event log", "Petrinet"}, 
		returnLabels = { "Laleled Log"},
		returnTypes = {XLog.class}, 
		userAccessible = true,
		help = "add additional label information into the event log "
		)

public class PreprocessPlugin { 
	/**
	 * This plugin assign label information randomly to event log to each trace and generate new event log 
	 * @param context  : nothing but to create the connection use...however, later use it 
	 * @param log : event log
	 * @return labeled event log 
	 */
	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Kefang", email = "***@gmail.com", uiLabel = UITopiaVariant.USEVARIANT)
	@PluginVariant(variantLabel = "Assign Label Randomly on Event Log",  requiredParameterLabels = { 0})
	public XLog assignRandomLabel(UIPluginContext context, XLog log) {
		// how to achieve it ?? 
		
		// do we need to consider the different variants?? No, we don't consider it 
		// to simplify it, we just consider the variant at first and then each trace???
		// -- random generator, if it is greater than a threshold, then assign ??? 
		// -- read and write to event attribute.. That's all..
		XLog label_log = (XLog)log.clone();
		XFactory factory = XFactoryRegistry.instance().currentDefault();
		label_log.getGlobalTraceAttributes().add(factory.createAttributeBoolean("label", false, null));
		// create random object
	    Random randomno = new Random();

		for (XTrace trace : label_log) {
			 // get next next boolean value 
		    boolean value = randomno.nextBoolean();
		    
		    XAttributeBoolean attr = factory.createAttributeBoolean(Configuration.POS_LABEL, value, null);
		    trace.getAttributes().put(attr.getKey(), attr);
		}	
		return  label_log;
	}
	
	
	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Kefang", email = "***@gmail.com", uiLabel = UITopiaVariant.USEVARIANT)
	@PluginVariant(variantLabel = "Assign Label w.r.t. Throughputtime",  requiredParameterLabels = { 0})
	public XLog assignLabel(UIPluginContext context, XLog log) {
		XLog label_log = (XLog)log.clone();
		
		XFactory factory = XFactoryRegistry.instance().currentDefault();
		// how to decide the throughputtime of each trace?? 
		label_log.getGlobalTraceAttributes().add(factory.createAttributeBoolean("label", false, null));
		
		EventLogUtilities.assignThroughTimeAttribute(label_log);

		// if the throughputtime is over the percentage of 70%, then we assign above it wrong..
		// double percentage = 0.7;
		
		// here we need to create the data sets.. Index, and throughputtime
		// kind of complex, so I need to push it back.. Now,just to get the mean value
		ArrayList<Long> time_list = new ArrayList<Long>();
		double sum = 0.0; int count = 0;
		for (XTrace trace : label_log) {
			// if it has throughputtime attribute, then we put them into a list 
			if(trace.getAttributes().containsKey(Configuration.TP_TIME)) {
				XAttributeDiscrete attr = (XAttributeDiscrete) trace.getAttributes().get(Configuration.TP_TIME);
				// time_list.add(attr.getKey());
				time_list.add(attr.getValue());
				sum += attr.getValue();
				count++;
			}	
		}
		// find the mean value and split it into two sets// how to make sure that they have the same traverse order??
		double mean = sum/count;
		for (XTrace trace : label_log) {
			if(trace.getAttributes().containsKey(Configuration.TP_TIME)) {
				XAttributeDiscrete attr = (XAttributeDiscrete) trace.getAttributes().get(Configuration.TP_TIME);
				if(attr.getValue() > mean) {
					// label it into one class 
					XAttributeBoolean nattr = factory.createAttributeBoolean(Configuration.POS_LABEL, false, null);
				    trace.getAttributes().put(nattr.getKey(), nattr);
				}else {
					// label it into another class
					XAttributeBoolean nattr = factory.createAttributeBoolean(Configuration.POS_LABEL, true, null);
				    trace.getAttributes().put(nattr.getKey(), nattr);
				}
			}		
		}
		return  label_log;
	}
	
	
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
	
	
	
	// here we need a Plugin to assign fit and not fit label to event log, we need the input Petrinet and Log
	// marking is also needed, but we don't write it down.
	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Kefang", email = "***@gmail.com", uiLabel = UITopiaVariant.USEVARIANT)
	@PluginVariant(variantLabel = "Assign Fit Label to Log",  requiredParameterLabels = { 0,1})
	public XLog assignFitLabel(UIPluginContext context, XLog log, Petrinet net) {
		// we could get the variants from log summary and then assign them to it. 
		// should we add one step to choose the classifier??? Anyway, it's troubling.
		Marking marking =null;
		XEventClassifier classifier = null;
		
		List<TraceVariant> variants = EventLogUtilities.getTraceVariants(log);
		Map<XEventClass, Transition> maps = EventLogUtilities.getEventTransitionMap(log, net , classifier);
		
		for(TraceVariant variant: variants) {
			if(NetUtilities.fitPN(net, marking, variant.getTraceVariant(), maps))
				EventLogUtilities.assignVariantLabel(variant, Configuration.FIT_LABEL, true);
			else
				EventLogUtilities.assignVariantLabel(variant, Configuration.FIT_LABEL, false);	
		}
		return log;
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
