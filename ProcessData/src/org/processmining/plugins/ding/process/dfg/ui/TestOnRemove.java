package org.processmining.plugins.ding.process.dfg.ui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
 
public class TestOnRemove extends JFrame{
	private static final long serialVersionUID = 2L;
	// to make the layout stable, not remove all the component 
	JPanel graphPanel; 
	JDesktopPane showCMPanel;
	boolean drawDfg = true;
	
	public TestOnRemove() {
		RelativeLayout rLayout = new RelativeLayout(RelativeLayout.Y_AXIS);
		rLayout.setFill( true );
		this.setLayout(rLayout);
		this.setSize(600, 600);
		
		JPanel choose = new JPanel();
		choose.setBackground(Color.CYAN);
		JButton graphRMButton = new JButton("remove Component graph");
		
		graphRMButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				// TODO remove the graphPanel from it 
				System.out.println("remove graph");
				remove(graphPanel);
				repaint();
			}
		});
		
		JButton showRMButton = new JButton("remove Component show");
		
		showRMButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				// TODO remove the showPanel from it 
				System.out.println("remove show");
				remove(showCMPanel);
				repaint();
			}
		});
		
		JButton graphAddButton = new JButton("add Component graph");
		
		graphAddButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				// TODO remove the graphPanel from it 
				// here we use to generate a new panel for it...
				// and we only use 10 % of it for choose, after this we see the layout..
				createGraph();
				repaint();
			}
		});
		
		JButton showAddButton = new JButton("add Component show");
		
		showAddButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				// TODO remove the showPanel from it 
				createShow();
				repaint();
			}
		});
		
		
		
		choose.add(graphRMButton);
		choose.add(showRMButton);
		choose.add(graphAddButton);
		choose.add(showAddButton);
		
		add(choose, new Float(10));
		
		
	}

	private void createGraph() {
		if(graphPanel == null) {
			System.out.println("First to create");
		}else {
			System.out.println("Other to create");
		}
		graphPanel = new JPanel();
		graphPanel.setBackground(Color.YELLOW);
		JLabel graphLabel = new JLabel("graph");
		graphPanel.add(graphLabel);
		graphPanel.setVisible(true);
		graphPanel.validate();
		add(graphPanel, new Float(70));
	}
	
	private void createShow() {
		if(showCMPanel == null) {
			System.out.println("First to create show");
		}else {
			System.out.println("Other to create show");
		}
		showCMPanel = new JDesktopPane();
		showCMPanel.setBackground(Color.RED);
		JLabel showLabel = new JLabel("show");
		showCMPanel.add(showLabel);
		showCMPanel.setVisible(true);
		showCMPanel.validate();
		add(showCMPanel, new Float(20));
		
	}
	
	
	private static void createAndShowGUI() {
        //Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);

        //Create and set up the window.
        TestOnRemove frame = new TestOnRemove();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Display the window.
        frame.setVisible(true);
        frame.pack();
    }

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
	
	
}
