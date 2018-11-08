package org.processmining.plugins.ding.process.dfg.train;
/**
 * this class is used as post processing. To detect the global dependency in the petri net
 * it is introduced to test my idea.
 * -- list all the global dependency that IM can't find out
 * -- how to detect the structure which contrains non-free choices[there two words, I exchange to use it]
 * -- use the negative information to address it 
 * -- create some relations and add places on it
 * @author dkf
 *
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.AbstractDirectedGraph;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.ding.preprocess.util.Configuration;
import org.processmining.plugins.ding.preprocess.util.EventLogUtilities;
import org.processmining.plugins.ding.preprocess.util.LabeledTraceVariant;
import org.processmining.plugins.ding.process.dfg.model.LTConnection;
import org.processmining.plugins.ding.process.dfg.model.ProcessConfiguration;
import org.processmining.plugins.ding.process.dfg.model.XORBranch;
import org.processmining.plugins.ding.process.dfg.model.XORPair;
import org.processmining.plugins.ding.process.dfg.model.XORStructure;
import org.processmining.plugins.ding.process.dfg.transform.XORPairGenerator;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.ProcessTreeElement;
import org.processmining.processtree.conversion.ProcessTree2Petrinet;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.InvalidProcessTreeException;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.NotYetImplementedException;
/**
 * still some obstacles around here, which worries me, but we can't waste time here, so continue
 * 
 * @author dkf
 *
 */
public class LTDependencyDetector {

	ProcessTree tree;
	XLog log;
	// from process tree
	// List<XORPair<ProcessTreeElement>> pairList;
	Map<Node, XEventClass> maps;
	
	public LTDependencyDetector(ProcessTree pTree, XLog xlog) {
		// there is no implemented way to clone it
		tree = pTree;
		log = xlog;
		maps = getProcessTree2EventMap(log, tree , null);
	}
	
	
	@Plugin(name = "Dfg incorporate negative information", level = PluginLevel.Regular, returnLabels = {"Petri Net" }, returnTypes = {Petrinet.class
	}, parameterLabels = { "XLog","Process Tree"}, userAccessible = true)
	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Kefang", email = "***@gmail.com", uiLabel = UITopiaVariant.USEVARIANT)
	@PluginVariant(variantLabel = "Add Long Term Dependency from  Process Tree",  requiredParameterLabels = { 0 , 1})
	public static Petrinet testMain(PluginContext context, XLog log, ProcessTree tree) 
	{ 
	    
		XORPairGenerator generator = new XORPairGenerator();
	    List<XORPair<ProcessTreeElement>> pairs = generator.generateXORPairs(tree);
	    System.out.println(pairs.size());
	    for(XORPair<ProcessTreeElement> p: pairs) {
	    	System.out.println(p.getSourceXOR().getKeyNode());
	    	System.out.println(p.getTargetXOR().getKeyNode());
	    }
	    
	    LTDependencyDetector detector = new LTDependencyDetector(tree, log);
	    detector.detectPairWithLTDependency(pairs);
	    
	    try {
			Petrinet net = ProcessTree2Petrinet.convert(tree, true).petrinet;
			detector.addLTDependency2Net(net, pairs);
		    return net;
		} catch (NotYetImplementedException | InvalidProcessTreeException e) {
			System.out.println("The method transfering the process tree to net is old");
			e.printStackTrace();
		}
	    return null;  
	} 


	// detect LT dependency
	public List<XORPair<ProcessTreeElement>>  detectPairWithLTDependency(List<XORPair<ProcessTreeElement>> pairList) {
		// one list to store the node which should have the connection but what to do ??
		// we can't use the map, if we use the list of them ?? Still some 
		
		List<LabeledTraceVariant> variants = EventLogUtilities.getLabeledTraceVariants(log, null);
		for(LabeledTraceVariant var : variants) {
			// for each var, we check for each xor pair 
			
			for(XORPair<ProcessTreeElement> pair : pairList) {
				
				fillLTConnectionFreq(var, pair);
			}
		}
		
		int i=0;
		while( i< pairList.size()) {
			XORPair<ProcessTreeElement> pair = pairList.get(i);
			if(pair.hasCompleteConnection()) {
				pairList.remove(i);// if we remove i, what we should add later?? we need to test on it!!
			}else 
				i++;
		}
		// after this we get all pairs with lt dependency
		return pairList;
	}
	
	// should we have another function to only add lt dependency to the petri net 
	public Petrinet addLTDependency2Net(Petrinet net, List<XORPair<ProcessTreeElement>> pairList)  {
	    
	    Map<Node, Transition> pnMap =  getProcessTree2NetMap(net, tree, null);
	    // here we should have the xor pairs with global dependency
	    int i=0;
		while( i< pairList.size()) {
			XORPair<ProcessTreeElement> pair = pairList.get(i);
			XORStructure<ProcessTreeElement> sourceXOR = pair.getSourceXOR();
			XORStructure<ProcessTreeElement> targetXOR = pair.getTargetXOR();
			String sourceLabel = sourceXOR.getLabel();
			String targetLabel = targetXOR.getLabel();
			List<PetrinetNode> sourceNodes =  new ArrayList<PetrinetNode>();
			
			List<PetrinetNode> targetNodes =  new ArrayList<PetrinetNode>();
			
			// create post places after end node of xor source branches and pre places before xor target branch begin
			for(XORBranch<ProcessTreeElement> sBranch: sourceXOR.getBranches()) {
				// create post place to the end of branch node
				// change the end node from process tree into the net
				PetrinetNode endNode = pnMap.get(sBranch.getEndNode());
				
				// add post place for it 
				Place postNode = addPlace(net, endNode, targetLabel, true);
				sourceNodes.add(postNode);
			}
			
			for(XORBranch<ProcessTreeElement> tBranch: targetXOR.getBranches()) {
				// create pre place to the begin node for one branch
				// change the begin node from process tree into the net
				PetrinetNode beginNode = pnMap.get(tBranch.getBeginNode());
				
				// add pre place for it 
				Place preNode = addPlace(net,beginNode, sourceLabel, false);
				targetNodes.add(preNode);
			}
			// check all the connection and add the silent transitions to it
			for(LTConnection<ProcessTreeElement> conn: pair.getLTDependency()) {
				// for all the connection with global dependency
				// but at first we need to find the added places for each
				PetrinetNode ruleSource = pnMap.get(conn.getFirstBranch().getEndNode());
				Place postNode = (Place) getPlace(sourceNodes, ruleSource);
				
				PetrinetNode ruleTarget = pnMap.get(conn.getSecondBranch().getBeginNode());
				Place preNode = (Place) getPlace(targetNodes, ruleTarget);
				
				Transition sTransition = net.addTransition(ruleSource.getLabel() + ruleTarget.getLabel());
				sTransition.setInvisible(true);
				net.addArc(postNode, sTransition);
				net.addArc(sTransition, preNode);
				
			}
			i++;
		}
		return net;
	}
	

	private PetrinetNode getPlace(List<PetrinetNode> nodes, PetrinetNode ruleSource) {
		// TODO we need to get the placce after this rule source, so how to get it ??
		// why so simple, because we have unique relation to make sure that arguments are unique
		for(PetrinetNode n: nodes)
			if(n.getLabel().contains(ruleSource.getLabel()))
				return n;
		return null;
	}

	private Place addPlace(Petrinet net, PetrinetNode node, String targetLabel, boolean post) {
		// TODO add PostPlace in this pair, we need to assign a good name to it 
		if(post) {
			Place postNode = net.addPlace(ProcessConfiguration.POST_PREFIX + node.getLabel()+ targetLabel);
			net.addArc((Transition) node, postNode);
			return postNode;
		}else {
			Place preNode = net.addPlace(ProcessConfiguration.PRE_PREFIX + node.getLabel()+ targetLabel);
			net.addArc(preNode, (Transition) node);
			return preNode;
		}
	}


	private void fillLTConnectionFreq(LabeledTraceVariant var, XORPair<ProcessTreeElement> pair) {
		// TODO fill the frequency for lt connection in pair.. But should we put the LTConnection into pair
		List<XEventClass> traceVariant = var.getTraceVariant();
		List<LTConnection<ProcessTreeElement>> connections = pair.getLTConnections();
		// even if we have sourceXOR but it includes branches, so we should go into the branches of sourceXOR
		int sourceIdx, targetIdx;
		for(LTConnection<ProcessTreeElement> conn: connections) {
			sourceIdx = findNodeIndex(conn.getFirstBranch().getEndNode(), traceVariant);
			if(sourceIdx!=-1) {
				targetIdx = findNodeIndex(conn.getSecondBranch().getBeginNode(), traceVariant);
				if(targetIdx > sourceIdx) {
					// we add the freq into this connection
					ArrayList<Double> counts = new ArrayList<Double>();
					counts.add(0, 0.0); // for existing ones
					counts.add((double) var.getPosNum());
					counts.add((double) var.getNegNum());
					
					conn.addConnectionValues(counts);
				}
			}
		}
	}

	private int findNodeIndex(ProcessTreeElement keyNode, List<XEventClass> traceVariant) {
		// TODO get the index of process tree element, if it is in the tracevariant
		return traceVariant.indexOf(maps.get(keyNode));
	}

	private Map<Node, XEventClass> getProcessTree2EventMap(XLog xLog, ProcessTree pTree, XEventClassifier classifier) {
		// TODO generate the transfer from process tree to event classes in log
		Map<Node, XEventClass> map = new HashMap<Node, XEventClass>();
		Collection<Node> nodes = pTree.getNodes();
		// leave only the leaf nodes
		
		
		XEventClasses classes = null;
		if(classifier != null && log.getClassifiers().contains(classifier)) 
			classes = XLogInfoFactory.createLogInfo(xLog).getEventClasses(classifier);
		else
			classes = XLogInfoFactory.createLogInfo(xLog).getNameClasses();
		
		XEventClass tauClassinLog = new XEventClass(Configuration.Tau_CLASS, classes.size());
		
		boolean match;
		for (Node node : nodes) {
			if(!node.isLeaf())
				continue;
			
			match = false;
			for (XEventClass eventClass : classes.getClasses()) { // transition.getLabel()
				// here we need to create a mapping from event log to graphs
				// need to check at first what the Name and other stuff
				if (eventClass.getId().equals(node.getName())) {
					map.put(node, eventClass);
					match = true;
					break;
				}
			}
			if(! match) {// it there is node not showing in the event log
				map.put(node, tauClassinLog);
			}
		}
		
		return map;
	}
	
	
	private Map<Node, Transition> getProcessTree2NetMap(Petrinet net, ProcessTree pTree, XEventClassifier classifier) {
		// TODO generate the transfer from process tree to event classes in log
		Map<Node, Transition> map = new HashMap<Node, Transition>();
		Collection<Node> nodes = pTree.getNodes();
		Collection<Transition> transitions = net.getTransitions();
		
		Transition tauTransition = new Transition(ProcessConfiguration.Tau_CLASS, (AbstractDirectedGraph<PetrinetNode, PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>>) net);
		
		boolean match;
		for (Node node : nodes) {
			if(!node.isLeaf())
				continue;
			
			match = false;
			for (Transition transition : transitions) {
				// here we need to create a mapping from event log to graphs
				// need to check at first what the Name and other stuff
				if (node.getName().equals(transition.getLabel())) {
					map.put(node, transition);
					match = true;
					break;
				}
			}
			if(! match) {// it there is node not showing in the petri net, which we don't really agree
				map.put(node, tauTransition);
			}
		}
		
		return map;
	}
}
