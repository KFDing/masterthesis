package org.processmining.plugins.InductiveMiner.dfgOnly.plugins.dialogs;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.plugins.InductiveMiner.Classifiers;
import org.processmining.plugins.InductiveMiner.Classifiers.ClassifierWrapper;
import org.processmining.plugins.InductiveMiner.dfgOnly.log2logInfo.IMLog2IMLogInfo;
import org.processmining.plugins.InductiveMiner.dfgOnly.log2logInfo.IMLog2IMLogInfoDefault;
import org.processmining.plugins.InductiveMiner.dfgOnly.log2logInfo.IMLog2IMLogInfoLifeCycle;
import org.processmining.plugins.InductiveMiner.dfgOnly.log2logInfo.IMLog2IMLogInfoStartEndComplete;

import com.fluxicon.slickerbox.factory.SlickerFactory;

public class XLog2DfgDialog extends JPanel {

	private static final long serialVersionUID = -3128018697672831767L;
	private final JComboBox<Variant> variantCombobox;
	private final JLabel noiseLabel;
	private final JSlider noiseSlider;
	private final JLabel noiseValue;
	private final JComboBox<ClassifierWrapper> classifierCombobox;

	public abstract class Variant {
		@Override
		public abstract String toString();

		public abstract boolean hasNoise();

		public abstract IMLog2IMLogInfo getIMLog2IMLogInfo();
	}

	public class VariantDefault extends Variant {

		public String toString() {
			return "use event classes - (IMd, IMfd, IMcd)";
		}

		public boolean hasNoise() {
			return false;
		}

		public IMLog2IMLogInfo getIMLog2IMLogInfo() {
			return new IMLog2IMLogInfoDefault();
		}
	}

	public class VariantLifeCycle extends Variant {

		public String toString() {
			return "use event classes and life cycles - (IMdlc, IMfdlc, IMcdlc)";
		}

		public boolean hasNoise() {
			return false;
		}

		public IMLog2IMLogInfo getIMLog2IMLogInfo() {
			return new IMLog2IMLogInfoLifeCycle();
		}
	}

	public class VariantPartialTraces extends Variant {

		public String toString() {
			return "use event classes but mind partial traces - (IMpt)";
		}

		public boolean hasNoise() {
			return false;
		}

		public IMLog2IMLogInfo getIMLog2IMLogInfo() {
			return new IMLog2IMLogInfoStartEndComplete();
		}
	}

	@SuppressWarnings("unchecked")
	public XLog2DfgDialog(XLog log) {
		SlickerFactory factory = SlickerFactory.instance();

		int gridy = 1;

		setLayout(new GridBagLayout());

		//variant
		{
			final JLabel variantLabel = factory.createLabel("Variant");
			{
				GridBagConstraints cVariantLabel = new GridBagConstraints();
				cVariantLabel.gridx = 0;
				cVariantLabel.gridy = gridy;
				cVariantLabel.weightx = 0.4;
				cVariantLabel.anchor = GridBagConstraints.NORTHWEST;
				add(variantLabel, cVariantLabel);
			}

			variantCombobox = factory.createComboBox(
					new Variant[] { new VariantDefault(), new VariantLifeCycle(), new VariantPartialTraces() });
			{
				GridBagConstraints cVariantCombobox = new GridBagConstraints();
				cVariantCombobox.gridx = 1;
				cVariantCombobox.gridy = gridy;
				cVariantCombobox.anchor = GridBagConstraints.NORTHWEST;
				cVariantCombobox.fill = GridBagConstraints.HORIZONTAL;
				cVariantCombobox.weightx = 0.6;
				add(variantCombobox, cVariantCombobox);
				variantCombobox.setSelectedIndex(0);
			}
		}

		gridy++;

		//spacer
		{
			JLabel spacer = factory.createLabel(" ");
			GridBagConstraints cSpacer = new GridBagConstraints();
			cSpacer.gridx = 0;
			cSpacer.gridy = gridy;
			cSpacer.anchor = GridBagConstraints.WEST;
			add(spacer, cSpacer);
		}

		gridy++;

		//noise threshold
		{
			noiseLabel = factory.createLabel("Noise threshold");
			{
				GridBagConstraints cNoiseLabel = new GridBagConstraints();
				cNoiseLabel.gridx = 0;
				cNoiseLabel.gridy = gridy;
				cNoiseLabel.anchor = GridBagConstraints.WEST;
				add(noiseLabel, cNoiseLabel);
				noiseLabel.setVisible(false);
			}

			noiseSlider = factory.createSlider(SwingConstants.HORIZONTAL);
			{
				noiseSlider.setMinimum(0);
				noiseSlider.setMaximum(1000);
				noiseSlider.setValue((int) (getNoiseThreshold() * 1000));
				GridBagConstraints cNoiseSlider = new GridBagConstraints();
				cNoiseSlider.gridx = 1;
				cNoiseSlider.gridy = gridy;
				cNoiseSlider.fill = GridBagConstraints.HORIZONTAL;
				add(noiseSlider, cNoiseSlider);
				noiseSlider.setVisible(false);
			}

			noiseValue = factory.createLabel(String.format("%.2f", getNoiseThreshold()));
			{
				GridBagConstraints cNoiseValue = new GridBagConstraints();
				cNoiseValue.gridx = 2;
				cNoiseValue.gridy = gridy;
				add(noiseValue, cNoiseValue);
				noiseValue.setVisible(false);
			}
		}

		gridy++;

		//spacer
		{
			JLabel spacer = factory.createLabel(" ");
			GridBagConstraints cSpacer = new GridBagConstraints();
			cSpacer.gridx = 0;
			cSpacer.gridy = gridy;
			cSpacer.anchor = GridBagConstraints.WEST;
			add(spacer, cSpacer);
		}

		gridy++;

		//classifiers
		{
			final JLabel classifierLabel = factory.createLabel("Event classifier");
			GridBagConstraints cClassifierLabel = new GridBagConstraints();
			cClassifierLabel.gridx = 0;
			cClassifierLabel.gridy = gridy;
			cClassifierLabel.weightx = 0.4;
			cClassifierLabel.anchor = GridBagConstraints.NORTHWEST;
			add(classifierLabel, cClassifierLabel);
		}

		classifierCombobox = factory.createComboBox(Classifiers.getClassifiers(log));
		{
			GridBagConstraints cClassifiers = new GridBagConstraints();
			cClassifiers.gridx = 1;
			cClassifiers.gridy = gridy;
			cClassifiers.anchor = GridBagConstraints.NORTHWEST;
			cClassifiers.fill = GridBagConstraints.HORIZONTAL;
			cClassifiers.weightx = 0.6;
			add(classifierCombobox, cClassifiers);
		}

		gridy++;

		{
			GridBagConstraints gbcFiller = new GridBagConstraints();
			gbcFiller.weighty = 1.0;
			gbcFiller.gridy = gridy;
			gbcFiller.fill = GridBagConstraints.BOTH;
			add(Box.createGlue(), gbcFiller);
		}

		variantCombobox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Variant variant = (Variant) variantCombobox.getSelectedItem();
				if (variant.hasNoise()) {
					noiseValue.setText(String.format("%.2f", getNoiseThreshold()));
				} else {
					int width = noiseValue.getWidth();
					int height = noiseValue.getHeight();
					noiseValue.setText("  ");
					noiseValue.setPreferredSize(new Dimension(width, height));
				}

				noiseLabel.setVisible(variant.hasNoise());
				noiseSlider.setVisible(variant.hasNoise());
				noiseValue.setVisible(variant.hasNoise());
			}
		});

		noiseSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				noiseValue.setText(String.format("%.2f", getNoiseThreshold()));
			}
		});
	}

	public XEventClassifier getClassifier() {
		return ((ClassifierWrapper) classifierCombobox.getSelectedItem()).classifier;
	}

	public IMLog2IMLogInfo getIMLog2IMLogInfo() {
		return ((Variant) variantCombobox.getSelectedItem()).getIMLog2IMLogInfo();
	}

	public float getNoiseThreshold() {
		if (noiseSlider == null) {
			return 0.2f;
		}
		return noiseSlider.getValue() / 1000f;
	}
}
