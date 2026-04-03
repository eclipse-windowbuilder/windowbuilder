/*******************************************************************************
 * Copyright (c) 2026 Patrick Ziegler and others.
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
package org.eclipse.wb.internal.core.gefTree;

import org.eclipse.wb.internal.core.editor.structure.components.DesignComponent;

import org.eclipse.gef.editparts.AbstractTreeEditPart;
import org.eclipse.swt.widgets.Tree;

import java.util.List;

/**
 * The contents of the components {@link TreeViewer}. This edit part holds the
 * {@link Tree} widget of this viewer and is not rendered.
 */
public class DesignComponentEditPart extends AbstractTreeEditPart {
	public DesignComponentEditPart(DesignComponent model) {
		setModel(model);
	}

	@Override
	public DesignComponent getModel() {
		return (DesignComponent) super.getModel();
	}

	@Override
	protected List<Object> getModelChildren() {
		return List.of(getModel().objectInfo());
	}
}
