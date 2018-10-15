package org.processmining.plugins.ding.process.dfg.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;

public class XorSplitCluster{
	int posIdx = ProcessConfiguration.RULESET_POS_IDX;
	int negIdx = ProcessConfiguration.RULESET_NEG_IDX;
	Map<PetrinetNode, ArrayList<Double>> xorSplitStructure ;// = new ArrayList<>();
	int completSize = -1;
	boolean complete = false;

	List<PetrinetNode> nfcNodes ;
	public XorSplitCluster() {
		xorSplitStructure =  new HashMap<PetrinetNode, ArrayList<Double>>();
		nfcNodes = new ArrayList<PetrinetNode>();
	}
	
	public XorSplitCluster(int size) {
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
			}
		}
		
		if(nfcNodes.size() < completSize)
			return nfcNodes;
		else {
			complete = true;
			return nfcNodes;
		}
	}
	
	public void addPotentialConnection(PetrinetNode node, ArrayList<Double> freq) {
		xorSplitStructure.put(node, freq);
	}
	
}