package org.processmining.plugins.InductiveMiner.efficienttree;

import org.processmining.plugins.InductiveMiner.efficienttree.reductionrules.LoopATauTau2flower;
import org.processmining.plugins.InductiveMiner.efficienttree.reductionrules.XorDoubleSingleChild;

public class EfficientTreeReduceParametersDuplicates extends EfficientTreeReduceParameters {

	public EfficientTreeReduceParametersDuplicates(boolean collapsed) {
		super(collapsed, false);

		EfficientTreeReductionRule[] rulesXorNew = new EfficientTreeReductionRule[rulesXor.length + 1];
		System.arraycopy(rulesXor, 0, rulesXorNew, 0, rulesXor.length);
		rulesXorNew[rulesXor.length] = new XorDoubleSingleChild();
		rulesXor = rulesXorNew;
		
		EfficientTreeReductionRule[] rulesLoopNew = new EfficientTreeReductionRule[rulesLoop.length + 1];
		System.arraycopy(rulesLoop, 0, rulesLoopNew, 0, rulesLoop.length);
		rulesLoopNew[rulesLoop.length] = new LoopATauTau2flower();
		rulesLoop = rulesLoopNew;
	}
}
