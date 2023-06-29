/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.xwt.model.forms.layout.column;

import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.rcp.model.forms.layout.column.IColumnLayoutDataInfo;
import org.eclipse.wb.internal.xwt.model.layout.LayoutDataInfo;

import org.eclipse.ui.forms.widgets.ColumnLayoutData;

/**
 * Model for {@link ColumnLayoutData}.
 *
 * @author scheglov_ke
 * @coverage XWT.model.forms
 */
public final class ColumnLayoutDataInfo extends LayoutDataInfo implements IColumnLayoutDataInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ColumnLayoutDataInfo(EditorContext context,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(context, description, creationSupport);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Size
	//
	////////////////////////////////////////////////////////////////////////////
	public void setWidthHint(int widthHint) throws Exception {
		getPropertyByTitle("widthHint").setValue(widthHint);
	}

	public void setHeightHint(int heightHint) throws Exception {
		getPropertyByTitle("heightHint").setValue(heightHint);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Alignment
	//
	////////////////////////////////////////////////////////////////////////////
	public int getHorizontalAlignment() throws Exception {
		return (Integer) getPropertyByTitle("horizontalAlignment").getValue();
	}

	public void setHorizontalAlignment(int horizontalAlignment) throws Exception {
		getPropertyByTitle("horizontalAlignment").setValue(horizontalAlignment);
	}
}