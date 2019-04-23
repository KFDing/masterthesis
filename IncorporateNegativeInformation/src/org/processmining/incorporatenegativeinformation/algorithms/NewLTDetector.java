package org.processmining.incorporatenegativeinformation.algorithms;
/**
 * this class is used to detect LT dependency between cluster pair.. 
 *  --- check all the traceVariant path in the tree
 *    --- check each cluster Pair to see if they have such connection
 */
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetImpl;
import org.processmining.incorporatenegativeinformation.help.EventLogUtilities;
import org.processmining.incorporatenegativeinformation.help.ProcessConfiguration;
import org.processmining.incorporatenegativeinformation.models.LTRule;
import org.processmining.incorporatenegativeinformation.models.LabeledTraceVariant;
import org.processmining.incorporatenegativeinformation.models.XORCluster;
import org.processmining.incorporatenegativeinformation.models.XORClusterPair;
import org.processmining.incorporatenegativeinformation.parameters.ControlParameters;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
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
	@SuppressWarnings("deprecation")
	PetrinetWithMarkings mnet;
	// how can we reset the original model?? By deleting all the places ??
	// we would say, ok, let's do it in this way, not the other way around on it 
	Petrinet net;
	Petrinet dnet = null;
	Map<Node, XEventClass> tlmaps;
	Map<XEventClass, Node> ltmaps;
	Map<LabeledTraceVariant, List<Node>> vpmaps;
	AddLT2Net adder;
	long traceNum = 0;

	@SuppressWarnings("deprecation")
	public NewLTDetector(ProcessTree pTree, XLog xlog, ControlParameters parameters, long tNum) {
		// there is no implemented way to clone it
		tree = pTree;
		log = xlog;
		this.parameters = parameters;
		traceNum = tNum;
		tlmaps = getProcessTree2EventMap( tree, log, null);
		ltmaps = AlignmentChecker.getEvent2ProcessTreeMap(xlog, pTree, null);
		
		
		try {
			// keep it not change!!! 
			mnet = ProcessTree2Petrinet.convert(tree, true);
			net = mnet.petrinet;
			adder = new AddLT2Net(net, mnet.mapPath2Transitions);
		} catch (NotYetImplementedException | InvalidProcessTreeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// reset the LTDetector, it happens when changing from the whole lt to single lt
	// or remove one lt, add one lt..so on
	// net returns to the one without any lt on it.. but the net is from the pTree, 
	// so for the NewLTDetector, the net should be fixed under this condition
	public void reset(List<XORClusterPair<ProcessTreeElement>> clusterPairs) {
		// here we check all the elements if it is not included in map, 
		// then we delete them from old times
		if(clusterPairs!=null && clusterPairs.size() >0) {
			for(XORClusterPair<ProcessTreeElement> pair: clusterPairs)
				rmLTOnSinglePair(pair);
			
			adder.initializeAdder();
		}
	}

	public void addLTOnPairList(List<XORClusterPair<ProcessTreeElement>> clusterPairs,
			List<LTRule<XORCluster<ProcessTreeElement>>> connSet) {
		// one mistake here, if we use all the connSet, then wehen adaptConnectionValue
		// it can't make sure we have the right connSet, 
		// on each clusterPair, we do it??  initializeConnection and then adapthConnectionValue
		// by ?? 
		
		initializeConnection(connSet);
		adaptConnectionValue(connSet, parameters);

		// detector.detectPairWithLTDependency(pairs, parameters);
		detectXORClusterLTDependency(clusterPairs);

		for (XORClusterPair<ProcessTreeElement> pair : clusterPairs) {
			addLTOnSinglePair(pair);
		}

	}

	public Petrinet deleteSilentTransition(Petrinet net) {
		return SilentTransitionDeletor.deleteForSeq(net);
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

		// get the addned values nad then pnNodeMap remove them form this
		for (PetrinetNode node : pair.getPNMap().values())
			net.removeNode(node);

		pair.getPNMap().clear();

		source.setAsSource(false);
		target.setAsTarget(false);
	}

	public Petrinet getReducedPetriNet() {
		dnet = deleteSilentTransition(net);
		return dnet;
	}

	public AcceptingPetriNet getAcceptionPN() {
		// it return the current net with lt on it
		AcceptingPetriNet manet = new AcceptingPetriNetImpl(net, mnet.initialMarking, mnet.finalMarking);
		return manet;
	}

	public  Map<LabeledTraceVariant, List<Node>> findVariantPathMap(){
		Map<LabeledTraceVariant, List<Node>> vpmaps = new HashMap<>();
		
		List<LabeledTraceVariant> variants = EventLogUtilities.getLabeledTraceVariants(log, null);
		for (LabeledTraceVariant var : variants) {
			// for each var, we check for each xor pair 
			// firstly, to get the path in the tree
			List<XEventClass> traceVariant = var.getTraceVariant();
			
			List<Node> nodeVariant = new ArrayList<>();
			for(XEventClass eClass: traceVariant) {
				nodeVariant.add(ltmaps.get(eClass));
			}
			// path is related to each tree, so we don't need to change it, if 
			// we check single pair, so we should keep them into tone map??
			// even the variants of logs, we shouldn't use it double time
			// there are more activities from log than in the existing model, so what to do??
			List<Node> path = AlignmentChecker.getPathOnTree(tree, nodeVariant);
			if(!vpmaps.containsKey(var))
				vpmaps.put(var, path);
		}
		return vpmaps;
	}
	
	// fill the connection with base data from event log 
	public void initializeConnection(List<LTRule<XORCluster<ProcessTreeElement>>> connSet) {
		if(vpmaps == null)
			vpmaps = findVariantPathMap();	
		Set<LabeledTraceVariant> variants = vpmaps.keySet();
		for (LabeledTraceVariant var : variants) {
			
			List<Node> path = vpmaps.get(var);
			
			for (LTRule<XORCluster<ProcessTreeElement>> conn : connSet) {
				if(isLTConnOK(path, conn)) {
					ArrayList<Double> counts = new ArrayList<Double>();
					for (int i = 0; i < ProcessConfiguration.LT_IDX_NUM * 2; i++)
						counts.add(0.0); // initialize it counts
		
					counts.set(1, (double) var.getPosNum());
					counts.set(2, (double) var.getNegNum());
		
					conn.addConnectionValues(counts);
				}
			}
		}
		// we need one step to change the freq into the weight situations.. So we need to change it here
		// do it in the adaptValue steps!! 
	}

	public void adaptConnectionValue(List<LTRule<XORCluster<ProcessTreeElement>>> connSet,
			ControlParameters parameters) {
		// first to set the weight on connection
		// if we create a hashMap, and then find all with same target, then it solved the problem
		Map<String, List<LTRule<XORCluster<ProcessTreeElement>>>> connGroup = new HashMap<>();
		for (LTRule<XORCluster<ProcessTreeElement>> conn : connSet) {
			// get source String name 
			String sourceName = "";
			for (XORCluster<ProcessTreeElement> source : conn.getSources()) {
				sourceName += source.getLabel();
			}

			if (!connGroup.containsKey(sourceName)) {
				List<LTRule<XORCluster<ProcessTreeElement>>> tmpConnList = new ArrayList<LTRule<XORCluster<ProcessTreeElement>>>();
				tmpConnList.add(conn);
				connGroup.put(sourceName, tmpConnList);
			} else {
				connGroup.get(sourceName).add(conn);
			}
		}

		// after getting the connGroup, now create the weight for each connSet
		for (String keyName : connGroup.keySet()) {
			List<LTRule<XORCluster<ProcessTreeElement>>> tmpConnList = connGroup.get(keyName);
			// get the weight of them, existing, pos and neg.. into
			// one consideration is if we also put the total traces num as one standardCardinality?? 
			// If we put total traces num, then it address the whole situations.
			// like what we have done before it.. IF we only focus separately, some situation happen
			// which violates the noise in data, if we use standardCardinality, then we have?? 
			// 
			List<Double> groupSum = getGroupWeight(tmpConnList);
			
			// existing weight, because we have all conn, so we can have all branches after this
			// visit each tmpConnList and then assign the values on it 
			for (LTRule<XORCluster<ProcessTreeElement>> conn : tmpConnList) {
				for (int i = 0; i < ProcessConfiguration.LT_IDX_NUM; i++)
					if (groupSum.get(i) > 0)
						conn.setConnValue(i, 1.0 * conn.getConnValue(i) / groupSum.get(i));
					else {
						conn.setConnValue(i, 0.0);
					}
			}
		}

		// then adapt the weight on it 
		// we adpat all the concrete connection, and check its connection 
		for (LTRule<XORCluster<ProcessTreeElement>> conn : connSet) {
			// one to add the control parameter effect
			conn.adaptValue(ProcessConfiguration.LT_EXISTING_IDX, parameters.getExistWeight());
			conn.adaptValue(ProcessConfiguration.LT_POS_IDX, parameters.getPosWeight());
			conn.adaptValue(ProcessConfiguration.LT_NEG_IDX, parameters.getNegWeight());
		}
	}

	private List<Double> getGroupWeight(List<LTRule<XORCluster<ProcessTreeElement>>> tmpConnList) {
		// TODO 
		List<Double> sums = new ArrayList<>();

		for (int i = 0; i < ProcessConfiguration.LT_IDX_NUM; i++)
			sums.add(0.0);
		// here we need to unify the effect from positive and negative
		// else the bias caused from the absolute values
		// to get the relative values, we need to find out the portitions 
		// for the negative connections?? Also, the ones in positive
		// sums(pos)=
		for (LTRule<XORCluster<ProcessTreeElement>> conn : tmpConnList) {
			for (int i = 0; i < ProcessConfiguration.LT_IDX_NUM; i++)
				sums.set(i, sums.get(i) + conn.getConnValue(i));

		}
		// here we set the sum to be the counts from positive and negative
		//sums.set(1, sums.get(1) + sums.get(2));
		// sums.set(2, sums.get(1));
		/*
		 * Here some thing not right because if we consider the whole effect,
		 * we use the trace num but we then need to find all xor branches in the
		 * model and then divide it sums.add((double) tmpConnList.size());
		 * sums.add((double) traceNum); sums.add((double) traceNum);
		 */
		return sums;
	}

	// we need one method to test if clusterpair has long-term dependency
	// if we need to make all the pair list to go and then we find out the xor pair is complete
	public List<XORClusterPair<ProcessTreeElement>> detectXORClusterLTDependency(
			List<XORClusterPair<ProcessTreeElement>> clusterPairs) {
		// but how could we detect it somehow?? Because we also create the childrenCluster from it, so if we want to test 
		// if they are complete, we can goes into its children but record it there 
		int i = 0;
		while (i < clusterPairs.size()) {
			XORClusterPair<ProcessTreeElement> pair = clusterPairs.get(i);
			pair.testConnected();
			if (pair.isComplete()) {
				System.out.format("This pair with source %s, target %s has no dependency. %n",
						pair.getSourceXORCluster().getLabel(), pair.getTargetXORCluster().getLabel());

				clusterPairs.remove(i);
			} else
				i++;
		}
		return clusterPairs;
	}

	private boolean isLTConnOK(List<Node> path, LTRule<XORCluster<ProcessTreeElement>> conn) {
		// TODO check if conn exists in this path and in right order
		
		Set<Node> sources = new HashSet<Node>();
		Set<Node> targets = new HashSet<Node>();
		for(XORCluster<ProcessTreeElement> source: conn.getSources()) {
			sources.add((Node) source.getKeyNode());
		}
		for(XORCluster<ProcessTreeElement> target: conn.getTargets()) {
			targets.add((Node) target.getKeyNode());
		}
		// after we have the path of this traceVariant, what we can do next??
		// firstly to get order for this path, or we can do before.. but for the parallel, 
		// we don't matter this, so we can still have this order?? I don't know..
		// or we assume, the connection has its order, we just need to make sure if they exist
		if(path.containsAll(sources) && path.containsAll(targets)){
			List<Integer> sIdxList = findNodesIndex(sources, path);
			List<Integer> tIdxList = findNodesIndex(targets, path);
			
			if(Collections.max(sIdxList) < Collections.min(tIdxList)) {
				return true;
				
			}
		}
		return false;
	}
	

	private List<Integer> findNodesIndex(Set<Node> nodes, List<Node> path) {
		// give out the whole index for the set od nodes, 
		List<Integer> idxList = new ArrayList<>();
		int idx ;
		for(Node node: nodes) {
			idx = path.indexOf(node);
			idxList.add(idx);
		}
		return idxList;
	}

	
	private Map<Node, XEventClass> getProcessTree2EventMap(ProcessTree pTree, XLog xLog, XEventClassifier classifier) {
		// TODO generate the transfer from process tree to event classes in log
		Map<Node, XEventClass> map = new HashMap<Node, XEventClass>();
		Collection<Node> nodes = pTree.getNodes();
		// leave only the leaf nodes

		XEventClasses classes = null;
		if (classifier != null && xLog.getClassifiers().contains(classifier))
			classes = XLogInfoFactory.createLogInfo(xLog).getEventClasses(classifier);
		else
			classes = XLogInfoFactory.createLogInfo(xLog).getNameClasses();

		XEventClass tauClassinLog = new XEventClass(ProcessConfiguration.Tau_CLASS, classes.size());
		// two situations: activity in log but not in model, activity in model but not in log.
		// but here, we just consider if the node from tree in log, if not in log, then tau event class
		boolean match;
		for (Node node : nodes) {
			if (!node.isLeaf())
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
			if (!match) {// it there is node not showing in the event log
				map.put(node, tauClassinLog);
			}
		}

		return map;
	}
	
	

}
