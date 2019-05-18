package org.processmining.plugins.InductiveMiner.efficienttree;

import org.apache.commons.lang3.ArrayUtils;
import org.processmining.plugins.InductiveMiner.efficienttree.reductionrules.LoopTau;
import org.processmining.plugins.InductiveMiner.efficienttree.reductionrules.XorTauTauLoop2flowerRevert;

public class EfficientTreeReduceParametersForPetriNet extends EfficientTreeReduceParameters {

	public EfficientTreeReduceParametersForPetriNet(boolean collapsed) {
		super(collapsed, false);

		EfficientTreeReductionRule[] rulesXorNew = new EfficientTreeReductionRule[rulesXor.length + 1];
		System.arraycopy(rulesXor, 0, rulesXorNew, 0, rulesXor.length);
		rulesXorNew[rulesXor.length] = new XorTauTauLoop2flowerRevert();
		
		for (int i = 0; i < rulesLoop.length; i++) {
			if (rulesLoop[i] instanceof LoopTau) {
				rulesLoop = ArrayUtils.remove(rulesLoop, i);
				break;
			}
			
		}
		rulesXor = rulesXorNew;
	}

}
