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
import org.processmining.incorporatenegativeinformation.models.XORCluster;
import org.processmining.processtree.Block;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.ProcessTreeElement;

/**
 * this relates to the alignment on the process tree
 * 
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
		Map<XEventClass, XORCluster<ProcessTreeElement>> map = new HashMap<XEventClass, XORCluster<ProcessTreeElement>>();
		Set<XORCluster<ProcessTreeElement>> parentSet = new HashSet<XORCluster<ProcessTreeElement>>();
		for (XEventClass eventClass : traceVariant) {
			// we get the name for this eventClass
			for (XORCluster<ProcessTreeElement> cluster : clusterList) {
				if (eventClass.getId().equals(cluster.getKeyNode().getName())) {
					// we create one relation
					map.put(eventClass, cluster);
					// set cluster can go up
					cluster.setLtVisited(true);

					// we check the parent of this cluster
					XORCluster<ProcessTreeElement> parent = cluster.getParent();
					if (parent.getKeyNode().equals(snode))
						return true;

					parentSet.add(parent);
					checkLtVisited(parent);
					// stop at the level that it can not go up
					while (parent.isLtVisited()) {
						// go up the higher level, put the parent of it available
						parentSet.remove(parent);
						parent = parent.getParent();

						if (parent.getKeyNode().equals(snode))
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
		if (parentSet.isEmpty()) {
			System.out.println("The trace is complete with it!!");
		} else {
			// we do recheck on the parent to find out if it is necessary to go through this node
			// if this node is one silent transition, we need to find out its ancestors,
			// it can happen that the parentSet is higher, and silent transition lower level
			// check each parentSet in remaining set, check its childrenCluster not ltVisited
			// then visit each children, if this node is on the way, then choose it 
			for (XORCluster<ProcessTreeElement> p : parentSet) {
				// get the childreCluster which is not ltVisited
				// we need to change the parentSet until it goes empty...
				// how to control it, then?? 
				// we need to divide into several situations of it
				// if parent is Seq, then we goes down check all the children no

				// we go up at first to check if we find out the snode of it

				if (goUpCheck(p, snode)) {
					return true;
				}

				if (goDownCheck(p, snode)) {
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
		while (parent != null && !parent.getKeyNode().equals(bnode)) {
			parent = parent.getParent();
		}
		if (parent != null)
			return true;

		return false;
	}

	// after upCheck, we go down checking... If the parent is equal to it, what to do..
	// before we put them into parentNode, but it can be removed from it, so we test it before
	// here node in lower level
	public static boolean goDownCheck(XORCluster<ProcessTreeElement> parent, Node snode) {
		// one situation, this parent missing one silent transitions, 
		// if the parent is the node in the parentSet, then we say it is a must, right??
		if (parent.getKeyNode().equals(snode)) {
			return true;
		}
		// Also some situations that snode is higher than the parent, what to do then?? 
		// after checking all the children, we can't find the parent, then it must goes down
		// because of the children, so the parent should execute !!! 

		List<XORCluster<ProcessTreeElement>> children = parent.getChildrenCluster();
		if (parent.isSeqCluster() || parent.isParallelCluster()) {
			// need to check all the children not ltVisited
			boolean flag = true;
			for (XORCluster<ProcessTreeElement> child : children) {
				if (!child.isLtVisited()) {
					// this node should be visited to complete the model
					// check the children if it equals to the node we are looking for;
					if (child.getKeyNode().equals(snode)) {
						child.setLtVisited(true);
						checkLtVisited(parent);
						return true;
					} else {
						// not equal, then go lower to find the children with one of them
						flag &= goDownCheck(child, snode);
					}

				}
			}
			checkLtVisited(parent);
			if (flag)
				return true;

		} else if (parent.isXORCluster()) {
			// we only need one child, enough
			for (XORCluster<ProcessTreeElement> child : children) {
				if (!child.isLtVisited()) {
					// if one is ltVisited, then the whole is ltVisited, so no right!!
					// if it is in one branch, but not the necessary one, so no need to report

					if (child.getKeyNode().equals(snode)) {
						// if we meet one of it then we return all of them??
						// even of they are equal, we are not sure that they goes this way!!
						child.setLtVisited(true);
						checkLtVisited(parent);
						return true;
					} else {
						// if the node is not equal to it, then goes into deeper
						// one limit, shall we remember all the children stuff here??
						// not really, only when the parent is not ltVisited 
						if (goDownCheck(child, snode)) {
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
		if (parent.isXORCluster()) {
			for (XORCluster<ProcessTreeElement> child : parent.getChildrenCluster())
				if (child.isLtVisited()) {
					parent.setLtVisited(true);
					break;
				}

		} else if (parent.isSeqCluster() || parent.isParallelCluster()) {
			boolean flag = true;
			for (XORCluster<ProcessTreeElement> child : parent.getChildrenCluster())
				if (!child.isLtVisited()) {
					flag = false;
					break;
				}
			parent.setLtVisited(flag);
		}

	}

	public Map<XEventClass, XORCluster<ProcessTreeElement>> getEvent2ProcessTreeMap(List<XEventClass> traceVariant,
			XORCluster<ProcessTreeElement> clusterList) {
		Map<XEventClass, XORCluster<ProcessTreeElement>> map = new HashMap<XEventClass, XORCluster<ProcessTreeElement>>();

		return map;
	}

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
		// create a tauNOde, check old examples..
		Node tauNode = null;

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
			if(node == null)
				continue;
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
