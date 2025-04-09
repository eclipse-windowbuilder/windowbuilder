/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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
package org.eclipse.wb.internal.core.model.property.table;

import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.model.property.table.editparts.PropertyEditPart.PropertyFigure;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

/**
 * Helper class for displaying tooltips.
 *
 * @author scheglov_ke
 * @coverage core.model.property.table
 */
public class PropertyTableTooltipHelper implements IPropertyTooltipSite {
	/**
	 * The position of the tool-tip relative to the mouse cursor. The tip should't
	 * be directly under the cursor so that it doesn't obscure the figure
	 * underneath.
	 */
	private static final Point OFFSET = new Point(12, 0);
	private final PropertyTable m_table;
	private Shell m_tooltip;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public PropertyTableTooltipHelper(PropertyTable table) {
		m_table = table;
		m_table.getControl().addListener(SWT.MouseExit, new Listener() {
			@Override
			public void handleEvent(Event event) {
				// check, may be cursor is now on tooltip, so ignore this MouseExit
				{
					Control control = Display.getCurrent().getCursorControl();
					while (control != null) {
						if (control == m_tooltip) {
							return;
						}
						control = control.getParent();
					}
				}
				// no, we should hide tooltip
				hideTooltip();
			}
		});
		m_table.getControl().addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				hideTooltip();
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	private int m_x;
	private int m_y;

	////////////////////////////////////////////////////////////////////////////
	//
	// IPropertyTooltipSite
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public PropertyTable getTable() {
		return m_table;
	}

	@Override
	public void hideTooltip() {
		if (m_tooltip != null && !m_tooltip.isDisposed()) {
			m_tooltip.dispose();
		}
		m_tooltip = null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Showing tooltip
	//
	////////////////////////////////////////////////////////////////////////////

	public void displayToolTipNear(PropertyFigure hoverSource, int eventX, int eventY) {
		m_x = eventX;
		m_y = eventY;
		hideTooltip();
		// prepare provider
		PropertyTooltipProvider provider = hoverSource.getPropertyTooltipProvider();
		if (provider == null) {
			return;
		}
		// create Shell
		{
			m_tooltip = new Shell(m_table.getControl().getShell(), SWT.NO_FOCUS | SWT.ON_TOP | SWT.TOOL | SWT.SINGLE);
			configureColors(m_tooltip);
			GridLayoutFactory.create(m_tooltip).noMargins();
		}
		// prepare control
		Control control = provider.createTooltipControl(hoverSource.getProperty(), m_tooltip, this);
		if (control == null) {
			hideTooltip();
			return;
		}
		// show Shell
		{
			// prepare tooltip location
			Point tooltipLocation;
			if (provider.getTooltipPosition() == PropertyTooltipProvider.ON) {
				tooltipLocation = m_table.getControl().toDisplay(new Point(m_x + OFFSET.x, m_y + OFFSET.y));
			} else {
				tooltipLocation = m_table.getControl().toDisplay(new Point(m_x + OFFSET.x, m_y + OFFSET.y + getTable().getRowHeight()));
			}
			// set location/size and open
			m_tooltip.setLocation(tooltipLocation.x, tooltipLocation.y);
			// for non-windows systems the tooltip may have invalid tooltip bounds
			// because some widget's API functions may fail if tooltip content is not visible
			// ex., on MacOSX tree widget's items has zero bounds since they are not yet visible.
			// the workaround is to preset tooltip size to big values before any computeSize called.
			if (!EnvironmentUtils.IS_WINDOWS) {
				m_tooltip.setSize(1000, 1000);
			}
			m_tooltip.setSize(m_tooltip.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			provider.show(m_tooltip);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Sets given {@link Control} correct background/foreground for tooltips.
	 */
	private void configureColors(Control control) {
		Display display = Display.getCurrent();
		control.setForeground(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
		control.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
	}
}
