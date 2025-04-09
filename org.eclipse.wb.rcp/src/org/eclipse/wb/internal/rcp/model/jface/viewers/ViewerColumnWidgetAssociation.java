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
import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.core.model.association.WrappedObjectAssociation;
import org.eclipse.wb.internal.swt.model.jface.viewer.ViewerInfo;
import org.eclipse.wb.internal.swt.model.widgets.WidgetInfo;

/**
 * Implementation of {@link Association} for column {@link WidgetInfo} of {@link ViewerColumnInfo}.
 * It delegates all operations to the {@link Association} of {@link ViewerColumnInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.jface.viewers
 */
public final class ViewerColumnWidgetAssociation extends WrappedObjectAssociation {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ViewerColumnWidgetAssociation(ViewerColumnInfo viewer) {
		super(viewer.getWrapper());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Operations
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void setParent(JavaInfo parent) throws Exception {
		super.setParent(parent.getChildren(ViewerInfo.class).get(0));
	}
}
