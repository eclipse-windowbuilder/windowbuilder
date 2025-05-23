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
package org.eclipse.wb.internal.rcp.model.rcp.perspective;

import org.eclipse.wb.internal.core.model.TopBoundsSupport;
import org.eclipse.wb.internal.swt.model.widgets.CompositeTopBoundsSupport;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.widgets.Shell;

/**
 * Implementation of {@link TopBoundsSupport} for {@link PageLayoutInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.rcp
 */
public final class PageLayoutTopBoundsSupport extends TopBoundsSupport {
	private final PageLayoutInfo m_page;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public PageLayoutTopBoundsSupport(PageLayoutInfo page) {
		super(page);
		m_page = page;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// TopBoundsSupport
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void apply() throws Exception {
		// set size from resource properties (or default)
		{
			Dimension size = getResourceSize();
			Shell shell = m_page.getShell();
			// "size" is size of _content_ for "shell", so calculate trim
			Rectangle trim = new Rectangle(shell.computeTrim(0, 0, size.width, size.height));
			// OK, set size from trim
			shell.setSize(trim.width, trim.height);
		}
		// layout perspective
		m_page.layoutPerspectiveParts();
	}

	@Override
	public void setSize(int width, int height) throws Exception {
		// remember size in resource properties
		setResourceSize(width, height);
	}

	@Override
	protected Dimension getDefaultSize() {
		return new Dimension(600, 500);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Show
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean show() throws Exception {
		// layout after resize
		final Shell shell = m_page.getShell();
		shell.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				shell.setRedraw(false);
				try {
					m_page.layoutPerspectiveParts();
				} finally {
					shell.setRedraw(true);
				}
			}
		});
		// OK, show
		CompositeTopBoundsSupport.show(m_page, shell);
		return true;
	}
}