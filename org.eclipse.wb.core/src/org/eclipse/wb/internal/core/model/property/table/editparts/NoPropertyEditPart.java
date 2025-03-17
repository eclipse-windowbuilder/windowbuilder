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