package org.processmining.plugins.ding.process.dfg.ui;

import java.util.ArrayList;

import javax.swing.JInternalFrame;

public class CMInternalFrame extends JInternalFrame {
	
	static int openFrameCount = 0;
	static final int xOffset = 30, yOffset = 30;
	
	// we need to give several parameter to create this frame
	// one is confusion matrix and the result of it ?? Not really
	// one is to set the name for it 
	public CMInternalFrame(String title, ArrayList<Integer> confusion_matrix) {
		super("Result With " + title, 
				true, true, true, true);
		
		// create the jpanel to add it here
		ConfusionMatrixJPanel cmPanel = new ConfusionMatrixJPanel(confusion_matrix);
		add(cmPanel);
		
		setSize(200, 200);
		// set windown location
		setLocation(xOffset * openFrameCount, yOffset* openFrameCount);
		openFrameCount++;
	}
	
	
	
}
