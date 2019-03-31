package org.processmining.incorporatenegativeinformation.dialogs.ui.process;

import java.awt.BorderLayout;
import java.util.ArrayList;

import javax.swing.JEditorPane;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;

import org.processmining.incorporatenegativeinformation.help.Configuration;

public class CMInternalFrame extends JInternalFrame {

	static int openFrameCount = 0;
	static final int xOffset = 20, yOffset = 20;

	// we need to give several parameter to create this frame
	// one is confusion matrix and the result of it ?? Not really
	// one is to set the name for it 
	public CMInternalFrame(String title, ArrayList<Integer> confusion_matrix) {
		super("Result With " + title, true, true, true, true);

		// create the jpanel to add it here
		ConfusionMatrixJPanel cmPanel = new ConfusionMatrixJPanel(confusion_matrix);
		add(cmPanel);

		setSize(400, 200);
		// set windown location
		setLocation(xOffset * openFrameCount, yOffset * openFrameCount);
		openFrameCount++;
	}

}

class ConfusionMatrixJPanel extends JPanel {

	/**
	 * Create the panel.
	 */
	public ConfusionMatrixJPanel() {

	}

	public ConfusionMatrixJPanel(ArrayList<Integer> confusion_matrix) {
		StringBuilder htmlTable = new StringBuilder();

		htmlTable
				.append("<table><thead><th></th><th>Allowed Behavior</th><th>Not Allowed Behavior</th></thead><tbody>");
		htmlTable.append("<tr><td>Positive</td><td>" + confusion_matrix.get(Configuration.ALLOWED_POS_IDX) + "</td><td>"
				+ confusion_matrix.get(Configuration.NOT_ALLOWED_POS_IDX) + "</td></tr>");
		htmlTable.append("<tr><td>Negative</td><td>" + confusion_matrix.get(Configuration.ALLOWED_NEG_IDX) + "</td><td>"
				+ confusion_matrix.get(Configuration.NOT_ALLOWED_NEG_IDX) + "</td></tr>");
		htmlTable.append("</tbody></table>");

		htmlTable.append("Show the evaluation criteria\n");
		htmlTable.append(
				"<table><thead><th>Recall</th><th>Precision</th><th>Accuracy</th><th>F-score</th></thead><tbody>");
		double[] criteria = new double[Configuration.CRITERIA_SIZE];
		// how to avoid the zeros denominator ??? 
		criteria[Configuration.RECALL] = 1.0 * confusion_matrix.get(Configuration.ALLOWED_POS_IDX)
				/ (confusion_matrix.get(Configuration.ALLOWED_POS_IDX)
						+ confusion_matrix.get(Configuration.NOT_ALLOWED_POS_IDX));
		criteria[Configuration.PRECISION] = 1.0 * confusion_matrix.get(Configuration.ALLOWED_POS_IDX)
				/ (confusion_matrix.get(Configuration.ALLOWED_POS_IDX)
						+ confusion_matrix.get(Configuration.ALLOWED_NEG_IDX));
		criteria[Configuration.ACCURACY] = 1.0
				* (confusion_matrix.get(Configuration.ALLOWED_POS_IDX)
						+ confusion_matrix.get(Configuration.NOT_ALLOWED_NEG_IDX))
				/ (confusion_matrix.get(Configuration.ALLOWED_POS_IDX)
						+ confusion_matrix.get(Configuration.NOT_ALLOWED_NEG_IDX)
						+ confusion_matrix.get(Configuration.ALLOWED_NEG_IDX)
						+ confusion_matrix.get(Configuration.NOT_ALLOWED_POS_IDX));
		criteria[Configuration.F1_SCORE] = 2.0 * confusion_matrix.get(Configuration.ALLOWED_POS_IDX)
				/ (2 * confusion_matrix.get(Configuration.ALLOWED_POS_IDX)
						+ confusion_matrix.get(Configuration.ALLOWED_NEG_IDX)
						+ confusion_matrix.get(Configuration.NOT_ALLOWED_POS_IDX));

		htmlTable.append("<tr><td>" + criteria[Configuration.RECALL] + "</td><td>" + criteria[Configuration.PRECISION]
				+ "</td><td>" + criteria[Configuration.ACCURACY] + "</td><td>" + criteria[Configuration.F1_SCORE]
				+ "</tr>");
		htmlTable.append("</tbody></table>");

		JEditorPane editorPane = new JEditorPane("text/html", htmlTable.toString());
		editorPane.setCaretPosition(0);

		setLayout(new BorderLayout(0, 15));
		add(editorPane, BorderLayout.CENTER);
	}
}