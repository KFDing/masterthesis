package org.processmining.incorporatenegativeinformation.models;

import java.util.ArrayList;
import java.util.List;

import org.processmining.incorporatenegativeinformation.help.ProcessConfiguration;

/**
 * this class is used to represent the structure of seq, parallel, loop for xor
 * structure, not sure about the xor big branch, what to do ?? I need to
 * organize it !! attributes:: -- keyNode: to store the element from process
 * tree -- xor structure list :: -- the begin and end xor structure for it
 * 
 * ++ begin and end xor structure:: -- seq: the first and end xor structure --
 * parallel: the xor from all branches -- xor: we need to merge it and still we
 * need to have the begin and end parts -- the previous process element
 * structure of it
 * 
 * methods:: -- assign and get the xor begin and end,set them -- create pair
 * combinations of it -- if it is xor, we need to merge them, or we create the
 * xor structure to use them?? [consider it later]
 * 
 * @author dkf
 *
 */
public class XORCluster<T> {
	private T keyNode;
	// in its branches it have it, like the branches?? Not sure, but we could use it, that's true
	private List<XORCluster<T>> xorList;

	// if in seq or parallel, there are a lot, then we need to use list to store them 
	// but anyway, we could use the branches of one list, but lost the structure information
	/**
	 * if it is one nested xor, then to to define its beginXORList and
	 * endXORList?? for one leaf node, we can only create its branch and put
	 * them in parallel, if we use xor structure, we can have different xor
	 * list, which is possible..That's true.. but xor pair is one xor structure
	 * S to T, if we use the e, what to do ?? if we have branch list here, but
	 * how do we give them an order here??
	 */
	private List<XORCluster<T>> beginXORList;
	private List<XORCluster<T>> endXORList;

	// to record the previous structure, or we only to remember the parents, it's already fine?? 
	public List<XORCluster<T>> childrenCluster;

	private XORCluster<T> parent;

	// if it's in xor cluster, it means this is a nested xor cluster.
	private boolean hasXOR = false;
	// the available implies if we need to visit children cluster to get the xor list..
	private boolean pairAvailable = false;
	// this is for the adding of lt dependency; 
	// it is visited, we should go deeper into the pure branch cluster and check for this.
	private boolean ltAvailable = false; //isPureBranchCluster();

	// asSource and asTarget is designed to add randomly pair into the model
	private boolean asSource = false;
	private boolean asTarget = false;
	private int level = -1;

	List<XORCluster<T>> endBranchList;

	public XORCluster(T key) {
		keyNode = key;
		xorList = new ArrayList<XORCluster<T>>();
		beginXORList = new ArrayList<XORCluster<T>>();
		endXORList = new ArrayList<XORCluster<T>>();

		endBranchList = new ArrayList<XORCluster<T>>();
	}

	public T getKeyNode() {
		return keyNode;
	}

	public void setKeyNode(T keyNode) {
		this.keyNode = keyNode;
	}

	public List<XORCluster<T>> getXorList() {
		return xorList;
	}

	public List<XORCluster<T>> getBeginXORList() {
		// here maybe some problem, actually 
		if (!beginXORList.isEmpty())
			return beginXORList;
		if (isPureBranchCluster()) {
			beginXORList.add(this);
		} else if (isSeqCluster()) {
			// even if there are some elements in seq, but we're not sure about the sequence, so we can't do it 
			// we just go to the first childrenCluster
			XORCluster<T> cluster = childrenCluster.get(0);
			// if there are some branches form it, what to do it ?? Nana, we need recursive run!!
			beginXORList.addAll(cluster.getBeginXORList());

		} else if (isParallelCluster()) {
			// beginXORList can be inferred from the xorList?? Right?? 
			// we really need to consider about the sequence and other stuffs here!! 
			// ok they can be across of them selfs, what we need is to get it from the childen parts
			for (XORCluster<T> cluster : childrenCluster) {
				// if there are some branches form it, what to do it ?? Nana, we need recursive run!!
				beginXORList.addAll(cluster.getBeginXORList());
			}
			// my question is should we stop here?? if we have deeper structure what to do ?	
		} else if (isXORCluster()) {
			// we need to deal with other situations, if there is something, 
			beginXORList.add(this);

		} else {
			System.out.println("in loop situation, not go deeper");
		}
		return beginXORList;
	}

	public void setBeginXORList(List<XORCluster<T>> beginXORList) {
		this.beginXORList = beginXORList;
	}

	public void addBeginXORList(XORCluster<T> xorS) {
		beginXORList.add(xorS);
	}

	public List<XORCluster<T>> getEndXORBranch() {

		if (!endBranchList.isEmpty())
			return endBranchList;
		// in nested end, we are going to do this, else not like this
		// something interesting because, we add something more, which is not like this..
		if (isPureBranchCluster()) {
			endBranchList.add(this);
		} else if (isSeqCluster()) {
			XORCluster<T> cluster = childrenCluster.get(childrenCluster.size() - 1);
			// if there are some branches form it, what to do it ?? Nana, we need recursive run!!
			endBranchList.addAll(cluster.getEndXORBranch());

		} else if (isParallelCluster()) {
			// beginXORList can be inferred from the xorList?? Right?? 
			// we really need to consider about the sequence and other stuffs here!! 
			// ok they can be across of them selfs, what we need is to get it from the childen parts
			for (XORCluster<T> cluster : childrenCluster) {
				// if there are some branches form it, what to do it ?? Nana, we need recursive run!!
				endBranchList.addAll(cluster.getEndXORBranch());
			}

		} else if (isNotNXORCluster()) {
			// here we need control of it, we need to go into the pure branch and also another branch
			// we would see the branch in xor should also return it 
			endBranchList.add(this);

		} else if (isNXORCluster()) {
			for (XORCluster<T> cluster : childrenCluster) {
				// if there are some branches form it, what to do it ?? Nana, we need recursive run!!
				endBranchList.addAll(cluster.getEndXORBranch());
			}
		} else {
			System.out.println("in loop situation, not go deeper");
		}
		return endBranchList;

	}

	public List<XORCluster<T>> getEndXORList() {
		if (!endXORList.isEmpty())
			return endXORList;

		if (isSeqCluster()) {
			XORCluster<T> cluster = childrenCluster.get(childrenCluster.size() - 1);
			// if there are some branches form it, what to do it ?? Nana, we need recursive run!!
			endXORList.addAll(cluster.getEndXORList());

		} else if (isParallelCluster()) {
			// beginXORList can be inferred from the xorList?? Right?? 
			// we really need to consider about the sequence and other stuffs here!! 
			// ok they can be across of them selfs, what we need is to get it from the childen parts
			for (XORCluster<T> cluster : childrenCluster) {
				// if there are some branches form it, what to do it ?? Nana, we need recursive run!!
				endXORList.addAll(cluster.getEndXORList());
			}

		} else if (isXORCluster()) {
			// here we need control of it, we need to go into the pure branch and also another branch
			// we would see the branch in xor should also return it 
			endXORList.add(this);

		} else {
			System.out.println("in loop situation, not go deeper");
		}
		return endXORList;
	}

	public void setEndXORList(List<XORCluster<T>> endXORList) {
		this.endXORList = endXORList;
	}

	// we will see if this method is in need
	public void addEndXORList(XORCluster<T> xorS) {
		endXORList.add(xorS);
	}

	public List<XORCluster<T>> getChildrenCluster() {
		return childrenCluster;
	}

	public void setChildrenCluster(List<XORCluster<T>> childrenCluster) {
		this.childrenCluster = childrenCluster;
	}

	public void addChilrenCluster(XORCluster<T> cluster) {
		if (childrenCluster == null)
			childrenCluster = new ArrayList<XORCluster<T>>();
		childrenCluster.add(cluster);
	}

	public void addXORCluster(XORCluster<T> xorS) {
		xorList.add(xorS);
	}

	/**
	 * check if it has xor structure. if it's un nested xor structure, it means,
	 * the children cluster has no XOR cluster
	 * 
	 * @return
	 */
	public boolean hasXOR() {
		return hasXOR;
	}

	public void setHasXOR(boolean hasXOR) {
		this.hasXOR = hasXOR;
	}

	public boolean isSeqCluster() {
		if (keyNode.getClass().getSimpleName().equals(ProcessConfiguration.SEQUENCE))
			return true;
		return false;
	}

	public boolean isParallelCluster() {
		if (keyNode.getClass().getSimpleName().equals(ProcessConfiguration.PARALLEL))
			return true;
		return false;
	}

	// we need to set the available into true or false
	// situation is that, if it's xor cluster, then return true
	// if not, we check the children, direct available, I mean 
	public void setAvailable(boolean value) {
		pairAvailable = value;
	}

	public boolean isAvailable() {
		return pairAvailable;
	}

	boolean ltVisited = false;

	public boolean isLtVisited() {
		return ltVisited;
	}

	public void setLtVisited(boolean ltVisited) {
		this.ltVisited = ltVisited;
	}

	public boolean isNotNXORCluster() {
		// TODO test if this cluster is not nested xor structure
		// if it is not nested xor cluster, then return true, 
		// nested xor cluster, then return false
		if (keyNode.getClass().getSimpleName().equals(ProcessConfiguration.XOR) && !hasXOR())
			return true;

		return false;
	}

	// if it is nested xor cluster
	public boolean isNXORCluster() {
		if (keyNode.getClass().getSimpleName().equals(ProcessConfiguration.XOR) && hasXOR())
			return true;
		return false;
	}

	// here we mean a cluster without xor and not xor 
	public boolean isPureBranchCluster() {
		if (!hasXOR && !isXORCluster())
			return true;
		return false;
	}

	// only for pure branch, we have this
	List<T> beginNodeList;

	public List<T> getBeginNodeList() {
		// TODO Auto-generated method stub, so should we also record its childrenCluster, 
		// yap, we need to do it 
		beginNodeList = new ArrayList<T>();
		// if it is just one single node, so what to to then?? 
		if (isLeafCluster()) {
			beginNodeList.add(keyNode);
		} else if (isSeqCluster()) {
			beginNodeList.addAll(childrenCluster.get(0).getBeginNodeList());
		} else if (isParallelCluster()) {
			for (XORCluster<T> child : childrenCluster) {
				beginNodeList.addAll(child.getBeginNodeList());
			}
		} else if (isXORCluster()) {
			for (XORCluster<T> child : childrenCluster) {
				beginNodeList.addAll(child.getBeginNodeList());
			}
		} else if (isLoopCluster()) {
			// here we can not give out the beginNode for loop!!
			// but we can add new silent transition before it and represent this branch
			// and also a end node to represent it!!
			// then we avoid this situation for it 
			beginNodeList.add(keyNode);
		}

		return beginNodeList;
	}

	// but after we do this, it means that we need to record all the childrenCluster of this branch
	// Could we stop them here?? No, we can't because we need data for parallel
	// assign their children cluster but assign them if they are leaf node
	// we should record that we are in a branch, and then record the cluster of them .'
	// if we meet one parallel, then we record its seq branch, but until we reach the leaf node, that's how it works
	// yes, we need to remember all things relative to xor structure

	// for pure branch, there is the End node list
	List<T> endNodeList;

	public List<T> getEndNodeList() {
		// TODO Auto-generated method stub
		endNodeList = new ArrayList<T>();
		if (isLeafCluster()) {
			endNodeList.add(keyNode);
		} else if (isSeqCluster()) {
			endNodeList.addAll(childrenCluster.get(childrenCluster.size() - 1).getEndNodeList());
		} else if (isParallelCluster()) {
			for (XORCluster<T> child : childrenCluster) {
				endNodeList.addAll(child.getEndNodeList());
			}
		} else if (isXORCluster()) {
			for (XORCluster<T> child : childrenCluster) {
				endNodeList.addAll(child.getEndNodeList());
			}
		} else if (isLoopCluster()) {
			endNodeList.addAll(childrenCluster.get(childrenCluster.size() - 1).getEndNodeList());

		}
		return endNodeList;
	}

	public boolean isXORCluster() {
		if (keyNode.getClass().getSimpleName().equals(ProcessConfiguration.XOR))
			return true;

		return false;
	}

	private boolean isLoopCluster() {
		// TODO if it is a loop cluster
		if (keyNode.getClass().getSimpleName().equals(ProcessConfiguration.LOOP))
			return true;
		return false;
	}

	boolean isLeaf = false;

	public boolean isLeafCluster() {
		return isLeaf;
	}

	public void setIsLeaf(boolean value) {
		isLeaf = value;
	}

	public String getLabel() {
		// TODO return the label for this cluster only required when it's xor cluster, need to see later
		return keyNode.toString();
	}

	public void testPairAvailable() {
		// if it is pure branch, then we have pair available true
		if (isPureBranchCluster())
			pairAvailable = true;
	}

	public boolean isPairAvailable() {
		return pairAvailable;
	}

	public void setPairAvailable(boolean pairAvailable) {
		this.pairAvailable = pairAvailable;
	}

	public boolean isLtAvailable() {
		return ltAvailable;
	}

	public void setLtAvailable(boolean ltAvailable) {
		this.ltAvailable = ltAvailable;
	}

	public XORCluster<T> getParent() {
		// TODO Auto-generated method stub
		return parent;
	}

	public void setParent(XORCluster<T> parent) {
		this.parent = parent;
	}

	public boolean isAsSource() {
		return asSource;
	}

	public void setAsSource(boolean asSource) {
		this.asSource = asSource;
	}

	public boolean isAsTarget() {
		return asTarget;
	}

	public void setAsTarget(boolean asTarget) {
		this.asTarget = asTarget;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}
}
