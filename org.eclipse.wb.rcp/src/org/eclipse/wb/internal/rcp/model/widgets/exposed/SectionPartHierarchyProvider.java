/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
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
package org.eclipse.wb.internal.rcp.model.widgets.exposed;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.JavaInfoUtils.HierarchyProvider;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.rcp.model.forms.SectionPartInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;

import org.eclipse.ui.forms.SectionPart;

/**
 * {@link HierarchyProvider} for {@link SectionPart}.
 *
 * @author sablin_aa
 * @coverage rcp.model.widgets
 */
public final class SectionPartHierarchyProvider extends HierarchyProvider {
	@Override
	public Object getParentObject(Object object) throws Exception {
		if (ReflectionUtils.isSuccessorOf(object.getClass(), "org.eclipse.ui.forms.SectionPart")) {
			Object control = ReflectionUtils.invokeMethod(object, "getSection()");
			return ReflectionUtils.invokeMethod(control, "getParent()");
		}
		return null;
	}

	@Override
	public void add(JavaInfo host, JavaInfo exposed) throws Exception {
		if (exposed instanceof SectionPartInfo && host instanceof CompositeInfo) {
			((SectionPartInfo) exposed).getWrapper().configureHierarchy(host);
		}
	}
}