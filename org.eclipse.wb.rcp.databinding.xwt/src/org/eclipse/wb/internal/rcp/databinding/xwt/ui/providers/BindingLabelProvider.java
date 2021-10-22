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
package org.eclipse.wb.internal.rcp.databinding.xwt.ui.providers;

import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.ui.SwtResourceManager;
import org.eclipse.wb.internal.rcp.databinding.xwt.model.BindingInfo;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 *
 * @author lobas_av
 *
 */
public class BindingLabelProvider extends LabelProvider implements ITableLabelProvider {
  public static final BindingLabelProvider INSTANCE = new BindingLabelProvider();

  ////////////////////////////////////////////////////////////////////////////
  //
  // ITableLabelProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getColumnText(final Object element, final int column) {
    return ExecutionUtils.runObjectLog(new RunnableObjectEx<String>() {
      public String runObject() throws Exception {
        BindingInfo binding = (BindingInfo) element;
        switch (column) {
          case 1 :
            // target
            return binding.getTargetPresentationText();
          case 2 :
            // model
            return binding.getModelPresentationText();
          case 3 :
            // mode
            return BindingInfo.MODES[binding.getMode()];
          default :
            return null;
        }
      }
    }, "<exception, see log>");
  }

  public Image getColumnImage(Object element, int column) {
    Image image = null;
    if (column == 0) {
      // binding
      image =
          org.eclipse.wb.internal.rcp.databinding.ui.providers.BindingLabelProvider.BIND_VALUE_IMAGE;
      // delay
      if (isDelayBinding(element)) {
        image =
            SwtResourceManager.decorateImage(
                image,
                org.eclipse.wb.internal.rcp.databinding.ui.providers.BindingLabelProvider.CLOCK_DECORATION_IMAGE,
                SwtResourceManager.BOTTOM_RIGHT);
      }
    }
    return image;
  }

  private static boolean isDelayBinding(Object element) {
    // XXX
    return false;
  }
}