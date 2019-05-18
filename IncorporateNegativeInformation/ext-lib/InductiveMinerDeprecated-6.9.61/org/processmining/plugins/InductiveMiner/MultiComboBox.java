package org.processmining.plugins.InductiveMiner;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Array;
import java.util.BitSet;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.accessibility.Accessible;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicComboPopup;

import org.processmining.framework.util.ui.widgets.WidgetColors;

import com.fluxicon.slickerbox.factory.SlickerDecorator;

public class MultiComboBox<E> extends JComboBox<Object> {
	public static final Color textfield_fg = new Color(32, 32, 32);
	public static final Color even_colour_bg = new Color(160, 160, 160);
	public static final Color odd_colour_bg = new Color(180, 180, 180);
	public static final Color colour_fg = new Color(30, 30, 30);
	public static final Color rollover_colour_bg = WidgetColors.COLOR_LIST_SELECTION_BG;
	public static final Color rollover_colour_fg = WidgetColors.COLOR_LIST_SELECTION_FG;
	public static final Color selection_colour_bg = new Color(50, 50, 50);
	public static final Color selection_colour_fg = new Color(180, 180, 180);

	private static final long serialVersionUID = -7720215210683281697L;
	//private transient CellButtonsMouseListener cbml;
	private BitSet selected; //denotes which elements are currently selected
	private BitSet singular; //denotes which elements do not allow other elements to be selected at the same time.
	private Class<E> clazz;

	/**
	 * We need to override setSelectedItem(Object) to provide developers with
	 * the ability to change the selected object. However, JComboBox calls this
	 * function as well internally. Therefore, we introduce a boolean that
	 * denotes that selection changes should be ignored.
	 */
	private boolean preventSelectionChange = false;

	/**
	 * We need to override setPopupVisible to prevent flickering of the popup
	 * when clicking on a checkbox. This boolean performs the trick.
	 */
	private boolean preventPopupClosing = false;

	/**
	 * We need to do the event handling ourselves; otherwise, listeners are
	 * called before we can do an update.
	 */
	protected CopyOnWriteArrayList<ActionListener> listenerList = new CopyOnWriteArrayList<>();

	@SuppressWarnings("unchecked")
	public MultiComboBox(Class<E> clazz, E[] aModel) {
		super(aModel);
		this.clazz = clazz;
		selected = new BitSet(aModel.length);
		singular = new BitSet(aModel.length);
		if (aModel.length > 0) {
			selected.set(0);
		}

		SlickerDecorator.instance().decorate(this);

		addPopupMenuListener(new BoundsPopupMenuListener(true, false));
		setPrototypeDisplayValue("classifiers");
		//setBackground(textfield_bg);
		//getList().setSelectionBackground(textfield_bg);
		//setOpaque(true);
		//updateUI();
		setRenderer((ListCellRenderer<? super Object>) new ButtonsRenderer());
		JList<E> list = getList();
		if (list != null) {
			CellButtonsMouseListener cbml = new CellButtonsMouseListener();
			list.addMouseListener(cbml);
			list.addMouseMotionListener(cbml);
			ComboBoxMouseListener cbbml = new ComboBoxMouseListener();
			addMouseListener(cbbml);
			addMouseMotionListener(cbbml);
		}
	}

	/**
	 * Add an action listener.
	 */
	public void addActionListener(ActionListener l) {
		listenerList.add(l);
	}

	/**
	 * Notify listeners that something changed.
	 */
	protected void notifyActionEvent() {
		for (ActionListener listener : listenerList) {
			listener.actionPerformed(new ActionEvent(MultiComboBox.this, ActionEvent.ACTION_PERFORMED, ""));
		}
	}

	/**
	 * Returns a string representation of the selected values. The objects
	 * themselves are not returned; use getSelectedItems() for that. Apologies
	 * for this annoyance, but the unaccessible paint() method in SlickerFactory
	 * calls this.
	 */
	@Override
	public String getSelectedItem() {
		return getTitle();
	};

	@Override
	public void setSelectedItem(Object anObject) {
		if (!clazz.isInstance(anObject)) {
			throw new RuntimeException("please give me an appropriate object to select");
		}
		if (!preventSelectionChange) {
			selected.clear();
			ListModel<E> model = getList().getModel();
			for (int i = 0; i < model.getSize(); i++) {
				if (model.getElementAt(i).equals(anObject)) {
					selected.set(i);
					return;
				}
			}
		}
	};

	/**
	 * Return an array of selected objects.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public E[] getSelectedObjects() {
		E[] result = (E[]) Array.newInstance(clazz, selected.cardinality());
		int j = 0;
		for (int i = selected.nextSetBit(0); i != -1 && i < getList().getModel().getSize(); i = selected
				.nextSetBit(i + 1)) {
			result[j] = getList().getModel().getElementAt(i);
			j++;
		}
		return result;
	};

	/**
	 * 
	 * @return A human-readable summary that represents the selection.
	 */
	public String getTitle() {
		E[] objects = getSelectedObjects();
		if (objects.length == 0) {
			return "...";
		} else if (objects.length == 1) {
			return objects[0].toString();
		} else {
			return "(" + objects.length + ")";
		}
	}

	@SuppressWarnings("unchecked")
	protected JList<E> getList() {
		Accessible a = getAccessibleContext().getAccessibleChild(0);
		if (a instanceof BasicComboPopup) {
			return ((BasicComboPopup) a).getList();
		} else {
			return null;
		}
	}

	@Override
	public void setPopupVisible(boolean v) {
		if (!preventPopupClosing) {
			super.setPopupVisible(v);
		}
	};

	/**
	 * Add elements to the combobox.
	 * 
	 * @param items
	 *            The elements to be added.
	 * @param allSingular
	 *            True means that each added element can only be selected by
	 *            itself. For these elements, the checkbox will not be drawn.
	 */
	public void addItems(E[] items, boolean allSingular) {
		for (E item : items) {
			addItem(item);
		}
		if (items.length > 0) {
			selected.set(0);
		}
	}

	/**
	 * Add an element to the combobox.
	 * 
	 * @param item
	 *            The element to be added.
	 * @param singular
	 *            If true, this element can only be selected on its own, i.e
	 *            there is no possibility to select another element as well.
	 */
	public void addItem(E item, boolean singular) {
		super.addItem(item);
		this.singular.set(getList().getModel().getSize() - 1, singular);
		if (getList().getModel().getSize() == 0) {
			selected.set(0);
		}
	}

	@Override
	public void removeAllItems() {
		super.removeAllItems();
		selected.clear();
		singular.clear();
	};

	private int prevIndex = -1;
	private JCheckBox prevButton;

	private void processMouseMove(Point pt) {
		JList<E> list = getList();
		int index = list.locationToIndex(pt);
		@SuppressWarnings("unchecked")
		ButtonsRenderer renderer = (ButtonsRenderer) list.getCellRenderer();
		renderer.rolloverIndex = index;
		if (index < 0) {
			renderer.rolloverCheckBoxIndex = index;
		}
		if (!list.getCellBounds(index, index).contains(pt)) {
			if (prevIndex >= 0) {
				Rectangle r = list.getCellBounds(prevIndex, prevIndex);
				listRepaint(list, r);
			}
			index = -1;
			prevButton = null;
			return;
		}
		if (index >= 0) {
			JCheckBox button = getCheckBox(list, pt, index);
			if (button != null) {
				renderer.rolloverCheckBoxIndex = index;
				if (!button.equals(prevButton)) {
					Rectangle r = list.getCellBounds(prevIndex, index);
					listRepaint(list, r);
				}
			} else {
				renderer.rolloverCheckBoxIndex = -1;
				Rectangle r = null;
				if (prevIndex == index) {
					if (prevIndex >= 0 && prevButton != null) {
						r = list.getCellBounds(prevIndex, prevIndex);
					}
				} else {
					r = list.getCellBounds(index, index);
				}
				listRepaint(list, r);
				prevIndex = -1;
			}
			prevButton = button;
		}
		prevIndex = index;
	}

	private void processMouseRelease(Point pt) {
		JList<E> list = getList();
		int index = list.locationToIndex(pt);
		if (index >= 0 && list.getCellBounds(index, index).contains(pt)) {
			preventSelectionChange = false;
			preventPopupClosing = false;
			JCheckBox checkBox = getCheckBox(list, pt, index);
			if (checkBox != null && !singular.get(index)) {
				//click on checkbox
				checkBox.doClick();
				Rectangle r = list.getCellBounds(index, index);
				listRepaint(list, r);
				repaint();
			} else {
				//click on object
				selected.clear();
				selected.set(index);
				//as we're preventing the pop-up from being hidden, we need to hide it ourselves now
				setPopupVisible(false);
			}
			//notify listeners that something changed
			notifyActionEvent();
		}
	}

	private void processMousePressed(Point pt) {
		JList<E> list = getList();
		int index = list.locationToIndex(pt);
		if (index >= 0) {
			JCheckBox button = getCheckBox(list, pt, index);
			if (button != null) {
				listRepaint(list, list.getCellBounds(index, index));
			}
			preventSelectionChange = true;
			preventPopupClosing = true;
		}
	}

	private void listRepaint(JList<E> list, Rectangle rect) {
		if (rect != null) {
			list.repaint(rect);
		}
	}

	private JCheckBox getCheckBox(JList<E> list, Point pt, int index) {
		Container c = (Container) list.getCellRenderer().getListCellRendererComponent(list, null, index, false, false);
		Rectangle r = list.getCellBounds(index, index);
		c.setBounds(r);
		//c.doLayout();
		pt.translate(-r.x, -r.y);
		Component b = SwingUtilities.getDeepestComponentAt(c, pt.x, pt.y);
		if (b instanceof JCheckBox) {
			return (JCheckBox) b;
		} else {
			return null;
		}
	}

	private class ComboBoxMouseListener extends MouseAdapter {

		/**
		 * This class contains the mouse listeners on the combobox. These
		 * require a point conversion.
		 * 
		 * @param e
		 * @return
		 */
		public Point transform(MouseEvent e) {
			return SwingUtilities.convertPoint(MultiComboBox.this, e.getPoint(), getList());
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			processMouseMove(transform(e));
		};

		@Override
		public void mouseReleased(MouseEvent e) {
			processMouseRelease(transform(e));
		}

		@Override
		public void mousePressed(MouseEvent e) {
			processMousePressed(transform(e));
		}
	}

	private class CellButtonsMouseListener extends MouseAdapter {

		@Override
		public void mouseDragged(MouseEvent e) {
			processMouseMove(e.getPoint());
		};

		@Override
		public void mouseMoved(MouseEvent e) {
			processMouseMove(e.getPoint());
		}

		@Override
		public void mousePressed(MouseEvent e) {
			processMousePressed(e.getPoint());
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			processMouseRelease(e.getPoint());
		}

		@Override
		public void mouseExited(MouseEvent e) {
			@SuppressWarnings("unchecked")
			JList<E> list = (JList<E>) e.getComponent();
			@SuppressWarnings("unchecked")
			ButtonsRenderer renderer = (ButtonsRenderer) list.getCellRenderer();
			renderer.rolloverCheckBoxIndex = -1;
			renderer.rolloverIndex = -1;
			list.repaint();
		}
	}

	private class ButtonsRenderer extends JPanel implements ListCellRenderer<E> {

		private static final long serialVersionUID = -7823075274156956172L;
		public int rolloverCheckBoxIndex = -1;
		public int rolloverIndex = -1;
		private final JLabel label = new DefaultListCellRenderer();
		private final JCheckBox checkBox = new JCheckBox(new AbstractAction("x") {

			private static final long serialVersionUID = 6852381387051875126L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (singular.get(selected.nextSetBit(0))) {
					selected.clear();
				}

				selected.flip(rolloverCheckBoxIndex);
				if (selected.isEmpty()) {
					selected.set(rolloverCheckBoxIndex);
				}
			}
		}) {
			private static final long serialVersionUID = 2518507718754515592L;

			@Override
			public Dimension getPreferredSize() {
				return new Dimension(16, 16);
			}
		};

		public ButtonsRenderer() {
			super(new BorderLayout(0, 0));
			label.setOpaque(false);
			setOpaque(true);
			add(label);
			SlickerDecorator.instance().decorate(checkBox);
			checkBox.setBorder(BorderFactory.createEmptyBorder());
			checkBox.setContentAreaFilled(false);
			add(checkBox, BorderLayout.EAST);
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected,
				boolean cellHasFocus) {
			label.setText(value != null ? value.toString() : "");
			if (index == -1 || list.getModel().getSize() <= 0) {
				//not in pop-up
				setOpaque(false);
				checkBox.setVisible(false);
				label.setForeground(textfield_fg);
				label.setText(getTitle());
			} else {
				//in pop-up
				if (index == rolloverIndex) {
					setBackground(rollover_colour_bg);
					label.setForeground(rollover_colour_fg);
				} else if (index >= 0 && selected.get(index)) {
					setBackground(selection_colour_bg);
					label.setForeground(selection_colour_fg);
				} else {
					setBackground(index % 2 == 0 ? even_colour_bg : odd_colour_bg);
					label.setForeground(colour_fg);
				}
				setOpaque(true);
				boolean f = index == rolloverCheckBoxIndex;
				checkBox.setVisible(!singular.get(index));
				checkBox.getModel().setRollover(f);
				checkBox.setForeground(f ? Color.WHITE : list.getForeground());
				checkBox.setSelected(selected.get(index));
			}
			return this;
		}
	}
}