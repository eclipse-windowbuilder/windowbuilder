/*******************************************************************************
 * Copyright (c) 2025 Google, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swing.preferences.laf;

import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.swing.laf.model.LafInfo;
import org.eclipse.wb.internal.swing.preferences.Messages;

import org.eclipse.swt.widgets.Composite;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Objects;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;

import swingintegration.example.EmbeddedSwingComposite;

/**
 * Preview of the currently selected LaF in the preference page.
 */
public final class LafCanvas extends EmbeddedSwingComposite {
	private final LafInfo selection;
	private LookAndFeel oldLookAndFeel;

	public LafCanvas(LafInfo selection, Composite parent, int style) {
		super(parent, style);
		this.selection = selection;
	}

	/**
	 * Called before the Swing components are created to set the selected
	 * {@link LookAndFeel}.
	 */
	private void setLookAndFeel() throws Exception {
		oldLookAndFeel = UIManager.getLookAndFeel();
		LookAndFeel lookAndFeelInstance = selection.getLookAndFeelInstance();
		UIManager.setLookAndFeel(lookAndFeelInstance);
	}

	/**
	 * Called after the Swing components were created to restore the original
	 * {@link LookAndFeel}.
	 */
	private void restoreLookAndFeel() throws Exception {
		Objects.requireNonNull(oldLookAndFeel, "Tried to restore old LookAndFeel which was never set");
		UIManager.setLookAndFeel(oldLookAndFeel);
	}

	@Override
	protected JComponent createSwingComponent() {
		try {
			ExecutionUtils.runLog(this::setLookAndFeel);
			// create the JRootPane
			JRootPane rootPane = new JRootPane();
			{
				JMenuBar menuBar = new JMenuBar();
				rootPane.setJMenuBar(menuBar);
				{
					JMenu mnFile = new JMenu(Messages.LafPreferencePage_previewFile);
					menuBar.add(mnFile);
					{
						JMenuItem mntmNew = new JMenuItem(Messages.LafPreferencePage_previewNew);
						mnFile.add(mntmNew);
					}
					{
						JMenuItem mntmExit = new JMenuItem(Messages.LafPreferencePage_previewExit);
						mnFile.add(mntmExit);
					}
				}
				{
					JMenu mnView = new JMenu(Messages.LafPreferencePage_previewView);
					menuBar.add(mnView);
					{
						JMenuItem mntmCommon = new JMenuItem(Messages.LafPreferencePage_previewCommon);
						mnView.add(mntmCommon);
					}
				}
			}
			GridBagLayout gridBagLayout = new GridBagLayout();
			gridBagLayout.columnWidths = new int[]{0, 0, 0};
			gridBagLayout.rowHeights = new int[]{0, 0, 0, 0};
			gridBagLayout.columnWeights = new double[]{0.0, 0.0, 1.0E-4};
			gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0E-4};
			rootPane.getContentPane().setLayout(gridBagLayout);
			{
				JLabel lblLabel = new JLabel(Messages.LafPreferencePage_previewLabel);
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.insets = new Insets(0, 0, 5, 5);
				gbc.gridx = 0;
				gbc.gridy = 0;
				rootPane.getContentPane().add(lblLabel, gbc);
			}
			{
				JButton btnPushButton = new JButton(Messages.LafPreferencePage_previewButton);
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.insets = new Insets(0, 0, 5, 0);
				gbc.gridx = 1;
				gbc.gridy = 0;
				rootPane.getContentPane().add(btnPushButton, gbc);
			}
			{
				JComboBox<String> comboBox = new JComboBox<>();
				comboBox.setModel(
						new DefaultComboBoxModel<>(new String[]{
								Messages.LafPreferencePage_previewCombo,
								"ComboBox Item 1",
						"ComboBox Item 2"}));
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.insets = new Insets(0, 0, 5, 5);
				gbc.fill = GridBagConstraints.HORIZONTAL;
				gbc.gridx = 0;
				gbc.gridy = 1;
				rootPane.getContentPane().add(comboBox, gbc);
			}
			{
				JRadioButton rdbtnRadioButton =
						new JRadioButton(Messages.LafPreferencePage_previewRadio);
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.insets = new Insets(0, 0, 5, 0);
				gbc.gridx = 1;
				gbc.gridy = 1;
				rootPane.getContentPane().add(rdbtnRadioButton, gbc);
			}
			{
				JCheckBox chckbxCheckbox = new JCheckBox(Messages.LafPreferencePage_previewCheck);
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.insets = new Insets(0, 0, 0, 5);
				gbc.gridx = 0;
				gbc.gridy = 2;
				rootPane.getContentPane().add(chckbxCheckbox, gbc);
			}
			{
				JTextField textField = new JTextField();
				textField.setText(Messages.LafPreferencePage_previewTextField);
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.fill = GridBagConstraints.HORIZONTAL;
				gbc.gridx = 1;
				gbc.gridy = 2;
				rootPane.getContentPane().add(textField, gbc);
			}
			return rootPane;
		} finally {
			// Delay restoration until after the components have been painted
			EventQueue.invokeLater(() -> ExecutionUtils.runLog(this::restoreLookAndFeel));
		}
	}
}