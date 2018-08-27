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
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XAttributeBoolean;
import org.deckfour.xes.model.XAttributeDiscrete;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.log.utils.XUtils;
import org.processmining.plugins.ding.baseline.EventLogUtilities;


@Plugin(
		name = "Label Event Log",
		parameterLabels = {"Event log"}, 
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
		    
		    XAttributeBoolean attr = factory.createAttributeBoolean(Configuration.LABEL_NAME, value, null);
		    trace.getAttributes().put(attr.getKey(), attr);
		}	
		return  label_log;
	}
	/**
	 * THis plugin is to add throughtime as one attribute in trace and then assign label into it
	 * @param context
	 * @param log
	 * @return labeled_log
	 */
	public XLog assignThroughTimeAttribute(XLog log) {
		XFactory factory = XFactoryRegistry.instance().currentDefault();
		for (XTrace trace : log) {
			// timestamp from one event> .. Literal 
			Date start_time = null, end_time = null, current_time;  
			XEvent event;
			Iterator titer = trace.iterator();
			if(titer.hasNext()) {
				event =  (XEvent) titer.next();
				start_time = end_time =  XUtils.getTimestamp(event);
			}
			while (titer.hasNext()) {
				event =  (XEvent) titer.next();
				current_time= XUtils.getTimestamp(event);
				 if(current_time.before(start_time))
					 start_time = current_time;
				 if(current_time.after(end_time))
					 end_time = current_time;
			}
			// in Milliseconds format
			long throughput_time = end_time.getTime() - start_time.getTime();
			
			XAttributeDiscrete attr = factory.createAttributeDiscrete(Configuration.TP_TIME, throughput_time, null); 
			trace.getAttributes().put(attr.getKey(), attr);
		}	
		return  log;
	}
	
	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Kefang", email = "***@gmail.com", uiLabel = UITopiaVariant.USEVARIANT)
	@PluginVariant(variantLabel = "Assign Label w.r.t. Throughputtime",  requiredParameterLabels = { 0})
	public XLog assignLabel(UIPluginContext context, XLog log) {
		XLog label_log = (XLog)log.clone();
		
		XFactory factory = XFactoryRegistry.instance().currentDefault();
		// how to decide the throughputtime of each trace?? 
		label_log.getGlobalTraceAttributes().add(factory.createAttributeBoolean("label", false, null));
		
		assignThroughTimeAttribute(label_log);

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
					XAttributeBoolean nattr = factory.createAttributeBoolean(Configuration.LABEL_NAME, false, null);
				    trace.getAttributes().put(nattr.getKey(), nattr);
				}else {
					// label it into another class
					XAttributeBoolean nattr = factory.createAttributeBoolean(Configuration.LABEL_NAME, true, null);
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
		label_log.getGlobalTraceAttributes().add(factory.createAttributeBoolean(Configuration.LABEL_NAME, false, null));
		// we need to create a dislogue for setting parameters
		LabelParameters parameters = new LabelParameters();
		parameters.setFit_overlap_rate(0);
		parameters.setFit_pos_rate(1.0);
		parameters.setUnfit_overlap_rate(1.0);
		parameters.setUnfit_pos_rate(1.0);
		
		
		List<TraceVariant> variants = EventLogUtilities.getTraceVariants(label_log); // for all variants 
		// fit and not fit for variants 
		// assignLabel to fit and not fit for also the variants.
		List<TraceVariant> fit_variants = new ArrayList<TraceVariant>();
		List<TraceVariant> unfit_variants = new ArrayList<TraceVariant>();
	
		for(TraceVariant var : variants) {
			var.setFitLabel(new Random().nextBoolean());
			if(var.getFitLabel())
				fit_variants.add(var);
			else
				unfit_variants.add(var);
		}
		assignVariantListLabel(fit_variants, parameters.getFit_overlap_rate(), parameters.getFit_pos_rate());
		// for unfit variants
		assignVariantListLabel(unfit_variants, parameters.getUnfit_overlap_rate(), parameters.getUnfit_pos_rate());
		
		return label_log;
	}
	
	private void assignVariantListLabel(List<TraceVariant> variants, double overlap_rate, double pos_rate ) {
		// for fit variants
		// overlap 0.3: get total num of variants + random index + number w.r.t. to 0.3; greater than 0.3 
		List<Integer> nolidx_list = sample(variants.size(), 1 - overlap_rate);
		// we have odidx_list, and also we have nolidx_list, 
		// they should then decide to assign different prob to pos and neg
		List<Integer> nolpos_list = sample(nolidx_list.size(), pos_rate);
	
		for(int idx : nolidx_list) {
			TraceVariant variant = variants.get(nolidx_list.get(idx));
			// assign pos to nooverlap variant
			if(nolpos_list.contains(idx)) {
				assignVariantLabel(variant, Configuration.LABEL_NAME, true);
			}else {
				// assign neg to nooverlap variants
				assignVariantLabel(variant, Configuration.LABEL_NAME, false);
			}
		}
		// then overlap
		// if could happen that all olidx_list is empty, so what to do then??? 
		List<Integer> olidx_list = new ArrayList<Integer>();
		for(int i=0;i<variants.size();i++)
			if(!nolidx_list.contains(i))
				olidx_list.add(i);
		
		for(int idx : olidx_list) {
			TraceVariant variant = variants.get(olidx_list.get(idx));
			// overlap variant, we need to assign both but according to different threshold
			assignVariantLabel(variant, Configuration.LABEL_NAME, pos_rate);
		}
		
	}
	
	private void assignVariantLabel(TraceVariant variant, String attr_name, boolean is_pos) {
		// for each trace in the variant, we create an attribution with name of attr_name, value isPos
		XFactory factory = XFactoryRegistry.instance().currentDefault();
		for (XTrace trace : variant.getTrace_list()) {
		    XAttributeBoolean attr = factory.createAttributeBoolean(attr_name, is_pos, null);
		    trace.getAttributes().put(attr.getKey(), attr);
		}	
	}
	
	private void assignVariantLabel(TraceVariant variant, String attr_name, double prob) {
		List<Integer> posidx_list = sample(variant.getCount(), prob);
		XFactory factory = XFactoryRegistry.instance().currentDefault();
		for (int idx =0; idx < variant.getCount(); idx++) {
			XTrace trace = variant.getTrace_list().get(idx);
			if(posidx_list.contains(idx)) {
				XAttributeBoolean attr = factory.createAttributeBoolean(attr_name, true, null);
			    trace.getAttributes().put(attr.getKey(), attr);
			}else {
				XAttributeBoolean attr = factory.createAttributeBoolean(attr_name, false, null);
			    trace.getAttributes().put(attr.getKey(), attr);
			}
		}
	}
	
	// one function to generate a sublist of index w.r.t. probability
	private List<Integer> sample(int bound, double prob){
		ArrayList<Integer> idx_list = new ArrayList<>();
		Random random = new Random();
		int index, num = ((int) prob*bound);
		while(num>0) {
			num--;
			index = random.nextInt(bound);
			if(random.nextDouble()< prob && !idx_list.contains(index)) 
				idx_list.add(index);
			
		}
		return idx_list;
	}
}
