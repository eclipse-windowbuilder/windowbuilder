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
package org.eclipse.wb.internal.core.databinding.ui.providers;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Collection;

/**
 * Implementation of {@link ITreeContentProvider} for {@link ObjectInfo}.
 *
 * @author lobas_av
 * @coverage bindings.ui
 */
public class ObjectsTreeContentProvider implements ITreeContentProvider {
	////////////////////////////////////////////////////////////////////////////
	//
	// Input
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Object[] getElements(Object input) {
		// case collection
		if (input instanceof Collection<?> inputCollection) {
			return inputCollection.toArray();
		}
		// case array
		if (input instanceof Object[]) {
			return (Object[]) input;
		}
		// case direct object
		if (input instanceof ObjectInfo) {
			return getChildren(input);
		}
		// no input
		return ArrayUtils.EMPTY_OBJECT_ARRAY;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Parent/Children
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Object getParent(Object element) {
		if (element instanceof ObjectInfo info) {
			return info.getParent();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof ObjectInfo info) {
			// prepare presentation
			final IObjectPresentation presentation = info.getPresentation();
			if (presentation != null) {
				// check children
				return ExecutionUtils.runObjectLog(() -> presentation.isVisible() && !presentation.getChildrenTree().isEmpty(), false);
			}
		}
		return false;
	}

	@Override
	public Object[] getChildren(Object element) {
		if (element instanceof ObjectInfo info) {
			// prepare presentation
			final IObjectPresentation presentation = info.getPresentation();
			if (presentation != null) {
				// get children
				return ExecutionUtils.runObjectLog(() -> presentation.getChildrenTree().toArray(), ArrayUtils.EMPTY_OBJECT_ARRAY);
			}
		}
		return ArrayUtils.EMPTY_OBJECT_ARRAY;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
}