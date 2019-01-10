package org.processmining.plugins.ding.process.dfg.train;

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
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.ding.preprocess.util.Configuration;
import org.processmining.plugins.ding.preprocess.util.EventLogUtilities;
import org.processmining.plugins.ding.preprocess.util.LabeledTraceVariant;
import org.processmining.plugins.ding.process.dfg.model.ControlParameters;
import org.processmining.plugins.ding.process.dfg.model.LTRule;
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

public class NewLTDetector {
	ProcessTree tree;
	XLog log;
	Map<Node, XEventClass> tlmaps;
	Map<Node, Transition> tnMap;
	Petrinet net;
	NewXORPairGenerator<ProcessTreeElement> generator;
	Map<String, PetrinetNode> pnNodeMap ;
	
	List<LTRule<PetrinetNode>> ruleSet;
	AddLT2Net adder;
	public NewLTDetector(ProcessTree pTree, XLog xlog) {
		// there is no implemented way to clone it
		tree = pTree;
		log = xlog;
		
	}
	
	
	public PetrinetWithMarkings buildPetrinetWithLT(XLog log, ProcessTree tree, ControlParameters parameters) {
		
		tlmaps = getProcessTree2EventMap(log, tree , null);
		
		generator = new NewXORPairGenerator<ProcessTreeElement>();
	    generator.generatePairs(tree);
	    
	    List<XORClusterPair<ProcessTreeElement>> clusterPairs = generator.getClusterPair();
	    Set<LTRule<XORCluster<ProcessTreeElement>>> connSet = generator.getAllLTConnection();
	  
	    initializeConnection(connSet);
	    adaptConnectionValue(connSet, parameters);
	    
	    // detector.detectPairWithLTDependency(pairs, parameters);
	    detectXORClusterLTDependency(clusterPairs);
	    try {
	    	@SuppressWarnings("deprecation")
			PetrinetWithMarkings mnet = ProcessTree2Petrinet.convert(tree, true);
			net = mnet.petrinet;
			
			
			pnNodeMap = new HashMap<String, PetrinetNode>();
			ruleSet = new ArrayList<LTRule<PetrinetNode>>();
			adder = new AddLT2Net(net, tree);
			// after we get the
			addLTOnNet(tree.getRoot());
		    return mnet;
		} catch (NotYetImplementedException | InvalidProcessTreeException e) {
			System.out.println("The method transfering the process tree to net is old");
			e.printStackTrace();
		}
	    return null;  
	}

	// after we checked all the pair list, we need to add the dependency on them
	// nested or not nestes, parallel, or others, but do we need to visit pair again, or not ??
	public void addLTOnNet(Node node) {
		// we need to get the cluster with respect to this node
		XORCluster<ProcessTreeElement> cluster = generator.getCluster(node);
		
		if(cluster!=null) {
			
			List<XORCluster<ProcessTreeElement>> childrenCluster = cluster.getChildrenCluster();
			for(XORCluster<ProcessTreeElement> child : childrenCluster) {
				// we need to make sure if the child is pure branch, then we don't need to go into it 
				// the assignment of it should be done before
				if(!child.isLtAvailable()) {
					addLTOnNet((Node)child.getKeyNode());
				}
			}
			// after that we come to the small unit of xor, but how about the seq, parallel, the thing else
			// what to deal them ?? but whatever, we need to find the connection with the nodes in them!! 
			
			// if only leaf node is available, now, we need to check its begin and end node list
			if(cluster.isSeqCluster()) {
				// here we need to check the xor size in this cluster if it has no xor cluster, we don't need to consider it
				if(cluster.getChildrenCluster().size() < 2) {
					System.out.println("too few xor in sequence to connect it");
				}else {
					XORCluster<ProcessTreeElement> sourceCluster, targetCluster;
					sourceCluster = childrenCluster.get(0);
					int i=1;
					XORClusterPair<ProcessTreeElement> pair;
					while(i< childrenCluster.size()) {
						targetCluster = childrenCluster.get(i);
						pair = generator.findClusterPair(sourceCluster, targetCluster);
						// we need to reset the pnNodeMap and ruleSet
						if(pair!=null) {
							adder.initializeAdder();
							adder.addLTOnPair(pair);
							adder.connectSourceWithTarget();
						}
						sourceCluster = targetCluster;
						i++;
					}
					
				}
			}
			cluster.setLtAvailable(true);
		}
	}




	// fill the connection with base data from event log 
	public void initializeConnection(Set<LTRule<XORCluster<ProcessTreeElement>>> connSet) {
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

	public void  adaptConnectionValue(Set<LTRule<XORCluster<ProcessTreeElement>>> connSet, ControlParameters parameters) {
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
			List<Double> groupSum = getGroupWeight(tmpConnList); ; // new ArrayList<>();
			// existing weight, because we have all conn, so we can have all branches after this
			// visit each tmpConnList and then assign the values on it 
			for(LTRule<XORCluster<ProcessTreeElement>> conn: tmpConnList) {
				for(int i=0;i<ProcessConfiguration.LT_IDX_NUM;i++) 
					if(groupSum.get(i)>0)
						conn.setConnValue(i, conn.getConnValue(i)/ groupSum.get(i));
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
