/*******************************************************************************
 * Copyright (c) 2011, 2026 Google, Inc. and others.
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
package org.eclipse.wb.internal.swing.model.property.editor.border;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Rectangle;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import swingintegration.example.EmbeddedSwingComposite;

/**
 * {@link Composite} for displaying {@link Border}.
 *
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class BorderPreviewCanvas extends EmbeddedSwingComposite {
	private JPanel m_awtRoot;
	private JPanel m_emptyPanel;
	private JPanel m_filledPanel_1;
	private JPanel m_filledPanel_2;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public BorderPreviewCanvas(Composite parent, int style) {
		super(parent, SWT.NONE);
		populate();
	}

	@Override
	protected JComponent createSwingComponent() {
		m_awtRoot = new JPanel();
		m_awtRoot.setLayout(new GridLayout(1, 0, 5, 0));
		m_awtRoot.setBackground(new Color(200, 225, 255));
		// empty JPanel
		{
			m_emptyPanel = new JPanel();
			m_awtRoot.add(m_emptyPanel);
		}
		// filled JPanel
		{
			m_filledPanel_1 = new JPanel();
			m_awtRoot.add(m_filledPanel_1);
			// fill JPanel
			m_filledPanel_1.setLayout(new GridLayout());
			m_filledPanel_1.add(new JButton("JButton"));
		}
		// filled JPanel
		{
			m_filledPanel_2 = new JPanel();
			m_awtRoot.add(m_filledPanel_2);
			m_filledPanel_2.setBackground(new Color(192, 192, 192));
			// fill JPanel
			m_filledPanel_2.setLayout(new GridLayout());
			m_filledPanel_2.add(new JButton("JButton"));
		}
		// OK, filled container
		return m_awtRoot;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	private static final Border ERROR_BORDER = new LineBorder(Color.RED, 5);

	/**
	 * Sets the {@link BorderValue} to display. Must not be {@code null}. Must be
	 * called from the AWT event dispatcher thread.
	 */
	public void setBorder(final BorderValue borderValue) {
		Assert.isLegal(SwingUtilities.isEventDispatchThread(), "Must be called from the AWT event dispatcher thread.");
		setBorder(borderValue.getValue());
	}

	/**
	 * Sets the {@link Border} to display.
	 */
	private void setBorder(final Border border) {
		try {
			if (border != null) {
				Graphics graphics = m_emptyPanel.getGraphics();
				graphics.setClip(new Rectangle(0, 0, 0, 0));
				border.paintBorder(m_emptyPanel, graphics, 0, 0, 0, 0);
			}
			// OK, now we know, that this Border can be set on JPanel
			m_emptyPanel.setBorder(border);
			m_filledPanel_1.setBorder(border);
			m_filledPanel_2.setBorder(border);
			m_awtRoot.validate();
		} catch (Throwable e) {
			// oops...this Border can not be used on JPanel
			setBorder(ERROR_BORDER);
		}
	}
}
