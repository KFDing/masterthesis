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
import org.processmining.models.graphbased.directed.AbstractDirectedGraph;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.ding.preprocess.util.Configuration;
import org.processmining.plugins.ding.preprocess.util.EventLogUtilities;
import org.processmining.plugins.ding.preprocess.util.LabeledTraceVariant;
import org.processmining.plugins.ding.process.dfg.model.ControlParameters;
import org.processmining.plugins.ding.process.dfg.model.LTConnection;
import org.processmining.plugins.ding.process.dfg.model.ProcessConfiguration;
import org.processmining.plugins.ding.process.dfg.model.XORBranch;
import org.processmining.plugins.ding.process.dfg.model.XORClusterPair;
import org.processmining.plugins.ding.process.dfg.model.XORPair;
import org.processmining.plugins.ding.process.dfg.model.XORStructure;
import org.processmining.plugins.ding.process.dfg.transform.XORPairGenerator;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.ProcessTreeElement;
import org.processmining.processtree.conversion.ProcessTree2Petrinet;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.InvalidProcessTreeException;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.NotYetImplementedException;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.PetrinetWithMarkings;
/**
 * still some obstacles around here, which worries me, but we can't waste time here, so continue
 * 
 * Here if we want to deal with the parallel situations with xor but in nested xor.. 
 * 
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
	
	@SuppressWarnings("deprecation")
	public static PetrinetWithMarkings buildPetrinetWithLT(XLog log, ProcessTree tree, ControlParameters parameters) {
		XORPairGenerator generator = new XORPairGenerator();
	    generator.generatePairs(tree);
	    
	    List<XORPair<ProcessTreeElement>> pairs = generator.getXORPair();
	    System.out.println("We have "+ pairs.size()+ " xor pairs");
	    for(XORPair<ProcessTreeElement> p: pairs) {
	    	System.out.println(p.getSourceXOR().getKeyNode());
	    	System.out.println(p.getTargetXOR().getKeyNode());
	    }
	    
	    List<XORClusterPair<ProcessTreeElement>> clusterPairs = generator.getClusterPair();
	    
	    LTDependencyDetector detector = new LTDependencyDetector(tree, log);
	    // initializeConnection, we only needs to do once?? Not really, every model changes, then one needs generated again
	    // and everytime there are control parameters there
	    detector.initializeConnection(pairs);
	    detector.adaptPairValue(pairs, parameters);
	    
	    // detector.detectPairWithLTDependency(pairs, parameters);
	    detector.detectXORClusterLTDependency(clusterPairs);
	    try {
	    	@SuppressWarnings("deprecation")
			PetrinetWithMarkings mnet = ProcessTree2Petrinet.convert(tree, true);
			Petrinet net = mnet.petrinet;
			// detector.addLTDependency2Net(net, pairs);
			detector.addClusterLT2Net(net, clusterPairs);
		    return mnet;
		} catch (NotYetImplementedException | InvalidProcessTreeException e) {
			System.out.println("The method transfering the process tree to net is old");
			e.printStackTrace();
		}
	    return null;  
	}

	// fill the connection with base data from event log 
	public void initializeConnection(List<XORPair<ProcessTreeElement>> pairList) {
		List<LabeledTraceVariant> variants = EventLogUtilities.getLabeledTraceVariants(log, null);
		for(LabeledTraceVariant var : variants) {
			// for each var, we check for each xor pair 
			for(XORPair<ProcessTreeElement> pair : pairList) {
				fillLTConnectionFreq(var, pair);
			}
		}
	}

	public void  adaptPairValue(List<XORPair<ProcessTreeElement>> pairList, ControlParameters parameters) {
		int i=0;
		while( i< pairList.size()) {
			XORPair<ProcessTreeElement> pair = pairList.get(i);
			pair.adaptLTConnection(ProcessConfiguration.LT_POS_IDX, parameters.getPosWeight());
			pair.adaptLTConnection(ProcessConfiguration.LT_NEG_IDX, parameters.getNegWeight());
			i++;
		}
		
	}
	// detect LT dependency according to the threshold from pos and neg data
	public List<XORPair<ProcessTreeElement>>  detectPairWithLTDependency(List<XORPair<ProcessTreeElement>> pairList, ControlParameters parameters) {
		// here we change the value according to the parameters: pos and neg values to the pair connection, and then detectPair
		int i=0;
		while( i< pairList.size()) {
			XORPair<ProcessTreeElement> pair = pairList.get(i);
			pair.adaptLTConnection(ProcessConfiguration.LT_POS_IDX, parameters.getPosWeight());
			pair.adaptLTConnection(ProcessConfiguration.LT_NEG_IDX, parameters.getNegWeight());
			//// here needs changes 
			if(pair.hasCompleteConnection()) {
				pairList.remove(i);// if we remove i, what we should add later?? we need to test on it!!
			}else 
				i++;
		}
		// after this we get all pairs with lt dependency
		return pairList;
	}
	
	// we need one method to test if clusterpair has long-term dependency
	// if we need to make all the pair list to go and then we find out the xor pair is complete
	public List<XORClusterPair<ProcessTreeElement>> detectXORClusterLTDependency(List<XORClusterPair<ProcessTreeElement>> clusterPairs){
		// but how could we detect it somehow?? Because we also create the childrenCluster from it, so if we want to test 
		// if they are complete, we can goes into its children but record it there 
		int i=0;
		while(i< clusterPairs.size()) {
			XORClusterPair<ProcessTreeElement> pair = clusterPairs.get(i);
			
			if(pair.testComplete()) {
				clusterPairs.remove(i);
			}else
				i++;
		}
		return clusterPairs;
	}
	public void addClusterLT2Net(Petrinet net, List<XORClusterPair<ProcessTreeElement>> clusterPairs) {
		// TODO to add places and silent transitions on it 
		// but we do it later, now we test if methods before are right
		// how to add places, if we look for one cluster pair if it is available, then we add them directly,
		// else not, but we need to find out the proper structure to add them..
		// now we need to find the biggest structure but in the same level, so 
		// we needs the process tree again.. 
		// one hand to visit the tree and reach proper level, 
		// get cluster pair
		// generate the places in the petri net for source and target?? Are you sure?? 
		// check the dependency on it 
		
		
		// anyway to check the codes before
		
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
				PetrinetNode ruleSource = pnMap.get(conn.getSourceBranch().getEndNode());
				// here we need to make sure that they don't have another places to add into it..
				// but how to say thoses??? We have pair, post_prefix is alreday fixed
				// to identify it, we also need the targetLabel of it.. So how to get it ??
				String postLabel = ProcessConfiguration.POST_PREFIX + ruleSource.getLabel() + targetLabel;
				Place postNode = (Place) getPlace(sourceNodes, postLabel);
				
				PetrinetNode ruleTarget = pnMap.get(conn.getTargetBranch().getBeginNode());
				String preLabel = ProcessConfiguration.PRE_PREFIX + ruleTarget.getLabel() + sourceLabel;
				Place preNode = (Place) getPlace(targetNodes, preLabel);
				
				Transition sTransition = net.addTransition(ruleSource.getLabel() + ruleTarget.getLabel());
				sTransition.setInvisible(true);
				net.addArc(postNode, sTransition);
				net.addArc(sTransition, preNode);
				
			}
			i++;
		}
		return net;
	}
	

	private PetrinetNode getPlace(List<PetrinetNode> nodes, String postLabel) {
		// TODO we need to get the placce after this rule source, so how to get it ??
		// why so simple, because we have unique relation to make sure that arguments are unique
		for(PetrinetNode n: nodes)
			if(n.getLabel().equals(postLabel))
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
			sourceIdx = findNodeIndex(conn.getSourceBranch().getEndNode(), traceVariant);
			if(sourceIdx!=-1) {
				targetIdx = findNodeIndex(conn.getTargetBranch().getBeginNode(), traceVariant);
				if(targetIdx > sourceIdx) {
					// we add the freq into this connection
					ArrayList<Double> counts = new ArrayList<Double>();
					for(int i=0;i<ProcessConfiguration.LT_IDX_NUM *2;i++)
						counts.add(0.0); // initialize it counts
					
					counts.set(1, (double) var.getPosNum());
					counts.set(2, (double) var.getNegNum());
					counts.set(ProcessConfiguration.LT_POS_IDX, (double) var.getPosNum());
					counts.set(ProcessConfiguration.LT_NEG_IDX, (double) var.getNegNum());
					
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
