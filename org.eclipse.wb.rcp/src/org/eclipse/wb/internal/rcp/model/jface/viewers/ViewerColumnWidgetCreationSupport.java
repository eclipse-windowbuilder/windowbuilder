/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
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
package org.eclipse.wb.internal.rcp.model.jface.viewers;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.clipboard.IClipboardCreationSupport;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.WrapperMethodControlCreationSupport;

/**
 * Implementation of {@link CreationSupport} for column widget of {@link ViewerColumnInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.jface.viewers
 */
public final class ViewerColumnWidgetCreationSupport extends WrapperMethodControlCreationSupport {
	private final ViewerColumnInfo m_viewer;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ViewerColumnWidgetCreationSupport(ViewerColumnInfo viewer) {
		super(viewer.getWrapper());
		m_viewer = viewer;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Clipboard
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public IClipboardCreationSupport getClipboard() throws Exception {
		final JavaInfoMemento viewerMemento = JavaInfoMemento.createMemento(m_viewer);
		return new IClipboardCreationSupport() {
			private static final long serialVersionUID = 0L;

			@Override
			public CreationSupport create(JavaInfo rootObject) throws Exception {
				ViewerColumnInfo viewer = (ViewerColumnInfo) viewerMemento.create(rootObject);
				return new ViewerColumnCreationSupport(viewer, false);
			}

			@Override
			public void apply(JavaInfo javaInfo) throws Exception {
				viewerMemento.apply();
			}
		};
	}
}
