package org.processmining.plugins.ding.process.dfg.ui;

import java.awt.BorderLayout;
import java.util.ArrayList;

import javax.swing.JEditorPane;
import javax.swing.JPanel;

import org.processmining.plugins.ding.preprocess.util.Configuration;

public class ConfusionMatrixJPanel extends JPanel {

	/**
	 * Create the panel.
	 */
	public ConfusionMatrixJPanel() {
		
	}
	
	public ConfusionMatrixJPanel(ArrayList<Integer> confusion_matrix) {
		StringBuilder htmlTable = new StringBuilder();
		
		htmlTable.append("<table><thead><th></th><th>Allowed Behavior</th><th>Not Allowed Behavior</th></thead><tbody>");
		htmlTable.append("<tr><td>Positive</td><td>"+ confusion_matrix.get(Configuration.ALLOWED_POS_IDX)+"</td><td>"+confusion_matrix.get(Configuration.NOT_ALLOWED_POS_IDX)+"</td></tr>");
		htmlTable.append("<tr><td>Negative</td><td>"+ confusion_matrix.get(Configuration.ALLOWED_NEG_IDX)+"</td><td>"+confusion_matrix.get(Configuration.NOT_ALLOWED_NEG_IDX)+"</td></tr>");
		htmlTable.append("</tbody></table>");
		
		htmlTable.append("Show the evaluation criteria\n");
		htmlTable.append("<table><thead><th>Recall</th><th>Precision</th><th>Accuracy</th><th>F-score</th></thead><tbody>");
		double[] criteria = new double[Configuration.CRITERIA_SIZE];
		// how to avoid the zeros denominator ??? 
		criteria[Configuration.RECALL] = 1.0*confusion_matrix.get(Configuration.ALLOWED_POS_IDX) / (confusion_matrix.get(Configuration.ALLOWED_POS_IDX) + confusion_matrix.get(Configuration.NOT_ALLOWED_POS_IDX) );
		criteria[Configuration.PRECISION] = 1.0*confusion_matrix.get(Configuration.ALLOWED_POS_IDX) / (confusion_matrix.get(Configuration.ALLOWED_POS_IDX) + confusion_matrix.get(Configuration.ALLOWED_NEG_IDX) );
		criteria[Configuration.ACCURACY] = 1.0*(confusion_matrix.get(Configuration.ALLOWED_POS_IDX) + confusion_matrix.get(Configuration.NOT_ALLOWED_NEG_IDX)) / 
				               ( confusion_matrix.get(Configuration.ALLOWED_POS_IDX) + confusion_matrix.get(Configuration.NOT_ALLOWED_NEG_IDX) + confusion_matrix.get(Configuration.ALLOWED_NEG_IDX) + confusion_matrix.get(Configuration.NOT_ALLOWED_POS_IDX) );
		criteria[Configuration.F1_SCORE] = 2.0*confusion_matrix.get(Configuration.ALLOWED_POS_IDX) / (2*confusion_matrix.get(Configuration.ALLOWED_POS_IDX) + confusion_matrix.get(Configuration.ALLOWED_NEG_IDX) + confusion_matrix.get(Configuration.NOT_ALLOWED_POS_IDX) );
		
		htmlTable.append("<tr><td>"+criteria[Configuration.RECALL] +"</td><td>"+ criteria[Configuration.PRECISION]+"</td><td>"+criteria[Configuration.ACCURACY]+"</td><td>"+ criteria[Configuration.F1_SCORE]+"</tr>");
		htmlTable.append("</tbody></table>");
		
		JEditorPane editorPane = new JEditorPane("text/html", htmlTable.toString());
		editorPane.setCaretPosition(0);
        
		setLayout(new BorderLayout(0, 15));
		add(editorPane, BorderLayout.CENTER);
	}
}
