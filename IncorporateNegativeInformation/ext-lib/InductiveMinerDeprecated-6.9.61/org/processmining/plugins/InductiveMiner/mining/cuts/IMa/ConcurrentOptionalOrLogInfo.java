package org.processmining.plugins.InductiveMiner.mining.cuts.IMa;

import java.util.BitSet;
import java.util.Set;

import org.processmining.plugins.InductiveMiner.Pair;

public class ConcurrentOptionalOrLogInfo {

	private final int numberOfComponents;
	private BitSet always;
	private BitSet implications;
	private BitSet hasIncomingImplications; //denotes whether a node has incoming (i.e. from other nodes) implications.

	public ConcurrentOptionalOrLogInfo(ConcurrentOptionalOrLog log) {
		this.numberOfComponents = log.getNumberOfComponents();

		/**
		 * Compute the always relation
		 */
		{
			always = new BitSet(numberOfComponents);
			always.set(0, numberOfComponents);
			for (BitSet trace : log.getTraces()) {
				always.and(trace);
			}
		}

		/**
		 * Compute the implications-relation
		 */
		{
			implications = new BitSet(numberOfComponents * numberOfComponents);
			implications.set(0, numberOfComponents * numberOfComponents);
			for (BitSet trace : log.getTraces()) {
				for (int component = 0; component < numberOfComponents; component++) {
					if (trace.get(component)) {
						//walk over the outgoing implications of this component  
						int endImplicationNumber = getImplicationNumber(component, numberOfComponents - 1);
						int implicationNumber = implications.nextSetBit(getImplicationNumber(component, 0));
						while (implicationNumber <= endImplicationNumber && implicationNumber >= 0) {
							//this implication held for the previous traces

							if (!trace.get(getImplicationImpliesComponent(implicationNumber))) {
								//we have found a counterexample for this implication
								implications.set(implicationNumber, false);
							}

							implicationNumber = implications.nextSetBit(implicationNumber + 1);
						}
					}
				}
			}
		}

		/**
		 * Compute the incoming implications relation
		 */
		{
			hasIncomingImplications = new BitSet(numberOfComponents);
			int implicationNumber = implications.nextSetBit(0);
			while (implicationNumber >= 0) {
				if (getImplicationImpliesComponent(implicationNumber) != getImplicationComponent(implicationNumber)) {
					hasIncomingImplications.set(getImplicationImpliesComponent(implicationNumber));
				}
				implicationNumber = implications.nextSetBit(implicationNumber + 1);
			}
		}
	}

	public Pair<Integer, Integer> findBiImplication() {
		int implicationNumber = implications.nextSetBit(0);
		int reverse;
		int componentA;
		int componentB;
		while (implicationNumber >= 0) {
			componentA = getImplicationComponent(implicationNumber);
			componentB = getImplicationImpliesComponent(implicationNumber);
			if (componentA != componentB) {
				reverse = getImplicationNumber(componentB, componentA);
				if (implications.get(reverse)) {
					return Pair.of(componentA, componentB);
				}
			}
			implicationNumber = implications.nextSetBit(implicationNumber + 1);
		}
		return null;
	}

	public Pair<Integer, Integer> findOr(Set<BitSet> traces) {
		int implicationNumber = implications.nextClearBit(0);
		int max = numberOfComponents * numberOfComponents;
		int componentA;
		int componentB;
		while (implicationNumber < max) {
			componentA = getImplicationComponent(implicationNumber);
			componentB = getImplicationImpliesComponent(implicationNumber);
			if (componentA != componentB && !hasIncomingImplications.get(componentA)
					&& !hasIncomingImplications.get(componentB)) {

				//walk through all the traces to verify the OR-relation
				boolean validCombination = true;
				for (BitSet trace : traces) {
					if (trace.get(componentA) || trace.get(componentB)) {
						//in this trace, A or B is present
						//then, the traces must also be present in which the other combinations of A and B are present
						BitSet ab = (BitSet) trace.clone();
						ab.set(componentA);
						ab.set(componentB);
						if (!traces.contains(ab)) {
							validCombination = false;
							break;
						}

						//only a
						ab.set(componentA);
						ab.set(componentB, false);
						if (!traces.contains(ab)) {
							validCombination = false;
							break;
						}

						//only b
						ab.set(componentA, false);
						ab.set(componentB);
						if (!traces.contains(ab)) {
							validCombination = false;
							break;
						}
					}
				}
				if (validCombination) {
					return Pair.of(componentA, componentB);
				}
			}

			implicationNumber = implications.nextClearBit(implicationNumber + 1);
		}
		return null;
	}

	/**
	 * Find a pair of components A, B such that A implies B and A is optional.
	 * 
	 * @param traces
	 * @return
	 */
	public Pair<Integer, Integer> findOptionalAnd(Set<BitSet> traces) {
		int implicationNumber = implications.nextSetBit(0);
		int reverse;
		int componentA;
		int componentB;
		while (implicationNumber >= 0) {
			componentA = getImplicationComponent(implicationNumber);
			componentB = getImplicationImpliesComponent(implicationNumber);
			if (componentA != componentB) {
				reverse = getImplicationNumber(componentB, componentA);
				if (!implications.get(reverse) && hasNoOtherIncomingImplications(componentB, componentA)) {
					//there is a one-way implication

					/**
					 * A must be optional. Thus, for each trace in which A is
					 * true, there must be the same trace in which A is false.
					 */
					boolean allTracesPresent = true;
					for (BitSet trace : traces) {
						if (trace.get(componentA)) {
							BitSet traceWithoutA = (BitSet) trace.clone();
							traceWithoutA.set(componentA, false);
							if (!traces.contains(traceWithoutA)) {
								allTracesPresent = false;
								break;
							}
						}
					}
					if (allTracesPresent) {
						return Pair.of(componentA, componentB);
					}
				}
			}

			implicationNumber = implications.nextSetBit(implicationNumber + 1);
		}
		return null;
	}

	/**
	 * 
	 * @param component
	 * @param thanComponent
	 * @return Whether component has at most two incoming implications: from
	 *         itself and from thanComponent.
	 */
	private boolean hasNoOtherIncomingImplications(int component, int thanComponent) {
		for (int componentB = 0; componentB < numberOfComponents; componentB++) {
			if (componentB != thanComponent && componentB != component) {
				if (implications.get(getImplicationNumber(componentB, component))) {
					return false;
				}
			}
		}
		return true;
	}

	private int getImplicationNumber(int component, int impliesComponent) {
		return component * numberOfComponents + impliesComponent;
	}

	private int getImplicationComponent(int implicationNumber) {
		return implicationNumber / numberOfComponents;
	}

	private int getImplicationImpliesComponent(int implicationNumber) {
		return implicationNumber % numberOfComponents;
	}
}
