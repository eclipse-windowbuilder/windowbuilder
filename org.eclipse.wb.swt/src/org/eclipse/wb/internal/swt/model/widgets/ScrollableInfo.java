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
package org.eclipse.wb.internal.swt.model.widgets;

import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.widgets.Scrollable;

/**
 * Model for any SWT {@link org.eclipse.swt.widgets.Scrollable}.
 *
 * @author scheglov_ke
 * @author mitin_aa
 * @coverage swt.model.widgets
 */
public class ScrollableInfo extends ControlInfo implements IScrollableInfo {
	private Rectangle m_clientArea;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ScrollableInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Refresh
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void refresh_fetch() throws Exception {
		super.refresh_fetch();
		m_clientArea = new Rectangle(getWidget().getClientArea());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link Rectangle}, same as {@link Scrollable#getClientArea()}.
	 */
	@Override
	public Rectangle getClientArea() {
		return m_clientArea;
	}

	@Override
	public Scrollable getWidget() {
		return (Scrollable) getObject();
	}
}