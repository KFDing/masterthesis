package org.processmining.plugins.ding.process.dfg.model;
/**
 * this class is used to represent the potential non-free choice cluster. 
 * Attributes:
 *   Key: one Place in xor join branch
 *   List of the set of one potential FC cluster,
 *       -- set: Place + frequency[Existing, Pos, Neg]
 *              -- Place: belongs to xor split branch
 *              -- frequency: record the different frequency it shows in the list
 * Methods:
 *    -- check if from key place there is a non free choice structure
 *        -- get all the possible set and test if there exists uniqueness or not completeness.
 *    -- create the potential free choice rules set from A. 
 *        ++ later could add such rules set into whole, and decide to keep which ones.     
 * @author dkf
 * @date  15 Oct. 2018
 */

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;

public class PotentialNFCCluster {
	private PetrinetNode keyNode;
	List<XorSplitCluster> NFCClusters ;
	List<PetrinetNode> nfcRules;
	
	public PotentialNFCCluster(PetrinetNode node){
		setKeyPlace(node); // or should we copy it ?? See later it is totally fine
		NFCClusters = new ArrayList<XorSplitCluster>();
		nfcRules = new ArrayList<PetrinetNode>();
	}
	
	public List<XorSplitCluster> getNFCClusters() {
		return NFCClusters;
	}

	public void setNFCClusters(List<XorSplitCluster> nFCClusters) {
		NFCClusters = nFCClusters;
	}

	
	public void addCluster(XorSplitCluster cluster) {
		NFCClusters.add(cluster);
	}

	public PetrinetNode getKeyNode() {
		return keyNode;
	}

	public void setKeyPlace(PetrinetNode node) {
		this.keyNode = node;
	}

	/**
	 * this method analyzes the frequency from one transititon to other split Structure ;;
	 * return the ruleSet with <Transition, Transition>
	 */
	public List<PetrinetNode> getRuleSet() {
		
		return nfcRules;
	}
	
	
	public void buildRuleSet() {
		Iterator<XorSplitCluster> iter =  NFCClusters.iterator();
		while(iter.hasNext()) {
			XorSplitCluster cluster =  iter.next();
			List<PetrinetNode> nfcNodes = cluster.getNFSet();
			if(nfcNodes.size() < 1) {
				// it is empty and then we need to remove it 
				iter.remove();
			}else if(! cluster.isComplete()) {
				for(PetrinetNode node : nfcNodes)
					nfcRules.add(node);
			}
		}
	}
	
}
