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

import org.eclipse.wb.internal.core.model.ModelMessages;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;

public final class NoPropertyEditPart extends AbstractPropertyEditPart {
	@Override
	protected IFigure createFigure() {
		Label label = new Label();
		label.setBackgroundColor(COLOR_BACKGROUND);
		label.setForegroundColor(COLOR_NO_PROPERTIES);
		label.setText(ModelMessages.PropertyTable_noProperties);
		label.setOpaque(true);
		return label;
	}
}