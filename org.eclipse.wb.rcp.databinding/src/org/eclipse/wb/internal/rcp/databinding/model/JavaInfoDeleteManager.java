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
package org.eclipse.wb.internal.rcp.databinding.model;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.IBindingInfo;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.model.nonvisual.NonVisualBeanContainerInfo;
import org.eclipse.wb.internal.rcp.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.JavaInfoReferenceProvider;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.WidgetBindableInfo;
import org.eclipse.wb.internal.swt.model.jface.viewer.ViewerInfo;

import java.util.List;

/**
 * Observe {@link JavaInfo} events for delete bindings that have reference to deleted
 * {@link JavaInfo}.
 * 
 * @author lobas_av
 * @coverage bindings.rcp.model
 */
public final class JavaInfoDeleteManager
    extends
      org.eclipse.wb.internal.core.databinding.model.JavaInfoDeleteManager {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public JavaInfoDeleteManager(DatabindingsProvider provider) {
    super(provider, provider.getJavaInfoRoot());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Handle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void deleteBinding(IBindingInfo ibinding, List<IBindingInfo> bindings) throws Exception {
    AbstractBindingInfo binding = (AbstractBindingInfo) ibinding;
    binding.delete();
  }

  @Override
  protected boolean accept(ObjectInfo javaInfo) throws Exception {
    return javaInfo instanceof AbstractComponentInfo
        || javaInfo instanceof ViewerInfo
        || javaInfo.getParent() instanceof NonVisualBeanContainerInfo;
  }

  @Override
  protected String getReference(ObjectInfo javaInfo) throws Exception {
    return JavaInfoReferenceProvider.getReference((JavaInfo) javaInfo);
  }

  @Override
  protected boolean equals(ObjectInfo javaInfo, String javaInfoReference, IObserveInfo iobserve)
      throws Exception {
    BindableInfo bindable = (BindableInfo) iobserve;
    return checkWidget((JavaInfo) javaInfo, bindable)
        || javaInfoReference.equals(bindable.getReference());
  }

  static boolean checkWidget(JavaInfo javaInfo, BindableInfo bindable) {
    if (bindable instanceof WidgetBindableInfo) {
      WidgetBindableInfo widgetBindable = (WidgetBindableInfo) bindable;
      return javaInfo == widgetBindable.getJavaInfo();
    }
    return false;
  }
}