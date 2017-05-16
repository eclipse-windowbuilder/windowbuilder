/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.utils.dialogfields;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class LayoutUtil {
  /**
   * Calculates the number of columns needed by field editors
   */
  public static int getNumberOfColumns(DialogField[] editors) {
    int nCulumns = 0;
    for (int i = 0; i < editors.length; i++) {
      nCulumns = Math.max(editors[i].getNumberOfControls(), nCulumns);
    }
    return nCulumns;
  }

  /**
   * Creates a composite and fills in the given editors.
   *
   * @param labelOnTop
   *          Defines if the label of all fields should be on top of the fields
   */
  public static void doDefaultLayout(Composite parent, DialogField[] editors, boolean labelOnTop) {
    doDefaultLayout(parent, editors, labelOnTop, 0, 0);
  }

  /**
   * Creates a composite and fills in the given editors.
   *
   * @param labelOnTop
   *          Defines if the label of all fields should be on top of the fields
   * @param marginWidth
   *          The margin width to be used by the composite
   * @param marginHeight
   *          The margin height to be used by the composite
   */
  public static void doDefaultLayout(Composite parent,
      DialogField[] editors,
      boolean labelOnTop,
      int marginWidth,
      int marginHeight) {
    int nCulumns = getNumberOfColumns(editors);
    Control[][] controls = new Control[editors.length][];
    for (int i = 0; i < editors.length; i++) {
      controls[i] = editors[i].doFillIntoGrid(parent, nCulumns);
    }
    if (labelOnTop) {
      nCulumns--;
      modifyLabelSpans(controls, nCulumns);
    }
    GridLayout layout = null;
    if (parent.getLayout() instanceof GridLayout) {
      layout = (GridLayout) parent.getLayout();
    } else {
      layout = new GridLayout();
    }
    if (marginWidth != SWT.DEFAULT) {
      layout.marginWidth = marginWidth;
    }
    if (marginHeight != SWT.DEFAULT) {
      layout.marginHeight = marginHeight;
    }
    layout.numColumns = nCulumns;
    parent.setLayout(layout);
  }

  private static void modifyLabelSpans(Control[][] controls, int nCulumns) {
    for (int i = 0; i < controls.length; i++) {
      SwtUtil.setHorizontalSpan(controls[i][0], nCulumns);
    }
  }
}
