package org.processmining.incorporatenegativeinformation.algorithms;

import java.util.ArrayList;
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
import org.processmining.incorporatenegativeinformation.help.ProcessConfiguration;
import org.processmining.processtree.Block;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;

/**
 * this relates to the alignment on the process tree
 * 
 * @author ding
 *
 */
public class AlignmentChecker {


	public static Map<XEventClass, Node> getEvent2ProcessTreeMap(XLog xLog, ProcessTree pTree, XEventClassifier classifier) {
		// TODO generate the transfer from process tree to event classes in log
		Map<XEventClass, Node> map = new HashMap<XEventClass, Node>();
		Collection<Node> nodes = pTree.getNodes();
		// leave only the leaf nodes

		XEventClasses classes = null;
		if (classifier != null && xLog.getClassifiers().contains(classifier))
			classes = XLogInfoFactory.createLogInfo(xLog).getEventClasses(classifier);
		else
			classes = XLogInfoFactory.createLogInfo(xLog).getNameClasses();
		// create a tauNOde, check old examples... we can not make it null, but create a empty node
		// it is ok.
		Node tauNode = null;//new AbstractTask.Manual("tau for log"); //, originators .Automatic("tau for log");

		boolean match;
		for (XEventClass eventClass : classes.getClasses()) {
			match = false;
			for (Node node : nodes) { // transition.getLabel()
				// here we need to create a mapping from event log to graphs
				// need to check at first what the Name and other stuff
				if (!node.isLeaf())
					continue;

				if (eventClass.getId().equals(node.getName())) {
					map.put(eventClass, node);
					match = true;
					break;
				}
			}
			if (!match) {// it there is node not showing in the event log
				map.put(eventClass, tauNode);
			}
		}

		return map;
	}
	
	
	// one function to get the path in the tree for a traceVariant..
	/**\
	 * given tree and the traceVariant from event log but in Node form, we give out the 
	 * necessary path in the tree to execute this list of Node
	 * @param tree
	 * @param traceVariant
	 * @return a path in the tree... Which we should organize them:: 
	 *     depth-first-visit and then visit the middle node at last
	 */
	static Map<Node, Boolean>  goUpMap ;
	public static List<Node> getPathOnTree(ProcessTree tree, List<Node> nodeTrace) {
		goUpMap= new HashMap<>();
		List<Node> path = new ArrayList<Node>();
		// path.addAll(nodeTrace);
		// of course there is some unordered traceVariant, so we just give out the nodes
		// that we should visit??
		Set<Node> parentSet = new HashSet<Node>();
		for(Node node: nodeTrace) {
			// if node is not in the model, then we do nothing here.. but onr not fit here
			// what nodes we must set  to complete the whole path in the tree?
			// we don't really care so much here, right?? we just discover the whole structure of them
			if(node == null) {
				System.out.println("Event not in model but in log");
				continue;
			}
			if(!goUpMap.containsKey(node))
				goUpMap.put(node, true);
			
			// here we believe node has only one parent
			Block parent = getParent(node);
			parentSet.add(parent);
			
			checkGoUP(parent);
			// stop at the level that it can not go up
			while (parent!=null) {
				// go up the higher level, put the parent of it available
				// after each is fine, we add them into path
				if(isGoUPOK(parent))
					parentSet.remove(parent);
				parent = getParent(parent);
				if(parent == null)
					break;
				parentSet.add(parent);
				
				checkGoUP(parent);
			}
		}
		
		// then we check parentSet one by one, to guarantee the completeness
		for(Node parent: parentSet) {
			if(!isGoUPOK(parent)) 
				goDownCheck(parent);
			// one effect is that how to make sure it recursively check the upper nodes??
		}

		
		for(Node nodeKey: goUpMap.keySet()) {
			if(goUpMap.get(nodeKey))
				if(!path.contains(nodeKey))
					path.add(nodeKey);
		}
		
		List<Node> npath = new ArrayList<>();
		AlignmentChecker.sortPath(tree.getRoot(), path, npath);
		
		return npath;
	}
	/**
	 * this is used to sort the path in the tree according to the visited relation
	 * we only concern the xor branch order in the path:::
	 *   -- nodes: S1,T1 // T1,S1 
	 *   -- we get the index of nodes in the path:: As we know, they have an order in themselves,
	 *   -- also, how to arrange the path ??? It is different, we 
	 *      so others doesn't matter
	 *  The order of nodes in path:: depth at first, current node visited at last
	 *      If we only focus on the leaves node, so it just add more silent nodes in the nodeVariant, 
	 *      after this, we try to check the position in the nodeVariant, so it should be fine
	 *      But the order should matter the other surroundings.  If the path is in order, 
	 *      then keep all the leaves node, it becomes the model move in process tree.. So
	 *        -- 1. sort the path in the tree
	 *        -- 2. leave the leaf nodes in it
	 * @param node :: keep the current node to visit, it begins from the root
	 * @param tree
	 * @param path
	 * @return
	 */
	public static void sortPath(Node node,  List<Node> path, List<Node> npath){
		if(node.isLeaf()) {
			npath.add(node);
			return ;
		}
		Block block = (Block) node;
		for(Node child: block.getChildren() ) {
			if(path.contains(child))
				sortPath(child, path, npath);
		}
		npath.add(block);
	}
	// check the information from bottom, but it is difficult to find the first one
	// so we need to recursively check the map information 
	private static void goDownCheck(Node node) {
		// if node is silent transition, then we return back!! 
		
		if(node.isLeaf()) {
			return ;
		}// this part is only ok, when there is no other choices, we choose silent transitions
		// if it is seq or parallel, then find children not fill, and go down too
		Block block = (Block) node;
		if(isSeqCluster(block) || isParallelCluster(block)) {
			
			for(Node child: block.getChildren()) {
				if(!isGoUPOK(child)) {
					// we should also add the child to the path
					goDownCheck(child);
				}
			}
		}else if(isXORCluster(block)) {
			// if it is xor, just choose one item goes down, it can be fine
			Node tau = null;
			for(Node child: block.getChildren()) {
				if(isTauNode(child)) {
					tau = child;
					continue;
				}
				goDownCheck(child);
				if(isGoUPOK(child)) {
					setGoUpOK(block);
				}
			// we can only choose silent transition, if we know there is no other choices
			// so we need to keep it until we checked all the other ndoes
			}
			if(!isGoUPOK(block) && tau!=null) {
				setGoUpOK(tau);
				// setGoUpOK(block);
			}
		}else if(isLoop(block)) {
			// here we need to visit the node and see 
			
			for(Node child: block.getChildren())
				goDownCheck(child);
				
			
		}
		checkGoUP(block);
		// after checking it, do checkGoUpOk, again and then turn that back..
	}
	private static boolean isTauNode(Node node) {
		if(node.getClass().getSimpleName().equals(ProcessConfiguration.TAU_AUTOMATIC))
			return true;
		return false;
	}

	private static Block getParent(Node node) {
		if(node.getParents() == null || node.getParents().isEmpty())
			return null;
		return node.getParents().iterator().next();
	}
	
	private static  void checkGoUP(Block block) {
		// TODO if parent is xor, one branch is ok
		// if parent is seq or parallel, all branches must be visited
		if (isXORCluster(block)) {
			for (Node child: block.getChildren()) {
				if(isGoUPOK(child)) {
					setGoUpOK(block);
					break;
				}
			}

		} else if (isSeqCluster(block) || isParallelCluster(block)) {
			boolean flag = true;
			for(Node child: block.getChildren()) {
				if(!isGoUPOK(child)) {
					flag = false;
					break;
				}
			}
			if(flag)
				setGoUpOK(block);
		}else if(isLoop(block)) {
			// if it is loop, how to make sure that it is ok??
			// the first child should be executed.. the second one is go back
			// the third one is away from loop..
			int redoIdx = 0, doIdx = 1, outIdx=2;
			List<Node> children = block.getChildren();
			
			Node redoNode = children.get(redoIdx);
			Node doNode = children.get(doIdx);
			Node outNode = children.get(outIdx);
			
			// if the redoNode is ok, then we just return it
			// after the do part, we don't really care
			if(isGoUPOK(redoNode) ) {
				if(isTauNode(doNode))
					setGoUpOK(doNode);
				if(isGoUPOK(doNode)) {
					setGoUpOK(outNode);
					setGoUpOK(block);
				}
			}
			
		}
		Block parent = getParent(block);
		if(parent!=null)
			checkGoUP(parent);
	}

	private static boolean isLoop(Block block) {
		if(block.getClass().getSimpleName().equals(ProcessConfiguration.LOOP))
			return true;
		return false;
	}

	private static  boolean isParallelCluster(Block block) {
		if(block.getClass().getSimpleName().equals(ProcessConfiguration.PARALLEL))
			return true;
		return false;
	}

	private static  boolean isSeqCluster(Block block) {
		if(block.getClass().getSimpleName().equals(ProcessConfiguration.SEQUENCE))
			return true;
		return false;
	}

	private static  boolean isXORCluster(Block block) {
		if(block.getClass().getSimpleName().equals(ProcessConfiguration.XOR))
			return true;
		return false;
	}
	// set one attribute into node to mark it is ok to go up
	
	private static void setGoUpOK(Node node) {
		// use one map to do it 
		if(goUpMap.containsKey(node))
			goUpMap.replace(node, true);
		else
			goUpMap.put(node, true);
	}
	
	// return that attribute in the node
	private static boolean isGoUPOK(Node node) {
		return goUpMap.getOrDefault(node, false);
	}
}
