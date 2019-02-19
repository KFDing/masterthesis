package org.processmining.plugins.ding.process.dfg.train;

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
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetImpl;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.plugins.ding.preprocess.util.Configuration;
import org.processmining.plugins.ding.preprocess.util.EventLogUtilities;
import org.processmining.plugins.ding.preprocess.util.LabeledTraceVariant;
import org.processmining.plugins.ding.process.dfg.model.ControlParameters;
import org.processmining.plugins.ding.process.dfg.model.LTRule;
import org.processmining.plugins.ding.process.dfg.model.ProcessConfiguration;
import org.processmining.plugins.ding.process.dfg.model.XORCluster;
import org.processmining.plugins.ding.process.dfg.model.XORClusterPair;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.ProcessTreeElement;
import org.processmining.processtree.conversion.ProcessTree2Petrinet;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.InvalidProcessTreeException;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.NotYetImplementedException;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.PetrinetWithMarkings;

public class NewLTDetector {
	ProcessTree tree;
	XLog log;
	ControlParameters parameters;
	AcceptingPetriNet manet;
	
	Map<Node, XEventClass> tlmaps;
	AddLT2Net adder;
	long traceNum =0;
	
	public NewLTDetector(ProcessTree pTree, XLog xlog, ControlParameters parameters, long tNum) {
		// there is no implemented way to clone it
		tree = pTree;
		log = xlog;
		this.parameters = parameters;
		traceNum = tNum;
		tlmaps = getProcessTree2EventMap(log, tree , null);
		
		@SuppressWarnings("deprecation")
		PetrinetWithMarkings mnet;
		try {
			mnet = ProcessTree2Petrinet.convert(tree, true);
			manet = new AcceptingPetriNetImpl(mnet.petrinet, mnet.initialMarking, mnet.finalMarking);
			
		} catch (NotYetImplementedException | InvalidProcessTreeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
    	Petrinet net = manet.getNet();
		adder = new AddLT2Net(net, tree);
	}
	
	// if sth changes, we need to create a new object of it, yes, a new object of it
	
	public void addLTOnPairList(List<XORClusterPair<ProcessTreeElement>> clusterPairs, 
			List<LTRule<XORCluster<ProcessTreeElement>>> connSet) {
		// one mistake here, if we use all the connSet, then wehen adaptConnectionValue
		// it can't make sure we have the right connSet, 
		// on each clusterPair, we do it??  initializeConnection and then adapthConnectionValue
		// by ?? 
		// 
	    initializeConnection(connSet);
	    adaptConnectionValue(connSet, parameters);
	    
	    // detector.detectPairWithLTDependency(pairs, parameters);
	    detectXORClusterLTDependency(clusterPairs);
	    
	    for(XORClusterPair<ProcessTreeElement> pair: clusterPairs) {
			addLTOnSinglePair(pair);
		}
		
	}
	
	public void addLTOnPairList(List<XORClusterPair<ProcessTreeElement>> clusterPairs) {
		// for each cluster pair we initialize connection and adaptConnectionValue
		for(XORClusterPair<ProcessTreeElement> pair: clusterPairs) {
			List<LTRule<XORCluster<ProcessTreeElement>>> connSet = pair.getConnection();
			initializeConnection(connSet); // from log, stays the same
			
			adaptConnectionValueForPair(connSet, parameters);
		}
		 
		
	}
	private void adaptConnectionValueForPair(List<LTRule<XORCluster<ProcessTreeElement>>> connSet,
			ControlParameters parameters2) {
		// TODO connSet is only for one pair, so if we use it, it can be used directly..
		// 
		
	}

	// if we want to add only one pair on petri net, or remove one from petri net, what to do ?
	public void addLTOnSinglePair(XORClusterPair<ProcessTreeElement> pair) {
		// but after this, we need to change the as source and as target setting here 
		pair.getSourceXORCluster().setAsSource(true);
		pair.getTargetXORCluster().setAsTarget(true);
		
		adder.initializeAdder();
		adder.addLTOnPair(pair);
		adder.connectSourceWithTarget();
		pair.setPNMap(adder.pnNodeMap);
	}
	
	// if we want to remove one pair from petri net
	public void rmLTOnSinglePair(XORClusterPair<ProcessTreeElement> pair) {
		// target with before, then delete it, but also the node there..
		// based on the net and remove it
		// also we have adder with pnNode in it, we need to delete it here
		XORCluster<ProcessTreeElement> source, target;
		source = pair.getSourceXORCluster();
		target = pair.getTargetXORCluster();
		
		Petrinet net = manet.getNet();
		// get the addned values nad then pnNodeMap remove them form this
		for(PetrinetNode node: pair.getPNMap().values())
			net.removeNode(node);
		
		pair.getPNMap().clear();
		
		source.setAsSource(false);
		target.setAsTarget(false);
	}

	private boolean containPlace(PetrinetNode pt, List<ProcessTreeElement> nodeList, boolean post) {
		// TODO if this place is after one the endNodeList, return true
		String name = pt.getLabel();
		String prefix;
		if(post)
			prefix = ProcessConfiguration.POST_PREFIX;
		else 
			prefix = ProcessConfiguration.PRE_PREFIX;
		
		for(ProcessTreeElement node: nodeList) {
			String nodeName = node.getName();
			
			if(name.contains(prefix) && name.contains(nodeName))
				return true;
			
		}
		return false;
	}

	public AcceptingPetriNet getAcceptionPN() {
		return manet;
	}
	
	// fill the connection with base data from event log 
	public void initializeConnection(List<LTRule<XORCluster<ProcessTreeElement>>> connSet) {
		List<LabeledTraceVariant> variants = EventLogUtilities.getLabeledTraceVariants(log, null);
		for(LabeledTraceVariant var : variants) {
			// for each var, we check for each xor pair 
			for(LTRule<XORCluster<ProcessTreeElement>> conn : connSet) {
				fillLTConnectionFreq(var, conn);
			}
		}
		// we need one step to change the freq into the weight situations.. So we need to change it here
		// do it in the adaptValue steps!! 
	}

	public void  adaptConnectionValue(List<LTRule<XORCluster<ProcessTreeElement>>> connSet, ControlParameters parameters) {
		// first to set the weight on connection
		// if we create a hashMap, and then find all with same target, then it solved the problem
		Map<String, List<LTRule<XORCluster<ProcessTreeElement>>>> connGroup = new HashMap();
		for(LTRule<XORCluster<ProcessTreeElement>>  conn: connSet) {
			// get source String name 
			String sourceName = "";
			for(XORCluster<ProcessTreeElement> source: conn.getSources()) {
				sourceName += source.getLabel();
			}
			
			if(!connGroup.containsKey(sourceName)) {
				List<LTRule<XORCluster<ProcessTreeElement>>> tmpConnList = new ArrayList<LTRule<XORCluster<ProcessTreeElement>>>();
				tmpConnList.add(conn);
				connGroup.put(sourceName, tmpConnList);
			}else {
				connGroup.get(sourceName).add(conn);
			}
		}
		
		// after getting the connGroup, now create the weight for each connSet
		for(String keyName : connGroup.keySet()) {
			List<LTRule<XORCluster<ProcessTreeElement>>> tmpConnList = connGroup.get(keyName);
			// get the weight of them, existing, pos and neg.. into
			// one consideration is if we also put the total traces num as one standardCardinality?? 
			// If we put total traces num, then it address the whole situations.
			// like what we have done before it.. IF we only focus separately, some situation happen
			// which violates the noise in data, if we use standardCardinality, then we have?? 
			// 
			List<Double> groupSum = getGroupWeight(tmpConnList); ; // new ArrayList<>();
			// existing weight, because we have all conn, so we can have all branches after this
			// visit each tmpConnList and then assign the values on it 
			for(LTRule<XORCluster<ProcessTreeElement>> conn: tmpConnList) {
				for(int i=0;i<ProcessConfiguration.LT_IDX_NUM;i++) 
					if(groupSum.get(i)>0)
						conn.setConnValue(i, 1.0*conn.getConnValue(i)/ groupSum.get(i));
					else {
						conn.setConnValue(i, 0.0);
					}
			}
		}
		
		// then adapt the weight on it 
		// we adpat all the concrete connection, and check its connection 
		for(LTRule<XORCluster<ProcessTreeElement>>  conn: connSet) {
			// one to add the control parameter effect
			conn.adaptValue(ProcessConfiguration.LT_EXISTING_IDX, parameters.getExistWeight());
			conn.adaptValue(ProcessConfiguration.LT_POS_IDX, parameters.getPosWeight());
			conn.adaptValue(ProcessConfiguration.LT_NEG_IDX, parameters.getNegWeight());	
		}
	}
	
	
	private List<Double> getGroupWeight(List<LTRule<XORCluster<ProcessTreeElement>>> tmpConnList) {
		// TODO 
		List<Double> sums = new ArrayList<>();
		
		for(int i=0;i<ProcessConfiguration.LT_IDX_NUM;i++)
			sums.add(0.0);
		
		for(LTRule<XORCluster<ProcessTreeElement>> conn: tmpConnList) {
			for(int i=0;i<ProcessConfiguration.LT_IDX_NUM;i++)
				sums.set(i, sums.get(i) + conn.getConnValue(i));
			
		}
		sums.set(1, sums.get(1)+sums.get(2));
		sums.set(2, sums.get(1));
		/*
		 * Here some thing not rightm because if we consider the whole effect, 
		 * we use the trace num but we then need to find all xor branches in the model
		 * and then divide it 
		sums.add((double) tmpConnList.size());
		sums.add((double) traceNum);
		sums.add((double) traceNum);
		*/
		return sums;
	}


	// we need one method to test if clusterpair has long-term dependency
	// if we need to make all the pair list to go and then we find out the xor pair is complete
	public List<XORClusterPair<ProcessTreeElement>> detectXORClusterLTDependency(List<XORClusterPair<ProcessTreeElement>> clusterPairs){
		// but how could we detect it somehow?? Because we also create the childrenCluster from it, so if we want to test 
		// if they are complete, we can goes into its children but record it there 
		int i=0;
		while(i< clusterPairs.size()) {
			XORClusterPair<ProcessTreeElement> pair = clusterPairs.get(i);
			pair.testConnected();
			if(pair.isComplete()) {
				System.out.format("This pair with source %s, target %s has no dependency. %n", pair.getSourceXORCluster().getLabel(),
						pair.getTargetXORCluster().getLabel());
				
				clusterPairs.remove(i);
			}else
				i++;
		}
		return clusterPairs;
	}
	
	private void fillLTConnectionFreq(LabeledTraceVariant var, LTRule<XORCluster<ProcessTreeElement>> conn) {
		// TODO fill the frequency for lt connection in pair.. But should we put the LTConnection into pair
		List<XEventClass> traceVariant = var.getTraceVariant();
		// even if we have sourceXOR but it includes branches, so we should go into the branches of sourceXOR
		int sourceIdx, targetIdx;
		// here if we find one represented node here, it is ok to decide it's fine 
		// here is only the pure branch, so we can make it, but if not we need another stategy
		XORCluster<ProcessTreeElement> source = conn.getSources().get(0);
		XORCluster<ProcessTreeElement> target = conn.getTargets().get(0);
		
		sourceIdx = findNodeIndex(source.getBeginNodeList().get(0), traceVariant);
		if(sourceIdx!=-1) {
			targetIdx = findNodeIndex(target.getBeginNodeList().get(0), traceVariant);
			if(targetIdx > sourceIdx) {
				// we add the freq into this connection
				ArrayList<Double> counts = new ArrayList<Double>();
				for(int i=0;i<ProcessConfiguration.LT_IDX_NUM *2;i++)
					counts.add(0.0); // initialize it counts
				
				counts.set(1, (double) var.getPosNum());
				counts.set(2, (double) var.getNegNum());
				
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
	
	
}
