package org.processmining.plugins.InductiveMiner.efficienttree;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.processmining.plugins.InductiveMiner.Triple;

public class EfficientTreeEditor extends JPanel {

	private static final long serialVersionUID = -75989442629887735L;

	private static final int update_delay = 500;
	private static final int spacesPerTab = 3;
	private static final Color errorColour = Color.yellow;

	protected final RSyntaxTextArea text;
	protected final JLabel errorMessage;
	private ActionListener actionListener;
	private boolean contentChangedFromController = false;

	/**
	 * 
	 * @param tree
	 *            May be null, in which case no tree will be set-up in the
	 *            editor.
	 * @param message
	 *            If not null, the message will be shown to the user and editing
	 *            the model will be disabled.
	 */
	public EfficientTreeEditor(EfficientTree tree, String message) {
		setLayout(new BorderLayout());
		setOpaque(false);

		//text area
		text = new RSyntaxTextArea();
		text.setTabSize(spacesPerTab);
		text.setWhitespaceVisible(true);
		text.discardAllEdits();
		JScrollPane textScroll = new JScrollPane(text);
		add(textScroll, BorderLayout.CENTER);

		//error message
		errorMessage = new JLabel(" ");
		errorMessage.setBackground(errorColour);
		errorMessage.setOpaque(false);
		add(errorMessage, BorderLayout.PAGE_END);

		if (tree != null) {
			setTree(tree);
		}
		if (message != null) {
			setMessage(message);
		} else {
			try {
				setErrorMessage(-1, null);
			} catch (BadLocationException e1) {
				//can never happen
				e1.printStackTrace();
			}
		}

		// set update timer
		final AtomicReference<Timer> updateTimerR = new AtomicReference<Timer>();
		final Timer updateTimer = new Timer(update_delay, new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				//if we're good to go, send an update to the controller
				if (actionListener != null && !contentChangedFromController) {
					try {
						Triple<EfficientTree, Integer, String> result = ProcessTreeParser.parse(text.getText(),
								spacesPerTab);
						if (result.getA() == null) {
							//set error message
							setErrorMessage(result.getB(), result.getC());
						} else {
							//remove error message
							setErrorMessage(-1, null);

							EfficientTree newTree = result.getA();
							final ActionEvent e2 = new ActionEvent(newTree, 0, "");
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									actionListener.actionPerformed(e2);
								}
							});
						}
					} catch (UnknownTreeNodeException | IOException e1) {
						e1.printStackTrace();
					} catch (BadLocationException e1) {
						e1.printStackTrace();
					}
				}
				contentChangedFromController = false;
				updateTimerR.get().stop();
			}
		});
		updateTimer.setCoalesce(true);
		updateTimerR.set(updateTimer);

		// set onkey handlers
		text.getDocument().addDocumentListener(new DocumentListener() {

			public void removeUpdate(DocumentEvent e) {
				try {
					setErrorMessage(-1, null);
				} catch (BadLocationException e1) {
					e1.printStackTrace();
				}
				updateGraphOnTimer(updateTimer);
			}

			public void insertUpdate(DocumentEvent e) {
				try {
					setErrorMessage(-1, null);
				} catch (BadLocationException e1) {
					e1.printStackTrace();
				}
				updateGraphOnTimer(updateTimer);
			}

			public void changedUpdate(DocumentEvent e) {
				try {
					setErrorMessage(-1, null);
				} catch (BadLocationException e1) {
					e1.printStackTrace();
				}
				updateGraphOnTimer(updateTimer);
			}
		});
	}

	/**
	 * If a message is set, editing the model is disabled.
	 * 
	 * @param message
	 */
	public void setMessage(String message) {
		text.removeAllLineHighlights();
		text.setEnabled(false);
		errorMessage.setOpaque(false);
		errorMessage.setText(message);
	}

	private void setErrorMessage(int line, String message) throws BadLocationException {
		if (line >= 0) {
			errorMessage.setText(message);
			errorMessage.setOpaque(true);
			text.addLineHighlight(line, errorColour);
		} else {
			errorMessage.setText("Operators: tau, xor, sequence, concurrent, interleaved, loop & or.");
			errorMessage.setOpaque(false);
			text.removeAllLineHighlights();
		}
	}

	/**
	 * Set the editor to the given tree.
	 * 
	 * @param tree
	 */
	public void setTree(EfficientTree tree) {
		assert (tree != null);
		contentChangedFromController = true;
		text.setText(EfficientTree2HumanReadableString.toString(tree));
		text.setEnabled(true);
	}

	protected static void updateGraphOnTimer(Timer updateTimer) {
		updateTimer.restart();
	}

	public void addActionListener(ActionListener actionListener) {
		this.actionListener = actionListener;
	}

}
