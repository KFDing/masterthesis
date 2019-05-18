package org.processmining.plugins.InductiveMiner.reduceacceptingpetrinet;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.framework.packages.PackageManager.Canceller;

public class ReduceAcceptingPetriNetKeepLanguage {

	public static void reduce(AcceptingPetriNet petriNet, Canceller canceller) {

		boolean reduced = false;
		do {
			reduced = false;
			reduced |= MurataFSP1keepLanguage.reduce(petriNet, canceller);
			reduced |= MurataFSP2keepLanguage.reduce(petriNet, canceller);
			reduced |= MurataFPPkeepLanguage.reduce(petriNet, canceller);
			reduced |= MurataESTkeepLanguage.reduce(petriNet, canceller);
			reduced |= MurataFST1keepLanguage.reduce(petriNet, canceller);
			reduced |= MurataFST2keepLanguage.reduce(petriNet, canceller);			
			reduced |= MurataFPTkeepLanguage.reduce(petriNet, canceller);

			if (canceller.isCancelled()) {
				return;
			}
		} while (reduced);
	}
}
