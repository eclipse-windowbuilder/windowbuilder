/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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
package org.eclipse.wb.internal.swing.customize;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.ui.dialogs.ResizableDialog;
import org.eclipse.wb.internal.swing.utils.SwingUtils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Panel;

import javax.swing.JRootPane;

/**
 * SWT dialog for showing AWT component.
 *
 * @author lobas_av
 * @coverage swing.customize
 */
public final class AwtComponentDialog extends ResizableDialog {
	private final Component m_component;
	private final String m_title;
	private final String m_settingsName;
	private Frame m_frame;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public AwtComponentDialog(AbstractUIPlugin plugin,
			Component component,
			String title,
			String settingsName) {
		super(DesignerPlugin.getShell(), plugin);
		m_component = component;
		m_title = title;
		m_settingsName = settingsName;
		setShellStyle(SWT.RESIZE | SWT.CLOSE | SWT.APPLICATION_MODAL);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// GUI
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(m_title);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new FillLayout());
		//
		Composite composite = new Composite(container, SWT.EMBEDDED) {
			@Override
			public Point computeSize(int wHint, int hHint, boolean changed) {
				Dimension preferredSize = m_component.getPreferredSize();
				return new Point(preferredSize.width, preferredSize.height);
			}
		};
		// SWT_AWT.new_Frame()
		m_frame = SWT_AWT.new_Frame(composite);
		// java.awt.Panel
		Panel panel = new Panel(new BorderLayout());
		m_frame.add(panel);
		// javax.swing.JRootPane
		JRootPane rootPane = new JRootPane();
		panel.add(rootPane);
		// add main Component
		rootPane.getContentPane().add(m_component);
		m_frame.doLayout();
		// done
		return container;
	}

	@Override
	protected String getDialogSettingsSectionName() {
		return m_settingsName;
	}

	@Override
	protected Point getDefaultSize() {
		//return new Point(450, 300);
		// User asked to use Customizer.getPreferredSize()
		// http://www.eclipse.org/forums/index.php/t/339421/
		return super.getDefaultSize();
	}

	@Override
	public boolean close() {
		SwingUtils.runLog(() -> m_frame.dispose());
		return super.close();
	}
}