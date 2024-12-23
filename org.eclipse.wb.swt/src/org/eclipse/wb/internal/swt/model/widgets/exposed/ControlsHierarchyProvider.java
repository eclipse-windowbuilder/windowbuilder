/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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