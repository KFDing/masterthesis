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

import org.processmining.plugins.InductiveMiner.dfgOnly.DfgMiningParameters;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgMiningParametersIMcd;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgMiningParametersIMd;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgMiningParametersIMfd;

import com.fluxicon.slickerbox.factory.SlickerFactory;

public class IMdMiningDialog extends JPanel {

	private static final long serialVersionUID = 7693870370139578439L;
	private final ParametersWrapper p = new ParametersWrapper();
	private final JComboBox<?> variantCombobox;
	private final JLabel noiseLabel;
	private final JSlider noiseSlider;
	private final JLabel noiseValue;

	public class ParametersWrapper {
		public DfgMiningParameters parameters;
	}

	public abstract class Variant {
		@Override
		public abstract String toString();

		public abstract boolean hasNoise();

		public abstract DfgMiningParameters getMiningParameters();
	}

	public class VariantIMd extends Variant {
		public String toString() {
			return "Inductive Miner - directly follows (IMd)";
		}

		public boolean hasNoise() {
			return false;
		}

		public DfgMiningParameters getMiningParameters() {
			return new DfgMiningParametersIMd();
		}
	}

	public class VariantIMfd extends Variant {
		public String toString() {
			return "Inductive Miner - infrequent - directly follows (IMfd)";
		}

		public boolean hasNoise() {
			return true;
		}

		public DfgMiningParameters getMiningParameters() {
			return new DfgMiningParametersIMfd();
		}

	}

	public class VariantIMcd extends Variant {
		public String toString() {
			return "Inductive Miner - incompleteness - directly follows (IMcd)";
		}

		public boolean hasNoise() {
			return false;
		}

		public DfgMiningParameters getMiningParameters() {
			return new DfgMiningParametersIMcd();
		}

	}

	public IMdMiningDialog() {
		p.parameters = new DfgMiningParametersIMfd();
		SlickerFactory factory = SlickerFactory.instance();

		int gridy = 1;

		setLayout(new GridBagLayout());

		//algorithm
		final JLabel variantLabel = factory.createLabel("Variant");
		{
			GridBagConstraints cVariantLabel = new GridBagConstraints();
			cVariantLabel.gridx = 0;
			cVariantLabel.gridy = gridy;
			cVariantLabel.weightx = 0.4;
			cVariantLabel.anchor = GridBagConstraints.NORTHWEST;
			add(variantLabel, cVariantLabel);
		}

		variantCombobox = factory.createComboBox(new Variant[] { new VariantIMd(), new VariantIMfd(), new VariantIMcd() });
		{
			GridBagConstraints cVariantCombobox = new GridBagConstraints();
			cVariantCombobox.gridx = 1;
			cVariantCombobox.gridy = gridy;
			cVariantCombobox.anchor = GridBagConstraints.NORTHWEST;
			cVariantCombobox.fill = GridBagConstraints.HORIZONTAL;
			cVariantCombobox.weightx = 0.6;
			add(variantCombobox, cVariantCombobox);
			variantCombobox.setSelectedIndex(1);
		}
		
		gridy++;
		
		JLabel spacer = factory.createLabel(" ");
		{
			GridBagConstraints cSpacer = new GridBagConstraints();
			cSpacer.gridx = 0;
			cSpacer.gridy = gridy;
			cSpacer.anchor = GridBagConstraints.WEST;
			add(spacer, cSpacer);
		}
		

		gridy++;

		//noise threshold
		noiseLabel = factory.createLabel("Noise threshold");
		{
			GridBagConstraints cNoiseLabel = new GridBagConstraints();
			cNoiseLabel.gridx = 0;
			cNoiseLabel.gridy = gridy;
			cNoiseLabel.anchor = GridBagConstraints.WEST;
			add(noiseLabel, cNoiseLabel);
		}

		noiseSlider = factory.createSlider(SwingConstants.HORIZONTAL);
		{
			noiseSlider.setMinimum(0);
			noiseSlider.setMaximum(1000);
			noiseSlider.setValue((int) (p.parameters.getNoiseThreshold() * 1000));
			GridBagConstraints cNoiseSlider = new GridBagConstraints();
			cNoiseSlider.gridx = 1;
			cNoiseSlider.gridy = gridy;
			cNoiseSlider.fill = GridBagConstraints.HORIZONTAL;
			add(noiseSlider, cNoiseSlider);
		}

		noiseValue = factory.createLabel(String.format("%.2f", p.parameters.getNoiseThreshold()));
		{
			GridBagConstraints cNoiseValue = new GridBagConstraints();
			cNoiseValue.gridx = 2;
			cNoiseValue.gridy = gridy;
			add(noiseValue, cNoiseValue);
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
				float noise = p.parameters.getNoiseThreshold();
				p.parameters = variant.getMiningParameters();
				p.parameters.setNoiseThreshold(noise);
				if (variant.hasNoise()) {
					noiseValue.setText(String.format("%.2f", p.parameters.getNoiseThreshold()));
				} else {
					int width = noiseValue.getWidth();
					int height = noiseValue.getHeight();
					noiseValue.setText("  ");
					noiseValue.setPreferredSize(new Dimension(width, height));
				}

				noiseLabel.setVisible(variant.hasNoise());
				noiseSlider.setVisible(variant.hasNoise());
			}
		});

		noiseSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				p.parameters.setNoiseThreshold((float) (noiseSlider.getValue() / 1000.0));
				noiseValue.setText(String.format("%.2f", p.parameters.getNoiseThreshold()));
			}
		});
	}

	public DfgMiningParameters getMiningParameters() {
		return p.parameters;
	}

}
