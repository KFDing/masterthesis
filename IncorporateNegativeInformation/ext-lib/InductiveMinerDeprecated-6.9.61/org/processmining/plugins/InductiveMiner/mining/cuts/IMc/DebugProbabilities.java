package org.processmining.plugins.InductiveMiner.mining.cuts.IMc;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.Matrix;
import org.processmining.plugins.InductiveMiner.graphs.Graph;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;

public class DebugProbabilities {
	
	public static String debug(CutFinderIMinInfo info, MiningParameters parameters, boolean useHTML) {
		
		String newLine;
		if (useHTML) {
			newLine = "<br>\n";
		} else {
			newLine = "\n";
		}
		
		Graph<XEventClass> graph = info.getGraph();

		if (graph.getNumberOfVertices() == 1) {
			return "";
		}

		StringBuilder r = new StringBuilder();
		{
			r.append("xor");
			r.append(newLine);
			Matrix<XEventClass, Double> m = new Matrix<XEventClass, Double>(graph.getVertices(), false);
			for (XEventClass a : graph.getVertices()) {
				for (XEventClass b : graph.getVertices()) {
					if (a != b) {
						m.set(a, b, parameters.getSatProbabilities().getProbabilityXor(info, a, b));
					}
				}
			}
			r.append(m.toString(useHTML));
		}

		{
			r.append("sequence");
			r.append(newLine);
			Matrix<XEventClass, Double> m = new Matrix<XEventClass, Double>(graph.getVertices(), false);
			for (XEventClass a : graph.getVertices()) {
				for (XEventClass b : graph.getVertices()) {
					if (a != b) {
						m.set(a, b, parameters.getSatProbabilities().getProbabilitySequence(info, a, b));
					}
				}
			}
			r.append(m.toString(useHTML));
		}

		{
			r.append("parallel");
			r.append(newLine);
			Matrix<XEventClass, Double> m = new Matrix<XEventClass, Double>(graph.getVertices(), false);
			for (XEventClass a : graph.getVertices()) {
				for (XEventClass b : graph.getVertices()) {
					if (a != b) {
						m.set(a, b, parameters.getSatProbabilities().getProbabilityParallel(info, a, b));
					}
				}
			}
			r.append(m.toString(useHTML));
		}

		{
			r.append("loop single");
			r.append(newLine);
			Matrix<XEventClass, Double> m = new Matrix<XEventClass, Double>(graph.getVertices(), false);
			for (XEventClass a : graph.getVertices()) {
				for (XEventClass b : graph.getVertices()) {
					if (a != b) {
						m.set(a, b, parameters.getSatProbabilities().getProbabilityLoopSingle(info, a, b));
					}
				}
			}
			r.append(m.toString(useHTML));
		}
		
		{
			r.append("loop indirect");
			r.append(newLine);
			Matrix<XEventClass, Double> m = new Matrix<XEventClass, Double>(graph.getVertices(), false);
			for (XEventClass a : graph.getVertices()) {
				for (XEventClass b : graph.getVertices()) {
					if (a != b) {
						m.set(a, b, parameters.getSatProbabilities().getProbabilityLoopIndirect(info, a, b));
					}
				}
			}
			r.append(m.toString(useHTML));
		}
		
		{
			r.append("loop double");
			r.append(newLine);
			Matrix<XEventClass, Double> m = new Matrix<XEventClass, Double>(graph.getVertices(), false);
			for (XEventClass a : graph.getVertices()) {
				for (XEventClass b : graph.getVertices()) {
					if (a != b) {
						m.set(a, b, parameters.getSatProbabilities().getProbabilityLoopDouble(info, a, b));
					}
				}
			}
			r.append(m.toString(useHTML));
		}

		return r.toString();
	}
}
