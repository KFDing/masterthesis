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
		
		PetrinetWithMarkings mnet = null;
		try {
			mnet = ProcessTree2Petrinet.convert(tree, true);
		} catch (NotYetImplementedException | InvalidProcessTreeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		generator = new NewXORPairGenerator<ProcessTreeElement>();
	    generator.generatePairs(tree);
	    
	    List<XORClusterPair<ProcessTreeElement>> clusterPairs = generator.getClusterPair();
	    if(clusterPairs.isEmpty()) {
	    	System.out.println("no need to to add long-term dependency");
	    	return mnet;
	    }
		
		
	    Set<NewLTConnection<ProcessTreeElement>> connSet = generator.getAllLTConnection();
	    
	    tlmaps = getProcessTree2EventMap(log, tree , null);
	    initializeConnection(connSet);
	    adaptConnectionValue(connSet, parameters);
	    
	    // detector.detectPairWithLTDependency(pairs, parameters);
	    detectXORClusterLTDependency(clusterPairs);
	    
    	net = mnet.petrinet;
		tnMap = getProcessTree2NetMap(net, tree, null);
		
		pnNodeMap = new HashMap<String, PetrinetNode>();
		addClusterLT2Net(tree.getRoot());
	    return mnet; 
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
		// we adpat all the concrete connection, and check its connection 
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
	 * we go into the structure again and then add places or silent transitions here; 
	 * @param node 
	 */
	public void addClusterLT2Net(Node node) {
		
		XORCluster<ProcessTreeElement> cluster = generator.getCluster(node);
		
		if(cluster!=null) {
			
			List<XORCluster<ProcessTreeElement>> childrenCluster = cluster.getChildrenCluster();
			for(XORCluster<ProcessTreeElement> child : childrenCluster) {
				// still not good effect... Nanan, because one level missed it
				if(!child.isLtVisited()) {
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
						// here we might need changes, because of difficulty to add places for parallel
						// we need to get them according different situations
						
						for(XORCluster<ProcessTreeElement> schild: sourceCluster.getEndXORList())
							for(XORCluster<ProcessTreeElement> tchild: targetCluster.getBeginXORList()) {
								pair = generator.findClusterPair(schild, tchild);
								if(pair != null)
									addLTOnXOR(pair);
							}
						
						sourceCluster = targetCluster;
						i++;
					}
				}
			}
			cluster.setLtVisited(true);
		}
		
	}

	// if it one is nested xor, one is not nested xor?? 
	// and then both is nested somehow??
	
	private void addLTOnXOR(XORClusterPair<ProcessTreeElement> pair) {
		// pair is one nested and then other is not nested
		XORCluster<ProcessTreeElement> sourceCluster, targetCluster;
		sourceCluster = pair.getSourceXORCluster();
		targetCluster = pair.getTargetXORCluster();
		
		
		
		for(XORCluster<ProcessTreeElement> sBranch: sourceCluster.getEndXORBranch()) {
			
			// but how to find the subBranchPair and get the relation of them till back ?? 
			List<XORClusterPair<ProcessTreeElement>> subBranchPairs = findLTBranchClusterPair(pair, sBranch, targetCluster);
			if(subBranchPairs.isEmpty())
				continue;
			Place sBranchPlace = addBranchPlace(net, sBranch);
			for(XORClusterPair<ProcessTreeElement> subPair: subBranchPairs)
				addLTInBranch(subPair, sBranchPlace);
				
			}
			
	}
	
	private List<XORClusterPair<ProcessTreeElement>> findLTBranchClusterPair(XORClusterPair<ProcessTreeElement> pair, XORCluster<ProcessTreeElement> branch,
			XORCluster<ProcessTreeElement> targetCluster) {
		// TODO this method return the subBranchPair on target 
		List<XORClusterPair<ProcessTreeElement>> branchPairList = new ArrayList<XORClusterPair<ProcessTreeElement>>();
		// we should get the branch which source they belong to 
		XORCluster<ProcessTreeElement> sourceXOR = null ;
		for(XORCluster<ProcessTreeElement> child: pair.getSourceXORCluster().getChildrenCluster()) {
			if(child.getEndXORBranch().contains(branch)) {
				sourceXOR = child;
				break;
			}
		}
		
		// then find the one with connection, what if there are many?? SO we need a list
		for(XORClusterPair<ProcessTreeElement> subPair: pair.getLtBranchClusterPair()) {
			if(subPair.getSourceXORCluster().equals(sourceXOR))
				if(subPair.isConnected()) {
					branchPairList.add(subPair);
				}
		}
		
		return branchPairList;
	}


	private Place addBranchPlace(Petrinet net2, XORCluster<ProcessTreeElement> sBranch) {
		
		// it shoudl generate from the 
		String sourceBranchLabel = sBranch.getLabel();
		// get the end node list of sBranch and beginNodeList of tBranch, we will check the result
		List<ProcessTreeElement> sNodeList = sBranch.getEndNodeList();
		
		String keyName;
		///// to merge all the sNode here by creating one silent transition
		// first to create the transition here
		Transition sTransition = null;
		if(! pnNodeMap.containsKey(sourceBranchLabel)) {
			sTransition = net.addTransition(sourceBranchLabel );
			sTransition.setInvisible(true);
			pnNodeMap.put(sourceBranchLabel , sTransition);
		}else {
			sTransition = (Transition) pnNodeMap.get(sourceBranchLabel);
		}
		
		// add places on them
		// if sNodeList is greater than 1, so we merge the two nodes places into one silent transiton
		for(ProcessTreeElement sNode: sNodeList) {
			// check if there is the place after it, if we have found it the pnNodeMap, then we use them directly
			PetrinetNode endNode = tnMap.get(sNode);
			keyName = ProcessConfiguration.POST_PREFIX + endNode.getLabel()+ sourceBranchLabel;
			Place postNode;
			if(!pnNodeMap.containsKey(keyName)) {
				postNode = addPlace(net, endNode, sourceBranchLabel, true);
				// we need to add the silent transition after it
				pnNodeMap.put(keyName, postNode);
			}else
				postNode = (Place) pnNodeMap.get(keyName);
			
			net.addArc(postNode, sTransition);

		}
		// generate one place here 
		// now create place for branch connection, only one is enough	
		keyName = ProcessConfiguration.POST_PREFIX + sourceBranchLabel;
		Place branchPlace;
		if(!pnNodeMap.containsKey(keyName)) {
			branchPlace = net.addPlace(keyName);
			
			pnNodeMap.put(keyName, branchPlace);
		}else
			branchPlace = (Place) pnNodeMap.get(keyName);
		
		net.addArc(sTransition, branchPlace);
		
		return branchPlace;
	}
	
	// we should add places w.r.t. the branch connection situations!!!! 
	private void addLTInBranch(XORClusterPair<ProcessTreeElement> branchPair, Place connPlace) {
		// TODO this method is used to connec to target cluster by providing this place from the sourcetarget
		// until the deeper solution, after guided
		XORCluster<ProcessTreeElement> sourceCluster, targetCluster;
		sourceCluster = branchPair.getSourceXORCluster();
		targetCluster = branchPair.getTargetXORCluster();
		String sourcePairLabel = branchPair.getSourceXORCluster().getLabel();
		String targetPairLabel = branchPair.getTargetXORCluster().getLabel();
		
		if(targetCluster.isPureBranchCluster()) {
			
			Transition tTransition = null;
			if(! pnNodeMap.containsKey(targetPairLabel)) {
				tTransition = net.addTransition(targetPairLabel );
				tTransition.setInvisible(true);
				pnNodeMap.put(targetPairLabel , tTransition);
			}else {
				tTransition = (Transition) pnNodeMap.get(targetPairLabel);
			}
			
			// if tNodeList is greater than 1, we split the silent transition into two places 
			// then connect them together 
			List<ProcessTreeElement> tNodeList = targetCluster.getBeginNodeList();
			for(ProcessTreeElement tNode: tNodeList) {
				// generate places pre them 
				PetrinetNode beginNode = tnMap.get(tNode);
				Place preNode;
				String keyName = ProcessConfiguration.PRE_PREFIX + beginNode.getLabel()+ targetPairLabel;
				if(!pnNodeMap.containsKey(keyName)) {
					 preNode = addPlace(net, beginNode, targetPairLabel, false);
					 pnNodeMap.put(keyName, preNode);
				}else
					preNode = (Place) pnNodeMap.get(keyName);
				
				net.addArc(tTransition, preNode);
			}
			
			net.addArc(connPlace, tTransition);
			
		}else if(targetCluster.isSeqCluster()) {
			// we need to find out the subBranchPair, but before we need to pass the place furthers
			List<XORClusterPair<ProcessTreeElement>> subltBranchPairs = branchPair.getLtBranchClusterPair();
			
			if(subltBranchPairs.size() >0 ) {
				// there is only one xor cluster there
				for(XORClusterPair<ProcessTreeElement> subBranchPair: subltBranchPairs) {
					addLTInBranch(subBranchPair, connPlace);
				}
			}
			
		}else if(targetCluster.isParallelCluster()){
			// if it is parallel, then we need to generate one silent transition and then places for each branchPair
			Transition tTransition = null;
			List<XORClusterPair<ProcessTreeElement>> subltBranchPairs = branchPair.getLtBranchClusterPair();
			
			if(subltBranchPairs.size() >0 ) {
				// go for ltbranch 
				for(XORClusterPair<ProcessTreeElement> subBranchPair: subltBranchPairs) {
					// but we should mark it belong also to source cluster
					if(! pnNodeMap.containsKey(sourcePairLabel + targetPairLabel)) {
						tTransition = net.addTransition(sourcePairLabel + targetPairLabel );
						tTransition.setInvisible(true);
						pnNodeMap.put(sourcePairLabel + targetPairLabel , tTransition);
					}else {
						tTransition = (Transition) pnNodeMap.get(sourcePairLabel + targetPairLabel);
					}
					
					
					// generate places for each xor cluster in parallel
					XORCluster<ProcessTreeElement> subtBranch = subBranchPair.getTargetXORCluster();
					// for each subtBranch
					Place preNode;
					String keyName = ProcessConfiguration.PRE_PREFIX + tTransition.getLabel()+ subtBranch.getLabel();
					if(!pnNodeMap.containsKey(keyName)) {
						 preNode = addPlace(net, tTransition, subtBranch.getLabel(), false);
						 pnNodeMap.put(keyName, preNode);
					}else
						preNode = (Place) pnNodeMap.get(keyName);
					
					net.addArc(tTransition, preNode);
					
					addLTInBranch(subBranchPair, preNode);
				}
				
				net.addArc(connPlace, tTransition);
			}
			
		}else if(targetCluster.isXORCluster()) {
			// both situations works ok.. don't worry
			List<XORClusterPair<ProcessTreeElement>> subltBranchPairs = branchPair.getLtBranchClusterPair();
			
			if(subltBranchPairs.size() >0 ) {
				// there is only one xor cluster there
				for(XORClusterPair<ProcessTreeElement> subBranchPair: subltBranchPairs) {
					addLTInBranch(subBranchPair, connPlace);
				}
			}
			
		}else {
			System.out.println("we can deal with this situation");
		} 
		
	}


	// break the program down and make them easy to complete,
	// one situation only add long-term dependency on the not nested xor structure
	// but should we just add the long-term dependency on branch?? we are not in a branch anyway 
	// only for pair not in branch, so what happens if it is in branch?? 
	private void addLTOnNotNestedXORNotInBranch(XORClusterPair<ProcessTreeElement> pair) {
		// it is not in branch
		String sourcePairLabel = pair.getSourceXORCluster().getLabel();
		String targetPairLabel = pair.getTargetXORCluster().getLabel();
		// check the ltBranchConnection
		List<XORClusterPair<ProcessTreeElement>> ltBranchPairs = pair.getLtBranchClusterPair();
		if(ltBranchPairs.size() >0 ) {
			for(XORClusterPair<ProcessTreeElement> branchPair: ltBranchPairs) {
				// get one branch Pair and add dependency on it
				XORCluster<ProcessTreeElement> sBranch, tBranch;
				sBranch = branchPair.getSourceXORCluster();
				tBranch = branchPair.getTargetXORCluster();
				String sourceBranchLabel = sBranch.getLabel();
				String targetBranchLabel = tBranch.getLabel();
				// get the end node list of sBranch and beginNodeList of tBranch, we will check the result
				List<ProcessTreeElement> sNodeList = sBranch.getEndNodeList();
				List<ProcessTreeElement> tNodeList = tBranch.getBeginNodeList();
				
				String keyName;
				///// to merge all the sNode here by creating one silent transition
				// first to create the transition here
				Transition sTransition = null;
				if(! pnNodeMap.containsKey(sourceBranchLabel)) {
					sTransition = net.addTransition(sourceBranchLabel );
					sTransition.setInvisible(true);
					pnNodeMap.put(sourceBranchLabel , sTransition);
				}else {
					sTransition = (Transition) pnNodeMap.get(sourceBranchLabel);
				}
				
				// add places on them
				// if sNodeList is greater than 1, so we merge the two nodes places into one silent transiton
				for(ProcessTreeElement sNode: sNodeList) {
					// check if there is the place after it, if we have found it the pnNodeMap, then we use them directly
					PetrinetNode endNode = tnMap.get(sNode);
					keyName = ProcessConfiguration.POST_PREFIX + endNode.getLabel()+ sourceBranchLabel;
					Place postNode;
					if(!pnNodeMap.containsKey(keyName)) {
						postNode = addPlace(net, endNode, sourceBranchLabel, true);
						// we need to add the silent transition after it
						pnNodeMap.put(keyName, postNode);
					}else
						postNode = (Place) pnNodeMap.get(keyName);
					
					net.addArc(postNode, sTransition);

				}
				//////////////////// end of the branch creation for the whole  
				
				// now create place for branch connection, only one is enough	
				keyName = ProcessConfiguration.POST_PREFIX + sourceBranchLabel + targetPairLabel;
				Place branchPlace;
				if(!pnNodeMap.containsKey(keyName)) {
					branchPlace = addPlace(net, sTransition, targetPairLabel, true);
					// we need to add the silent transition after it
					pnNodeMap.put(keyName, branchPlace);
				}else
					branchPlace = (Place) pnNodeMap.get(keyName);
				
				//////////////////// begin of the transiton for tBranches 
				// first to create the silent transition 
				Transition tTransition = null;
				if(! pnNodeMap.containsKey(targetBranchLabel)) {
					tTransition = net.addTransition(targetBranchLabel );
					tTransition.setInvisible(true);
					pnNodeMap.put(targetBranchLabel , tTransition);
				}else {
					tTransition = (Transition) pnNodeMap.get(targetBranchLabel);
				}
				
				// if tNodeList is greater than 1, we split the silent transition into two places 
				// then connect them together 
				for(ProcessTreeElement tNode: tNodeList) {
					// generate places pre them 
					PetrinetNode beginNode = tnMap.get(tNode);
					Place preNode;
					keyName = ProcessConfiguration.PRE_PREFIX + beginNode.getLabel()+ targetBranchLabel;
					if(!pnNodeMap.containsKey(keyName)) {
						 preNode = addPlace(net, beginNode, targetBranchLabel, false);
						 pnNodeMap.put(keyName, preNode);
					}else
						preNode = (Place) pnNodeMap.get(keyName);
					
					net.addArc(tTransition, preNode);
				}
				
				// connect branch place with silent transition
				net.addArc(sTransition, branchPlace);
				net.addArc(branchPlace, tTransition);
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
