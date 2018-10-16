package org.processmining.plugins.ding.process.dfg.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;

public class XorSplitCluster{
	PetrinetNode keyNode;
	int posIdx = ProcessConfiguration.RULESET_POS_IDX;
	int negIdx = ProcessConfiguration.RULESET_NEG_IDX;
	Map<PetrinetNode, ArrayList<Double>> xorSplitStructure ;// = new ArrayList<>();
	int completSize = -1;
	boolean complete = false;
	boolean visited = false;

	List<PetrinetNode> nfcNodes ;
	public XorSplitCluster() {
		xorSplitStructure =  new HashMap<PetrinetNode, ArrayList<Double>>();
		nfcNodes = new ArrayList<PetrinetNode>();
	}
	
	public XorSplitCluster(PetrinetNode node, int size) {
		keyNode = node;
		xorSplitStructure =  new HashMap<PetrinetNode, ArrayList<Double>>();
		completSize = size;
		nfcNodes = new ArrayList<PetrinetNode>();
	}

	public boolean isComplete() {
		return complete;
	}

	public void setComplete(boolean complete) {
		this.complete = complete;
	}

	public Map<PetrinetNode, ArrayList<Double>> getStructure() {
		return xorSplitStructure;
	}

	public void setStructure(Map<PetrinetNode, ArrayList<Double>> cluster) {
		this.xorSplitStructure = cluster;
	}
	
	public int getCompletSize() {
		return completSize;
	}

	public void setCompletSize(int completSize) {
		this.completSize = completSize;
	}

	public  Set<PetrinetNode> getKeySet() {
		return xorSplitStructure.keySet();
	}
	
	public List<PetrinetNode> getNFSet() {
		// we check the freq in each map if are complete and the potential NFC rules
		if(nfcNodes.size() > 0)
			return nfcNodes;
		
		for(PetrinetNode key: xorSplitStructure.keySet()) {
			ArrayList<Double> keyValue =xorSplitStructure.get(key);
			
			if(keyValue.get(posIdx)> keyValue.get(negIdx)) {
				
				nfcNodes.add(key);
			}else if(keyValue.get(posIdx)>0){
				for(PetrinetNode otherKey: xorSplitStructure.keySet()) {
					if(otherKey != key) {
						// to the other situations
						ArrayList<Double> otherValue =xorSplitStructure.get(otherKey);
						if(otherValue.get(negIdx) >= otherValue.get(posIdx)) {	
							nfcNodes.add(key);
						}
						// but how to say this, that we acutally need some positive examples on rule
						// and then we can say it is allowed by it, if not, we see not..
					}
				}
				
			}
			
			// when we check one branch we also need to consider the other branch, Also about the connection
			// to source passed here
			// pos in this branch and other branches are in negative or a lot of from negative ones
			
		}
		
		if(nfcNodes.size() < completSize)
			return nfcNodes;
		else {
			complete = true;
			return nfcNodes;
		}
	}
	
	public void addPotentialConnection(PetrinetNode node, ArrayList<Double> freq) {
		// here overwrite the data, not good
		ArrayList<Double> origFreq ;
		if(xorSplitStructure.containsKey(node)) {
			origFreq = xorSplitStructure.get(node);
		}
		else {
			origFreq = new ArrayList<Double>();
			for(int i=0; i< ProcessConfiguration.RULESET_IDX_NUM; i++) {
				origFreq.add(0.0);
			}
		}
		
		addArrayList(origFreq, freq);
		xorSplitStructure.put(node, origFreq);
	}
	
	private void addArrayList(ArrayList<Double> origFreq, ArrayList<Double> freq) {
		for(int i=0; i< freq.size();i++) {
			origFreq.set(i, origFreq.get(i) + freq.get(i));
		}
			
	}
	


	public boolean isVisited() {
		// TODO Auto-generated method stub
		return visited;
	}
	
	public void setVisited(boolean value) {
		visited = value;
	}

	public boolean inSameStructure(XorSplitCluster refCluster) {
		//they are in the same structure, so they have the same node collection
		if(xorSplitStructure.keySet().containsAll(refCluster.getKeySet()))
			return true;
		
		return false;
	}

	public String getName() {
		// TODO Auto-generated method stub
		String name = "";
		for(PetrinetNode node: getKeySet())
			name += node.getLabel();
		return name;
	}
	
}