/*******************************************************************************
 * Copyright (c) 2025 Patrick Ziegler and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
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