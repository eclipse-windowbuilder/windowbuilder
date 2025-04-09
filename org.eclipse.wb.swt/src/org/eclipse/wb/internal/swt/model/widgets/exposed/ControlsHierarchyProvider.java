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
package org.eclipse.wb.internal.swt.model.widgets.exposed;

import org.eclipse.wb.internal.core.model.JavaInfoUtils.HierarchyProvider;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

import org.apache.commons.lang3.ArrayUtils;

/**
 * {@link HierarchyProvider} for SWT controls.
 *
 * @author mitin_aa
 * @coverage swt.model.widgets
 */
public final class ControlsHierarchyProvider extends HierarchyProvider {
	@Override
	public Object getParentObject(Object object) throws Exception {
		if (object instanceof Control control) {
			return control.getParent();
		}
		if (object instanceof Menu menu) {
			return menu.getParent();
		}
		return null;
	}

	@Override
	public Object[] getChildrenObjects(Object object) throws Exception {
		if (object instanceof Composite composite) {
			return composite.getChildren();
		}
		return ArrayUtils.EMPTY_OBJECT_ARRAY;
	}
}