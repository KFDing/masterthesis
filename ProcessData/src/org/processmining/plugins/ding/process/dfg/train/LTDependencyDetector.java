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
import java.util.Set;

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
import org.processmining.plugins.ding.process.dfg.model.NewLTConnection;
import org.processmining.plugins.ding.process.dfg.model.ProcessConfiguration;
import org.processmining.plugins.ding.process.dfg.model.XORCluster;
import org.processmining.plugins.ding.process.dfg.model.XORClusterPair;
import org.processmining.plugins.ding.process.dfg.transform.NewXORPairGenerator;
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
	Map<Node, XEventClass> tlmaps;
	Map<Node, Transition> tnMap;
	Petrinet net;
	NewXORPairGenerator<ProcessTreeElement> generator;
	Map<String, PetrinetNode> pnNodeMap ;
	
	public LTDependencyDetector(ProcessTree pTree, XLog xlog) {
		// there is no implemented way to clone it
		tree = pTree;
		log = xlog;
		
	}
	
	
	public PetrinetWithMarkings buildPetrinetWithLT(XLog log, ProcessTree tree, ControlParameters parameters) {
		
		tlmaps = getProcessTree2EventMap(log, tree , null);
		
		generator = new NewXORPairGenerator<ProcessTreeElement>();
	    generator.generatePairs(tree);
	    
	    List<XORClusterPair<ProcessTreeElement>> clusterPairs = generator.getClusterPair();
	    Set<NewLTConnection<ProcessTreeElement>> connSet = generator.getAllLTConnection();
	  
	    initializeConnection(connSet);
	    adaptConnectionValue(connSet, parameters);
	    
	    // detector.detectPairWithLTDependency(pairs, parameters);
	    detectXORClusterLTDependency(clusterPairs);
	    try {
	    	@SuppressWarnings("deprecation")
			PetrinetWithMarkings mnet = ProcessTree2Petrinet.convert(tree, true);
			net = mnet.petrinet;
			tnMap = getProcessTree2NetMap(net, tree, null);
			
			pnNodeMap = new HashMap<String, PetrinetNode>();
			addClusterLT2Net(tree.getRoot());
		    return mnet;
		} catch (NotYetImplementedException | InvalidProcessTreeException e) {
			System.out.println("The method transfering the process tree to net is old");
			e.printStackTrace();
		}
	    return null;  
	}

	// fill the connection with base data from event log 
	public void initializeConnection(Set<NewLTConnection<ProcessTreeElement>> connSet) {
		List<LabeledTraceVariant> variants = EventLogUtilities.getLabeledTraceVariants(log, null);
		for(LabeledTraceVariant var : variants) {
			// for each var, we check for each xor pair 
			for(NewLTConnection<ProcessTreeElement> conn : connSet) {
				fillLTConnectionFreq(var, conn);
			}
		}
	}

	public void  adaptConnectionValue(Set<NewLTConnection<ProcessTreeElement>> connSet, ControlParameters parameters) {
		
		for(NewLTConnection<ProcessTreeElement>  conn: connSet) {
			
			conn.adaptValue(ProcessConfiguration.LT_POS_IDX, parameters.getPosWeight());
			conn.adaptValue(ProcessConfiguration.LT_NEG_IDX, parameters.getNegWeight());	
		}
		
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
	
	/**
	 * we goes into the structure again and then add places or silent transitions here
	 * @param node the current visited node in process treee
	 * @param net
	 * @param clusterPairs
	 */
	public void addClusterLT2Net(Node node) {
		
		XORCluster<ProcessTreeElement> cluster = generator.getCluster(node);
		
		if(cluster!=null) {
			
			List<XORCluster<ProcessTreeElement>> childrenCluster = cluster.getChildrenCluster();
			for(XORCluster<ProcessTreeElement> child : childrenCluster) {
				// still not good effect... Nanan, because one level missed it
				if(!child.isAvailable()) {
					addClusterLT2Net((Node)child.getKeyNode());
				}
				
			}
			
			if(cluster.isSeqCluster()) { // all the elements are available, so we just do use it 
				// we just have the useful childrencluster to create pair 
				if(cluster.getChildrenCluster().size() < 2) {
					System.out.println("too few xor in sequence to connect it");
				}else {
					XORCluster<ProcessTreeElement> sourceCluster, targetCluster;
					sourceCluster = childrenCluster.get(0);
					int i=1;
					XORClusterPair<ProcessTreeElement> pair;
					while(i< childrenCluster.size()) {
						targetCluster = childrenCluster.get(i);
						
						for(XORCluster<ProcessTreeElement> schild: sourceCluster.getEndXORList())
							for(XORCluster<ProcessTreeElement> tchild: targetCluster.getBeginXORList()) {
								pair = generator.findClusterPair(schild, tchild);
								if(pair != null)
									addLTWithClusterPair(pair);
							}
						
						sourceCluster = targetCluster;
						i++;
					}
				}
			}
		}
		
	}

	// Place, or transition?? If we need one transition, 
	// we check if there is a place already connects to the transiton and the tragetCluster
	// if there is one place, then we use it, else, we need to generate one for it
	// if we use place directly, we need to generate it before, so my decision is?? 
	// we use transition!!! for the concrete, we just see it as the branchEndNodeList, one by one to check them
	// maybe it's a list, somehow ?? so we need to gather them together?? 
	private void addLTInBranch(XORClusterPair<ProcessTreeElement> branchPair, List<Transition> midTransitions, boolean singleSource) {
		// goes at first only single source
		XORCluster<ProcessTreeElement> sBranch, tBranch;
		sBranch = branchPair.getSourceXORCluster();
		tBranch = branchPair.getTargetXORCluster();
		String sourceLabel = sBranch.getLabel();
		String targetLabel = tBranch.getLabel();
		Transition sTransition = null;
		List<Transition> subMidTransitions = new ArrayList<Transition>();
		
		if(! pnNodeMap.containsKey(sBranch.getLabel() + tBranch.getLabel())) {
			sTransition = net.addTransition(sBranch.getLabel() + tBranch.getLabel());
			sTransition.setInvisible(true);
			pnNodeMap.put(sBranch.getLabel() + tBranch.getLabel(), sTransition);
		}else {
			sTransition = (Transition) pnNodeMap.get(sBranch.getLabel() + tBranch.getLabel());
		}
		subMidTransitions.add(sTransition);
		
		for(Transition t: midTransitions) {
			// check if there is the place after it, if we have found it the pnNodeMap, then we use them directly
			String keyName = ProcessConfiguration.POST_PREFIX + t.getLabel()+ targetLabel;
			Place postNode;
			if(!pnNodeMap.containsKey(keyName)) {
				postNode = addPlace(net, t, targetLabel, true);
				// we need to add the silent transition after it
				pnNodeMap.put(keyName, postNode);
			}else
				postNode = (Place) pnNodeMap.get(keyName);
			
			net.addArc(postNode, sTransition);

		}
		// after creating places, we generate one silent transition, and then goes deeper to see,
		// what happends with the targetBranch
		if(tBranch.isPureBranchCluster()) {
			
			List<ProcessTreeElement> tNodeList = tBranch.getBeginNodeList();
			// pure then we have EndNodeList
			for(ProcessTreeElement tNode: tNodeList) {
				// generate places pre them 
				PetrinetNode beginNode = tnMap.get(tNode);
				Place preNode;
				String keyName = ProcessConfiguration.PRE_PREFIX + beginNode.getLabel()+ sourceLabel;
				if(!pnNodeMap.containsKey(keyName)) {
					 preNode = addPlace(net, beginNode, sourceLabel, false);
					 pnNodeMap.put(keyName, preNode);
				}else
					preNode = (Place) pnNodeMap.get(keyName);
				
				net.addArc(sTransition, preNode);
			}
	
		}else {
			// else, we goes into deeper branch, but how to get all the transitions?? 
			
			for(XORClusterPair<ProcessTreeElement> subBranchPair : branchPair.getBranchClusterPair())
				addLTInBranch(subBranchPair, subMidTransitions, false);
		}
		
		
		
		
		
		
		// how about if the source target has branchClusterpair, then what to do ?? 
		// we need to mark which direction, we need to go, source or target
	}
	
	private void addLTWithClusterPair(XORClusterPair<ProcessTreeElement> pair) {
		// add long-term dependency within cluster pair
		
		// we only have xor to xor cluster pair, maybe nested, maybe not !! so we need to prepare for this!!
		XORCluster<ProcessTreeElement> sourceCluster, targetCluster;

		sourceCluster = pair.getSourceXORCluster();
		targetCluster = pair.getTargetXORCluster();
		String sourceLabel = sourceCluster.getLabel();
		String targetLabel = targetCluster.getLabel();
		
		Transition sTransition = null;
		// we are in a cluster pair
		for(XORClusterPair<ProcessTreeElement> branchPair : pair.getLtBranchClusterPair()) {
			// based on branch
			XORCluster<ProcessTreeElement> sBranch = branchPair.getSourceXORCluster();
			
			XORCluster<ProcessTreeElement> tBranch = branchPair.getTargetXORCluster();
			// we can't go this step until we are sure, they can use the sNodeList and tNodeList
			
			// branch can be considered directly.. we can't change sBranch, but we need to pass
			// the transition for it
			if(sBranch.isPureBranchCluster() && tBranch.isPureBranchCluster()) {
				// we just add them directly by using this
				List<ProcessTreeElement> sNodeList = sBranch.getEndNodeList();
				List<ProcessTreeElement> tNodeList = tBranch.getBeginNodeList();
				
				// but before we need to get the transition to add such places
				if(! pnNodeMap.containsKey(sBranch.getLabel() + tBranch.getLabel())) {
					sTransition = net.addTransition(sBranch.getLabel() + tBranch.getLabel());
					sTransition.setInvisible(true);
					pnNodeMap.put(sBranch.getLabel() + tBranch.getLabel(), sTransition);
				}else {
					sTransition = (Transition) pnNodeMap.get(sBranch.getLabel() + tBranch.getLabel());
				}
				
				
				// we have the post places there and then, we nned to do?? 
				for(ProcessTreeElement sNode: sNodeList) {
					// here the nodes alreay changes, if we add the silent transition, we shoudl looke for them from here
					// not just add the places here, could we also find the transitions like this??
					// somehow??
					PetrinetNode endNode = tnMap.get(sNode);
					// there is no post place exists, so we need to add them
					String keyName = ProcessConfiguration.POST_PREFIX + endNode.getLabel()+ targetLabel;
					Place postNode;
					if(!pnNodeMap.containsKey(keyName)) {
						postNode = addPlace(net, endNode, targetLabel, true);
						// we need to add the silent transition after it
						pnNodeMap.put(keyName, postNode);
					}else
						postNode = (Place) pnNodeMap.get(keyName);
					
					net.addArc(postNode, sTransition);
				}

				for(ProcessTreeElement tNode: tNodeList) {
					// generate places pre them 
					PetrinetNode beginNode = tnMap.get(tNode);
					Place preNode;
					String keyName = ProcessConfiguration.PRE_PREFIX + beginNode.getLabel()+ sourceLabel;
					if(!pnNodeMap.containsKey(keyName)) {
						 preNode = addPlace(net, beginNode, sourceLabel, false);
						 pnNodeMap.put(keyName, preNode);
					}else
						preNode = (Place) pnNodeMap.get(keyName);
					net.addArc(sTransition, preNode);
				}
				
			}else {
				
				addLTWithClusterPair(branchPair);
				// but how to connect places to transitions?? 
			}
		
			
		}

			
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


	private void fillLTConnectionFreq(LabeledTraceVariant var, NewLTConnection<ProcessTreeElement> conn) {
		// TODO fill the frequency for lt connection in pair.. But should we put the LTConnection into pair
		List<XEventClass> traceVariant = var.getTraceVariant();
		// even if we have sourceXOR but it includes branches, so we should go into the branches of sourceXOR
		int sourceIdx, targetIdx;
		sourceIdx = findNodeIndex(conn.getSourceBranch(), traceVariant);
		if(sourceIdx!=-1) {
			targetIdx = findNodeIndex(conn.getTargetBranch(), traceVariant);
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

	private int findNodeIndex(ProcessTreeElement keyNode, List<XEventClass> traceVariant) {
		// TODO get the index of process tree element, if it is in the tracevariant
		return traceVariant.indexOf(tlmaps.get(keyNode));
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
