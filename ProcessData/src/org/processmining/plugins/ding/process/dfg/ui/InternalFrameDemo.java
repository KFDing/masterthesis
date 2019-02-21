package org.processmining.plugins.ding.process.dfg.ui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class InternalFrameDemo extends JFrame implements ActionListener {

	JDesktopPane desktop;
	
	public InternalFrameDemo() {
		super("Evaluation Result");
		
		// set the size of this windows
		int inset = 50;
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds(inset, inset, screenSize.width - inset*2, screenSize.height - inset*2);
		
		// set up GUI
		desktop = new JDesktopPane();
		createFrame();
		setContentPane(desktop);
		
		setJMenuBar(createMenuBar());
		
		desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
	}
	
	protected JMenuBar createMenuBar() {
		JMenuBar menuBar =  new JMenuBar();
		
		// set up the menu
		JMenu menu = new JMenu("Document");
		menu.setMnemonic(KeyEvent.VK_D);
		menuBar.add(menu);
		
		JMenuItem menuItem = new JMenuItem("new");
		menuItem.setMnemonic(KeyEvent.VK_N);
		menuItem.setActionCommand("new");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Quit");
        menuItem.setMnemonic(KeyEvent.VK_Q);
        menuItem.setActionCommand("quit");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
		return menuBar;
	}
	
	// create an internal frame
	private void createFrame() {
		// TODO if we want to createFrame, we need to make sure that we have confusion_matrix
		String title = "Ext: 1.0; Pos: 1.0; Neg: 1.0";
		ArrayList<Integer> confusion_matrix = new ArrayList<>();
		confusion_matrix.add(100);
		confusion_matrix.add(10);
		confusion_matrix.add(50);
		confusion_matrix.add(100);
		
		CMInternalFrame cmFrame = new CMInternalFrame(title, confusion_matrix);
		cmFrame.setVisible(true);
		
		desktop.add(cmFrame);
		
		try {
			cmFrame.setSelected(true);
			
		}catch (Exception e) {
			// TODO: handle exception
		}
	}

	// if the action is a button pressed, create a new frame
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if("new".equals(e.getActionCommand())) {
			createFrame();
		}else {
			quit();
		}
	}

	private void quit() {
		// TODO Auto-generated method stub
		System.exit(0);
	}

	private static void createAndShowGUI() {
        //Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);

        //Create and set up the window.
        InternalFrameDemo frame = new InternalFrameDemo();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Display the window.
        frame.setVisible(true);
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
