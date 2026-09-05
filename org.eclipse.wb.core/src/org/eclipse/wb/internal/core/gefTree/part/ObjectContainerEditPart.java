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
package org.eclipse.wb.internal.core.gefTree.part;

import org.eclipse.wb.internal.core.model.ObjectInfoContainer;

import org.eclipse.gef.editparts.AbstractTreeEditPart;
import org.eclipse.gef.ui.parts.TreeViewer;

import java.util.Collections;
import java.util.List;

/**
 * Edit part for the {@link ObjectInfoContainer} which is used as input for the
 * {@link TreeViewer}. This edit part is not shown.
 */
public class ObjectContainerEditPart extends AbstractTreeEditPart {
	public ObjectContainerEditPart(ObjectInfoContainer model) {
		setModel(model);
	}

	@Override
	protected List<?> getModelChildren() {
		return Collections.singletonList(getModel().objectInfo());
	}

	@Override
	public ObjectInfoContainer getModel() {
		return (ObjectInfoContainer) super.getModel();
	}
}
