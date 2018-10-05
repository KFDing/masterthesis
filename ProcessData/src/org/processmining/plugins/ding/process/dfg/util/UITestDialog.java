package org.processmining.plugins.ding.process.dfg.util;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.processmining.plugins.InductiveMiner.dfgOnly.DfgMiningParameters;
import org.processmining.plugins.InductiveMiner.dfgOnly.plugins.dialogs.IMdMiningDialog;

public final class UITestDialog extends JFrame {

   /*
    * Our one and only constructor.
    * @param title the title of the Main window.
    */
   public UITestDialog(String title) {
        super(title);
      getContentPane().setLayout(new FlowLayout());

      JButton button = new JButton("Show Dialog");
      button.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            // showDialog();
        	 showMyDialog();
         }
      });

      getContentPane().add(button);
      setSize(300, 300);
      setLocation(450, 300);
      show();
   }

   /*
    * The method used to display a JOptionPane dialog.
    */
   private void showDialog() {
      JPanel panel = new JPanel();
     
      // Generate a row of buttons..
      for (int i=0; i<5; i++) {
          panel.add(new JButton("Button " + i));
      }
      JPanel holder = new JPanel(
                         new BorderLayout());
      holder.add(panel, BorderLayout.NORTH);

      JScrollPane scroll = new JScrollPane(
                           new JTextArea("This is where your contents could " +
                                         "go - be it HTML or whatever"));
      scroll.setPreferredSize(new Dimension(300, 450));

      holder.add(scroll, BorderLayout.CENTER);
     
      // This is where the dialog is actually displayed..
      JOptionPane.showMessageDialog( this,
                                    holder,
                                    "JOptionPane dialog",
                                    JOptionPane.INFORMATION_MESSAGE);
     
   }
   
   private void showMyDialog() {
	   IMdMiningDialog dialog = new IMdMiningDialog();
	   // it is a Jpanel, not a dialog, now I just want to get the pop up JPanel and read the input from it 
	   // String parameters = JOptionPane.showInputDialog(this, "Set parameters fro IM", "Setting", JOptionPane.QUESTION_MESSAGE);
	   JOptionPane.showMessageDialog( this,
			   dialog,
               "JOptionPane dialog",
               JOptionPane.INFORMATION_MESSAGE);
	   DfgMiningParameters parameters = dialog.getMiningParameters();
       System.out.println("Paras " + parameters.getNoiseThreshold());
       System.out.println("Paras " + parameters.getDfgSplitter().toString());
      
   }

   /*
    * Main method. Application entry point.
    * @param args[] A String array representing the command line arguments.
    */
   public static void main(String args[]) {
       new UITestDialog("Testing a JOptionPane's Options...");
   }
}