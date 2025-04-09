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
package org.eclipse.wb.internal.rcp.nebula.ganttchart;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.nebula.widgets.ganttchart.GanttEvent;

/**
 * Model {@link GanttEvent}.
 *
 * @author sablin_aa
 * @coverage nebula.model
 */
public final class GanttEventInfo extends AbstractComponentInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public GanttEventInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Convenience method that returns the actual type contained of this
	 * {@link GanttEventInfo}.
	 */
	protected GanttEvent getEvent() {
		return (GanttEvent) getObject();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Refresh
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void refresh_fetch() throws Exception {
		{
			Rectangle bounds = new Rectangle(getEvent().getBounds());
			setModelBounds(bounds);
		}
		// continue in super()
		super.refresh_fetch();
	}
}
