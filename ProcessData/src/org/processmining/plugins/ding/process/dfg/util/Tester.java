package org.processmining.plugins.ding.process.dfg.util;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;
import javax.swing.JFrame;

public class Tester {

	    public Tester(){

	        JComboBox box = new JComboBox();
	        box.addItem("One");
	        box.addItem("Two");
	        box.addItem("Three");
	        box.addItem("Four");

	        box.addItemListener(new ItemListener(){
	            public void itemStateChanged(ItemEvent e){
	                System.out.println(e.getItem() + " " + e.getStateChange() );
	            }
	        });

	        JFrame frame = new JFrame();
	        frame.getContentPane().add(box);
	        frame.pack();
	        frame.setVisible(true);
	    }

	    public static void main(String [] args) {
	        Tester tester = new Tester();
	    }
	}
