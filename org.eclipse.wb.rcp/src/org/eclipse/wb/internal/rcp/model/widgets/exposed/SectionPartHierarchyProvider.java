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