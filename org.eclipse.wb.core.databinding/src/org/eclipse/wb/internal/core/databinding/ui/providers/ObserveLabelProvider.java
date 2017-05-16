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
package org.eclipse.wb.internal.core.databinding.ui.providers;

import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.model.IObservePresentation;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Implementation of {@link LabelProvider} for {@link IObserveInfo}.
 *
 * @author lobas_av
 * @coverage bindings.ui
 */
public class ObserveLabelProvider extends LabelProvider {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  private static IObservePresentation getPresentation(Object element) {
    IObserveInfo observe = (IObserveInfo) element;
    return observe.getPresentation();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // LabelProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getText(final Object element) {
    return ExecutionUtils.runObjectLog(new RunnableObjectEx<String>() {
      public String runObject() throws Exception {
        return getPresentation(element).getText();
      }
    }, "<exception, see log>");
  }

  @Override
  public Image getImage(final Object element) {
    return ExecutionUtils.runObjectLog(new RunnableObjectEx<Image>() {
      public Image runObject() throws Exception {
        return getPresentation(element).getImage();
      }
    }, null);
  }
}