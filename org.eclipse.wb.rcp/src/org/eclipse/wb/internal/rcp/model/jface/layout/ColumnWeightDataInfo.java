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
package org.eclipse.wb.internal.rcp.model.jface.layout;

import org.eclipse.wb.core.model.broadcast.GenericPropertySetValue;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;

import org.eclipse.jface.viewers.ColumnWeightData;

/**
 * Model for {@link ColumnWeightData}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.jface.layout
 */
public final class ColumnWeightDataInfo extends ColumnLayoutDataInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ColumnWeightDataInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
		// instead of "column.width" property set "minimumWidth" property of ColumnWeightData
		addBroadcastListener(new GenericPropertySetValue() {
			@Override
			public void invoke(GenericPropertyImpl property, Object[] value, boolean[] shouldSetValue)
					throws Exception {
				if (property.getJavaInfo() == getParentJava() && property.getTitle().equals("width")) {
					getPropertyByTitle("minimumWidth").setValue(value[0]);
					shouldSetValue[0] = false;
				}
			}
		});
	}
}
