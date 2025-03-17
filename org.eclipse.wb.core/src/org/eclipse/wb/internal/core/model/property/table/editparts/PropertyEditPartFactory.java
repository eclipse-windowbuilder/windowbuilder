/*******************************************************************************
 * Copyright (c) 2025 Patrick Ziegler and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Patrick Ziegler - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.model.property.table.editparts;

import org.eclipse.wb.internal.core.model.property.table.PropertyTable.PropertyInfo;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;

import java.util.List;

public final class PropertyEditPartFactory implements EditPartFactory {
	@Override
	@SuppressWarnings("unchecked")
	public EditPart createEditPart(EditPart context, Object model) {
		if (model instanceof List properties) {
			if (properties.isEmpty()) {
				return new NoPropertyEditPart();
			}
			return new PropertyRootEditPart((List<PropertyInfo>) model);
		}
		return new PropertyEditPart((PropertyInfo) model);
	}
}