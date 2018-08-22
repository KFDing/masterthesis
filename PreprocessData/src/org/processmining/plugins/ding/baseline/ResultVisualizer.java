package org.processmining.plugins.ding.baseline;

import java.awt.BorderLayout;
import java.util.ArrayList;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.ding.preprocess.Configuration;

@Plugin(name = "Confusion Matrix Result overview", parameterLabels = { "Confusion Matrix" }, returnLabels = { "JPanel" }, returnTypes = { JPanel.class })
@Visualizer
public class ResultVisualizer {
	@PluginVariant(requiredParameterLabels = { 0 })
	public static JPanel overviewVisualizer(PluginContext context,  ArrayList<Integer> confuison_matrix) {
		JEditorPane editorPane = new JEditorPane("text/html", getModelAsHtmlTable(confuison_matrix));
		editorPane.setCaretPosition(0);
        
		JScrollPane scrollPane = new JScrollPane(editorPane);
		
		JPanel returnedPanel = new JPanel();
		returnedPanel.setLayout(new BorderLayout(0, 15));
		returnedPanel.add(scrollPane, BorderLayout.CENTER);
		
		return returnedPanel;
	}
	
	public static String getModelAsHtmlTable(ArrayList<Integer> confusion_matrix) {
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
		
		return htmlTable.toString();
	}
}
