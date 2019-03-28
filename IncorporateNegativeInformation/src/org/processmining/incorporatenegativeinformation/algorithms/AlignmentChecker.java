package org.processmining.incorporatenegativeinformation.algorithms;

import java.util.Collection;
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
import org.processmining.incorporatenegativeinformation.models.XORCluster;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.ProcessTreeElement;
/**
 * this relates to the alignment on the process tree
 * @author ding
 *
 */
public class AlignmentChecker {
	
	// this method checks if one node is in the necessary alignment(Path)
	public static boolean isNodeOnPath(Node snode, List<XEventClass> traceVariant, 
			List<XORCluster<ProcessTreeElement>> clusterList) {
		
		// 1. get all the leaf nodes for each event class in traceVariant
			// it might have event class not in the process tree... If so, we don't consider this trace?
			// or we keep it, only check if this is on the path???? 
			// because of xor it has several choices of execution
		// find the corresponding tree element on it 
		// get the leaf cluster for each eventClass one map on them
		Map<XEventClass, XORCluster<ProcessTreeElement>> map = new HashMap<XEventClass,XORCluster<ProcessTreeElement>>();
		Set<XORCluster<ProcessTreeElement>> parentSet = new HashSet<XORCluster<ProcessTreeElement>>();
		for(XEventClass eventClass: traceVariant) {
			// we get the name for this eventClass
			for(XORCluster<ProcessTreeElement> cluster: clusterList) {
				if(eventClass.getId().equals(cluster.getKeyNode().getName())) {
					// we create one relation
					map.put(eventClass, cluster);
					// set cluster can go up
					cluster.setLtVisited(true);
				
					// we check the parent of this cluster
					XORCluster<ProcessTreeElement> parent = cluster.getParent();
					if(parent.getKeyNode().equals(snode))
						return true;
					
					parentSet.add(parent);
					checkLtVisited(parent);
					// stop at the level that it can not go up
					while(parent.isLtVisited()) {
						// go up the higher level, put the parent of it available
						parentSet.remove(parent);
						parent = parent.getParent();
						
						if(parent.getKeyNode().equals(snode))
							return true;
						
						parentSet.add(parent);
						checkLtVisited(parent);
					}
					
					// put the parent into one set??? Not really, how could we do it back?
					// we just keep all the parent which can not go up!!
					break;
				}
			}
		}
		
		// 2. recursively check the parents set and see if they can be combined into one?
			// we can delete those parent, or just keep them?? both are fine
			// now we have the parentSet: one is empty, because of the root;
			// one contains the incomplete nodes..
		if(parentSet.isEmpty()) {
			System.out.println("The trace is complete with it!!");
		}else {
			// we do recheck on the parent to find out if it is necessary to go through this node
			// if this node is one silent transition, we need to find out its ancestors,
			// it can happen that the parentSet is higher, and silent transition lower level
			// check each parentSet in remaining set, check its childrenCluster not ltVisited
			// then visit each children, if this node is on the way, then choose it 
			for(XORCluster<ProcessTreeElement> p: parentSet) {
				// get the childreCluster which is not ltVisited
				// we need to change the parentSet until it goes empty...
				// how to control it, then?? 
				// we need to divide into several situations of it
				// if parent is Seq, then we goes down check all the children no
				
				// we go up at first to check if we find out the snode of it
				
				if(goUpCheck(p,snode)) {
					return true;
				}
				
				if(goDownCheck(p,snode)) {
					// if we find it in this parent, it should exist
					// we just stop it
					return true;
				}
				// will it change the parentSet, definitely!!
			
			}
				
		}
		
		return false;
	}
	
	// should we begin at the lower level or the higher level?? 
	// begin from higher level:: effective to get the parent and dispose
	// begin from lower level, we can delete the parent set to check, but takes time
	// if we use the goUpCheck, we really need to extend the parentSet of it..
	// or we just go visit until we reach the root
	private static boolean goUpCheck(XORCluster<ProcessTreeElement> p, Node bnode) {
		// TODO go up to check if the snode in parent upper set
		XORCluster<ProcessTreeElement> parent = p.getParent();
		while(parent!= null && !parent.getKeyNode().equals(bnode)) {
			parent = parent.getParent();
		}
		if(parent!=null)
			return true;
		
		return false;
	}

	// after upCheck, we go down checking... If the parent is equal to it, what to do..
	// before we put them into parentNode, but it can be removed from it, so we test it before
	// here node in lower level
	public static boolean goDownCheck(XORCluster<ProcessTreeElement> parent, Node snode) {
		// one situation, this parent missing one silent transitions, 
		// if the parent is the node in the parentSet, then we say it is a must, right??
		if(parent.getKeyNode().equals(snode)) {
			return true;
		}
		// Also some situations that snode is higher than the parent, what to do then?? 
		// after checking all the children, we can't find the parent, then it must goes down
		// because of the children, so the parent should execute !!! 
		
		List<XORCluster<ProcessTreeElement>> children = parent.getChildrenCluster();
		if(parent.isSeqCluster() || parent.isParallelCluster()) {
			// need to check all the children not ltVisited
			boolean flag = true;
			for(XORCluster<ProcessTreeElement> child: children) {
				if(!child.isLtVisited()) {
					// this node should be visited to complete the model
					// check the children if it equals to the node we are looking for;
					if(child.getKeyNode().equals(snode)) {
						child.setLtVisited(true);
						checkLtVisited(parent);
						return true;
					}else {
						// not equal, then go lower to find the children with one of them
						flag &= goDownCheck(child, snode);
					}
					
				}
			}
			checkLtVisited(parent);
			if(flag)
				return true;
			
		}else if(parent.isXORCluster()) {
			// we only need one child, enough
			for(XORCluster<ProcessTreeElement> child: children) {
				if(!child.isLtVisited()) {
					// if one is ltVisited, then the whole is ltVisited, so no right!!
					// if it is in one branch, but not the necessary one, so no need to report
				
					if(child.getKeyNode().equals(snode)) {
						// if we meet one of it then we return all of them??
						// even of they are equal, we are not sure that they goes this way!!
						child.setLtVisited(true);
						checkLtVisited(parent);
						return true;
					}else {
						// if the node is not equal to it, then goes into deeper
						// one limit, shall we remember all the children stuff here??
						// not really, only when the parent is not ltVisited 
						if(goDownCheck(child,snode)) {
							// if we find this one here, then we stop
							parent.setLtVisited(true);
							return true;
						}
					}
				}
			}
			
		}
		
		return false;
	}
	
	
	private static void checkLtVisited(XORCluster<ProcessTreeElement> parent) {
		// TODO if parent is xor, one branch is ok
		// if parent is seq or parallel, all branches must be visited
		if(parent.isXORCluster()) {
			for(XORCluster<ProcessTreeElement> child: parent.getChildrenCluster())
				if(child.isLtVisited()) {
					parent.setLtVisited(true);
					break;
				} 
					
		}else if(parent.isSeqCluster() || parent.isParallelCluster()) {
			boolean flag = true;
			for(XORCluster<ProcessTreeElement> child: parent.getChildrenCluster())
				if(!child.isLtVisited()) {
					flag = false;
					break;
				} 
			parent.setLtVisited(flag);
		}
		
	}

	public Map<XEventClass,XORCluster<ProcessTreeElement>> getEvent2ProcessTreeMap(List<XEventClass> traceVariant, XORCluster<ProcessTreeElement> clusterList) {
		Map<XEventClass, XORCluster<ProcessTreeElement>> map = new HashMap<XEventClass,XORCluster<ProcessTreeElement>>();
		
		
		return map;
	}
	

	public Map<XEventClass,Node> getEvent2ProcessTreeMap(XLog xLog, ProcessTree pTree, XEventClassifier classifier) {
		// TODO generate the transfer from process tree to event classes in log
		Map<XEventClass, Node> map = new HashMap<XEventClass,Node>();
		Collection<Node> nodes = pTree.getNodes();
		// leave only the leaf nodes
		
		
		XEventClasses classes = null;
		if(classifier != null && xLog.getClassifiers().contains(classifier)) 
			classes = XLogInfoFactory.createLogInfo(xLog).getEventClasses(classifier);
		else
			classes = XLogInfoFactory.createLogInfo(xLog).getNameClasses();
		// create a tauNOde, check old examples..
		Node tauNode = null;
		
		boolean match;
		for (XEventClass eventClass : classes.getClasses()) {
			match = false;
			for (Node node : nodes) { // transition.getLabel()
				// here we need to create a mapping from event log to graphs
				// need to check at first what the Name and other stuff
				if(!node.isLeaf())
					continue;
				
				if (eventClass.getId().equals(node.getName())) {
					map.put(eventClass, node);
					match = true;
					break;
				}
			}
			if(! match) {// it there is node not showing in the event log
				map.put(eventClass, tauNode);
			}
		}
		
		return map;
	}
}
