/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
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
import org.eclipse.wb.internal.swt.support.ContainerSupport;
import org.eclipse.wb.internal.swt.support.ControlSupport;
import org.eclipse.wb.internal.swt.support.MenuSupport;

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
		if (ControlSupport.isControl(object) || MenuSupport.isMenu(object)) {
			return ControlSupport.getParent(object);
		}
		return null;
	}

	@Override
	public Object[] getChildrenObjects(Object object) throws Exception {
		if (ContainerSupport.isComposite(object)) {
			return ContainerSupport.getChildren(object);
		}
		return ArrayUtils.EMPTY_OBJECT_ARRAY;
	}
}